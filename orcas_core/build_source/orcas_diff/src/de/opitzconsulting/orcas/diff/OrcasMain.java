package de.opitzconsulting.orcas.diff;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.inject.Injector;

import de.opitzconsulting.OrcasDslStandaloneSetup;
import de.opitzconsulting.orcas.diff.OrcasDiff.DiffResult;
import de.opitzconsulting.orcas.diff.Parameters.ParameterTypeMode;
import de.opitzconsulting.orcas.extensions.AllExtensions;
import de.opitzconsulting.orcas.orig.diff.DiffRepository;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperCallableStatement;
import de.opitzconsulting.orcas.sql.WrapperExecuteStatement;
import de.opitzconsulting.orcas.sql.WrapperReturnFirstValue;
import de.opitzconsulting.orcas.syex.load.DataReader;
import de.opitzconsulting.orcas.syex.trans.TransformSyexOrig;
import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.OrcasDslPackage;
import de.opitzconsulting.orcasDsl.impl.ModelImpl;

public class OrcasMain
{
  private static Log _log;
  private static Map<Object,Object> loadOptions;
  private static XtextResourceSet resourceSet;
  private static int counter;

  private static Model callPlSqlExtensions( final Model pModel, final Parameters pParameters, final CallableStatementProvider pCallableStatementProvider )
  {
    String lCallExtensions = "" + //
                             " { " + //
                             "   ? = call " +
                             pParameters.getOrcasDbUser() +
                             ".pa_orcas_extensions.call_extensions( ? ) " + //
                             " } " + //
                             "";

    final Model lOutputModel = new ModelImpl();

    new WrapperCallableStatement( lCallExtensions, pCallableStatementProvider )
    {
      @Override
      protected void useCallableStatement( CallableStatement pCallableStatement ) throws SQLException
      {
        pCallableStatement.registerOutParameter( 1, java.sql.Types.STRUCT, (pParameters.getOrcasDbUser() + ".ot_syex_model").toUpperCase() );
        pCallableStatement.setObject( 2, createDataWriter( pCallableStatementProvider ).getStructModel( pModel ) );

        pCallableStatement.execute();

        DataReader.setIntNullValue( -1 );
        DataReader.loadIntoModel( lOutputModel, (Struct)pCallableStatement.getObject( 1 ) );
      }
    }.execute();

    return lOutputModel;
  }

  private static de.opitzconsulting.orcas.syex.load.DataWriter createDataWriter( final CallableStatementProvider pCallableStatementProvider )
  {
    final de.opitzconsulting.orcas.syex.load.DataWriter lDataWriter = new de.opitzconsulting.orcas.syex.load.DataWriter()
    {
      @Override
      protected Struct createStruct( String pTypeName, Object[] pAttributes )
      {
        return JdbcConnectionHandler.createStruct( pTypeName, pAttributes, pCallableStatementProvider );
      }

      @Override
      protected Array createArrayOf( String pTypeName, Object[] pElements )
      {
        return JdbcConnectionHandler.createArrayOf( pTypeName, pElements, pCallableStatementProvider );
      }
    };
    return lDataWriter;
  }

  private static void callTargetPlSql( final Model pModel, final Parameters pParameters, final CallableStatementProvider pCallableStatementProvider )
  {
    String lCallExtensions = "" + //
                             " begin " + //
                             pParameters.getOrcasDbUser() +
                             "." +
                             pParameters.getTargetplsql() +
                             ".run( ? ); " + //
                             " end; " + //
                             "";

    new WrapperCallableStatement( lCallExtensions, pCallableStatementProvider )
    {
      @Override
      protected void useCallableStatement( CallableStatement pCallableStatement ) throws SQLException
      {
        pCallableStatement.setObject( 1, createDataWriter( pCallableStatementProvider ).getStructModel( pModel ) );

        pCallableStatement.execute();
      }
    }.execute();
  }

