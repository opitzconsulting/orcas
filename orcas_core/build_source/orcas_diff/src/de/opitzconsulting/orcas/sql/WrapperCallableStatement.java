package de.opitzconsulting.orcas.sql;

import java.sql.CallableStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The basic sql-wrapping-class, which wrapps the use of an CallableStatement.
 */
public abstract class WrapperCallableStatement
{
  private static Log _log = LogFactory.getLog( WrapperCallableStatement.class );

  private String _sqlString;
  private CallableStatementProvider _callableStatementProvider;

  /**
   * Standard Constructor.
   */
  public WrapperCallableStatement( String pSqlString, CallableStatementProvider pCallableStatementProvider, String pClientContextName )
  {
    _sqlString = pSqlString;
    _callableStatementProvider = pCallableStatementProvider;
  }

  /**
   * Standard Constructor.
   */
  public WrapperCallableStatement( String pSqlString, CallableStatementProvider pCallableStatementProvider )
  {
    this( pSqlString, pCallableStatementProvider, null );
  }

  /**
   * Executes the CallableStatement. This method has to be called in order to make this class do anything.
   */
  public final void execute()
  {
    _log.debug( _sqlString );

    CallableStatement lCallableStatement = null;

    try
    {
      lCallableStatement = _callableStatementProvider.createCallableStatement( _sqlString );

      useCallableStatement( lCallableStatement );
    }
    catch( SQLException e )
    {
      _log.debug( e, e );

      throw ExceptionManager.createException( e );
    }
    finally
    {
      if( lCallableStatement != null )
      {
        try
        {
          lCallableStatement.close();
        }
        catch( SQLException e )
        {
          _log.debug( e, e );

          throw ExceptionManager.createException( e );
        }
      }
    }
  }

  /**
   * Is called when the CallableStatement is ready-to-use. Only use the CallableStatement during this method, do not close it.
   */
  protected abstract void useCallableStatement( CallableStatement pCallableStatement ) throws SQLException;
}
