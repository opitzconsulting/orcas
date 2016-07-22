package de.opitzconsulting.orcas.sql;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This Interafce describes Factory for callable statements
 */
public interface CallableStatementProvider
{
  /**
   * This method returns a callable statement from the given string. The caller of this method is supposed to close the statement after usage.
   */
  CallableStatement createCallableStatement( String pStatement ) throws SQLException;

  /**
   * This method returns a prepared statement from the given string. The caller of this method is supposed to close the statement after usage.
   */
  PreparedStatement createPreparedStatement( String pStatement ) throws SQLException;

  Statement createStatement( String pStatement ) throws SQLException;
}