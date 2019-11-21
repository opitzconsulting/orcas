package com.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.opitzconsulting.orcas.diff.ExecuteSqlErrorHandler;
import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.Parameters;
import de.opitzconsulting.orcas.diff.Parameters.FailOnErrorMode;
import de.opitzconsulting.orcas.diff.Parameters.JdbcConnectParameters;
import de.opitzconsulting.orcas.diff.ParametersCall;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;

public class OrcasParallelConnectionHandler {
    static JdbcConnectParameters _connectParametersDba;
    private static OrcasCoreIntegrationConfig _orcasCoreIntegrationConfig;

    private static List<String> _usedUsernames = new ArrayList<String>();

    private static void initIfNeeded() {
        if (_orcasCoreIntegrationConfig == null) {
            _orcasCoreIntegrationConfig = OrcasCoreIntegrationConfigSystemProperties.getOrcasCoreIntegrationConfig();

            _connectParametersDba = new JdbcConnectParameters();
            _connectParametersDba.setJdbcUrl(_orcasCoreIntegrationConfig.getJdbcUrl());
            _connectParametersDba.setJdbcUser(_orcasCoreIntegrationConfig.getJdbcUser());
            _connectParametersDba.setJdbcPassword(_orcasCoreIntegrationConfig.getJdbcPassword());
        }
    }

    private static String getFreeUserName() {
        String lUsernamePrefix = _orcasCoreIntegrationConfig.getUsernamePrefix() + "target";

        String lUsername = lUsernamePrefix;

        int lIndex = 0;

        while (_usedUsernames.contains(lUsername)) {
            lUsername = lUsernamePrefix + "_" + lIndex;
            lIndex++;
        }

        _usedUsernames.add(lUsername);

        return lUsername;
    }

    public static synchronized JdbcConnectParameters createConnectionParametersForTargetUser() {
        initIfNeeded();

        JdbcConnectParameters lJdbcConnectParameters = new JdbcConnectParameters();
        copyConnectionParameters(_connectParametersDba, lJdbcConnectParameters);
        lJdbcConnectParameters.setJdbcUser(getFreeUserName());
        lJdbcConnectParameters.setJdbcPassword(lJdbcConnectParameters.getJdbcUser());
        return lJdbcConnectParameters;
    }

    public static synchronized void returnConnectionParametersForTargetUser(JdbcConnectParameters pJdbcConnectParameters) {
        _usedUsernames.remove(pJdbcConnectParameters.getJdbcUser());
    }

    public static synchronized void resetUser(JdbcConnectParameters pJdbcConnectParameters) {
        ParametersCall lParametersCall = new ParametersCall();

        copyConnectionParameters(_connectParametersDba, lParametersCall.getJdbcConnectParameters());

        lParametersCall.setAdditionalParameters(Arrays.asList(new String[] {
            pJdbcConnectParameters.getJdbcUser(),
            pJdbcConnectParameters.getJdbcPassword(),
            _orcasCoreIntegrationConfig.getTablespace() }));
        lParametersCall.setIsOneTimeScriptMode(false);
        lParametersCall.setExecuteSqlErrorHandler(new ExecuteSqlErrorHandler() {
            @Override
            public void handleExecutionError(
                RuntimeException e,
                String pSql,
                CallableStatementProvider pCallableStatementProvider,
                Parameters pParameters,
                ExecuteSqlErrorHandlerCallback pExecuteSqlErrorHandlerCallback) {
                if (!e.getMessage().contains("ORA-01918")) {
                    pExecuteSqlErrorHandlerCallback.rethrow();
                }
            }
        });

        lParametersCall.setModelFile(_orcasCoreIntegrationConfig.getBaseDir() + "reset_user.sql");

        new OrcasScriptRunner().mainRun(lParametersCall);
    }

    private static void copyConnectionParameters(JdbcConnectParameters pSourceConnectParameters, JdbcConnectParameters pDestConnectParameters) {
        pDestConnectParameters.setJdbcDriver(pSourceConnectParameters.getJdbcDriver());
        pDestConnectParameters.setJdbcUrl(pSourceConnectParameters.getJdbcUrl());
        pDestConnectParameters.setJdbcUser(pSourceConnectParameters.getJdbcUser());
        pDestConnectParameters.setJdbcPassword(pSourceConnectParameters.getJdbcPassword());
    }
}
