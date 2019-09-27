package de.opitzconsulting.orcas.diff;

import de.opitzconsulting.orcas.sql.CallableStatementProvider;

public interface MultiSchemaConnectionManager {
    void releaseAllConnections();

    CallableStatementProvider getCallableStatementProviderForSchema(CallableStatementProvider pCallableStatementProviderDefault, String pSchemaName, Parameters pParameters);
}
