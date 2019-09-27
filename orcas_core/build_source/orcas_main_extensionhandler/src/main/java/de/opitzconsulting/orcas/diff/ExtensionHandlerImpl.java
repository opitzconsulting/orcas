package de.opitzconsulting.orcas.diff;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.List;
import java.net.URL;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.opitzconsulting.orcas.xslt.XsltExtractDirAccessClass;

import org.apache.commons.logging.Log;

import de.opitzconsulting.OrcasDslStandaloneSetup;
import de.opitzconsulting.orcas.diff.JdbcConnectionHandler.RunWithCallableStatementProvider;
import de.opitzconsulting.orcas.extensions.AllExtensions;
import de.opitzconsulting.orcas.orig.diff.DiffRepository;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperCallableStatement;
import de.opitzconsulting.orcas.syex.load.DataReader;
import de.opitzconsulting.orcas.syex.trans.TransformOrigSyex;
import de.opitzconsulting.orcas.syex.trans.TransformSyexOrig;
import de.opitzconsulting.orcas.syex.xml.XmlExport;
import de.opitzconsulting.orcasDsl.OrcasDslPackage;
import de.opitzconsulting.orcasDsl.Sequence;
import de.opitzconsulting.orcasDsl.Table;
import de.opitzconsulting.orcasDsl.impl.ModelImpl;
import de.opitzconsulting.origOrcasDsl.Model;


public class ExtensionHandlerImpl extends BaseExtensionHandlerImpl<de.opitzconsulting.orcasDsl.Model>
{
  protected de.opitzconsulting.orcasDsl.Model loadModelFromSqlplusTable() throws Exception
  {
    final de.opitzconsulting.orcasDsl.Model lOutputModel = new ModelImpl();

    JdbcConnectionHandler.runWithCallableStatementProvider( getParameters(), getParameters().getOrcasJdbcConnectParameters(), new RunWithCallableStatementProvider()
    {
      public void run( CallableStatementProvider pCallableStatementProvider ) throws Exception
      {
        String lCallExtensions = "" + //
        " declare" + " v_model " + getParameters().getOrcasDbUser() + ".ot_syex_model;" + " v_anydata SYS.ANYDATA;" + " begin " + //
        "   select model into v_anydata from " + getParameters().getOrcasDbUser() + ".orcas_sqlplus_model;" + " if( v_anydata.getObject( v_model ) = DBMS_TYPES.SUCCESS )" + " then " + "    null; " + " end if;" + //
        " ? := v_model;" + //
        " end; " + //
        "";

        new WrapperCallableStatement( lCallExtensions, pCallableStatementProvider )
        {
          @Override
          protected void useCallableStatement( CallableStatement pCallableStatement ) throws SQLException
          {
            pCallableStatement.registerOutParameter( 1, java.sql.Types.STRUCT, (getParameters().getOrcasDbUser() + ".ot_syex_model").toUpperCase() );

            pCallableStatement.execute();

            DataReader.setIntNullValue( DiffRepository.getNullIntValue() );
            DataReader.loadIntoModel( lOutputModel, (Struct) pCallableStatement.getObject( 1 ) );
          }
        }.execute();
      }
    } );

    return lOutputModel;
  }

  @Override
  public Model loadModel()
  {
    return TransformSyexOrig.convertModel( loadSyexModel() );
  }

  public de.opitzconsulting.orcasDsl.Model loadSyexModel()
  {
    de.opitzconsulting.orcasDsl.Model lSyexModel;
    if( getParameters().getModelFile().endsWith( "xml" ) )
    {
      lSyexModel = createlXtextFileLoader().loadModelXml( getParameters().getModelFile(), OrcasDslPackage.eNS_URI, OrcasDslPackage.eINSTANCE );
    }
    else
    {
      if( getParameters().getSqlplustable() )
      {
        logInfo( "loading sqlplus data" );
        try
        {
          lSyexModel = loadModelFromSqlplusTable();
        }
        catch( Exception e )
        {
          throw new RuntimeException( e );
        }
      }
      else
      {
        logInfo( "loading files" );
        lSyexModel = loadSyexModelFromFiles();
      }

      AllExtensions lAllExtensions = new AllExtensions();
      lAllExtensions.setUseReverseExtension( false );
      if( lAllExtensions.hasExtension() )
      {
        logInfo( "calling java extensions" );
        lSyexModel = callJavaExtensions( lSyexModel, lAllExtensions );
      }

      List<UnaryOperator<de.opitzconsulting.orcasDsl.Model>> lAdditionalExtensions = getParameters().getAdditionalOrcasExtensionFactory().getAdditionalExtensions( de.opitzconsulting.orcasDsl.Model.class, false );
      if( !lAdditionalExtensions.isEmpty() )
      {
        logInfo( "calling additional extensions" );
        for( UnaryOperator<de.opitzconsulting.orcasDsl.Model> lOrcasExtension : lAdditionalExtensions )
        {
          lSyexModel = lOrcasExtension.apply( lSyexModel );
        }
      }

      if( PlSqlHandler.isPlSqlEextensionsExistst() )
      {
        logInfo( "calling pl/sql extensions" );
        try
        {
          lSyexModel = PlSqlHandler.callPlSqlExtensions( lSyexModel, getParameters(), false );
        }
        catch( Exception e )
        {
          throw new RuntimeException( e );
        }
      }
    }

    return lSyexModel;
  }

