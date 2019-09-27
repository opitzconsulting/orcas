package de.opitzconsulting.orcas.diff;

import de.opitzconsulting.orcas.sql.CallableStatementProvider;

public class MultiSchemaConnectionManagerSimple implements MultiSchemaConnectionManager {
    @Override
    public void releaseAllConnections() {
    }

    @Override
    public CallableStatementProvider getCallableStatementProviderForSchema(
        CallableStatementProvider pCallableStatementProviderDefault, String pSchemaName, Parameters pParameters) {
        return pCallableStatementProviderDefault;
    }
}
