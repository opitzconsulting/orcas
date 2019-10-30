package de.opitzconsulting.orcas.diff;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.eclipse.emf.ecore.EObject;

import de.opitzconsulting.orcas.sql.CallableStatementProvider;

public abstract class Parameters {
    public void setExtractViewCommnets(boolean pExtractViewCommnets) {
        isExtractViewCommnets = pExtractViewCommnets;
    }

    private boolean isExtractViewCommnets = ParameterDefaults.isExtractViewCommnets;

    public Consumer<CallableStatementProvider> getConnectionInitializer() {
        return connectionInitializer;
    }

    public boolean isExtractViewCommnets() {
        return isExtractViewCommnets;
    }

    public static class JdbcConnectParameters {
        String _jdbcDriver;
        String _jdbcUrl;
        String _jdbcUser;
        String _jdbcPassword;

        public String getJdbcDriver() {
            return _jdbcDriver;
        }

        public String getJdbcUrl() {
            return checkNull(_jdbcUrl);
        }

        public String getJdbcUser() {
            return checkNull(_jdbcUser);
        }

        public String getJdbcPassword() {
            return checkNull(_jdbcPassword);
        }

        public void setJdbcDriver(String pJdbcDriver) {
            _jdbcDriver = pJdbcDriver;
        }

        public void setJdbcUrl(String pJdbcUrl) {
            _jdbcUrl = pJdbcUrl;
        }

        public void setJdbcUser(String pJdbcUser) {
            _jdbcUser = pJdbcUser;
        }

        public void setJdbcPassword(String pJdbcPassword) {
            _jdbcPassword = pJdbcPassword;
        }
    }

    public static enum FailOnErrorMode {
        NEVER, ALWAYS, IGNORE_DROP
    }

    public void setMultiSchemaConnectionManager(MultiSchemaConnectionManager pMultiSchemaConnectionManager) {
        _multiSchemaConnectionManager = pMultiSchemaConnectionManager;
        if (_multiSchemaConnectionManager instanceof BaseMultiSchemaConnectionManager) {
            ((BaseMultiSchemaConnectionManager) _multiSchemaConnectionManager).setParameters(this);
        }
    }

    private MultiSchemaConnectionManager _multiSchemaConnectionManager = new MultiSchemaConnectionManagerSimple();
    protected JdbcConnectParameters _jdbcConnectParameters = new JdbcConnectParameters();
    protected JdbcConnectParameters _srcJdbcConnectParameters;
    protected JdbcConnectParameters _orcasJdbcConnectParameters;
    protected Boolean _logonly;
    protected Boolean _dropmode;
    protected Boolean _indexparallelcreate;
    protected Boolean _indexmovetablespace;
    protected Boolean _tablemovetablespace;

    public void setConnectionInitializer(Consumer<CallableStatementProvider> pConnectionInitializer) {
        connectionInitializer = pConnectionInitializer;
    }

    private Consumer<CallableStatementProvider> connectionInitializer = p -> {
    };

    public int getStaticsSpoolMaxLineLength() {
        return staticsSpoolMaxLineLength;
    }

    public void setStaticsSpoolMaxLineLength(int pStaticsSpoolMaxLineLength) {
        staticsSpoolMaxLineLength = pStaticsSpoolMaxLineLength;
    }

    private int staticsSpoolMaxLineLength = ParameterDefaults.staticsSpoolMaxLineLength;

    public boolean isLogCompileErrors() {
        return logCompileErrors;
    }

    public void setLogCompileErrors(boolean pLogCompileErrors) {
        logCompileErrors = pLogCompileErrors;
    }

    private boolean logCompileErrors = ParameterDefaults.logCompileErrors;

    public boolean isMviewlogmovetablespace() {
        return _mviewlogmovetablespace;
    }

    public void setMviewlogmovetablespace(boolean pMviewlogmovetablespace) {
        _mviewlogmovetablespace = pMviewlogmovetablespace;
    }

    protected boolean _mviewlogmovetablespace = ParameterDefaults.mviewlogmovetablespace;
    protected Boolean _createmissingfkindexes;
    protected Boolean _isOneTimeScriptMode;
    protected Boolean _isOneTimeScriptLogonlyMode;
    protected String _modelFile;
    protected String _spoolfile;
    protected String _excludewheretable;
    protected String _excludewheresequence;

    public String getExcludewheremview() {
        return _excludewheremview;
    }

    public void setExcludewheremview(String pExcludewheremview) {
        _excludewheremview = pExcludewheremview;
    }