  public static void main( String[] pArgs ) throws Exception
  {
    try
    {
      Parameters lParameters = new Parameters( pArgs, ParameterTypeMode.ORCAS_MAIN );

      _log = LogFactory.getLog( OrcasMain.class );

      if( lParameters.getSrcJdbcConnectParameters() != null )
      {
        doSchemaSync( lParameters );
      }
      else
      {
        CallableStatementProvider lCallableStatementProvider = JdbcConnectionHandler.createCallableStatementProvider( lParameters );
        InitDiffRepository.init( lCallableStatementProvider );

        log( "starting orcas statics" );

        Model lSyexModel;

        if( lParameters.getModelFile().endsWith( "xml" ) )
        {
          lSyexModel = loadModelXml( lParameters.getModelFile() );
        }
        else
        {
          initXtext();

          log( "loading files" );
          lSyexModel = loadModelDslFolder( new File( lParameters.getModelFile() ), lParameters );

          log( "calling java extensions" );
          lSyexModel = callJavaExtensions( lSyexModel );

          if( isPlSqlEextensionsExistst( lParameters, lCallableStatementProvider ) )
          {
            log( "calling pl/sql extensions" );
            lSyexModel = callPlSqlExtensions( lSyexModel, lParameters, lCallableStatementProvider );
          }
        }

        lSyexModel = new InlineCommentsExtension().transformModel( lSyexModel );
        lSyexModel = new InlineIndexExtension().transformModel( lSyexModel );

        if( lParameters.isCreatemissingfkindexes() )
        {
          lSyexModel = new AddFkIndexExtension().transformModel( lSyexModel );
        }

        if( lParameters.getTargetplsql().equals( "" ) )
        {
          log( "loading database" );
          de.opitzconsulting.origOrcasDsl.Model lDatabaseModel = new LoadIst( lCallableStatementProvider, lParameters ).loadModel();

          log( "building diff" );
          DiffRepository.getModelMerge().cleanupValues( lDatabaseModel );
          de.opitzconsulting.origOrcasDsl.Model lSollModel = TransformSyexOrig.convertModel( lSyexModel );
          DiffRepository.getModelMerge().cleanupValues( lSollModel );
          DiffResult lDiffResult = new OrcasDiff( lCallableStatementProvider, lParameters ).compare( lSollModel, lDatabaseModel );

          handleDiffResult( lParameters, lCallableStatementProvider, lDiffResult );
        }
        else
        {
          log( "executing " + lParameters.getTargetplsql() );
          callTargetPlSql( lSyexModel, lParameters, lCallableStatementProvider );
        }

        log( "done orcas statics" );
      }
    }
    catch( Exception e )
    {
      _log.error( e, e );

      System.exit( -1 );
    }
  }

  private static void handleDiffResult( Parameters pParameters, CallableStatementProvider pCallableStatementProvider, DiffResult pDiffResult ) throws FileNotFoundException
  {
    PrintStream lPrintStream = new PrintStream( new FileOutputStream( pParameters.getSpoolfile() ) );
    for( String lLine : pDiffResult.getSqlStatements() )
    {
      int lMaxLineLength = 2000;

      lLine = addLineBreaksIfNeeded( lLine, lMaxLineLength );

      if( lLine.endsWith( ";" ) )
      {
        lPrintStream.println( lLine );
        lPrintStream.println( "/" );
        log( lLine );
        log( "/" );
      }
      else
      {
        lPrintStream.println( lLine + ";" );
        log( lLine + ";" );
      }

      if( !pParameters.isLogonly() )
      {
        new WrapperExecuteStatement( lLine, pCallableStatementProvider ).execute();
      }
    }

    lPrintStream.close();
  }

  private static void doSchemaSync( Parameters pParameters ) throws FileNotFoundException
  {
    log( "starting orcas statics schema sync" );

    log( "loading source database" );
    CallableStatementProvider lSrcCallableStatementProvider = JdbcConnectionHandler.createCallableStatementProvider( pParameters, pParameters.getSrcJdbcConnectParameters() );
    InitDiffRepository.init( lSrcCallableStatementProvider );
    de.opitzconsulting.origOrcasDsl.Model lSrcModel = new LoadIst( lSrcCallableStatementProvider, pParameters ).loadModel();
    DiffRepository.getModelMerge().cleanupValues( lSrcModel );

    log( "loading destination database" );
    CallableStatementProvider lDestCallableStatementProvider = JdbcConnectionHandler.createCallableStatementProvider( pParameters, pParameters.getJdbcConnectParameters() );
    InitDiffRepository.init( lDestCallableStatementProvider );
    de.opitzconsulting.origOrcasDsl.Model lDstModel = new LoadIst( lDestCallableStatementProvider, pParameters ).loadModel();
    DiffRepository.getModelMerge().cleanupValues( lDstModel );

    log( "building diff" );
    DiffResult lDiffResult = new OrcasDiff( lDestCallableStatementProvider, pParameters ).compare( lSrcModel, lDstModel );

    handleDiffResult( pParameters, lDestCallableStatementProvider, lDiffResult );

    log( "done orcas statics schema sync" );
  }

