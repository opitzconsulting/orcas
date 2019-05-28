package de.opitzconsulting.orcas.diff;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

public class ParametersCall extends Parameters
{
  public void setCleanupFkValuesOnDropmode( boolean pIsCleanupFkValuesOnDropmode )
  {
    _cleanupFkValuesOnDropmode = pIsCleanupFkValuesOnDropmode;
  }

  public void setMinimizeStatementCount( boolean pMinimizeStatementCount )
  {
    _minimizeStatementCount = pMinimizeStatementCount;
  }

  public void setAdditionsOnly( boolean pAdditionsOnly )
  {
    _additionsOnly = pAdditionsOnly;
  }

  public void setSrcJdbcConnectParameters( JdbcConnectParameters pSrcJdbcConnectParameters )
  {
    _srcJdbcConnectParameters = pSrcJdbcConnectParameters;
  }

  public void setOrcasJdbcConnectParameters( JdbcConnectParameters pOrcasJdbcConnectParameters )
  {
    _orcasJdbcConnectParameters = pOrcasJdbcConnectParameters;
  }

  private String convertNullString( String pValue )
  {
    return pValue == null ? "" : pValue;
  }

  public void setLogonly( Boolean pLogonly )
  {
    _logonly = pLogonly;
  }

  public void setDropmode( Boolean pDropmode )
  {
    _dropmode = pDropmode;
  }

  public void setIndexparallelcreate( Boolean pIndexparallelcreate )
  {
    _indexparallelcreate = pIndexparallelcreate;
  }

  public void setIndexmovetablespace( Boolean pIndexmovetablespace )
  {
    _indexmovetablespace = pIndexmovetablespace;
  }

  public void setTablemovetablespace( Boolean pTablemovetablespace )
  {
    _tablemovetablespace = pTablemovetablespace;
  }

  public void setCreatemissingfkindexes( Boolean pCreatemissingfkindexes )
  {
    _createmissingfkindexes = pCreatemissingfkindexes;
  }

  public void setIsOneTimeScriptMode( Boolean pIsOneTimeScriptMode )
  {
    _isOneTimeScriptMode = pIsOneTimeScriptMode;
  }

  public void setIsOneTimeScriptLogonlyMode( Boolean pIsOneTimeScriptLogonlyMode )
  {
    _isOneTimeScriptLogonlyMode = pIsOneTimeScriptLogonlyMode;
  }

  public void setModelFile( String pModelFile )
  {
    _modelFile = convertNullString( pModelFile );
  }

  public void setSpoolfile( String pSpoolfile )
  {
    _spoolfile = convertNullString( pSpoolfile );
  }

  public void setExcludewheretable( String pExcludewheretable )
  {
    _excludewheretable = convertNullString( pExcludewheretable );
  }

  public void setCharsetName( String pCharsetName )
  {
    _charsetName = pCharsetName;
  }

  public void setCharsetNameSqlLog( String pCharsetNameSqlLog )
  {
    _charsetNameSqlLog = pCharsetNameSqlLog;
  }

  public void setExcludewheresequence( String pExcludewheresequence )
  {
    _excludewheresequence = convertNullString( pExcludewheresequence );
  }

  public void setDateformat( String pDateformat )
  {
    _dateformat = convertNullString( pDateformat );
  }

  public void setOrcasDbUser( String pOrcasDbUser )
  {
    _orcasDbUser = convertNullString( pOrcasDbUser );
  }

  public void setScriptfolderrecursive( Boolean pScriptfolderrecursive )
  {
    _scriptfolderrecursive = pScriptfolderrecursive;
  }

  public void setScriptprefix( String pScriptprefix )
  {
    _scriptprefix = convertNullString( pScriptprefix );
  }

  public void setScriptpostfix( String pScriptpostfix )
  {
    _scriptpostfix = convertNullString( pScriptpostfix );
  }

  public void setLoglevel( String pLoglevel )
  {
    _loglevel = convertNullString( pLoglevel );
  }

  public void setTargetplsql( String pTargetplsql )
  {
    _targetplsql = convertNullString( pTargetplsql );
  }

  public void setSqlplustable( Boolean pSqlplustable )
  {
    _sqlplustable = pSqlplustable;
  }

  public void setOrderColumnsByName( Boolean pOrderColumnsByName )
  {
    _orderColumnsByName = pOrderColumnsByName;
  }

  public void setRemoveDefaultValuesFromModel( Boolean pRemoveDefaultValuesFromModel )
  {
    _removeDefaultValuesFromModel = pRemoveDefaultValuesFromModel;
  }

  public void setViewExtractMode( String pViewExtractMode )
  {
    _viewExtractMode = convertNullString( pViewExtractMode );
  }

  public void setAdditionalParameters( List<String> pAdditionalParameters )
  {
    _additionalParameters = pAdditionalParameters == null ? Collections.<String>emptyList() : pAdditionalParameters;
  }

  public void setLogname( String pLogname )
  {
    _logname = convertNullString( pLogname );
  }

  public void setSpoolfolder( String pSpoolfolder )
  {
    _spoolfolder = convertNullString( pSpoolfolder );
  }

  public void setFailOnErrorMode( FailOnErrorMode pFailOnErrorMode )
  {
    _failOnErrorMode = pFailOnErrorMode;
  }

  public void setExtensionParameter( String pExtensionParameter )
  {
    _extensionParameter = convertNullString( pExtensionParameter );
  }

