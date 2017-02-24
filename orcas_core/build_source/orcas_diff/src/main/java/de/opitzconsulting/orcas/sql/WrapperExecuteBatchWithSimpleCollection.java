package de.opitzconsulting.orcas.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

/**
 * @author fsa
 * 
 */
public class WrapperExecuteBatchWithSimpleCollection extends WrapperExecuteBatchWithCollection
{
  /**
   * Standard Konstruktor.
   */
  public WrapperExecuteBatchWithSimpleCollection( String pSqlString, CallableStatementProvider pCallableStatementProvider, Collection pParameterCollection )
  {
    super( pSqlString, pCallableStatementProvider, pParameterCollection );
  }

  protected void setParameter( PreparedStatement pPreparedStatement, Object pCollectionObject ) throws SQLException
  {
    pPreparedStatement.setObject( 1, pCollectionObject );
  }
}
