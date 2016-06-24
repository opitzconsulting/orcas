package de.opitzconsulting.orcas.diff;

import java.io.IOException;
import java.io.Writer;
import java.sql.Array;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Struct;

import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.JdbcCallableStatementProvider;
import oracle.jdbc.driver.OracleConnection;
import oracle.sql.CLOB;

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

  public static CallableStatementProvider createCallableStatementProvider( Parameters pParameters )
  {
    return createCallableStatementProvider( pParameters, pParameters.getJdbcConnectParameters() );
  }

  public static CallableStatementProvider createCallableStatementProvider( Parameters pParameters, Parameters.JdbcConnectParameters pJdbcConnectParameters )
  {
    try
    {
      Class.forName( pJdbcConnectParameters.getJdbcDriver() );

      Connection lConnection = DriverManager.getConnection( pJdbcConnectParameters.getJdbcUrl(), pJdbcConnectParameters.getJdbcUser(), pJdbcConnectParameters.getJdbcPassword() );

      return new CallableStatementProviderImpl( lConnection, pParameters );
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }

  public static Struct createStruct( String pTypeName, Object[] pAttributes, CallableStatementProvider pCallableStatementProvider )
  {
    try
    {
      CallableStatementProviderImpl lCallableStatementProviderImpl = (CallableStatementProviderImpl)pCallableStatementProvider;
      return lCallableStatementProviderImpl._connection.createStruct( lCallableStatementProviderImpl._parameters.getOrcasDbUser().toUpperCase() + "." + pTypeName.toUpperCase(), pAttributes );
    }
    catch( SQLException e )
    {
      throw new RuntimeException( e );
    }
  }

  public static Array createArrayOf( String pTypeName, Object[] pElements, CallableStatementProvider pCallableStatementProvider )
  {
    try
    {
      CallableStatementProviderImpl lCallableStatementProviderImpl = (CallableStatementProviderImpl)pCallableStatementProvider;
      return ((OracleConnection)lCallableStatementProviderImpl._connection).createARRAY( lCallableStatementProviderImpl._parameters.getOrcasDbUser().toUpperCase() + "." + pTypeName.toUpperCase(), pElements );
    }
    catch( SQLException e )
    {
      throw new RuntimeException( e );
    }
  }

  public static Clob createClob( String pValue, CallableStatementProvider pCallableStatementProvider )
  {
    try
    {
      CallableStatementProviderImpl lCallableStatementProviderImpl = (CallableStatementProviderImpl)pCallableStatementProvider;

      CLOB lClob = CLOB.createTemporary( (OracleConnection)lCallableStatementProviderImpl._connection, true, CLOB.DURATION_SESSION );

      lClob.open( CLOB.MODE_READWRITE );

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
