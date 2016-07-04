package de.opitzconsulting.orcas.diff;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.opitzconsulting.orcas.diff.Parameters.ParameterTypeMode;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperIteratorResultSet;

public class OrcasExtractViews extends Orcas
{
  public static void main( String[] pArgs )
  {
    new OrcasExtractViews().mainRun( pArgs );
  }

  private static void streamCopy( Reader pReader, Writer pWriter ) throws IOException
  {
    char[] lBuffer = new char[32000];

    int lCurrentCahrCount = 0;

    while( -1 != (lCurrentCahrCount = pReader.read( lBuffer )) )
    {
      pWriter.write( lBuffer, 0, lCurrentCahrCount );
    }
  }

  @Override
  protected ParameterTypeMode getParameterTypeMode()
  {
    return ParameterTypeMode.ORCAS_EXTRACT_VIEWS;
  }

  @Override
  protected void run( Parameters pParameters )
  {
    final boolean lFullMode = pParameters.getViewExtractMode().equals( "full" );

    CallableStatementProvider lCallableStatementProvider = JdbcConnectionHandler.createCallableStatementProvider( pParameters );

    String lSql;
    if( lFullMode )
    {
      lSql = "select lower(view_name), dbms_metadata.get_ddl('VIEW',view_name) from user_views";
    }
    else
    {
      lSql = "select lower(view_name), text from user_views";
    }

    new WrapperIteratorResultSet( lSql, lCallableStatementProvider )
    {
      @Override
      protected void useResultSetRow( ResultSet pResultSet ) throws SQLException
      {
        try
        {
          FileOutputStream lFileOutputStream = new FileOutputStream( pResultSet.getString( 1 ) + ".sql" );

          OutputStreamWriter lOutputStreamWriter = new OutputStreamWriter( lFileOutputStream, "utf8" );

          if( !lFullMode )
          {
            lOutputStreamWriter.write( "create or replace force view " + pResultSet.getString( 1 ) + " as\n" );
          }

          streamCopy( pResultSet.getCharacterStream( 2 ), lOutputStreamWriter );

          lOutputStreamWriter.write( "\n/" );

          lOutputStreamWriter.flush();
          lFileOutputStream.flush();
        }
        catch( IOException e )
        {
          throw new RuntimeException( e );
        }
      }
    }.execute();
  }
}
