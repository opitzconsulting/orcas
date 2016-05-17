package de.opitzconsulting.orcas.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import oracle.jdbc.OraclePreparedStatement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Dient zum ausfuehren von DML Operationen im Batch Modus.
 */
public abstract class WrapperExecuteBatch extends WrapperPreparedStatement
{
  private static Log _log = LogFactory.getLog( WrapperExecuteBatch.class );

  /**
   * Standard Konstruktor.
   */
  public WrapperExecuteBatch( String pSqlString, CallableStatementProvider pCallableStatementProvider )
  {
    super( pSqlString, pCallableStatementProvider );
  }

  protected final void usePreparedStatement( PreparedStatement pPreparedStatement ) throws SQLException
  {
    boolean lOracleMode = pPreparedStatement instanceof OraclePreparedStatement;

    if( lOracleMode )
    {
      ((OraclePreparedStatement)pPreparedStatement).setExecuteBatch( 10 );
    }
    else
    {
      _log.info( "oracle batch not possible using default: " + pPreparedStatement );
    }

    while( isMoreBatchOperations() )
    {
      setParameter( pPreparedStatement );

      if( lOracleMode )
      {
        pPreparedStatement.execute();
      }
      else
      {
        pPreparedStatement.addBatch();
      }
    }

    if( lOracleMode )
    {
      ((OraclePreparedStatement)pPreparedStatement).sendBatch();
    }
    else
    {
      pPreparedStatement.execute();
    }
  }

  /**
   * Is called before each batch execution to determine wether the bacth job is complete.
   */
  protected abstract boolean isMoreBatchOperations();

  /**
   * Is called each time before the PreparedStatement is executed.
   */
  protected abstract void setParameter( PreparedStatement pPreparedStatement ) throws SQLException;
}