  private static boolean isPlSqlEextensionsExistst( Parameters pParameters, CallableStatementProvider pCallableStatementProvider )
  {
    try
    {
      return BigDecimal.valueOf( 1 ).equals( new WrapperReturnFirstValue( "select " + pParameters.getOrcasDbUser() + ".pa_orcas_extensions.is_extensions_exists() from dual", pCallableStatementProvider ).executeForValue() );
    }
    catch( Exception e )
    {
      // package pa_orcas_extensions may not be installed 
      _log.debug( e, e );
      return false;
    }
  }

  private static void log( String pString )
  {
    _log.info( pString );
  }

  private static Model callJavaExtensions( Model lSyexModel )
  {
    lSyexModel = new AllExtensions().transformModel( lSyexModel );
    return lSyexModel;
  }

  private static String addLineBreaksIfNeeded( String pLine, int pMaxLineLength )
  {
    if( pLine.length() > pMaxLineLength )
    {
      List<String> lSubstrings = new ArrayList<String>();

      while( pLine.length() > 0 )
      {
        if( pLine.length() > pMaxLineLength )
        {
          lSubstrings.add( pLine.substring( 0, pMaxLineLength ) );
          pLine = pLine.substring( pMaxLineLength );
        }
        else
        {
          lSubstrings.add( pLine );
          pLine = "";
        }
      }

      StringBuilder lStringBuilder = new StringBuilder();

      for( String lLine : lSubstrings )
      {
        if( !lLine.contains( "\n" ) )
        {
          int lSpaceIndex = lLine.indexOf( " " );

          lLine = lLine.substring( 0, lSpaceIndex ) + "\n" + lLine.substring( lSpaceIndex );
        }

        lStringBuilder.append( lLine );
      }

      return lStringBuilder.toString();
    }
    else
    {
      return pLine;
    }
  }

  public static Model loadModelXml( String pFilename )
  {
    EPackage.Registry.INSTANCE.put( OrcasDslPackage.eNS_URI, OrcasDslPackage.eINSTANCE );
    Resource.Factory.Registry lRegistry = Resource.Factory.Registry.INSTANCE;
    Map<String,Object> lMap = lRegistry.getExtensionToFactoryMap();
    lMap.put( "xml", new XMLResourceFactoryImpl() );

    ResourceSet lResourceSet = new ResourceSetImpl();
    Resource lResource = lResourceSet.createResource( URI.createFileURI( pFilename ) );

    ((XMLResource)lResource).getDefaultSaveOptions();

    try
    {
      lResource.load( Collections.EMPTY_MAP );
    }
    catch( IOException e )
    {
      throw new RuntimeException( e );
    }

    return (Model)lResource.getContents().get( 0 );
  }

  public static Model loadModelDslFolder( File pFolder, Parameters pParameters )
  {
    Model lReturn = new ModelImpl();

    for( File lFile : pFolder.listFiles() )
    {
      if( lFile.isDirectory() )
      {
        if( pParameters.getScriptfolderrecursive() )
        {
          lReturn.getModel_elements().addAll( loadModelDslFolder( lFile, pParameters ).getModel_elements() );
        }
      }
      else
      {
        if( lFile.getName().startsWith( pParameters.getScriptprefix() ) && lFile.getName().endsWith( pParameters.getScriptpostfix() ) )
        {
          lReturn.getModel_elements().addAll( loadModelDslFile( lFile ).getModel_elements() );
        }
      }
    }

    return lReturn;
  }

  public static Model loadModelDslFile( File pFile )
  {
    Resource lResource = resourceSet.createResource( URI.createURI( "dummy:/dummy" + counter++ + ".orcasdsl" ) );
    try
    {
      FileInputStream lInputStream = new FileInputStream( pFile );
      lResource.load( lInputStream, loadOptions );
      lInputStream.close();
      return (Model)lResource.getContents().get( 0 );
    }
    catch( Exception e )
    {
      for( Diagnostic lDiagnostic : lResource.getErrors() )
      {
        System.err.println( "Error in File: " + pFile );
        System.err.println( lDiagnostic );
      }

      throw new RuntimeException( e );
    }
  }

  private static void initXtext()
  {
    Injector lInjector = new OrcasDslStandaloneSetup().createInjectorAndDoEMFRegistration();
    resourceSet = lInjector.getInstance( XtextResourceSet.class );

    resourceSet.addLoadOption( XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE );

    loadOptions = resourceSet.getLoadOptions();
    loadOptions.put( XtextResource.OPTION_ENCODING, "utf8" );
  }
}
