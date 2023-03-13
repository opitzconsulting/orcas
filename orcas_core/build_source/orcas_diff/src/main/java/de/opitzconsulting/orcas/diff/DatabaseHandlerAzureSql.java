package de.opitzconsulting.orcas.diff;

import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperExecutePreparedStatement;
import de.opitzconsulting.origOrcasDsl.CharType;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandlerAzureSql extends DatabaseHandler {
    @Override
    public void createOrcasUpdatesTable(String pOrcasUpdatesTableName, CallableStatementProvider pOrcasCallableStatementProvider) {
        String lSql = "create table " + pOrcasUpdatesTableName + " ( scup_id int not null IDENTITY, scup_script_name varchar(4000) not null, scup_logname varchar(100) not null, scup_date date not null, scup_schema varchar(30) not null, primary key (scup_id))";
        new WrapperExecutePreparedStatement(lSql, pOrcasCallableStatementProvider).execute();
    }

    @Override
    public void insertIntoOrcasUpdatesTable(String pOrcasUpdatesTableName, CallableStatementProvider pOrcasCallableStatementProvider, String pFilePart, String pLogname) {
        new WrapperExecutePreparedStatement("begin transaction", pOrcasCallableStatementProvider).execute();
        String lSql = "" + //
                " insert into " + pOrcasUpdatesTableName + "(" + //
                "        scup_script_name," + //
                "        scup_date," + //
                "        scup_schema," + //
                "        scup_logname" + //
                "        )" + //
                " values (" + //
                "        ?," + //
                "        getutcdate()," + //
                "        schema_name()," + //
                "        ?" + //
                "        )" + //
                "";
        List<Object> lInsertParameters = new ArrayList<Object>();
        lInsertParameters.add(pFilePart);
        lInsertParameters.add(pLogname);
        new WrapperExecutePreparedStatement(lSql, pOrcasCallableStatementProvider, lInsertParameters).execute();
        new WrapperExecutePreparedStatement("commit", pOrcasCallableStatementProvider).execute();
    }

    @Override
    public LoadIst createLoadIst(CallableStatementProvider pCallableStatementProvider, Parameters pParameters) {
        return new LoadIstAzureSql(pCallableStatementProvider, pParameters);
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
        return new DdlBuilderAzureSql(pParameters, this);
    }

    @Override
    public void executeDiffResultStatement(String pStatementToExecute, CallableStatementProvider pCallableStatementProvider) {
        new WrapperExecutePreparedStatement(pStatementToExecute, pCallableStatementProvider).execute();
    }

    @Override
    public boolean isRenamePrimaryKey() {
        return false;
    }

    @Override
    public boolean isRenameIndex() {
        return false;
    }

    @Override
    public boolean isRenameMView() {
        return false;
    }

    @Override
    public boolean isRenameForeignKey() {
        return false;
    }

    @Override
    public boolean isRenameUniqueKey() {
        return false;
    }

    @Override
    public boolean isRenameConstraint() {
        return false;
    }

    @Override
    public boolean isUpdateIdentity() {
        return false;
    }

    @Override
    protected boolean isExpressionDifferentNotNull(String pExpression1, String pExpression2) {
        return super.isExpressionDifferentNotNull(cleanupExpression(pExpression1), cleanupExpression(pExpression2));
    }

    private String cleanupSubExpression(String pExpression) {
        String lReturn = pExpression;

        lReturn = lReturn.trim();

        if (lReturn.startsWith("(") && lReturn.endsWith(")")) {
            lReturn = lReturn.substring(1, lReturn.length() - 1);
            return cleanupSubExpression(lReturn);
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
        lReturn = lReturn.replace("::numeric", "");
        lReturn = lReturn.replace("!=", "<>");

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

        if (pExpression.endsWith("'")) {
            lReturn += "'";
        }

        return lReturn;
    }
}
