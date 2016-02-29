package  de.opitzconsulting.orcas;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import oracle.jdbc.OracleDriver;

import org.apache.commons.io.IOUtils;


public class OrcasExtractViews 
{
  public static void main(String[] pArgs) {
    try
    {
      boolean lFullMode = pArgs[3].equals("full");

      Class.forName( "oracle.jdbc.OracleDriver" );
      if( OracleDriver.class != null )
        ;

      Connection lConnection = DriverManager.getConnection( pArgs[0], pArgs[1], pArgs[2]);

      PreparedStatement lPrepareStatement;

      if( lFullMode )
      {
        lPrepareStatement = lConnection.prepareStatement("select lower(view_name), dbms_metadata.get_ddl('VIEW',view_name) from user_views");
      }
      else
      {
        lPrepareStatement = lConnection.prepareStatement("select lower(view_name), text from user_views");
      }
      
      lPrepareStatement.execute();
      
      ResultSet lResultSet = lPrepareStatement.getResultSet();

      while(lResultSet.next())
      {
        FileOutputStream lFileOutputStream = new FileOutputStream(lResultSet.getString(1) + ".sql");
      
        OutputStreamWriter lOutputStreamWriter = new OutputStreamWriter(lFileOutputStream,"utf8");

        if(!lFullMode)
        {
          lOutputStreamWriter.write("create or replace force view " + lResultSet.getString(1) + " as\n");
        }
       
        IOUtils.copy(lResultSet.getCharacterStream(2), lOutputStreamWriter);

        lOutputStreamWriter.write("\n/");
      
        lOutputStreamWriter.flush();
        lFileOutputStream.flush();
      }
      
      lResultSet.close();      
      lConnection.close();
    }
    catch( Exception e )
    {
      e.printStackTrace();
    }
  }
}
