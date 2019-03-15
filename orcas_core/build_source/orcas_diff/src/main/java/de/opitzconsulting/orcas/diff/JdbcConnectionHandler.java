package de.opitzconsulting.orcas.diff;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.sql.Array;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.Properties;

import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.JdbcCallableStatementProvider;
import de.opitzconsulting.orcas.sql.oracle.OracleDriverSpecificHandler;

public class JdbcConnectionHandler
{
  private static class CallableStatementProviderImpl extends JdbcCallableStatementProvider
  {
    private Connection _connection;
    private Parameters _parameters;

    public CallableStatementProviderImpl( Connection pConnection, Parameters pParameters )
    {
      super( pConnection );

      _connection = pConnection;
      _parameters = pParameters;
    }
  }

  public interface RunWithCallableStatementProvider
  {
    void run( CallableStatementProvider pCallableStatementProvider ) throws Exception;
  }

  public static void runWithCallableStatementProvider( Parameters pParameters, RunWithCallableStatementProvider pRunWithCallableStatementProvider ) throws Exception
  {
    runWithCallableStatementProvider( pParameters, pParameters.getJdbcConnectParameters(), pRunWithCallableStatementProvider );
  }

  public static void runWithCallableStatementProvider( Parameters pParameters, Parameters.JdbcConnectParameters pJdbcConnectParameters, RunWithCallableStatementProvider pRunWithCallableStatementProvider ) throws Exception
  {
    try
    {
      boolean lIsDriverSet = pJdbcConnectParameters.getJdbcDriver() != null && !pJdbcConnectParameters.getJdbcDriver().equals( "" );
      if( lIsDriverSet )
      {
        if( pParameters.isKeepDriverClassLoadMessages() )
        {
          loadDriverClass( pJdbcConnectParameters );
        }
        else
        {
          PrintStream lOriginalSystemErr = System.err;
          PrintStream lOriginalSystemOut = System.out;

          try
          {
            // oracle driver logs MBean Registration-Messages that cant be
            // prevented otherwise
            System.setErr( new PrintStream( new ByteArrayOutputStream() ) );
            System.setOut( new PrintStream( new ByteArrayOutputStream() ) );
            loadDriverClass( pJdbcConnectParameters );
          }
          finally
          {
            System.setErr( lOriginalSystemErr );
            System.setOut( lOriginalSystemOut );
          }
        }
      }

      boolean lIsProxyUsed = pParameters.getProxyUser() != null;

      Connection lConnection;

      Properties lProperties = new Properties();
      lProperties.setProperty( "user", pJdbcConnectParameters.getJdbcUser() );
      lProperties.setProperty( "password", pJdbcConnectParameters.getJdbcPassword() );

      try
      {
        lConnection = DriverManager.getConnection(pJdbcConnectParameters.getJdbcUrl(), lProperties);
      }
      catch (Exception e) {
        throw new RuntimeException("connection failed: jdbc-url:" + pJdbcConnectParameters.getJdbcUrl() + " user: " + pJdbcConnectParameters.getJdbcUser(), e);
      }

      if (lIsProxyUsed)
      {
        OracleDriverSpecificHandler.openProxyConnection(pParameters, lConnection);
      }

      try
      {
        pRunWithCallableStatementProvider.run( new CallableStatementProviderImpl( lConnection, pParameters ) );
      }
      finally
      {
        if (lIsProxyUsed)
        {
          OracleDriverSpecificHandler.closeConnection(lConnection);
        }
        lConnection.close();
      }
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }

  private static void loadDriverClass( Parameters.JdbcConnectParameters pJdbcConnectParameters ) throws ClassNotFoundException
  {
    Class.forName( pJdbcConnectParameters.getJdbcDriver() );
  }

  public static Struct createStruct( String pTypeName, Object[] pAttributes, CallableStatementProvider pCallableStatementProvider )
  {
    try
    {
      CallableStatementProviderImpl lCallableStatementProviderImpl = (CallableStatementProviderImpl) pCallableStatementProvider;
      return lCallableStatementProviderImpl._connection.createStruct( lCallableStatementProviderImpl._parameters.getOrcasDbUser().toUpperCase() + "." + pTypeName.toUpperCase(), pAttributes );
    }
    catch( SQLException e )
    {
      throw new RuntimeException( e );
    }
  }

  public static Array createArrayOf( String pTypeName, Object[] pElements, CallableStatementProvider pCallableStatementProvider )
  {
    CallableStatementProviderImpl lCallableStatementProviderImpl = (CallableStatementProviderImpl) pCallableStatementProvider;
    return OracleDriverSpecificHandler.call_OracleConnection_createARRAY( lCallableStatementProviderImpl._connection, lCallableStatementProviderImpl._parameters.getOrcasDbUser().toUpperCase() + "." + pTypeName.toUpperCase(), pElements );
  }

  public static Clob createClob( String pValue, CallableStatementProvider pCallableStatementProvider )
  {
    try
    {
      CallableStatementProviderImpl lCallableStatementProviderImpl = (CallableStatementProviderImpl) pCallableStatementProvider;

      Clob lClob = OracleDriverSpecificHandler.call_CLOB_createTemporary_MODE_READWRITE( lCallableStatementProviderImpl._connection );

      Writer lSetCharacterStream = lClob.setCharacterStream( 1 );
      lSetCharacterStream.append( pValue );

      lSetCharacterStream.close();

      return lClob;
    }
    catch( SQLException e )
    {
      throw new RuntimeException( e );
    }
    catch( IOException e )
    {
      throw new RuntimeException( e );
    }
  }
}
