package de.opitzconsulting.orcas.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * This class is used to return the first-value of the first row of the resultset. A NoDataFoundException is thrown, if there is no first row (can be configured to return null instead).
 */
public class WrapperReturnFirstValue extends WrapperReturnValueFromResultSet
{
  private boolean _returnNullInsteadOfNoDataFoundException;

  /**
   * Standard Konstruktor.
   */
  public WrapperReturnFirstValue( String pSqlString, CallableStatementProvider pCallableStatementProvider, List pParameters )
  {
    this( pSqlString, pCallableStatementProvider, false, pParameters );
  }

  /**
   * Standard Constructor.
   */
  public WrapperReturnFirstValue( String pSqlString, CallableStatementProvider pCallableStatementProvider, boolean pReturnNullInsteadOfNoDataFoundException, List pParameters )
  {
    super( pSqlString, pCallableStatementProvider, pParameters );

    _returnNullInsteadOfNoDataFoundException = pReturnNullInsteadOfNoDataFoundException;
  }

  /**
   * Standard Constructor.
   */
  public WrapperReturnFirstValue( String pSqlString, CallableStatementProvider pCallableStatementProvider )
  {
    this( pSqlString, pCallableStatementProvider, null );
  }

  /**
   * Extracts the first value.
   */
  protected final Object getValueFromResultSet( ResultSet pResultSet ) throws SQLException
  {
    if( !pResultSet.next() )
    {
      if( _returnNullInsteadOfNoDataFoundException )
      {
        return null;
      }

      throw new NoDataFoundException();
    }

    return pResultSet.getObject( getObjectIndex() );
  }

  protected int getObjectIndex()
  {
    return 1;
  }

  public class NoDataFoundException extends RuntimeException
  {
  }
}
