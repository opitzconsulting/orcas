package de.opitzconsulting.orcas.sql;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This Class is a CallableStatementProvider that works on a JDBC session.
 */
public class JdbcCallableStatementProvider implements CallableStatementProvider
{
  private Connection _connection;

  public JdbcCallableStatementProvider( Connection pConnection )
  {
    _connection = pConnection;
  }

  public CallableStatement createCallableStatement( String pStatement ) throws SQLException
  {
    return _connection.prepareCall( pStatement );
  }

  public PreparedStatement createPreparedStatement( String pStatement ) throws SQLException
  {
    return _connection.prepareStatement( pStatement );
  }

  public Statement createStatement( String pStatement ) throws SQLException
  {
    return _connection.createStatement();
  }
}