package de.opitzconsulting.orcas.diff;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;

import de.opitzconsulting.orcas.diff.OrcasDiff.DiffResult;
import de.opitzconsulting.orcas.diff.Parameters.ParameterTypeMode;
import de.opitzconsulting.orcas.extensions.AllExtensions;
import de.opitzconsulting.orcas.orig.diff.DiffRepository;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperCallableStatement;
import de.opitzconsulting.orcas.sql.WrapperExecuteStatement;
import de.opitzconsulting.orcas.syex.load.DataReader;
import de.opitzconsulting.orcas.syex.trans.TransformSyexOrig;
import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.impl.ModelImpl;

public class OrcasMain extends Orcas
{
  public static void main( String[] pArgs )
  {
    new OrcasMain().mainRun( pArgs );
  }

  @Override
  protected ParameterTypeMode getParameterTypeMode()
  {
    return ParameterTypeMode.ORCAS_MAIN;
  }

  @Override
  protected void run() throws Exception
  {
    if( getParameters().getSrcJdbcConnectParameters() != null )
    {
      doSchemaSync( getParameters() );
    }
    else
    {
      CallableStatementProvider lCallableStatementProvider = JdbcConnectionHandler.createCallableStatementProvider( getParameters() );
      InitDiffRepository.init( lCallableStatementProvider );

      _log.info( "starting orcas statics" );

      Model lSyexModel;

      if( getParameters().getModelFile().endsWith( "xml" ) )
      {
        lSyexModel = XtextFileLoader.loadModelXml( getParameters().getModelFile() );
      }
      else
      {
        if( getParameters().getSqlplustable() )
        {
          _log.info( "loading sqlplus data" );
          lSyexModel = loadModelFromSqlplusTable( getParameters(), lCallableStatementProvider );
        }
        else
        {
          _log.info( "loading files" );
          XtextFileLoader.initXtext();
          lSyexModel = XtextFileLoader.loadModelDslFolder( getParameters() );
        }

        _log.info( "calling java extensions" );
        lSyexModel = callJavaExtensions( lSyexModel );

        if( PlSqlHandler.isPlSqlEextensionsExistst( getParameters(), lCallableStatementProvider ) )
        {
          _log.info( "calling pl/sql extensions" );
          lSyexModel = PlSqlHandler.callPlSqlExtensions( lSyexModel, getParameters(), lCallableStatementProvider, false );
        }
      }

      lSyexModel = new InlineCommentsExtension().transformModel( lSyexModel );
      lSyexModel = new InlineIndexExtension().transformModel( lSyexModel );

      if( getParameters().isCreatemissingfkindexes() )
      {
        lSyexModel = new AddFkIndexExtension().transformModel( lSyexModel );
      }

      if( getParameters().getTargetplsql().equals( "" ) )
      {
        _log.info( "loading database" );
        de.opitzconsulting.origOrcasDsl.Model lDatabaseModel = new LoadIst( lCallableStatementProvider, getParameters() ).loadModel( true );

        _log.info( "building diff" );
        DiffRepository.getModelMerge().cleanupValues( lDatabaseModel );
        de.opitzconsulting.origOrcasDsl.Model lSollModel = TransformSyexOrig.convertModel( lSyexModel );
        DiffRepository.getModelMerge().cleanupValues( lSollModel );
        DiffResult lDiffResult = new OrcasDiff( lCallableStatementProvider, getParameters() ).compare( lSollModel, lDatabaseModel );

        handleDiffResult( getParameters(), lCallableStatementProvider, lDiffResult );
      }
      else
      {
        _log.info( "executing " + getParameters().getTargetplsql() );
        PlSqlHandler.callTargetPlSql( lSyexModel, getParameters(), lCallableStatementProvider );
      }

      _log.info( "done orcas statics" );
    }
  }

  private static Model loadModelFromSqlplusTable( final Parameters pParameters, CallableStatementProvider pCallableStatementProvider )
  {
    String lCallExtensions = "" + //
                             " declare" +
                             " v_model " +
                             pParameters.getOrcasDbUser() +
                             ".ot_syex_model;" +
                             " v_anydata SYS.ANYDATA;" +
                             " begin " + //
                             "   select model into v_anydata from " +
                             pParameters.getOrcasDbUser() +
                             ".orcas_sqlplus_model;" +
                             " if( v_anydata.getObject( v_model ) = DBMS_TYPES.SUCCESS )" +
                             " then " +
                             "    null; " +
                             " end if;" + //
                             " ? := v_model;" + //
                             " end; " + //
                             "";

    final Model lOutputModel = new ModelImpl();

    new WrapperCallableStatement( lCallExtensions, pCallableStatementProvider )
    {
      @Override
      protected void useCallableStatement( CallableStatement pCallableStatement ) throws SQLException
      {
        pCallableStatement.registerOutParameter( 1, java.sql.Types.STRUCT, (pParameters.getOrcasDbUser() + ".ot_syex_model").toUpperCase() );

        pCallableStatement.execute();

        DataReader.setIntNullValue( DiffRepository.getNullIntValue() );
        DataReader.loadIntoModel( lOutputModel, (Struct)pCallableStatement.getObject( 1 ) );
      }
    }.execute();

    return lOutputModel;
  }

  private static void handleDiffResult( Parameters pParameters, CallableStatementProvider pCallableStatementProvider, DiffResult pDiffResult ) throws FileNotFoundException
  {
    PrintStream lPrintStream;
    if( !pParameters.getSpoolfile().equals( "" ) )
    {
      lPrintStream = new PrintStream( new FileOutputStream( pParameters.getSpoolfile() ) );
    }
    else
    {
      // null lPrintStream 
      lPrintStream = new PrintStream( new ByteArrayOutputStream() );
    }
    for( String lLine : pDiffResult.getSqlStatements() )
    {
      int lMaxLineLength = 2000;

      lLine = addLineBreaksIfNeeded( lLine, lMaxLineLength );

      if( lLine.endsWith( ";" ) )
      {
        lPrintStream.println( lLine );
        lPrintStream.println( "/" );
        _log.info( lLine );
        _log.info( "/" );
      }
      else
      {
        lPrintStream.println( lLine + ";" );
        _log.info( lLine + ";" );
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
    _log.info( "starting orcas statics schema sync" );

    _log.info( "loading source database" );
    CallableStatementProvider lSrcCallableStatementProvider = JdbcConnectionHandler.createCallableStatementProvider( pParameters, pParameters.getSrcJdbcConnectParameters() );
    InitDiffRepository.init( lSrcCallableStatementProvider );
    de.opitzconsulting.origOrcasDsl.Model lSrcModel = new LoadIst( lSrcCallableStatementProvider, pParameters ).loadModel( false );
    DiffRepository.getModelMerge().cleanupValues( lSrcModel );

    _log.info( "loading destination database" );
    CallableStatementProvider lDestCallableStatementProvider = JdbcConnectionHandler.createCallableStatementProvider( pParameters, pParameters.getJdbcConnectParameters() );
    InitDiffRepository.init( lDestCallableStatementProvider );
    de.opitzconsulting.origOrcasDsl.Model lDstModel = new LoadIst( lDestCallableStatementProvider, pParameters ).loadModel( true );
    DiffRepository.getModelMerge().cleanupValues( lDstModel );

    _log.info( "building diff" );
    DiffResult lDiffResult = new OrcasDiff( lDestCallableStatementProvider, pParameters ).compare( lSrcModel, lDstModel );

    handleDiffResult( pParameters, lDestCallableStatementProvider, lDiffResult );

    _log.info( "done orcas statics schema sync" );
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
}