  public void setScriptUrl( URL pScriptUrl, String pFilename, Charset pScriptUrlCharset )
  {
    _scriptUrl = pScriptUrl;
    _scriptUrlFilename = pFilename;
    _scriptUrlCharset = pScriptUrlCharset;
  }

  public void setInitializeChecksumTotal( String pInitializeChecksumTotal )
  {
    _initializeChecksumTotal = pInitializeChecksumTotal;
  }

  public void setInitializeChecksumExtension( String pInitializeChecksumExtension )
  {
    _initializeChecksumExtension = pInitializeChecksumExtension;
  }

  public void setModelFiles( List<File> pModelFiles )
  {
    _modelFiles = pModelFiles;
  }

  public void setMultiSchema( Boolean pMultiSchema )
  {
    _multiSchema = pMultiSchema;
  }

  public void setMultiSchemaExcludewhereowner( String pMultiSchemaExcludewhereowner )
  {
    _multiSchemaExcludewhereowner = pMultiSchemaExcludewhereowner;
  }

  public void setMultiSchemaDbaViews( Boolean pMultiSchemaDbaViews )
  {
    _multiSchemaDbaViews = pMultiSchemaDbaViews;
  }

  @Override
  public boolean isAbortJvmOnExit()
  {
    return false;
  }

  public void setLogIgnoredStatements( boolean pLogIgnoredStatements )
  {
    _logIgnoredStatements = pLogIgnoredStatements;
  }

  public void setXmlLogFile( String pXmlLogFile )
  {
    _xmlLogFile = pXmlLogFile;
  }

  public void setXmlInputFile( String pXmlInputFile )
  {
    _xmlInputFile = pXmlInputFile;
  }

  public void setSetUnusedInsteadOfDropColumn( boolean pSetUnusedInsteadOfDropColumn )
  {
    _setUnusedInsteadOfDropColumn = pSetUnusedInsteadOfDropColumn;
  }

  public void setCreateIndexOnline( boolean pCreateIndexOnline )
  {
    _createIndexOnline = pCreateIndexOnline;
  }


  public void setDbdocPlantuml( boolean pDbdocPlantuml )
  {
    _dbdocPlantuml = pDbdocPlantuml;
  }

  public static ParametersCall createWithDefaults()
  {
    ParametersCall lReturn = new ParametersCall();

    lReturn.getJdbcConnectParameters().setJdbcDriver(ParameterDefaults.jdbcdriver);
    lReturn.setScriptpostfix(ParameterDefaults.scriptfolderPostfix);
    lReturn.setScriptprefix(ParameterDefaults.scriptfolderPrefix);
    lReturn.setScriptfolderrecursive(ParameterDefaults.scriptfolderrecursive);
    lReturn.setSpoolfile(ParameterDefaults.spoolfile);
    lReturn.setSpoolfolder(ParameterDefaults.spoolfolder);
    lReturn.setLoglevel(ParameterDefaults.loglevel);
    lReturn.setFailOnErrorMode(ParameterDefaults.failOnErrorMode);
    lReturn.setOrcasDbUser(ParameterDefaults.usernameorcas);
    lReturn.setLogonly(ParameterDefaults.logonly);
    lReturn.setDropmode(ParameterDefaults.dropmode);
    lReturn.setIndexparallelcreate(ParameterDefaults.indexparallelcreate);
    lReturn.setIndexmovetablespace(ParameterDefaults.indexmovetablespace);
    lReturn.setTablemovetablespace(ParameterDefaults.tablemovetablespace);
    lReturn.setCreatemissingfkindexes(ParameterDefaults.createmissingfkindexes);
    lReturn.setExcludewheretable(ParameterDefaults.excludewheretable);
    lReturn.setExcludewheresequence(ParameterDefaults.excludewheresequence);
    lReturn.setDateformat(ParameterDefaults.dateformat);
    lReturn.setExtensionParameter(ParameterDefaults.extensionparameter);
    lReturn.setTargetplsql(ParameterDefaults.targetplsql);

    lReturn.setAdditionsOnly(ParameterDefaults.additionsonly);
    lReturn.setLogIgnoredStatements(ParameterDefaults.logignoredstatements);
    lReturn.setXmlLogFile(ParameterDefaults.xmllogfile);
    lReturn.setSetUnusedInsteadOfDropColumn(ParameterDefaults.setunusedinsteadofdropcolumn);
    lReturn.setCreateIndexOnline(ParameterDefaults.indexonlinecreate);
    lReturn.setMinimizeStatementCount(ParameterDefaults.minimizestatementcount);
    lReturn.setCharsetName(ParameterDefaults.charsetname);
    lReturn.setCharsetNameSqlLog(ParameterDefaults.charsetnamesqllog);

    lReturn.setRemoveDefaultValuesFromModel(ParameterDefaults.extractremovedefaultvaluesfrommodel);
    lReturn.setViewExtractMode(ParameterDefaults.viewextractmode);

    lReturn.setSqlplustable(ParameterDefaults.sqlplustable);
    lReturn.setOrderColumnsByName(ParameterDefaults.orderColumnsByName);

    lReturn.setOrcasJdbcConnectParameters(lReturn.getJdbcConnectParameters());

    lReturn.setDbdocPlantuml(ParameterDefaults.dbdocPlantuml);

    return lReturn;
  }
}
