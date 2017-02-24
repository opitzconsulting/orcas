package de.opitzconsulting.orcas.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

public abstract class WrapperExecuteBatchWithCollection extends WrapperExecuteBatch
{
  private Iterator _parameterCollectionIterator;

  /**
   * Standard Konstruktor.
   */
  public WrapperExecuteBatchWithCollection( String pSqlString, CallableStatementProvider pCallableStatementProvider, Collection pParameterCollection )
  {
    super( pSqlString, pCallableStatementProvider );

    _parameterCollectionIterator = pParameterCollection.iterator();
  }

  protected boolean isMoreBatchOperations()
  {
    return _parameterCollectionIterator.hasNext();
  }

  protected void setParameter( PreparedStatement pPreparedStatement ) throws SQLException
  {
    setParameter( pPreparedStatement, _parameterCollectionIterator.next() );
  }

  /**
   * This Method initializes the PreparedStatement with the values from the CollectionObject. Returns wether the operation is to be done.
   */
  protected abstract void setParameter( PreparedStatement pPreparedStatement, Object pCollectionObject ) throws SQLException;
}
