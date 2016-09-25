package de.opitzconsulting.orcas.sql;

import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Diese Klasse fuehrt das callable Statement einfach aus.
 */
public class WrapperExecuteUpdate
{
  private static Log _log = LogFactory.getLog( WrapperExecuteUpdate.class );

  private String _sqlString;
  private CallableStatementProvider _callableStatementProvider;

  /**
   * Standard Constructor.
   */
  public WrapperExecuteUpdate( String pSqlString, CallableStatementProvider pCallableStatementProvider )
  {
    _sqlString = pSqlString;
    _callableStatementProvider = pCallableStatementProvider;
  }

  public void execute()
  {
    _log.debug( _sqlString );

    Statement lStatement = null;

    try
    {
      lStatement = _callableStatementProvider.createStatement( _sqlString );

      lStatement.executeUpdate( _sqlString );
    }
    catch( SQLException e )
    {
      _log.debug( e + ": " + _sqlString, e );

      throw ExceptionManager.createException( e );
    }
    finally
    {
      if( lStatement != null )
      {
        try
        {
          lStatement.close();
        }
        catch( SQLException e )
        {
          _log.debug( e, e );

          throw ExceptionManager.createException( e );
        }
      }
    }
  }
}
