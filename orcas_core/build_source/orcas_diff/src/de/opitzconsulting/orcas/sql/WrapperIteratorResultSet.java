package de.opitzconsulting.orcas.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * This class iterates through a resultset and calls its callback method for each entry in the resultset.
 */
public abstract class WrapperIteratorResultSet extends WrapperResultSet
{
  /**
   * Standard Constructor.
   */
  public WrapperIteratorResultSet( String pSqlString, CallableStatementProvider pCallableStatementProvider, List pParameters, String pClientContextName )
  {
    super( pSqlString, pCallableStatementProvider, pParameters, pClientContextName );
  }

  /**
   * Standard Constructor.
   */
  public WrapperIteratorResultSet( String pSqlString, CallableStatementProvider pCallableStatementProvider, String pClientContextName )
  {
    this( pSqlString, pCallableStatementProvider, null, pClientContextName );
  }

  /**
   * Standard Constructor.
   */
  public WrapperIteratorResultSet( String pSqlString, CallableStatementProvider pCallableStatementProvider, List pParameters )
  {
    this( pSqlString, pCallableStatementProvider, pParameters, null );
  }

  /**
   * Standard Constructor.
   */
  public WrapperIteratorResultSet( String pSqlString, CallableStatementProvider pCallableStatementProvider )
  {
    this( pSqlString, pCallableStatementProvider, null, null );
  }

  /**
   * iterates through the resultset and calls the callback methods.
   */
  protected final void useResultSet( ResultSet pResultSet ) throws SQLException
  {
    boolean lFirst = true;

    if( !pResultSet.next() )
    {
      handleEmptyResultSet();
    }
    else
    {
      while( lFirst || pResultSet.next() )
      {
        lFirst = false;

        useResultSetRow( pResultSet );
      }
    }
  }

  /**
   * Is called for ech row in the ResultSet.
   */
  protected abstract void useResultSetRow( ResultSet pResultSet ) throws SQLException;

  /**
   * Is called, if the ResultSet is empty. (and only if it is empty).
   */
  protected void handleEmptyResultSet()
  {
  }
}
