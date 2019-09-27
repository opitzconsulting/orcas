package de.opitzconsulting.orcas.diff;

import de.opitzconsulting.orcas.diff.Parameters.JdbcConnectParameters;

public class MultiSchemaConnectionManagerProxyConnect extends BaseMultiSchemaConnectionManager {
    @Override
    protected JdbcConnectParameters getJdbcConnectParametersForSchema(String pSchemaName, Parameters pParameters) {
        JdbcConnectParameters lJdbcConnectParameters = new JdbcConnectParameters();

        lJdbcConnectParameters.setJdbcUrl(pParameters.getJdbcConnectParameters().getJdbcUrl());
        lJdbcConnectParameters.setJdbcDriver(pParameters.getJdbcConnectParameters().getJdbcDriver());
        lJdbcConnectParameters.setJdbcUser(pParameters.getJdbcConnectParameters().getJdbcUser() + "[" + pSchemaName + "]");
        lJdbcConnectParameters.setJdbcPassword(pParameters.getJdbcConnectParameters().getJdbcPassword());

        return lJdbcConnectParameters;
    }
}
