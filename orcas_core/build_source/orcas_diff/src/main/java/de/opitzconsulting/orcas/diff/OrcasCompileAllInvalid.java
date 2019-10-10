package de.opitzconsulting.orcas.diff;

import java.net.URL;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.opitzconsulting.orcas.diff.Parameters.FailOnErrorMode;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperIteratorResultSet;

public class OrcasCompileAllInvalid extends OrcasScriptRunner {
    private List<CompileInfo> compileInfos;

    @Deprecated
    public void setGetCompileInfos() {
    }

    public List<CompileInfo> getCompileInfos() {
        return compileInfos;
    }

    @Override
    public void runURL(
        URL pURL, CallableStatementProvider pCallableStatementProvider, Parameters pParameters, Charset pCharset) throws Exception {

        ((ParametersCall) pParameters).setAdditionalParameters(Stream
            .of("nojava"
                , pParameters.getExcludewherepackage()
                , pParameters.getExcludewhereobjecttype()
                , pParameters.getExcludewhereprocedure()
                , pParameters.getExcludewherefunction()
                , pParameters.getExcludewhereview()
                , pParameters.getExcludewheretrigger()
                , pParameters.getExcludewheresynonym())
            .collect(Collectors.toList()));

        String lFromAndWherePart =
            "        from user_objects where status = 'INVALID' and"
                + " ("
                + "    (object_type = 'PACKAGE'      and not (" + pParameters.getExcludewherepackage() + "))"
                + " or (object_type = 'PACKAGE BODY' and not (" + pParameters.getExcludewherepackage() + "))"
                + " or (object_type = 'TYPE'         and not (" + pParameters.getExcludewhereobjecttype() + "))"
                + " or (object_type = 'TYPE BODY'    and not (" + pParameters.getExcludewhereobjecttype() + "))"
                + " or (object_type = 'PROCEDURE'    and not (" + pParameters.getExcludewhereprocedure() + "))"
                + " or (object_type = 'FUNCTION'     and not (" + pParameters.getExcludewherefunction() + "))"
                + " or (object_type = 'VIEW'         and not (" + pParameters.getExcludewhereview() + "))"
                + " or (object_type = 'TRIGGER'      and not (" + pParameters.getExcludewheretrigger() + "))"
                + " or (object_type = 'SYNONYM'      and object_name in (select synonym_name from all_synonyms where not(" + pParameters.getExcludewheresynonym() + ")))"
                + " )";

        String lInavlidObjectsSql =
            "    select object_name, " +
                "       object_type " +
                lFromAndWherePart;

        String lCompileErrorsSql =
            "             select name,\n"
                + "       type,\n"
                + "       line,\n"
                + "       position,\n"
                + "       text,\n"
                + "       (\n"
                + "       select trim(text)\n"
                + "         from user_source\n"
                + "        where user_source.name = user_errors.name\n"
                + "          and user_source.type = user_errors.type\n"
                + "          and user_source.line = user_errors.line\n"
                + "       ) as line_text\n"
                + "  from user_errors\n"
                + " where name in \n"
                + "       ( \n"
                + "         select object_name\n"
                + lFromAndWherePart
                + "       )\n"
                + " order by 1, 3, 4";

        compileInfos = new ArrayList<>();

        new WrapperIteratorResultSet(lInavlidObjectsSql, pCallableStatementProvider) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                compileInfos.add(new CompileInfo(pResultSet.getString("object_name"), pResultSet.getString("object_type")));
            }
        }.execute();

        super.runURL(pURL, pCallableStatementProvider, pParameters, pCharset);

        new WrapperIteratorResultSet(lInavlidObjectsSql, pCallableStatementProvider) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                String lObjectName = pResultSet.getString("object_name");
                String lObjectType = pResultSet.getString("object_type");
                CompileInfo lCompileInfo = compileInfos.stream()
                                                       .filter(p -> p.getObjectName().equals(lObjectName))
                                                       .filter(p -> p.getObjectType().equals(lObjectType))
                                                       .findFirst()
                                                       .orElseGet(() -> {
                                                           CompileInfo lReturn = new CompileInfo(lObjectName, lObjectType);
                                                           compileInfos.add(lReturn);
                                                           return lReturn;
                                                       });

                lCompileInfo.setNotValidated();
            }
        }.execute();

        new WrapperIteratorResultSet(lCompileErrorsSql, pCallableStatementProvider) {
            @Override
            protected void useResultSetRow(ResultSet pResultSet) throws SQLException {
                String lObjectName = pResultSet.getString("name");
                String lObjectType = pResultSet.getString("type");
                CompileInfo lCompileInfo = compileInfos.stream()
                                                       .filter(p -> p.getObjectName().equals(lObjectName))
                                                       .filter(p -> p.getObjectType().equals(lObjectType))
                                                       .findFirst()
                                                       .orElseGet(() -> {
                                                           CompileInfo lReturn = new CompileInfo(lObjectName, lObjectType);
                                                           compileInfos.add(lReturn);
                                                           return lReturn;
                                                       });

                lCompileInfo.addCompileErrorLine(
                    pResultSet.getString("text"),
                    pResultSet.getString("line"),
                    pResultSet.getString("position"),
                    pResultSet.getString("line_text"));
            }
        }.execute();

        if (getParameters().isLogCompileErrors()) {
            getCompileInfos()
                .stream()
                .filter(p -> !p.isValidated())
                .forEach(p -> logInfo("invalid " +
                    p.getObjectType() +
                    ": " +
                    p.getObjectName() +
                    "\n" +
                    p.getCompileLineErrorList()
                     .stream()
                     .map(pLine -> ""
                         + pLine.getErrorMessage()
                         + " at "
                         + pLine.getLineNumber()
                         + ":"
                         + pLine.getPositionInLine()
                         + ": "
                         + pLine.getLineSource())
                     .collect(Collectors.joining("\n"))));
        }

        if (getParameters().getFailOnErrorMode() != FailOnErrorMode.NEVER) {
            if (getCompileInfos().stream().anyMatch(p -> !p.isValidated())) {
                throw new RuntimeException("compile errors");
            }
        }
    }

    public static class CompileLineError {
        private final String errorMessage;
        private final Integer lineNumber;
        private final Integer positionInLine;
        private final String lineSource;

        CompileLineError(String pErrorMessage, Integer pLineNumber, Integer pPositionInLine, String pLineSource) {
            errorMessage = pErrorMessage;
            lineNumber = pLineNumber;
            positionInLine = pPositionInLine;
            lineSource = pLineSource;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public Integer getLineNumber() {
            return lineNumber;
        }

        public Integer getPositionInLine() {
            return positionInLine;
        }

        public String getLineSource() {
            return lineSource;
        }

        @Override
        public String toString() {
            return "CompileLineError{" +
                "errorMessage='" + errorMessage + '\'' +
                ", lineNumber=" + lineNumber +
                ", positionInLine=" + positionInLine +
                ", lineSource='" + lineSource + '\'' +
                '}';
        }
    }

    public static class CompileInfo {
        private String objectName;
        private String objectType;
        private boolean validated;
        private List<CompileLineError> compileLineErrorList = new ArrayList<>();

        void setNotValidated() {
            validated = false;
        }

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

        public List<CompileLineError> getCompileLineErrorList() {
            return compileLineErrorList;
        }

        @Override
        public String toString() {
            return "CompileInfo{" +
                "objectName='" + objectName + '\'' +
                ", objectType='" + objectType + '\'' +
                ", validated=" + validated +
                ", compileLineErrorList=" + compileLineErrorList +
                '}';
        }

        public void addCompileErrorLine(String pErrorMessage, String pLineNumber, String pPositionInLine, String pLineSource) {
            compileLineErrorList.add(new CompileLineError(
                pErrorMessage,
                pLineNumber == null ? null : Integer.parseInt(pLineNumber),
                pPositionInLine == null ? null : Integer.parseInt(pPositionInLine),
                pLineSource));
        }
    }
}
