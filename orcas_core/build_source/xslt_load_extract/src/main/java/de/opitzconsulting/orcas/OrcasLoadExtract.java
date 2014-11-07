package  de.opitzconsulting.orcas;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import oracle.jdbc.OracleDriver;

import org.apache.commons.io.IOUtils;


public class OrcasLoadExtract 
{
	public static void main(String[] pArgs) {
    try
    {
      Class.forName( "oracle.jdbc.OracleDriver" );
      if( OracleDriver.class != null )
        ;

      Connection lConnection = DriverManager.getConnection( pArgs[0], pArgs[1], pArgs[2]);
      
      PreparedStatement lPrepareStatement = lConnection.prepareStatement("select pa_orcas_xml_syex.get_model( pa_orcas_extensions.call_reverse_extensions( pa_orcas_trans_orig_syex.trans_orig_syex( pa_orcas_load_ist.get_ist()))) json_col from dual");
      
      lPrepareStatement .execute();
      
      ResultSet lResultSet = lPrepareStatement.getResultSet();
      
      lResultSet.next();
      
      FileOutputStream lFileOutputStream = new FileOutputStream(pArgs[3]);
      
      OutputStreamWriter lOutputStreamWriter = new OutputStreamWriter(lFileOutputStream,"utf8");
      
      IOUtils.copy(lResultSet.getClob(1).getCharacterStream(), lOutputStreamWriter);
      
      lOutputStreamWriter.flush();
      lFileOutputStream.flush();
      
      lResultSet.close();      
      lConnection.close();
    }
    catch( Exception e )
    {
      e.printStackTrace();
    }
  }
}
