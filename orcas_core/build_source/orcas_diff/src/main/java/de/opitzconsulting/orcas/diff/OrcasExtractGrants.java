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

public class OrcasExtractGrants extends Orcas {
    public static void main(String[] pArgs) {
        new OrcasExtractGrants().mainRun(pArgs);
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
                    "select 'grant ' || privilege || ' on ' || grantor || '.' || table_name || ' to ' || grantee || case when grantable = 'YES' then ' with grant option' end || case when hierarchy = 'YES' then ' with hierarchy option' end || ';' as grant_statement from user_tab_privs_made where not("
                        + getParameters().getExcludewheregrant()
                        + ") order by table_name, grantee, privilege";

                new WrapperIteratorResultSet(lSql, pCallableStatementProvider) {
                    @Override
                    protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                        try {
                            lWriter.append(pResultSet.getString("grant_statement"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }.execute();
            });
        }
    }
}
