package de.opitzconsulting.orcas.sql;

import java.sql.Connection;
import java.sql.SQLException;

public class JdbcTransactionalCallableStatementProvider extends JdbcCallableStatementProvider implements TransactionalCallableStatementProvider
{
  private Connection _connection;

  /**
   * Standard Konstruktor.
   */
  public JdbcTransactionalCallableStatementProvider( Connection pConnection )
  {
    super( pConnection );

    _connection = pConnection;
  }

  public void commit()
  {
    try
    {
      _connection.commit();
    }
    catch( SQLException e )
    {
      throw ExceptionManager.createException( e );
    }
  }

  public void rollback()
  {
    try
    {
      _connection.rollback();
    }
    catch( SQLException e )
    {
      throw ExceptionManager.createException( e );
    }
  }
}