    protected String _excludewheremview = ParameterDefaults.excludewheremview;
    private String excludewheregrant = ParameterDefaults.excludewheregrant;
    private String excludewheresynonym = ParameterDefaults.excludewheresynonym;
    protected String _dateformat;
    protected String _orcasDbUser;
    protected Boolean _scriptfolderrecursive;
    protected String _scriptprefix;
    protected String _scriptpostfix;
    protected String _loglevel;
    protected String _targetplsql;
    protected Boolean _sqlplustable;
    protected Boolean _orderColumnsByName;
    protected Boolean _removeDefaultValuesFromModel;
    protected String _viewExtractMode;
    protected List<String> _additionalParameters;
    protected String _logname;
    protected String _spoolfolder;
    protected FailOnErrorMode _failOnErrorMode = FailOnErrorMode.ALWAYS;
    protected String _extensionParameter;
    protected String _initializeChecksumTotal;
    protected String _initializeChecksumExtension;
    protected boolean _keepDriverClassLoadMessages = true;
    protected List<File> _modelFiles;
    protected List<File> _relevantModelFiles;
    protected List<String> _relevantTables;
    protected List<String> _relevantSequences;

    public List<String> getRelevantMviews() {
        return _relevantMviews;
    }

    public void setRelevantMviews(List<String> pRelevantMviews) {
        _relevantMviews = pRelevantMviews;
    }

    protected List<String> _relevantMviews;
    protected List<File> _schemaFiles = null;
    protected URL _scriptUrl;
    protected String _scriptUrlFilename;
    protected Charset _scriptUrlCharset;
    protected Boolean _loadExtractWithReverseExtensions = true;
    protected Boolean _multiSchema = false;
    protected Boolean _multiSchemaDbaViews = false;
    protected String _multiSchemaExcludewhereowner;
    protected boolean _additionsOnly = false;
    protected boolean _logIgnoredStatements = true;
    protected String _xmlLogFile;
    protected String _xmlInputFile;
    protected boolean _setUnusedInsteadOfDropColumn = false;
    protected boolean _createIndexOnline = false;
    protected boolean _minimizeStatementCount = false;
    protected boolean _cleanupFkValuesOnDropmode = false;
    private boolean updateEnabledStatus = ParameterDefaults.updateEnabledStatus;

    private String excludewhereview = ParameterDefaults.excludewhereview;

    public String getExcludewhereview() {
        return excludewhereview;
    }

    public void setExcludewhereview(String pExcludewhereview) {
        excludewhereview = pExcludewhereview;
    }

    public String getExcludewhereobjecttype() {
        return excludewhereobjecttype;
    }

    public void setExcludewhereobjecttype(String pExcludewhereobjecttype) {
        excludewhereobjecttype = pExcludewhereobjecttype;
    }

    public String getExcludewherepackage() {
        return excludewherepackage;
    }

    public void setExcludewherepackage(String pExcludewherepackage) {
        excludewherepackage = pExcludewherepackage;
    }

    public String getExcludewheretrigger() {
        return excludewheretrigger;
    }

    public void setExcludewheretrigger(String pExcludewheretrigger) {
        excludewheretrigger = pExcludewheretrigger;
    }

    public String getExcludewherefunction() {
        return excludewherefunction;
    }

    public void setExcludewherefunction(String pExcludewherefunction) {
        excludewherefunction = pExcludewherefunction;
    }

    public String getExcludewhereprocedure() {
        return excludewhereprocedure;
    }

    public void setExcludewhereprocedure(String pExcludewhereprocedure) {
        excludewhereprocedure = pExcludewhereprocedure;
    }

    private String excludewhereobjecttype = ParameterDefaults.excludewhereobjecttype;
    private String excludewherepackage = ParameterDefaults.excludewherepackage;
    private String excludewheretrigger = ParameterDefaults.excludewheretrigger;
    private String excludewherefunction = ParameterDefaults.excludewherefunction;
    private String excludewhereprocedure = ParameterDefaults.excludewhereprocedure;

    public ExecuteSqlErrorHandler getExecuteSqlErrorHandler() {
        return executeSqlErrorHandler;
    }

    public void setExecuteSqlErrorHandler(ExecuteSqlErrorHandler pExecuteSqlErrorHandler) {
        executeSqlErrorHandler = pExecuteSqlErrorHandler;
    }

    private ExecuteSqlErrorHandler executeSqlErrorHandler = ParameterDefaults.executeSqlErrorHandler;
    private InfoLogHandler _infoLogHandler;
    private String _removePromptPrefix;

    protected String _charsetName = StandardCharsets.UTF_8.name();
    protected String _charsetNameSqlLog = null;

