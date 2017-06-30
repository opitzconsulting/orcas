package de.opitzconsulting.orcas.diff;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class ParametersCall extends Parameters
{
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

  public void setScriptUrl( URL pScriptUrl, String pFilename )
  {
    _scriptUrl = pScriptUrl;
    _scriptUrlFilename = pFilename;
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
}
