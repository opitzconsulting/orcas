package de.opitzconsulting.orcas.diff;

import java.util.HashMap;
import java.util.Map;

import de.opitzconsulting.orcas.diff.JdbcConnectionHandler.CallableStatementProviderImpl;
import de.opitzconsulting.orcas.diff.Parameters.JdbcConnectParameters;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;

public abstract class BaseMultiSchemaConnectionManager implements MultiSchemaConnectionManager {
    private Map<String, CallableStatementProviderImpl> connections = new HashMap<>();
    private Parameters parameters;
    private boolean keepMultipleConnectionsOpen = true;

    public void setKeepMultipleConnectionsOpen(boolean pKeepMultipleConnectionsOpen) {
        keepMultipleConnectionsOpen = pKeepMultipleConnectionsOpen;
    }

    void setParameters(Parameters pParameters) {
        parameters = pParameters;
    }

    @Override
    public void releaseAllConnections() {
        connections.values().forEach(CallableStatementProviderImpl::close);
        connections.clear();
    }

    protected CallableStatementProviderImpl createCallableStatementProvider(JdbcConnectParameters pJdbcConnectParameters) {
        return JdbcConnectionHandler.createCallableStatementProvider(parameters, pJdbcConnectParameters);
    }

    protected abstract JdbcConnectParameters getJdbcConnectParametersForSchema(String pSchemaName, Parameters pParameters);

    @Override
    public CallableStatementProvider getCallableStatementProviderForSchema(
        CallableStatementProvider pCallableStatementProviderDefault, String pSchemaName, Parameters pParameters) {
        if (connections.containsKey(pSchemaName)) {
            return connections.get(pSchemaName);
        } else {
            if (!keepMultipleConnectionsOpen) {
                releaseAllConnections();
            }
            connections.put(pSchemaName, createCallableStatementProvider(getJdbcConnectParametersForSchema(pSchemaName, pParameters)));
            return connections.get(pSchemaName);
        }
    }
}
