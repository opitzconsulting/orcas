package de.opitzconsulting.orcas.sql.oracle;

import de.opitzconsulting.orcas.diff.Parameters;

import java.sql.Array;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

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

  public static Connection openProxyConnection(Parameters pParameters, Parameters.JdbcConnectParameters pJdbcConnectParameters) {
    Connection lConnection;

    try
    {
      Class<?> oracleConnection = getClass( "oracle.jdbc.OracleConnection" );

      Properties lProperties = new Properties();
      lProperties.setProperty( (String) oracleConnection.getDeclaredField( "PROXY_USER_NAME" ).get( null ), pJdbcConnectParameters.getJdbcUser() );
      lProperties.setProperty( (String) oracleConnection.getDeclaredField( "PROXY_USER_PASSWORD" ).get( null ), pJdbcConnectParameters.getJdbcPassword() );

      Properties lProxyProperties = new Properties();
      lProxyProperties.setProperty( "user", pParameters.getProxyJdbcConnectParameters().getJdbcUser() );
      lProxyProperties.setProperty( "password", pParameters.getProxyJdbcConnectParameters().getJdbcPassword() );

      try
      {
        lConnection = DriverManager.getConnection( pJdbcConnectParameters.getJdbcUrl(), lProxyProperties );
      }
      catch ( Exception e )
      {
        throw new RuntimeException( "proxy connection failed: jdbc-url:" + pJdbcConnectParameters.getJdbcUrl() + " user: " + pParameters.getProxyJdbcConnectParameters().getJdbcUser(), e );
      }

      try
      {
        oracleConnection.getMethod( "openProxySession", int.class, Properties.class ).invoke( lConnection, (int) oracleConnection.getDeclaredField( "PROXYTYPE_USER_NAME" ).get( null ), lProperties );
      }
      catch ( Exception e )
      {
        throw new RuntimeException( "connection failed: jdbc-url:" + pJdbcConnectParameters.getJdbcUrl() + " user: " + pJdbcConnectParameters.getJdbcUser(), e );
      }
    }
    catch ( Exception e )
    {
      throw new RuntimeException( e );
    }
    return lConnection;
  }

  public static void closeConnection(Connection pConnection) {
    try
    {
      Class<?> oracleConnection = getClass( "oracle.jdbc.OracleConnection" );
      oracleConnection.getMethod( "close" , int.class).invoke( pConnection, (int) oracleConnection.getDeclaredField( "PROXY_SESSION" ).get( null ) );
    }
    catch ( Exception e )
    {
      throw new RuntimeException( e );
    }
  }
}
