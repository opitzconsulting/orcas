package de.opitzconsulting.orcas.diff;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
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

import de.opitzconsulting.orcas.diff.Parameters.JdbcConnectParameters;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.JdbcCallableStatementProvider;
import de.opitzconsulting.orcas.sql.oracle.OracleDriverSpecificHandler;

public class JdbcConnectionHandler {
    public static class CallableStatementProviderImpl extends JdbcCallableStatementProvider implements Closeable {
        private Connection _connection;
        private Parameters _parameters;
        private String _proxyUser;

        private CallableStatementProviderImpl(Connection pConnection, Parameters pParameters, String pProxyUser) {
            super(pConnection);

            _connection = pConnection;
            _parameters = pParameters;
            _proxyUser = pProxyUser;
        }

        @Override
        public void close() {
            if (_proxyUser != null) {
                OracleDriverSpecificHandler.closeConnection(_connection);
            }
            try {
                _connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public interface RunWithCallableStatementProvider {
        void run(CallableStatementProvider pCallableStatementProvider) throws Exception;
    }

    public static void runWithCallableStatementProvider(Parameters pParameters, RunWithCallableStatementProvider pRunWithCallableStatementProvider)
        throws Exception {
        runWithCallableStatementProvider(pParameters, pParameters.getJdbcConnectParameters(), pRunWithCallableStatementProvider);
    }

    public static void runWithCallableStatementProvider(
        Parameters pParameters,
        Parameters.JdbcConnectParameters pJdbcConnectParameters,
        RunWithCallableStatementProvider pRunWithCallableStatementProvider) throws Exception {
        try (CallableStatementProviderImpl lCallableStatementProvider = createCallableStatementProvider(pParameters, pJdbcConnectParameters)) {
            pRunWithCallableStatementProvider.run(lCallableStatementProvider);
        }
    }

    public static CallableStatementProviderImpl createCallableStatementProvider(
        Parameters pParameters,
        JdbcConnectParameters pJdbcConnectParameters) {
        boolean lIsDriverSet = pJdbcConnectParameters.getJdbcDriver() != null && !pJdbcConnectParameters.getJdbcDriver().equals("");
        if (lIsDriverSet) {
            if (pParameters.isKeepDriverClassLoadMessages()) {
                loadDriverClass(pJdbcConnectParameters);
            } else {
                PrintStream lOriginalSystemErr = System.err;
                PrintStream lOriginalSystemOut = System.out;

                try {
                    // oracle driver logs MBean Registration-Messages that cant be
                    // prevented otherwise
                    System.setErr(new PrintStream(new ByteArrayOutputStream()));
                    System.setOut(new PrintStream(new ByteArrayOutputStream()));
                    loadDriverClass(pJdbcConnectParameters);
                } finally {
                    System.setErr(lOriginalSystemErr);
                    System.setOut(lOriginalSystemOut);
                }
            }
        }

        String lUsername = pJdbcConnectParameters.getJdbcUser();
        String lProxyUser = null;
        if (pJdbcConnectParameters.getJdbcUrl().startsWith("jdbc:oracle")
            && lUsername != null
            && lUsername.matches("\\.+\\[\\.+\\]$")) {
            int startOfProxy = lUsername.indexOf("[");

            lProxyUser = lUsername.substring(startOfProxy + 1, lUsername.length() - 1);
            pJdbcConnectParameters.setJdbcUser(lUsername.substring(0, startOfProxy));
        }

        Connection lConnection = null;

        Properties lProperties = new Properties();
        lProperties.setProperty("user", pJdbcConnectParameters.getJdbcUser());
        lProperties.setProperty("password", pJdbcConnectParameters.getJdbcPassword());

        try {
            lConnection = DriverManager.getConnection(pJdbcConnectParameters.getJdbcUrl(), lProperties);
        } catch (Exception e) {
            throw new RuntimeException("connection failed: jdbc-url:"
                + pJdbcConnectParameters.getJdbcUrl()
                + " user: "
                + pJdbcConnectParameters.getJdbcUser(), e);
        }

        CallableStatementProviderImpl lCallableStatementProvider = new CallableStatementProviderImpl(lConnection, pParameters, lProxyUser);

        try {
            if (lProxyUser != null) {
                OracleDriverSpecificHandler.openProxyConnection(lConnection, lProxyUser);
            }
        } catch (Exception e) {
            try {
                lConnection.close();
            } catch (SQLException pE) {
                throw new RuntimeException(pE);
            }
            throw new RuntimeException(e);
        }
        return lCallableStatementProvider;
    }

    private static void loadDriverClass(Parameters.JdbcConnectParameters pJdbcConnectParameters) {
        try {
            Class.forName(pJdbcConnectParameters.getJdbcDriver());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Struct createStruct(String pTypeName, Object[] pAttributes, CallableStatementProvider pCallableStatementProvider) {
        try {
            CallableStatementProviderImpl lCallableStatementProviderImpl = (CallableStatementProviderImpl) pCallableStatementProvider;
            return lCallableStatementProviderImpl._connection.createStruct(lCallableStatementProviderImpl._parameters
                .getOrcasDbUser()
                .toUpperCase()
                + "."
                + pTypeName.toUpperCase(), pAttributes);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Array createArrayOf(String pTypeName, Object[] pElements, CallableStatementProvider pCallableStatementProvider) {
        CallableStatementProviderImpl lCallableStatementProviderImpl = (CallableStatementProviderImpl) pCallableStatementProvider;
        return OracleDriverSpecificHandler.call_OracleConnection_createARRAY(
            lCallableStatementProviderImpl._connection,
            lCallableStatementProviderImpl._parameters.getOrcasDbUser().toUpperCase() + "." + pTypeName.toUpperCase(),
            pElements);
    }

    public static Clob createClob(String pValue, CallableStatementProvider pCallableStatementProvider) {
        try {
            CallableStatementProviderImpl lCallableStatementProviderImpl = (CallableStatementProviderImpl) pCallableStatementProvider;

            Clob lClob = OracleDriverSpecificHandler.call_CLOB_createTemporary_MODE_READWRITE(lCallableStatementProviderImpl._connection);

            Writer lSetCharacterStream = lClob.setCharacterStream(1);
            lSetCharacterStream.append(pValue);

            lSetCharacterStream.close();

            return lClob;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
