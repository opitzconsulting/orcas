package de.opitzconsulting.orcas.diff;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import de.opitzconsulting.orcas.diff.Parameters.ParameterTypeMode;
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
  protected void run( Parameters pParameters ) throws Exception
  {
    CallableStatementProvider lCallableStatementProvider = JdbcConnectionHandler.createCallableStatementProvider( pParameters );

    _log.info( "loading database" );
    de.opitzconsulting.origOrcasDsl.Model lOrigModel = new LoadIst( lCallableStatementProvider, pParameters ).loadModel();

    if( pParameters.isRemoveDefaultValuesFromModel() )
    {
      InitDiffRepository.init( lCallableStatementProvider );
      _log.info( "removing default values" );
      DiffRepository.getModelMerge().cleanupValues( lOrigModel );
    }

    Model lSyexModel = TransformOrigSyex.convertModel( lOrigModel );

    if( !pParameters.getModelFile().equals( "" ) )
    {
      _log.info( "loading additional model files" );
      XtextFileLoader.initXtext();
      lSyexModel.getModel_elements().addAll( XtextFileLoader.loadModelDslFolder( new File( pParameters.getModelFile() ), pParameters ).getModel_elements() );
    }

    if( PlSqlHandler.isPlSqlEextensionsExistst( pParameters, lCallableStatementProvider ) )
    {
      _log.info( "calling pl/sql reverse-extensions" );
      lSyexModel = PlSqlHandler.callPlSqlExtensions( lSyexModel, pParameters, lCallableStatementProvider, true );
    }

    _log.info( "writing xml" );
    FileOutputStream lFileOutputStream = new FileOutputStream( pParameters.getSpoolfile() );

    OutputStreamWriter lOutputStreamWriter = new OutputStreamWriter( lFileOutputStream, "utf8" );

    lOutputStreamWriter.append( new XmlExport().getModel( lSyexModel, true ) );
    lOutputStreamWriter.flush();
    lFileOutputStream.close();
  }
}
