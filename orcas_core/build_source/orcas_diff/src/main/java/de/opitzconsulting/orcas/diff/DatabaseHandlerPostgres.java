package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.List;

import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperExecuteStatement;
import de.opitzconsulting.origOrcasDsl.CharType;

public class DatabaseHandlerPostgres extends DatabaseHandler {
    @Override
    public void createOrcasUpdatesTable(String pOrcasUpdatesTableName, CallableStatementProvider pOrcasCallableStatementProvider) {
        String
            lSql =
            "create table "
                + pOrcasUpdatesTableName
                + " ( scup_id serial, scup_script_name varchar(4000) not null, scup_logname varchar(100) not null, scup_date date not null, scup_schema varchar(30) not null, primary key (scup_id))";
        new WrapperExecuteStatement(lSql, pOrcasCallableStatementProvider).execute();
    }

    @Override
    public void insertIntoOrcasUpdatesTable(
        String pOrcasUpdatesTableName,
        CallableStatementProvider pOrcasCallableStatementProvider,
        String pFilePart,
        String pLogname) {
        String lSql = "" + //
            " insert into " + pOrcasUpdatesTableName + "(" + //
            "        scup_script_name," + //
            "        scup_date," + //
            "        scup_schema," + //
            "        scup_logname" + //
            "        )" + //
            " values (" + //
            "        ?," + //
            "        current_timestamp," + //
            "        user," + //
            "        ?" + //
            "        )" + //
            "";
        List<Object> lInsertParameters = new ArrayList<Object>();
        lInsertParameters.add(pFilePart);
        lInsertParameters.add(pLogname);
        new WrapperExecuteStatement(lSql, pOrcasCallableStatementProvider, lInsertParameters).execute();
        new WrapperExecuteStatement("commit", pOrcasCallableStatementProvider).execute();
    }

    @Override
    public LoadIst createLoadIst(CallableStatementProvider pCallableStatementProvider, Parameters pParameters) {
        return new LoadIstPostgres(pCallableStatementProvider, pParameters);
    }

    @Override
    public CharType getDefaultCharType(CallableStatementProvider pCallableStatementProvider) {
        return CharType.CHAR;
    }

    @Override
    public String getDefaultTablespace(CallableStatementProvider pCallableStatementProvider) {
        return null;
    }

    @Override
    public DdlBuilder createDdlBuilder(Parameters pParameters) {
        return new DdlBuilderPostgres(pParameters, this);
    }

    @Override
    public void executeDiffResultStatement(String pStatementToExecute, CallableStatementProvider pCallableStatementProvider) {
        new WrapperExecuteStatement(pStatementToExecute, pCallableStatementProvider).execute();
    }

    @Override
    public boolean isRenamePrimaryKey() {
        return true;
    }

    @Override
    public boolean isRenameIndex() {
        return true;
    }

    @Override
    public boolean isRenameMView() {
        return true;
    }

    @Override
    public boolean isRenameForeignKey() {
        return true;
    }

    @Override
    public boolean isRenameUniqueKey() {
        return false;
    }

    @Override
    public boolean isRenameConstraint() {
        return true;
    }

    @Override
    public boolean isUpdateIdentity() {
        return false;
    }

    private String cleanupSubExpression(String pExpression) {
        String lReturn = pExpression;

        lReturn = lReturn.trim();

        if (lReturn.startsWith("(") && lReturn.endsWith(")")) {
            lReturn = lReturn.substring(1, lReturn.length() - 1);
        }

        lReturn = lReturn.toLowerCase();

        lReturn = lReturn.replace("current_timestamp", "now");



        lReturn = lReturn.replace("(", "");
        lReturn = lReturn.replace(")", "");
        lReturn = lReturn.replace("[", "");
        lReturn = lReturn.replace("]", "");
        lReturn = lReturn.replace(" ", "");

        lReturn = lReturn.replace("=anyarray", "in");
        lReturn = lReturn.replace("::charactervarying", "");
        lReturn = lReturn.replace("::text", "");

        return lReturn;
    }

    private String cleanupExpression(String pExpression) {
        String lReturn = "";

        String[] lSplit = pExpression.split("'");
        boolean lIsIn = false;
        for (int i = 0; i < lSplit.length; i++) {
            if (lIsIn) {
                lReturn += lSplit[i];
            } else {
                lReturn += cleanupSubExpression(lSplit[i]);
            }
            lIsIn = !lIsIn;
            if (i != lSplit.length - 1) {
                lReturn += "'";
            }
        }

        return lReturn;
    }

    @Override
    protected boolean isExpressionDifferentNotNull(String pExpression1, String pExpression2) {
        return super.isExpressionDifferentNotNull(cleanupExpression(pExpression1), cleanupExpression(pExpression2));
    }

    @Override
    public boolean isCanDiffFunctionBasedIndexExpression() {
        return false;
    }
}
