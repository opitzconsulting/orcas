package com.opitzconsulting.orcas.diff;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.junit.Assume;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.UseParametersRunnerFactory;

import de.opitzconsulting.orcas.diff.BaseMultiSchemaConnectionManager;
import de.opitzconsulting.orcas.diff.OrcasDiff.DiffResult;
import de.opitzconsulting.orcas.diff.OrcasExtractStatics;
import de.opitzconsulting.orcas.diff.OrcasMain;
import de.opitzconsulting.orcas.diff.OrcasScriptRunner;
import de.opitzconsulting.orcas.diff.ParameterDefaults;
import de.opitzconsulting.orcas.diff.Parameters.AdditionalExtensionFactory;
import de.opitzconsulting.orcas.diff.Parameters.FailOnErrorMode;
import de.opitzconsulting.orcas.diff.Parameters.JdbcConnectParameters;
import de.opitzconsulting.orcas.diff.ParametersCall;
import de.opitzconsulting.orcas.diff.XmlLogFileHandler;
import de.opitzconsulting.orcasDsl.Model;

@RunWith(OrcasCoreIntegrationTest.MyOrcasParameterizedParallel.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@UseParametersRunnerFactory(OrcasParameterizedParallel.OrcasParametersRunnerFactory.class)
public class OrcasCoreIntegrationTest {
    public static class MyOrcasParameterizedParallel extends OrcasParameterizedParallel {
        public MyOrcasParameterizedParallel(Class<?> pKlass) throws Throwable {
            super(pKlass);
        }

        @Override
        public void assumeShouldExecuteTestcase(String pTestName) {
            new TestSetup(pTestName).assumeShouldExecuteTestcase();
        }
    }

    private static OrcasCoreIntegrationConfig orcasCoreIntegrationConfig = OrcasCoreIntegrationConfigSystemProperties.getOrcasCoreIntegrationConfig();

    private static final String EXTRACT_FOLDER_NAME = "00_extract";
    private static final String REFERENCE_NAME = "00_schema";
    static final String DEFAULT_EXCLUDE = "object_name like '%$%'";

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        List<Object[]> lReturn = new ArrayList<Object[]>();

        for (File lFile : new File(orcasCoreIntegrationConfig.getBaseDir() + "tests").listFiles()) {
            String lTestName = lFile.getName();
            if (lTestName.startsWith("test_")) {
                if (lTestName.matches(orcasCoreIntegrationConfig.getExecuteTests())) {
                    lReturn.add(new Object[]{lTestName});
                }
            }
        }

        return lReturn;
    }

    @Parameter
    public String testName;

    private TestSetup _testSetup;

    private String _lognameFirstRun = "01_statics";

    private static final Map<String, Map<String, JdbcConnectParameters>> _connectParametersTargetUserMap = new HashMap<>();

    private static List<String> parseReaderToLines(File pFile) {
        try {
            BufferedReader lBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(pFile), StandardCharsets.UTF_8));

            List<String> lLines = new ArrayList<String>();

            String lFileLine;
            while ((lFileLine = lBufferedReader.readLine()) != null) {
                lLines.add(lFileLine);
            }
            lBufferedReader.close();

            return lLines;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteRecursive(File pFile) {
        if (pFile.isDirectory()) {
            for (String lFile : pFile.list()) {
                deleteRecursive(new File(pFile, lFile));
            }
        }

        pFile.delete();
    }

    interface MultiSchemaSetup {
        String getFilePostfix();

        default String getSchemaName() {
            return getJdbcConnectParameters().getJdbcUser();
        }

        String getSchemaAlias();

        String[] getParameters(String... pOtherParameters);

        JdbcConnectParameters getJdbcConnectParameters();
    }

    private static class TestSetup {
        private final boolean _testWithData;
        private final String _dialect;
        private String testName;

        private boolean _dropmode;
        private boolean _indexparallelcreate;
        private boolean _createmissingfkindexes;
        private boolean _test_extract;
        private boolean _test_spool;
        private boolean _tablemovetablespace;
        private boolean _indexmovetablespace;
        private String _extensionfolder;
        private String _required_feature_list;
        private String _dateformat;
        private String relevantFiles;
        private String _excludewheresequence;
        private boolean _customExtensionFolder;
        private boolean _availableFeatureRequirementMatched = true;
        private boolean _additionsOnly;
        private boolean _columnsSetUnused;
        private boolean _indexonlinecreation;
        private String _expectfailure;
        public boolean _minimizeStatementCount;
        public List<String> _schemaNames;

        boolean isMultiSchema() {
            return _schemaNames.size() > 1;
        }

        private void runMultiSchemaSetupWithDbaSetup(Consumer<MultiSchemaSetup> pMultiSchemaSetupConsumer) {
            internalRunMultiSchemaSetup(pMultiSchemaSetupConsumer, true);
        }

        public void runMultiSchemaSetup(Consumer<MultiSchemaSetup> pMultiSchemaSetupConsumer) {
            internalRunMultiSchemaSetup(pMultiSchemaSetupConsumer, false);
        }

        public void internalRunMultiSchemaSetup(Consumer<MultiSchemaSetup> pMultiSchemaSetupConsumer, boolean pWithDbaSetup) {
            if (!isMultiSchema()) {
                pMultiSchemaSetupConsumer.accept(new MultiSchemaSetup() {
                    @Override
                    public String getFilePostfix() {
                        return "";
                    }

                    @Override
                    public String getSchemaAlias() {
                        return _schemaNames.get(0);
                    }

                    @Override
                    public String[] getParameters(String... pOtherParameters) {
                        return pOtherParameters;
                    }

                    @Override
                    public JdbcConnectParameters getJdbcConnectParameters() {
                        synchronized (_connectParametersTargetUserMap) {
                            return _connectParametersTargetUserMap.get(testName).get(_schemaNames.get(0));
                        }
                    }
                });
            } else {
                if (pWithDbaSetup) {
                    pMultiSchemaSetupConsumer.accept(new MultiSchemaSetup() {
                        @Override
                        public String getFilePostfix() {
                            return "";
                        }

                        @Override
                        public String getSchemaAlias() {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public String[] getParameters(String... pOtherParameters) {
                            String[] lReturn = new String[_schemaNames.size() + pOtherParameters.length];

                            synchronized (_connectParametersTargetUserMap) {
                                for (int i = 0; i < _schemaNames.size(); i++) {
                                    lReturn[i] = _connectParametersTargetUserMap.get(testName).get(_schemaNames.get(i)).getJdbcUser();
                                }
                            }

                            for (int i = 0; i < pOtherParameters.length; i++) {
                                lReturn[_schemaNames.size() + i] = pOtherParameters[i];
                            }

                            return lReturn;
                        }

                        @Override
                        public JdbcConnectParameters getJdbcConnectParameters() {
                            return OrcasParallelConnectionHandler._connectParametersDba;
                        }
                    });
                }

                _schemaNames.forEach(p -> {
                    pMultiSchemaSetupConsumer.accept(new MultiSchemaSetup() {
                        @Override
                        public String getFilePostfix() {
                            return "_" + p;
                        }

                        @Override
                        public String getSchemaAlias() {
                            return p;
                        }

                        @Override
                        public String[] getParameters(String... pOtherParameters) {
                            String[] lReturn = new String[_schemaNames.size() + pOtherParameters.length];

                            synchronized (_connectParametersTargetUserMap) {
                                for (int i = 0; i < _schemaNames.size(); i++) {
                                    lReturn[i] = _connectParametersTargetUserMap.get(testName).get(_schemaNames.get(i)).getJdbcUser();
                                }
                            }

                            for (int i = 0; i < pOtherParameters.length; i++) {
                                lReturn[_schemaNames.size() + i] = pOtherParameters[i];
                            }

                            return lReturn;
                        }

                        @Override
                        public JdbcConnectParameters getJdbcConnectParameters() {
                            synchronized (_connectParametersTargetUserMap) {
                                return _connectParametersTargetUserMap.get(testName).get(p);
                            }
                        }
                    });
                });
            }
        }

        public boolean isMviewsWithColumns() {
            return _mviewsWithColumns;
        }

        public boolean _mviewsWithColumns;

        private boolean _cleanupfkvaluesondropmode;

        public TestSetup(String pTestName) {
            try {
                testName = pTestName;

                Properties lDefaultProperties = new Properties();
                lDefaultProperties.load(new FileInputStream(orcasCoreIntegrationConfig.getBaseDir() + "/parameter.properties"));

                Properties lTestProperties = new Properties();
                String lTestPropertiesFileName = orcasCoreIntegrationConfig.getBaseDir() + "tests/" + pTestName + "/parameter.properties";
                if (new File(lTestPropertiesFileName).exists()) {
                    lTestProperties.load(new FileInputStream(lTestPropertiesFileName));
                }

                _testWithData = getBooleanProperty("test_with_data", lDefaultProperties, lTestProperties);
                _dialect = getProperty("dialect", lDefaultProperties, lTestProperties);
                _dropmode = getBooleanProperty("dropmode", lDefaultProperties, lTestProperties);
                _indexparallelcreate = getBooleanProperty("indexparallelcreate", lDefaultProperties, lTestProperties);
                _createmissingfkindexes = getBooleanProperty("createmissingfkindexes", lDefaultProperties, lTestProperties);
                _test_extract = getBooleanProperty("test_extract", lDefaultProperties, lTestProperties);
                _test_spool = getBooleanProperty("test_spool", lDefaultProperties, lTestProperties);
                _tablemovetablespace = getBooleanProperty("tablemovetablespace", lDefaultProperties, lTestProperties);
                _indexmovetablespace = getBooleanProperty("indexmovetablespace", lDefaultProperties, lTestProperties);
                _extensionfolder = getProperty("extensionfolder", lDefaultProperties, lTestProperties);
                _required_feature_list = getProperty("required_feature_list", lDefaultProperties, lTestProperties);
                _dateformat = getProperty("dateformat", lDefaultProperties, lTestProperties);
                _excludewheresequence = getProperty("excludewheresequence", lDefaultProperties, lTestProperties);
                _excludewheresequence = _excludewheresequence.replaceAll("''", "'");
                _additionsOnly = getBooleanProperty("additionsonly", lDefaultProperties, lTestProperties);
                _columnsSetUnused = getBooleanProperty("setunused", lDefaultProperties, lTestProperties);
                _indexonlinecreation = getBooleanProperty("indexonlinecreation", lDefaultProperties, lTestProperties);
                _expectfailure = getProperty("expectfailure", lDefaultProperties, lTestProperties);
                _minimizeStatementCount = getBooleanProperty("minimizestatementcount", lDefaultProperties, lTestProperties);
                _cleanupfkvaluesondropmode = getBooleanProperty("cleanupfkvaluesondropmode", lDefaultProperties, lTestProperties);
                _mviewsWithColumns = getBooleanProperty("mviewswithcolumns", lDefaultProperties, lTestProperties);
                relevantFiles = getProperty("relevantFiles", lDefaultProperties, lTestProperties);

                if (_expectfailure != null && _expectfailure.trim().length() == 0) {
                    _expectfailure = null;
                }

                _schemaNames = Arrays.asList(getProperty("schema_names", lDefaultProperties, lTestProperties).split(","));

                _availableFeatureRequirementMatched = true;
                for (String lRequiredFeature : Arrays.asList(_required_feature_list.split(","))) {
                    if (!orcasCoreIntegrationConfig.getAvailableFeatureList().contains(lRequiredFeature)) {
                        _availableFeatureRequirementMatched = false;
                    }
                }

                if (!_extensionfolder.equals("extensions")) {
                    _customExtensionFolder = true;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void assumeShouldExecuteTestcase() {
            Assume.assumeTrue("testcase ignored custom extensionfolder not supported yet " + _extensionfolder, !_customExtensionFolder);
            Assume.assumeTrue(
                    "testcase ignored: " + testName + " " + orcasCoreIntegrationConfig.getExecuteTests(),
                    testName.matches(orcasCoreIntegrationConfig.getExecuteTests()));
            Assume.assumeTrue("testcase skipped, required features not available: required: "
                    + _required_feature_list
                    + " available: "
                    + orcasCoreIntegrationConfig.getAvailableFeatureList(), _availableFeatureRequirementMatched);
            Assume.assumeTrue("testcase skipped, dialect missmatch: "
                    + _dialect
                    + " "
                    + orcasCoreIntegrationConfig.getDialect(), _dialect.contains(orcasCoreIntegrationConfig.getDialect()));
        }

        private boolean getBooleanProperty(String pKey, Properties pDefaultProperties, Properties pTestProperties) {
            String lValue = getProperty(pKey, pDefaultProperties, pTestProperties);

            return lValue.equals("true");
        }

        private String getProperty(String pKey, Properties pDefaultProperties, Properties pTestProperties) {
            if (pTestProperties.getProperty(pKey) != null) {
                return pTestProperties.getProperty(pKey);
            }

            return pDefaultProperties.getProperty(pKey);
        }
    }

    @Before
    public void beforeTest() throws Exception {
        _testSetup = new TestSetup(testName);

        synchronized (_connectParametersTargetUserMap) {
            if (!_connectParametersTargetUserMap.containsKey(testName)) {
                Map<String, JdbcConnectParameters> lMap = new HashMap<>();

                _testSetup._schemaNames.forEach(p -> lMap.put(p, OrcasParallelConnectionHandler.createConnectionParametersForTargetUser()));

                _connectParametersTargetUserMap.put(testName, lMap);
            }
        }
    }

    public void afterAllTests() {
        synchronized (_connectParametersTargetUserMap) {
            _connectParametersTargetUserMap.get(testName).values().forEach(p -> {
                if (_testSetup.isMultiSchema()) {
                    try {
                        OrcasParallelConnectionHandler.resetUser(p);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                OrcasParallelConnectionHandler.returnConnectionParametersForTargetUser(p);
            });
            _connectParametersTargetUserMap.put(testName, null);
        }

    }

    private JdbcConnectParameters getConnectParametersTargetUser() {
        synchronized (_connectParametersTargetUserMap) {
            return _connectParametersTargetUserMap.get(testName).get(_testSetup._schemaNames.get(0));
        }
    }

    private static void executeScript(String pBaseDir, JdbcConnectParameters pJdbcConnectParameters, String pScriptName, String... pParameters) {
        ParametersCall lParametersCall = new ParametersCall();

        copyConnectionParameters(pJdbcConnectParameters, lParametersCall.getJdbcConnectParameters());

        lParametersCall.setAdditionalParameters(Arrays.asList(pParameters));
        lParametersCall.setIsOneTimeScriptMode(false);
        lParametersCall.setFailOnErrorMode(FailOnErrorMode.IGNORE_DROP);

        lParametersCall.setModelFile(pBaseDir + pScriptName);

        new OrcasScriptRunner().mainRun(lParametersCall);
    }

    private static void executeScriptIfExists(JdbcConnectParameters pJdbcConnectParameters, String pScriptName, String... pParameters) {
        if (new File(orcasCoreIntegrationConfig.getBaseDir() + pScriptName).exists()) {
            executeScript(orcasCoreIntegrationConfig.getBaseDir(), pJdbcConnectParameters, pScriptName, pParameters);
        }
    }

    private static void executeScript(JdbcConnectParameters pJdbcConnectParameters, String pScriptName, String... pParameters) {
        executeScript(orcasCoreIntegrationConfig.getBaseDir(), pJdbcConnectParameters, pScriptName, pParameters);
    }

    private static void copyConnectionParameters(JdbcConnectParameters pSourceConnectParameters, JdbcConnectParameters pDestConnectParameters) {
        pDestConnectParameters.setJdbcDriver(pSourceConnectParameters.getJdbcDriver());
        pDestConnectParameters.setJdbcUrl(pSourceConnectParameters.getJdbcUrl());
        pDestConnectParameters.setJdbcUser(pSourceConnectParameters.getJdbcUser());
        pDestConnectParameters.setJdbcPassword(pSourceConnectParameters.getJdbcPassword());
    }

    private void asserSchemasEqual(String pName, boolean pIncludeData) {
        extractSchemas(pName, pIncludeData);

        assertFilesEqual(
                pName,
                getSchemaExtarctFileName(REFERENCE_NAME, false, true, testName, ""),
                getSchemaExtarctFileName(pName, false, true, testName, ""),
                pIncludeData);

        if (pIncludeData) {
            _testSetup.runMultiSchemaSetup(p -> {
                assertFilesEqual(
                        pName,
                        getSchemaExtarctFileName(REFERENCE_NAME, pIncludeData, false, testName, p.getFilePostfix()),
                        getSchemaExtarctFileName(pName, pIncludeData, false, testName, p.getFilePostfix()),
                        pIncludeData);
            });
        }
    }

    static void asserSchemaEqual(
            String pNameExpected,
            String pNameActual,
            boolean pIncludeData,
            String pTestName,
            String pExcludeWhereSequence,
            String pDateformat,
            JdbcConnectParameters pJdbcConnectParameters,
            boolean pIsMviewsWithColumns) {

        extractSchema(
                pJdbcConnectParameters,
                pNameActual,
                pIncludeData,
                pTestName,
                pExcludeWhereSequence,
                pDateformat,
                pIsMviewsWithColumns,
                "");

        assertFilesEqual(
                pNameActual,
                getSchemaExtarctFileName(pNameExpected, pIncludeData, true, pTestName, ""),
                getSchemaExtarctFileName(pNameActual, pIncludeData, true, pTestName, ""),
                pIncludeData);
        assertFilesEqual(
                pNameActual,
                getSchemaExtarctFileName(pNameExpected, pIncludeData, false, pTestName, ""),
                getSchemaExtarctFileName(pNameActual, pIncludeData, false, pTestName, ""),
                pIncludeData);
    }

    private static void assertFilesEqual(String pName, String pExpectedFile, String pActualFile) {
        assertFilesEqual(pName, pExpectedFile, pActualFile, true);
    }

    private static void assertFilesEqual(String pName, String pExpectedFile, String pActualFile, boolean pIncludeData) {
        List<String> lExpectedLines = parseReaderToLines(new File(pExpectedFile));
        List<String> lActualLines = parseReaderToLines(new File(pActualFile));

        String lMessage = "files not equal: " + pName + " " + pExpectedFile + " " + pActualFile;

        int lMinLines = Math.min(lExpectedLines.size(), lActualLines.size());
        for (int i = 0; i < lMinLines; i++) {
            if (pIncludeData || !lExpectedLines.get(i).contains("max_value_select")) {
                assertEquals(lMessage, lExpectedLines.get(i), lActualLines.get(i));
            }
        }

        assertEquals(lMessage, lExpectedLines.size(), lActualLines.size());
    }

    private void extractSchemas(String pName, boolean pIncludeData) {
        if (pIncludeData) {
            _testSetup.runMultiSchemaSetup(p -> {
                extractSchemaSpool(p.getJdbcConnectParameters(), pName, pIncludeData, testName, p.getFilePostfix());
            });
        }

        extractSchemaOrcas(
                getConnectParametersTargetUser(),
                pName,
                testName,
                _testSetup._excludewheresequence,
                _testSetup._dateformat,
                _testSetup.isMviewsWithColumns(),
                this::multiSchemaSetup);

    }

    static void extractSchema(
            JdbcConnectParameters pConnectParametersTargetUser,
            String pName,
            boolean pIncludeData,
            String pTestName,
            String pExcludeWhereSequence,
            String pDateformat,
            boolean pIsMviewsWithColumns,
            String pSchemaFilePostfix) {

        extractSchemaSpool(pConnectParametersTargetUser, pName, pIncludeData, pTestName, pSchemaFilePostfix);

        extractSchemaOrcas(
                pConnectParametersTargetUser,
                pName,
                pTestName,
                pExcludeWhereSequence,
                pDateformat,
                pIsMviewsWithColumns,
                p -> {
                });
    }

    private static void extractSchemaSpool(
            JdbcConnectParameters pConnectParametersTargetUser,
            String pName,
            boolean pIncludeData,
            String pTestName,
            String pSchemaFilePostfix) {
        executeScript(
                pConnectParametersTargetUser,
                "spool_" + orcasCoreIntegrationConfig.getDialect() + ".sql",
                getSchemaExtarctFileName(pName, pIncludeData, false, pTestName, pSchemaFilePostfix),
                orcasCoreIntegrationConfig.getWorkfolder() + pTestName + "/tmp_data.log",
                pIncludeData ? "1=1" : "1=2");
    }

    private static void extractSchemaOrcas(
            JdbcConnectParameters pConnectParametersTargetUser,
            String pName,
            String pTestName,
            String pExcludeWhereSequence,
            String pDateformat,
            boolean pIsMviewsWithColumns,
            Consumer<ParametersCall> pParameterModifier) {
        ParametersCall lParametersCall = new ParametersCall();

        copyConnectionParameters(pConnectParametersTargetUser, lParametersCall.getJdbcConnectParameters());

        lParametersCall.setExcludewheresequence(pExcludeWhereSequence);
        lParametersCall.setExcludewheretable(DEFAULT_EXCLUDE);
        lParametersCall.setDateformat(pDateformat);

        lParametersCall.setSpoolfile(getSchemaExtarctFileName(pName, false, true, pTestName, ""));
        lParametersCall.setModelFile(null);
        lParametersCall.setScriptprefix(null);
        lParametersCall.setScriptpostfix(null);
        lParametersCall.setOrderColumnsByName(true);
        lParametersCall.setRemoveDefaultValuesFromModel(true);
        lParametersCall.setViewExtractMode(pIsMviewsWithColumns ? "full" : ParameterDefaults.viewextractmode);

        pParameterModifier.accept(lParametersCall);

        new OrcasExtractStatics().mainRun(lParametersCall);
    }

    private static String getSchemaExtarctFileName(
            String pString,
            boolean pIncludeData,
            boolean pOrcas,
            String pTestName,
            String pSchemaFilePostfix) {
        return getWorkfolderFilename(
                pTestName,
                pString + (pOrcas ? "_orcas" : "_spool" + (pIncludeData ? "data" : "")) + pSchemaFilePostfix + (pOrcas ? ".xml" : ".txt"));
    }

    private static String getWorkfolderFilename(String pTestName, String pFilename) {
        return orcasCoreIntegrationConfig.getWorkfolder() + pTestName + "/" + pFilename;
    }

    private void orcasExtract(String pString) {
        String lSpoolFolder = getSpoolFolder(pString);

        ParametersCall lParametersCall = new ParametersCall();

        copyConnectionParameters(getConnectParametersTargetUser(), lParametersCall.getJdbcConnectParameters());
        multiSchemaSetup(lParametersCall);

        lParametersCall.setExcludewheresequence(_testSetup._excludewheresequence);
        lParametersCall.setExcludewheretable(DEFAULT_EXCLUDE);
        lParametersCall.setDateformat(_testSetup._dateformat);

        lParametersCall.setSpoolfile(null);
        lParametersCall.setSpoolfolder(lSpoolFolder);
        lParametersCall.setModelFile(null);
        lParametersCall.setScriptprefix(null);
        lParametersCall.setScriptpostfix(null);
        lParametersCall.setOrderColumnsByName(false);
        lParametersCall.setRemoveDefaultValuesFromModel(true);
        lParametersCall.setViewExtractMode(_testSetup.isMviewsWithColumns() ? "full" : ParameterDefaults.viewextractmode);

        new OrcasExtractStatics().mainRun(lParametersCall);
    }

    private void executeOrcasStatics(JdbcConnectParameters pJdbcConnectParameters, String pSpoolName, boolean pLogIgnoredStatements) {
        String lXmlInputFile = null;
        if (isXmlInputFileExists()) {
            lXmlInputFile = getXmlInputFile();
        }

        executeOrcasStatics(
                pJdbcConnectParameters,
                pSpoolName,
                orcasCoreIntegrationConfig.getBaseDir() + "tests/" + testName + "/tabellen",
                pLogIgnoredStatements,
                lXmlInputFile);
    }

    private boolean isXmlInputFileExists() {
        return new File(getXmlInputFile()).exists();
    }

    private String getXmlInputFile() {
        return getBasedirFilename("input_xml.xml");
    }

    private String getBasedirFilename(String pFilename) {
        return orcasCoreIntegrationConfig.getBaseDir() + "tests/" + testName + "/" + pFilename;
    }

    private void executeOrcasStatics(
            JdbcConnectParameters pJdbcConnectParameters,
            String pSpoolName,
            String pModelFolder,
            boolean pLogIgnoredStatements,
            String pXmlInputFile) {
        ParametersCall lParametersCall = new ParametersCall();

        copyConnectionParameters(pJdbcConnectParameters, lParametersCall.getJdbcConnectParameters());

        lParametersCall.setModelFile(pModelFolder);
        lParametersCall.setSqlplustable(false);
        lParametersCall.setTargetplsql(null);
        lParametersCall.setScriptprefix(null);
        lParametersCall.setScriptpostfix("sql");
        lParametersCall.setExcludewheresequence(_testSetup._excludewheresequence);
        lParametersCall.setExcludewheretable(DEFAULT_EXCLUDE);
        lParametersCall.setOrderColumnsByName(false);
        lParametersCall.setCreatemissingfkindexes(_testSetup._createmissingfkindexes);
        lParametersCall.setDropmode(_testSetup._dropmode);
        lParametersCall.setLogonly(false);
        lParametersCall.setLogname(pSpoolName);
        lParametersCall.setSpoolfolder(getSpoolFolder(pSpoolName));
        lParametersCall.setXmlLogFile(orcasCoreIntegrationConfig.getWorkfolder() + testName + "/xmllog_" + pSpoolName + ".xml");
        lParametersCall.setTablemovetablespace(_testSetup._tablemovetablespace);
        lParametersCall.setIndexmovetablespace(_testSetup._indexmovetablespace);
        lParametersCall.setMviewlogmovetablespace(true);
        lParametersCall.setIndexparallelcreate(_testSetup._indexparallelcreate);
        lParametersCall.setUpdateEnabledStatus(true);
        lParametersCall.setDateformat(_testSetup._dateformat);
        lParametersCall.setAdditionsOnly(_testSetup._additionsOnly);
        lParametersCall.setLogIgnoredStatements(pLogIgnoredStatements);
        lParametersCall.setSetUnusedInsteadOfDropColumn(_testSetup._columnsSetUnused);
        lParametersCall.setCreateIndexOnline(_testSetup._indexonlinecreation);
        String lXmlLogFile = getLogfileName(pSpoolName);
        lParametersCall.setXmlLogFile(lXmlLogFile);
        lParametersCall.setXmlInputFile(pXmlInputFile);
        lParametersCall.setMinimizeStatementCount(_testSetup._minimizeStatementCount);
        lParametersCall.setCleanupFkValuesOnDropmode(_testSetup._cleanupfkvaluesondropmode);
        lParametersCall.setViewExtractMode(_testSetup.isMviewsWithColumns() ? "full" : ParameterDefaults.viewextractmode);
        List<File> lRelevantFileList = Stream.of(_testSetup.relevantFiles.split(","))
                .filter(it -> !it.isEmpty())
                .map(it -> new File(pModelFolder + "/" + it))
                .collect(Collectors.toList());
        if (!lRelevantFileList.isEmpty()) {
            lParametersCall.setRelevantModelFiles(lRelevantFileList);
        }

        multiSchemaSetup(lParametersCall);

        lParametersCall.setAdditionalOrcasExtensionFactory(new AdditionalExtensionFactory() {
            @SuppressWarnings("unchecked")
            @Override
            public <T extends EObject> List<UnaryOperator<T>> getAdditionalExtensions(Class<T> pModelClass, boolean pReverseMode) {
                List<UnaryOperator<T>> lList = new ArrayList<>(Collections.singletonList(p -> (T) new TablespaceRemapperExtension(
                        orcasCoreIntegrationConfig.getAlternateTablespace1(),
                        orcasCoreIntegrationConfig.getAlternateTablespace2()).transformModel((Model) p)));

                if (_testSetup.isMultiSchema()) {
                    lList.add(p -> {
                        ReplaceMultiSchemaPrefixExtension lReplaceMultiSchemaPrefixExtension = new ReplaceMultiSchemaPrefixExtension();

                        _testSetup.runMultiSchemaSetup(p1 -> lReplaceMultiSchemaPrefixExtension.initSchema(p1));

                        return (T) lReplaceMultiSchemaPrefixExtension.transformModel((Model) p);
                    });
                }

                return lList;
            }
        });

        if (isExpectfailure()) {
            try {
                new OrcasMain().mainRun(lParametersCall);

                fail("expected faliure: " + _testSetup._expectfailure);
            } catch (Exception e) {
                String lMessage = e.getMessage();
                boolean lMatches = lMessage.matches(_testSetup._expectfailure);

                if (!lMatches) {
                    throw new RuntimeException("no matching error: " + _testSetup._expectfailure + " " + lMessage, e);
                }
            }
        } else {
            new OrcasMain().mainRun(lParametersCall);
        }

        if (pXmlInputFile == null) {
            assertXmlLogFileParse(pSpoolName, lXmlLogFile);
        }
    }

    private void multiSchemaSetup(ParametersCall pParametersCall) {
        if (_testSetup.isMultiSchema()) {
            pParametersCall.setMultiSchema(true);
            pParametersCall.setMultiSchemaExcludewhereowner("owner not in (','");
            _testSetup.runMultiSchemaSetup(p ->
                    pParametersCall.setMultiSchemaExcludewhereowner(pParametersCall.getMultiSchemaExcludewhereowner() + ",'" + p
                            .getSchemaName()
                            .toUpperCase() + "'"));
            pParametersCall.setMultiSchemaExcludewhereowner(pParametersCall.getMultiSchemaExcludewhereowner() + ")");

            copyConnectionParameters(OrcasParallelConnectionHandler._connectParametersDba, pParametersCall.getJdbcConnectParameters());
            pParametersCall.setMultiSchemaDbaViews(true);
            pParametersCall.setMultiSchemaConnectionManager(new BaseMultiSchemaConnectionManager() {
                @Override
                protected JdbcConnectParameters getJdbcConnectParametersForSchema(
                        String pSchemaName,
                        de.opitzconsulting.orcas.diff.Parameters pParameters) {
                    JdbcConnectParameters lJdbcConnectParameters = new JdbcConnectParameters();
                    copyConnectionParameters(OrcasParallelConnectionHandler._connectParametersDba, lJdbcConnectParameters);
                    lJdbcConnectParameters.setJdbcUser(pSchemaName);
                    lJdbcConnectParameters.setJdbcPassword(pSchemaName.toLowerCase());
                    return lJdbcConnectParameters;
                }
            });
        }
    }

    private String getLogfileName(String pSpoolName) {
        return getSpoolFolder(pSpoolName + "_log.xml");
    }

    private void assertXmlLogFileParse(String pSpoolName, String pXmlLogFile) {
        ParametersCall lParametersCall = new ParametersCall();

        XmlLogFileHandler lXmlLogFileHandler = new XmlLogFileHandler(lParametersCall);
        DiffResult lParseXmlDiffResult = lXmlLogFileHandler.parseXml(pXmlLogFile);
        String lXmlLogFileFromXmlLogFileHandlerParse = getSpoolFolder(pSpoolName + "_log_reparse.xml");
        lXmlLogFileHandler.logXml(lParseXmlDiffResult, lXmlLogFileFromXmlLogFileHandlerParse);
        assertFilesEqual("xml-log-parse", pXmlLogFile, lXmlLogFileFromXmlLogFileHandlerParse);
    }

    private String getSpoolFolder(String pSpoolName) {
        return orcasCoreIntegrationConfig.getWorkfolder() + testName + "/" + pSpoolName;
    }

    private void assumeTestNotSkipped(boolean pNoSkipTest) {
        Assume.assumeTrue("test skipped", pNoSkipTest);
    }

    @Test
    public void test_00_setup_reference() {
        File lFile = new File(orcasCoreIntegrationConfig.getWorkfolder() + testName);
        deleteRecursive(lFile);
        lFile.mkdirs();

        resetUsers();

        if (!isExpectfailure()) {
            _testSetup.runMultiSchemaSetupWithDbaSetup(p ->
                    executeScriptIfExists(
                            p.getJdbcConnectParameters(),
                            "tests/" + testName + "/" + "erzeuge_zielzustand" + p.getFilePostfix() + ".sql",
                            p.getParameters(
                                    orcasCoreIntegrationConfig.getAlternateTablespace1(),
                                    orcasCoreIntegrationConfig.getAlternateTablespace2())));

            extractSchemas(REFERENCE_NAME, _testSetup._testWithData);

            if (orcasCoreIntegrationConfig.isWithRunWithExtractTest()) {
                if (_testSetup._testWithData) {
                    extractSchemas(REFERENCE_NAME, false);
                }
                orcasExtract(EXTRACT_FOLDER_NAME);
            }
        }
    }

    private void resetUsers() {
        synchronized (_connectParametersTargetUserMap) {
            _connectParametersTargetUserMap
                    .get(testName)
                    .values()
                    .forEach(OrcasParallelConnectionHandler::resetUser);
        }
    }

    private boolean isExpectfailure() {
        return _testSetup._expectfailure != null;
    }

    @Test
    public void test_01_update_statics() {
        assumeTestNotSkipped(orcasCoreIntegrationConfig.isWithFirstRunTest());
        resetUsers();
        setupInitialState();

        executeOrcasStatics(getConnectParametersTargetUser(), _lognameFirstRun, true);

        if (!isExpectfailure()) {
            asserSchemasEqual("01_schema", _testSetup._testWithData);
        }

        String lReferenceXmlFile = getBasedirFilename("reference_log.xml");

        if (new File(lReferenceXmlFile).exists()) {
            assertFilesEqual("reference_log", lReferenceXmlFile, getLogfileName(_lognameFirstRun));
        }
    }

    private void setupInitialState() {
        _testSetup.runMultiSchemaSetupWithDbaSetup(p ->
                executeScriptIfExists(
                        getConnectParametersTargetUser(),
                        "tests/" + testName + "/" + "erzeuge_ausgangszustand" + p.getFilePostfix() + ".sql",
                        p.getParameters(
                                orcasCoreIntegrationConfig.getAlternateTablespace1(),
                                orcasCoreIntegrationConfig.getAlternateTablespace2())));
    }

    @Test
    public void test_02_second_run_empty() {
        assumeTestNotSkipped(orcasCoreIntegrationConfig.isWithSecondRunEmptyTest() && !isXmlInputFileExists() && !isExpectfailure());

        String lLognameSecondRun = "02_statics";
        executeOrcasStatics(getConnectParametersTargetUser(), lLognameSecondRun, false);
        assertFalse("second run not empty", new File(getSpoolFolder(lLognameSecondRun)).exists());
    }

    @Test
    public void test_03_run_with_spool() {
        assumeTestNotSkipped(orcasCoreIntegrationConfig.isWithRunWithSpoolTest() && !isExpectfailure());
        Assume.assumeTrue("spool test not possible for testcase", _testSetup._test_spool);

        resetUsers();
        setupInitialState();
        executeScript(
                orcasCoreIntegrationConfig.getWorkfolder() + testName + "/" + _lognameFirstRun + "/",
                _testSetup.isMultiSchema() ? OrcasParallelConnectionHandler._connectParametersDba : getConnectParametersTargetUser(),
                "master_install.sql");
        asserSchemasEqual("03_schema", _testSetup._testWithData);
    }

    @Test
    public void test_04_run_with_extract() {
        try {
            assumeTestNotSkipped(orcasCoreIntegrationConfig.isWithRunWithExtractTest() && !isExpectfailure());
            Assume.assumeTrue("extract test not possible for testcase", _testSetup._test_extract);

            resetUsers();
            String lErzeugeAusgangszustandExtractFile = getBasedirFilename("erzeuge_ausgangszustand_extract.sql");
            if (new File(lErzeugeAusgangszustandExtractFile).exists()) {
                executeScript(
                        getConnectParametersTargetUser(),
                        lErzeugeAusgangszustandExtractFile,
                        orcasCoreIntegrationConfig.getAlternateTablespace1(),
                        orcasCoreIntegrationConfig.getAlternateTablespace2());
            }
            executeOrcasStatics(
                    getConnectParametersTargetUser(),
                    "04_statics",
                    orcasCoreIntegrationConfig.getWorkfolder() + testName + "/" + EXTRACT_FOLDER_NAME,
                    true,
                    null);
            asserSchemasEqual("04_schema", false);
        } finally {
            afterAllTests();
        }
    }
}
