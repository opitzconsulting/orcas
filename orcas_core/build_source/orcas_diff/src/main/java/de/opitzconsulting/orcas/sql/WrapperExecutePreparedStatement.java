package de.opitzconsulting.orcas.sql;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Diese Klasse fuehrt das callable Statement einfach aus.
 */
public class WrapperExecutePreparedStatement extends WrapperPreparedStatement
{
  private List _parameters;

  /**
   * Standard Konstruktor.
   */
  public WrapperExecutePreparedStatement( String pSqlString, CallableStatementProvider pCallableStatementProvider )
  {
    this( pSqlString, pCallableStatementProvider, null );
  }

  /**
   * Standard Konstruktor.
   */
  public WrapperExecutePreparedStatement( String pSqlString, CallableStatementProvider pCallableStatementProvider, List pParameters )
  {
    super( pSqlString, pCallableStatementProvider );

    _parameters = pParameters;
  }

  @Override
  protected void usePreparedStatement( PreparedStatement pPreparedStatement ) throws SQLException
  {
    if( _parameters != null )
    {
      for( int i = 0; i < _parameters.size(); i++ )
      {
        pPreparedStatement.setObject( i + 1, _parameters.get( i ) );
      }
    }

    pPreparedStatement.executeUpdate();
  }
}
