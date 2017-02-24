package de.opitzconsulting.orcas.sql;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Diese Klasse fuehrt das callable Statement einfach aus.
 */
public class WrapperExecuteStatement extends WrapperCallableStatement
{
  private List _parameters;

  /**
   * Standard Konstruktor.
   */
  public WrapperExecuteStatement( String pSqlString, CallableStatementProvider pCallableStatementProvider )
  {
    this( pSqlString, pCallableStatementProvider, null );
  }

  /**
   * Standard Konstruktor.
   */
  public WrapperExecuteStatement( String pSqlString, CallableStatementProvider pCallableStatementProvider, List pParameters )
  {
    super( pSqlString, pCallableStatementProvider );

    _parameters = pParameters;
  }

  protected void useCallableStatement( CallableStatement pCallableStatement ) throws SQLException
  {
    if( _parameters != null )
    {
      for( int i = 0; i < _parameters.size(); i++ )
      {
        pCallableStatement.setObject( i + 1, _parameters.get( i ) );
      }
    }

    pCallableStatement.executeUpdate();
  }
}
