package de.opitzconsulting.orcas.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * This class handles returnvalues from resultsets. Note, that {@link de.oc.gds.framework.businesstier.util.PlSqlRunner PlSqlRunner} should be use to handle return values from functions.
 * 
 * It is also importatnt to know, that using this class or any of its subclasse, executeForValue has to be used instaed of execute.
 * 
 * The returnvalue is internally cached at instance level. This may lead to deadlock problems when differnet threads use the same instance of this class (which is not recommended).
 */
public abstract class WrapperReturnValueFromResultSet extends WrapperResultSet
{
  private Object _tempReturnValue;

  /**
   * Standard Constructor.
   */
  public WrapperReturnValueFromResultSet( String pSqlString, CallableStatementProvider pCallableStatementProvider )
  {
    this( pSqlString, pCallableStatementProvider, null );
  }

  /**
   * Standard Constructor.
   */
  public WrapperReturnValueFromResultSet( String pSqlString, CallableStatementProvider pCallableStatementProvider, List pParameters )
  {
    super( pSqlString, pCallableStatementProvider, pParameters );
  }

  /**
   * This implementaion redirects to getValueFromResultSet.
   */
  protected final void useResultSet( ResultSet pResultSet ) throws SQLException
  {
    _tempReturnValue = getValueFromResultSet( pResultSet );
  }

  /**
   * Is used to return a Value form the ResultSet. Resultset.next() has to be called by the implementation.
   */
  protected abstract Object getValueFromResultSet( ResultSet pResultSet ) throws SQLException;

  /**
   * Executes and Returns the Value. This method has to be used instaed of execute (it internally exceutes execute).
   */
  public final synchronized Object executeForValue()
  {
    execute();

    return _tempReturnValue;
  }
}
