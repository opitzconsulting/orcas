package de.opitzconsulting.orcas.diff;

import java.net.URL;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperIteratorResultSet;

public class OrcasCompileAllInvalid extends OrcasScriptRunner {
    private boolean getCompileInfos;
    private List<CompileInfo> compileInfos;

    public void setGetCompileInfos() {
        getCompileInfos = true;
    }

    public List<CompileInfo> getCompileInfos() {
        return compileInfos;
    }

    @Override
    public void runURL(
        URL pURL, CallableStatementProvider pCallableStatementProvider, Parameters pParameters, Charset pCharset) throws Exception {

        String lSql =
            "    select substr(object_name,1,30) object_name, " +
                "       object_type " +
                "  from user_objects " +
                " where status = 'INVALID'";

        if (getCompileInfos) {
            compileInfos = new ArrayList<>();

            new WrapperIteratorResultSet(lSql, pCallableStatementProvider) {
                @Override
                protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                    compileInfos.add(new CompileInfo(pResultSet.getString("object_name"), pResultSet.getString("object_type")));
                }
            }.execute();
        }

        super.runURL(pURL, pCallableStatementProvider, pParameters, pCharset);

        if (getCompileInfos) {
            new WrapperIteratorResultSet(lSql, pCallableStatementProvider) {
                @Override
                protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                    String lObjectName = pResultSet.getString("object_name");
                    String lObjectType = pResultSet.getString("object_type");
                    compileInfos.stream()
                                .filter(p -> p.getObjectName().equals(lObjectName))
                                .filter(p -> p.getObjectType().equals(lObjectType))
                                .forEach(CompileInfo::setNotValidated);
                }
            }.execute();
        }
    }

    public static class CompileInfo {
        private String objectName;
        private String objectType;

        void setNotValidated() {
            validated = false;
        }

        private boolean validated;

        public CompileInfo(String pObjectName, String pObjectType) {
            objectName = pObjectName;
            objectType = pObjectType;
            validated = true;
        }

        public String getObjectName() {
            return objectName;
        }

        public String getObjectType() {
            return objectType;
        }

        public boolean isValidated() {
            return validated;
        }

        @Override
        public String toString() {
            return "CompileInfo{" +
                "objectName='" + objectName + '\'' +
                ", objectType='" + objectType + '\'' +
                ", validated=" + validated +
                '}';
        }
    }
}
