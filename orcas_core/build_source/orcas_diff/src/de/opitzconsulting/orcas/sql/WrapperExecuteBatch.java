package de.opitzconsulting.orcas.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.opitzconsulting.orcas.sql.oracle.OracleDriverSpecificHandler;

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
    boolean lOracleMode = OracleDriverSpecificHandler.isInstanceofOraclePreparedStatement( pPreparedStatement );

    if( lOracleMode )
    {
      OracleDriverSpecificHandler.call_OraclePreparedStatement_setExecuteBatch( pPreparedStatement, 10 );
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
      OracleDriverSpecificHandler.call_OraclePreparedStatement_sendBatch( pPreparedStatement );
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
