package de.opitzconsulting.orcas.diff;

import java.sql.Connection;
import java.sql.DriverManager;

import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.JdbcCallableStatementProvider;

public class JdbcConnectionHandler
{
  private static Connection connection;

  public static void initWithMainParameters( String[] pArgs )
  {
    try
    {
      Class.forName( pArgs[0] );

      connection = DriverManager.getConnection( pArgs[1], pArgs[2], pArgs[3] );
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }

  public static CallableStatementProvider getCallableStatementProvider()
  {
    return new JdbcCallableStatementProvider( connection );
  }
}
