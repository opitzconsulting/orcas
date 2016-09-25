package de.opitzconsulting.orcas.diff;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import de.opitzconsulting.orcas.diff.ParametersCommandline.ParameterTypeMode;
import de.opitzconsulting.orcas.orig.diff.DiffRepository;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.syex.trans.TransformOrigSyex;
import de.opitzconsulting.orcas.syex.xml.XmlExport;
import de.opitzconsulting.orcasDsl.Model;

public class OrcasLoadExtract extends Orcas
{
  public static void main( String[] pArgs )
  {
    new OrcasLoadExtract().mainRun( pArgs );
  }

  @Override
  protected ParameterTypeMode getParameterTypeMode()
  {
    return ParameterTypeMode.ORCAS_LOAD_EXTRACT;
  }

  @Override
  protected void run() throws Exception
  {
    CallableStatementProvider lCallableStatementProvider = JdbcConnectionHandler.createCallableStatementProvider( getParameters() );

    _log.info( "loading database" );
    de.opitzconsulting.origOrcasDsl.Model lOrigModel = new LoadIst( lCallableStatementProvider, getParameters() ).loadModel( true );

    if( getParameters().isRemoveDefaultValuesFromModel() )
    {
      InitDiffRepository.init( lCallableStatementProvider );
      _log.info( "removing default values" );
      DiffRepository.getModelMerge().cleanupValues( lOrigModel );
    }

    Model lSyexModel = TransformOrigSyex.convertModel( lOrigModel );

    if( !getParameters().getModelFile().equals( "" ) )
    {
      _log.info( "loading additional model files" );
      XtextFileLoader.initXtext();
      lSyexModel.getModel_elements().addAll( XtextFileLoader.loadModelDslFolder( getParameters() ).getModel_elements() );
    }

    if( PlSqlHandler.isPlSqlEextensionsExistst( getParameters(), lCallableStatementProvider ) )
    {
      _log.info( "calling pl/sql reverse-extensions" );
      lSyexModel = PlSqlHandler.callPlSqlExtensions( lSyexModel, getParameters(), lCallableStatementProvider, true );
    }

    _log.info( "writing xml" );
    FileOutputStream lFileOutputStream = new FileOutputStream( getParameters().getSpoolfile() );

    OutputStreamWriter lOutputStreamWriter = new OutputStreamWriter( lFileOutputStream, "utf8" );

    lOutputStreamWriter.append( new XmlExport().getModel( lSyexModel, true ) );
    lOutputStreamWriter.flush();
    lFileOutputStream.close();
  }
}
