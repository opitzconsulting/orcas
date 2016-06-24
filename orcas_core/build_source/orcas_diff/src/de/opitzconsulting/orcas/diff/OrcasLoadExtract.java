package de.opitzconsulting.orcas.diff;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.opitzconsulting.orcas.diff.Parameters.ParameterTypeMode;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.syex.trans.TransformOrigSyex;
import de.opitzconsulting.orcas.syex.xml.XmlExport;
import de.opitzconsulting.orcasDsl.Model;

public class OrcasLoadExtract
{
  private static Log _log;

  public static void main( String[] pArgs )
  {
    try
    {
      Parameters lParameters = new Parameters( pArgs, ParameterTypeMode.ORCAS_LOAD_EXTRACT );

      _log = LogFactory.getLog( OrcasLoadExtract.class );

      CallableStatementProvider lCallableStatementProvider = JdbcConnectionHandler.createCallableStatementProvider( lParameters );

      _log.info( "loading database" );
      Model lModel = TransformOrigSyex.convertModel( new LoadIst( lCallableStatementProvider, lParameters ).loadModel() );

      if( !lParameters.getModelFile().equals( "" ) )
      {
        _log.info( "loading additional model files" );
        XtextFileLoader.initXtext();
        lModel.getModel_elements().addAll( XtextFileLoader.loadModelDslFolder( new File( lParameters.getModelFile() ), lParameters ).getModel_elements() );
      }

      if( PlSqlHandler.isPlSqlEextensionsExistst( lParameters, lCallableStatementProvider ) )
      {
        _log.info( "calling pl/sql reverse-extensions" );
        lModel = PlSqlHandler.callPlSqlExtensions( lModel, lParameters, lCallableStatementProvider, true );
      }

      _log.info( "writing xml" );
      FileOutputStream lFileOutputStream = new FileOutputStream( lParameters.getSpoolfile() );

      OutputStreamWriter lOutputStreamWriter = new OutputStreamWriter( lFileOutputStream, "utf8" );

      lOutputStreamWriter.append( new XmlExport().getModel( lModel, true ) );
      lOutputStreamWriter.flush();
      lFileOutputStream.close();

    }
    catch( Exception e )
    {
      e.printStackTrace();
    }
  }
}
