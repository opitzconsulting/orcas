package de.opitzconsulting.orcas.diff;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.opitzconsulting.orcas.diff.ParametersCommandline.ParameterTypeMode;
import de.opitzconsulting.orcas.sql.WrapperIteratorResultSet;

public class OrcasExtractSynonyms extends Orcas {
    public static void main(String[] pArgs) {
        new OrcasExtractSynonyms().mainRun(pArgs);
    }

    @Override
    protected ParameterTypeMode getParameterTypeMode() {
        return ParameterTypeMode.ORCAS_EXTRACT_REPLACEABLES;
    }

    @Override
    protected void run() throws Exception {
        File lFile = new File(getParameters().getSpoolfile());

        lFile.getParentFile().mkdirs();

        try (Writer lWriter = new OutputStreamWriter(new FileOutputStream(lFile), getParameters().getEncoding())) {
            JdbcConnectionHandler.runWithCallableStatementProvider(getParameters(), pCallableStatementProvider -> {
                String
                    lSql =
                    "select 'create or replace synonym ' || synonym_name || ' for ' || table_owner || '.' || table_name || case when db_link is not null then '@' || db_link end || ';' as sysnonym_statement from user_synonyms where not("
                        + getParameters().getExcludewheresynonym()
                        + ") order by synonym_name";

                new WrapperIteratorResultSet(lSql, pCallableStatementProvider) {
                    @Override
                    protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                        try {
                            lWriter.append(pResultSet.getString("sysnonym_statement"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }.execute();
            });
        }
    }
}
