package de.opitzconsulting.orcas.diff;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import de.opitzconsulting.orcas.diff.Parameters.ParameterTypeMode;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.syex.trans.TransformOrigSyex;
import de.opitzconsulting.orcas.syex.xml.XmlExport;
import de.opitzconsulting.orcasDsl.Model;

public class OrcasLoadExtract
{
  public static void main( String[] pArgs )
  {
    try
    {
      Parameters lParameters = new Parameters( pArgs, ParameterTypeMode.ORCAS_LOAD_EXTRACT );

      CallableStatementProvider lCallableStatementProvider = JdbcConnectionHandler.createCallableStatementProvider( lParameters );

      Model lModel = TransformOrigSyex.convertModel( new LoadIst( lCallableStatementProvider, lParameters ).loadModel() );

      FileOutputStream lFileOutputStream = new FileOutputStream( lParameters.getModelFile() );

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
