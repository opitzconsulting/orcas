package de.opitzconsulting.orcas.diff;

import java.sql.Connection;
import java.sql.DriverManager;

import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.JdbcCallableStatementProvider;

public class JdbcConnectionHandler
{
  private static Connection connection;

  public static void initWithMainParameters( Parameters pParameters )
  {
    try
    {
      Class.forName( pParameters.getJdbcDriver() );

      connection = DriverManager.getConnection( pParameters.getJdbcUrl(), pParameters.getJdbcUser(), pParameters.getJdbcPassword() );
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
