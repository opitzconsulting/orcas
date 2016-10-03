package de.opitzconsulting.orcas.sql.oracle;

import java.sql.Array;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Execute OracleDirver-specific Methods via Reflection to prevent compile-time-dependencies. These Methods are optional anyways and are onla required if orcas is deployed to a database.
 */
public class OracleDriverSpecificHandler
{
  private static Class<?> getClass( String pClassName ) throws ClassNotFoundException
  {
    return Class.forName( pClassName );
  }

  public static Array call_OracleConnection_createARRAY( Connection pOracleConnection, String pArrayName, Object[] pElements )
  {
    try
    {
      return (Array)getClass( "oracle.jdbc.driver.OracleConnection" ).getMethod( "createARRAY", String.class, Object.class ).invoke( pOracleConnection, pArrayName, pElements );
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }

  public static Clob call_CLOB_createTemporary_MODE_READWRITE( Connection pOracleConnection )
  {
    try
    {
      Clob lClob = (Clob)getClass( "oracle.sql.CLOB" ).getMethod( "createTemporary", Connection.class, boolean.class, int.class ).invoke( pOracleConnection, true, 10 /*CLOB.DURATION_SESSION*/ );

      getClass( "oracle.sql.CLOB" ).getMethod( "open", int.class ).invoke( lClob, 1 /*CLOB.MODE_READWRITE*/ );

      return lClob;
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }

  public static boolean isInstanceofOraclePreparedStatement( PreparedStatement pPreparedStatement )
  {
    try
    {
      return getClass( "oracle.jdbc.OraclePreparedStatement" ).isInstance( pPreparedStatement );
    }
    catch( ClassNotFoundException e )
    {
      return false;
    }
  }

  public static void call_OraclePreparedStatement_setExecuteBatch( PreparedStatement pPreparedStatement, int pBatchSize )
  {
    try
    {
      getClass( "oracle.jdbc.OraclePreparedStatement" ).getMethod( "setExecuteBatch", int.class ).invoke( pPreparedStatement, pBatchSize );
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }

  public static void call_OraclePreparedStatement_sendBatch( PreparedStatement pPreparedStatement )
  {
    try
    {
      getClass( "oracle.jdbc.OraclePreparedStatement" ).getMethod( "sendBatch" ).invoke( pPreparedStatement );
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }
}
