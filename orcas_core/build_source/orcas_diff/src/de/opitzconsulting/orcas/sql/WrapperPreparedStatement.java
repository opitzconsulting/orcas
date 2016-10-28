package de.opitzconsulting.orcas.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The basic sql-wrapping-class, which wrapps the use of an CallableStatement.
 */
public abstract class WrapperPreparedStatement
{
  private static Log _log = LogFactory.getLog( WrapperPreparedStatement.class );

  private String _sqlString;

  private CallableStatementProvider _callableStatementProvider;

  /**
   * Standard Constructor.
   */
  public WrapperPreparedStatement( String pSqlString, CallableStatementProvider pCallableStatementProvider )
  {
    _sqlString = pSqlString;
    _callableStatementProvider = pCallableStatementProvider;
  }

  /**
   * Executes the CallableStatement. This method has to be called in order to make this class do anything.
   */
  public final void execute()
  {
    _log.debug( _sqlString );

    PreparedStatement lPreparedStatement = null;

    try
    {
      lPreparedStatement = _callableStatementProvider.createPreparedStatement( _sqlString );

      usePreparedStatement( lPreparedStatement );
    }
    catch( SQLException e )
    {
      if( !handleSQLException( e ) )
      {
        throw new RuntimeException( e );
      }
    }
    finally
    {
      if( lPreparedStatement != null )
      {
        try
        {
          lPreparedStatement.close();
        }
        catch( SQLException eSQLException )
        {
          throw ExceptionManager.createException( eSQLException, _sqlString );
        }
      }
    }
  }

  protected boolean handleSQLException( SQLException pSQLException )
  {
    return false;
  }

  /**
   * Is called when the CallableStatement is ready-to-use. Only use the CallableStatement during this method, do not close it.
   */
  protected abstract void usePreparedStatement( PreparedStatement pPreparedStatement ) throws SQLException;
}
