package de.opitzconsulting.orcas.diff;

import de.opitzconsulting.orcas.diff.Parameters;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;

public interface ExecuteSqlErrorHandler {
    void handleExecutionError(
        RuntimeException e,
        String pSql,
        CallableStatementProvider pCallableStatementProvider,
        Parameters pParameters,
        ExecuteSqlErrorHandlerCallback pExecuteSqlErrorHandlerCallback);

    interface ExecuteSqlErrorHandlerCallback {
        void rethrow();

        void logError();

        void logInfo(String pMessage);
    }
}
