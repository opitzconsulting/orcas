package de.opitzconsulting.orcas.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Gibt zurueck, ob zu einem Statment eine Zeile existiert oder nicht.
 */
public class WrapperExists extends WrapperReturnValueFromResultSet
{
  /**
   * Standard Konstruktor.
   */
  public WrapperExists( String pSqlString, CallableStatementProvider pCallableStatementProvider )
  {
    super( pSqlString, pCallableStatementProvider );
  }

  /**
   * Standard Konstruktor.
   */
  public WrapperExists( String pSqlString, CallableStatementProvider pCallableStatementProvider, List pParameters )
  {
    super( pSqlString, pCallableStatementProvider, pParameters );
  }

  protected Object getValueFromResultSet( ResultSet pResultSet ) throws SQLException
  {
    return pResultSet.next() ? Boolean.TRUE : Boolean.FALSE;
  }

  /**
   * Gibt zurueck, ob es zu der Anfrage mindestens eine Zeile gibt.
   */
  public boolean executeForBoolean()
  {
    return ((Boolean)executeForValue()).booleanValue();
  }
}
