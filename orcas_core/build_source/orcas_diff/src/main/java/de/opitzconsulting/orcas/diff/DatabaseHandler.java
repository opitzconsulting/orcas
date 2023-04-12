package de.opitzconsulting.orcas.diff;

import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.origOrcasDsl.CharType;

public abstract class DatabaseHandler {
    public abstract void createOrcasUpdatesTable(String pOrcasUpdatesTableName, CallableStatementProvider pOrcasCallableStatementProvider);

    public abstract void insertIntoOrcasUpdatesTable(String pOrcasUpdatesTableName, CallableStatementProvider pOrcasCallableStatementProvider, String pFilePart, String pLogname);

    public abstract LoadIst createLoadIst(CallableStatementProvider pCallableStatementProvider, Parameters pParameters);

    public abstract CharType getDefaultCharType(CallableStatementProvider pCallableStatementProvider);

    public abstract String getDefaultTablespace(CallableStatementProvider pCallableStatementProvider);

    public abstract DdlBuilder createDdlBuilder(Parameters pParameters);

    public abstract void executeDiffResultStatement(String pStatementToExecute, CallableStatementProvider pCallableStatementProvider);

    public abstract boolean isRenamePrimaryKey();

    public boolean isCanDiffFunctionBasedIndexExpression() {
        return true;
    }

    public boolean isCanDiffUniqueKeyIndex() {
        return true;
    }

    public abstract boolean isRenameIndex();

    public abstract boolean isRenameMView();

    public abstract boolean isRenameForeignKey();

    public abstract boolean isRenameUniqueKey();

    public abstract boolean isRenameConstraint();

    public abstract boolean isUpdateIdentity();

    public final boolean isExpressionDifferent(String pExpression1, String pExpression2) {
        if (pExpression1 == null && pExpression2 == null) {
            return false;
        }
        if (pExpression1 == null || pExpression2 == null) {
            return true;
        }
        return isExpressionDifferentNotNull(pExpression1, pExpression2);
    }

    protected boolean isExpressionDifferentNotNull(String pExpression1, String pExpression2) {
        return !pExpression1.equals(pExpression2);
    }

    public int getDefaultFloatPrecision() {
        return 126;
    }

    public Integer getDefaultNumberPrecision() {
        return null;
    }
}