    protected boolean _dbdocPlantuml = false;

    public boolean isUpdateEnabledStatus() {
        return updateEnabledStatus;
    }

    public void setUpdateEnabledStatus(boolean pUpdateEnabledStatus) {
        updateEnabledStatus = pUpdateEnabledStatus;
    }

    private AdditionalExtensionFactory _additionalExtensionFactory = new AdditionalExtensionFactory() {
        @SuppressWarnings("unchecked")
        @Override
        public <T extends EObject> List<UnaryOperator<T>> getAdditionalExtensions(Class<T> pModelClass, boolean pReverseMode) {
            return Collections.EMPTY_LIST;
        }
    };

    private ExtensionHandler extensionHandler;
    private String extensionhandlerClass = "de.opitzconsulting.orcas.diff.ExtensionHandlerImpl";

    public void setExtensionhandlerClass(String pExtensionhandlerClass) {
        extensionhandlerClass = pExtensionhandlerClass;
    }

    public String getExcludewheregrant() {
        return excludewheregrant;
    }

    public void setExcludewheregrant(String pExcludewheregrant) {
        excludewheregrant = pExcludewheregrant;
    }

    public boolean isMinimizeStatementCount() {
        return _minimizeStatementCount;
    }

    public boolean isAdditionsOnly() {
        return _additionsOnly;
    }

    public boolean isOrderColumnsByName() {
        return checkNull(_orderColumnsByName);
    }

    public boolean isRemoveDefaultValuesFromModel() {
        return checkNull(_removeDefaultValuesFromModel);
    }

    public Boolean getSqlplustable() {
        return checkNull(_sqlplustable);
    }

    public String getExcludewheresynonym() {
        return excludewheresynonym;
    }

    public void setExcludewheresynonym(String pExcludewheresynonym) {
        excludewheresynonym = pExcludewheresynonym;
    }

    public JdbcConnectParameters getSrcJdbcConnectParameters() {
        return _srcJdbcConnectParameters;
    }

    public JdbcConnectParameters getOrcasJdbcConnectParameters() {
        return checkNull(_orcasJdbcConnectParameters);
    }

    public String getTargetplsql() {
        return checkNull(_targetplsql);
    }

    public String getInitializeChecksumTotal() {
        return _initializeChecksumTotal;
    }

    public String getInitializeChecksumExtension() {
        return _initializeChecksumExtension;
    }

    public List<String> getAdditionalParameters() {
        return checkNull(_additionalParameters);
    }

    public String getViewExtractMode() {
        return checkNull(_viewExtractMode);
    }

    public boolean isViewExtractModeFull() {
        return "full".equals( getViewExtractMode() );
    }

    public boolean getScriptfolderrecursive() {
        return checkNull(_scriptfolderrecursive);
    }

    public String getScriptprefix() {
        return checkNull(_scriptprefix);
    }

    public String getLogname() {
        return checkNull(_logname);
    }

    public boolean isLognameSet() {
        return _logname != null && !getLogname().equals("");
    }

    public String getSpoolfolder() {
        return checkNull(_spoolfolder);
    }

    public boolean isSpoolfolderSet() {
        return _spoolfolder != null && !getSpoolfolder().equals("");
    }

    public String getScriptpostfix() {
        return checkNull(_scriptpostfix);
    }

    public String getOrcasDbUser() {
        return checkNull(_orcasDbUser);
    }

    private static <T> T checkNull(T pValue) {
        if (pValue == null) {
            //throw new IllegalArgumentException("Parameter not set");
        }

        return pValue;
    }

    public String getExcludewheretable() {
        return checkNull(_excludewheretable);
    }

    public String getExcludewheresequence() {
        return checkNull(_excludewheresequence);
    }

    public String getDateformat() {
        return checkNull(_dateformat);
    }

    public String getModelFile() {
        return checkNull(_modelFile);
    }

    public String getSpoolfile() {
        return checkNull(_spoolfile);
    }

    public JdbcConnectParameters getJdbcConnectParameters() {
        return _jdbcConnectParameters;
    }

    public MultiSchemaConnectionManager getMultiSchemaConnectionManager() {
        return _multiSchemaConnectionManager;
    }

    public boolean isLogonly() {
        return checkNull(_logonly);
    }

    public boolean isDropmode() {
        return checkNull(_dropmode);
    }

    public boolean isCleanupFkValuesOnDropmode() {
        return isDropmode() && _cleanupFkValuesOnDropmode;
    }

    public boolean isIndexparallelcreate() {
        return checkNull(_indexparallelcreate);
    }

    public boolean isIndexmovetablespace() {
        return checkNull(_indexmovetablespace);
    }