  @Override
  protected XtextFileLoader<de.opitzconsulting.orcasDsl.Model> createlXtextFileLoader()
  {
    return new XtextFileLoader<de.opitzconsulting.orcasDsl.Model>()
    {

      @Override
      protected List<String> getTableNames(de.opitzconsulting.orcasDsl.Model pModel) {
        return pModel.getModel_elements()
              .stream()
              .filter(p->p instanceof Table)
              .map(p->(Table)p)
              .map(Table::getName)
              .map(String::toUpperCase)
              .collect(Collectors.toList());
      }

      @Override
      protected List<String> getSequenceNames(de.opitzconsulting.orcasDsl.Model pModel) {
        return pModel.getModel_elements()
                     .stream()
                     .filter(p->p instanceof Sequence)
                     .map(p->(Sequence)p)
                     .map(Sequence::getSequence_name)
                     .map(String::toUpperCase)
                     .collect(Collectors.toList());
      }

      @Override
      protected void combinModelResults( de.opitzconsulting.orcasDsl.Model pCombinedModel, de.opitzconsulting.orcasDsl.Model pModelPartFromSingleFile )
      {
        pCombinedModel.getModel_elements().addAll( pModelPartFromSingleFile.getModel_elements() );
      }

      @Override
      protected de.opitzconsulting.orcasDsl.Model createModelInstance()
      {
        return new ModelImpl();
      }

      @Override
      protected String getXtextExpectedFileEnding()
      {
        return "orcasdsl";
      }
    };
  }

  @Override
  public String convertModelToXMLString( Model lOrigModel )
  {
    de.opitzconsulting.orcasDsl.Model lSyexModel = TransformOrigSyex.convertModel( lOrigModel );

    if( getParameters().getModelFiles() != null || !getParameters().getModelFile().equals( "" ) )
    {
      logInfo( "loading additional model files" );
      lSyexModel.getModel_elements().addAll( loadSyexModelFromFiles().getModel_elements() );
    }

    if( PlSqlHandler.isPlSqlEextensionsExistst() && getParameters().isLoadExtractWithReverseExtensions() )
    {
      logInfo( "calling pl/sql reverse-extensions" );
      try
      {
        lSyexModel = PlSqlHandler.callPlSqlExtensions( lSyexModel, getParameters(), true );
      }
      catch( Exception e )
      {
        throw new RuntimeException( e );
      }
    }

    AllExtensions lAllExtensions = new AllExtensions();
    lAllExtensions.setUseReverseExtension( true );
    if( lAllExtensions.hasExtension() )
    {
      logInfo( "calling java reverse-extensions" );
      lSyexModel = callJavaExtensions( lSyexModel, lAllExtensions );
    }

    List<UnaryOperator<de.opitzconsulting.orcasDsl.Model>> lAdditionalReverseExtensions = getParameters().getAdditionalOrcasExtensionFactory().getAdditionalExtensions( de.opitzconsulting.orcasDsl.Model.class, true );
    if( !lAdditionalReverseExtensions.isEmpty() )
    {
      logInfo( "calling additional reverse-extensions" );
      for( UnaryOperator<de.opitzconsulting.orcasDsl.Model> lOrcasExtension : lAdditionalReverseExtensions )
      {
        lSyexModel = lOrcasExtension.apply( lSyexModel );
      }
    }

    return new XmlExport().getModel( lSyexModel, true );
  }

  protected de.opitzconsulting.orcasDsl.Model callJavaExtensions( de.opitzconsulting.orcasDsl.Model pSyexModel, AllExtensions pAllExtensions )
  {
    if( getParameters().getExtensionParameter().length() != 0 )
    {
      pAllExtensions.setParameter( getParameters().getExtensionParameter() );
    }
    pSyexModel = pAllExtensions.transformModel( pSyexModel );

    return pSyexModel;
  }

  @Override
  public void handleTargetplsql( CallableStatementProvider pCallableStatementProvider )
  {
    logInfo( "executing " + getParameters().getTargetplsql() );
    PlSqlHandler.callTargetPlSql( loadSyexModelFromFiles(), getParameters(), pCallableStatementProvider );
  }

  protected de.opitzconsulting.orcasDsl.Model loadSyexModelFromFiles()
  {
    return loadSyexModelFromFiles( new OrcasDslStandaloneSetup().createInjectorAndDoEMFRegistration() );
  }

  @Override
  public void initOrcasDbIfNeeded( Log pLog )
  {
    new OrcasInitializeOrcasDbImpl( getParameters() ).initOrcasDb( pLog );
  }

  
  @Override
  public URL getXsltExtractFileURL()
  {
    return XsltExtractDirAccessClass.getXsltExtractFileURL();
  }

  @Override
  public URL getUriResolverURLForImport( String pHref )
  {
    return XsltExtractDirAccessClass.getUriResolverURLForImport( pHref );
  }
}