    public boolean isTablemovetablespace() {
        return checkNull(_tablemovetablespace);
    }

    public boolean isCreatemissingfkindexes() {
        return checkNull(_createmissingfkindexes);
    }

    public FailOnErrorMode getFailOnErrorMode() {
        return checkNull(_failOnErrorMode);
    }

    public boolean isOneTimeScriptMode() {
        return checkNull(_isOneTimeScriptMode);
    }

    public boolean isOneTimeScriptLogonlyMode() {
        return checkNull(_isOneTimeScriptLogonlyMode);
    }

    public String getExtensionParameter() {
        return checkNull(_extensionParameter);
    }

    public String getloglevel() {
        return checkNull(_loglevel);
    }

    public URL getScriptUrl() {
        return _scriptUrl;
    }

    public Charset getScriptUrlCharset() {
        return _scriptUrlCharset;
    }

    public String getScriptUrlFilename() {
        return _scriptUrlFilename;
    }

    public interface InfoLogHandler {
        void logInfo(String pLogMessage);
    }

    public InfoLogHandler getInfoLogHandler() {
        return _infoLogHandler;
    }

    public void setInfoLogHandler(InfoLogHandler pInfoLogHandler) {
        _infoLogHandler = pInfoLogHandler;
    }

    public boolean isLoadExtractWithReverseExtensions() {
        return checkNull(_loadExtractWithReverseExtensions);
    }

    public String getRemovePromptPrefix() {
        return _removePromptPrefix;
    }

    public void setRemovePromptPrefix(String pRemovePromptPrefix) {
        _removePromptPrefix = pRemovePromptPrefix;
    }

    public boolean isKeepDriverClassLoadMessages() {
        return _keepDriverClassLoadMessages;
    }

    public List<File> getModelFiles() {
        return _modelFiles;
    }

    public ExtensionHandler getExtensionHandler() {
        if (extensionHandler == null) {
            try {
                Class<?> lExtensionHandlerImplClass = Thread.currentThread().getContextClassLoader().loadClass(extensionhandlerClass);
                setExtensionHandler((ExtensionHandler) lExtensionHandlerImplClass.newInstance());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return extensionHandler;
    }

    public void setExtensionHandler(ExtensionHandler pExtensionHandler) {
        extensionHandler = pExtensionHandler;
        extensionHandler.setParameters(this);
    }

    public abstract boolean isAbortJvmOnExit();

    public Boolean getMultiSchema() {
        return checkNull(_multiSchema);
    }

    public String getMultiSchemaExcludewhereowner() {
        return _multiSchemaExcludewhereowner;
    }

    public Boolean getMultiSchemaDbaViews() {
        return checkNull(_multiSchemaDbaViews);
    }

    public AdditionalExtensionFactory getAdditionalOrcasExtensionFactory() {
        return _additionalExtensionFactory;
    }

    public void setAdditionalOrcasExtensionFactory(AdditionalExtensionFactory pAdditionalExtensionFactory) {
        _additionalExtensionFactory = pAdditionalExtensionFactory;
    }

    public interface AdditionalExtensionFactory {
        <T extends EObject> List<UnaryOperator<T>> getAdditionalExtensions(Class<T> pModelClass, boolean pReverseMode);
    }

    public boolean isLogIgnoredStatements() {
        return _logIgnoredStatements;
    }

    public String getXmlLogFile() {
        return _xmlLogFile;
    }

    public String getXmlInputFile() {
        return _xmlInputFile;
    }

    public boolean isSetUnusedInsteadOfDropColumn() {
        return _setUnusedInsteadOfDropColumn;
    }

    public boolean isCreateIndexOnline() {
        return _createIndexOnline;
    }

    public Charset getEncoding() {
        return Charset.forName(_charsetName);
    }

    public Charset getEncodingForSqlLog() {
        return _charsetNameSqlLog == null ? getEncoding() : Charset.forName(_charsetNameSqlLog);
    }

    public boolean getDbdocPlantuml() {
        return _dbdocPlantuml;
    }

    public List<File> getSchemaFiles() {
        return _schemaFiles;
    }

    public List<File> getRelevantModelFiles() {
        return _relevantModelFiles;
    }

    public List<String> getRelevantTables() {
        return _relevantTables;
    }

    public List<String> getRelevantSequences() {
        return _relevantSequences;
    }

    public void setRelevantTables(List<String> pRelevantTables) {
        _relevantTables = pRelevantTables;
    }

    public void setRelevantSequences(List<String> pRelevantSequences) {
        _relevantSequences = pRelevantSequences;
    }
}
