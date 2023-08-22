package com.opitzconsulting.orcas.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.logging.LogLevel

import de.opitzconsulting.orcas.diff.Parameters.FailOnErrorMode;
import de.opitzconsulting.orcas.diff.ExecuteSqlErrorHandler;
import de.opitzconsulting.orcas.diff.ParametersCall;
import de.opitzconsulting.orcas.diff.Parameters.InfoLogHandler;
import de.opitzconsulting.orcas.diff.Parameters.JdbcConnectParameters;

public abstract class BaseOrcasTask extends DefaultTask 
{
  @Internal
  FailOnErrorMode failOnErrorMode
  @Internal
  ExecuteSqlErrorHandler executeSqlErrorHandler
  private def parameterModifier;
  @Internal
  def boolean nologging;

  BaseOrcasTask() {
    outputs.upToDateWhen { false }
  }

  @TaskAction
  def executeOrcasTask()
  {
    ParametersCall lParametersCall = new ParametersCall();

    OrcasGradlePluginExtension orcasconfiguration = project.orcasconfiguration
    lParametersCall.getJdbcConnectParameters().setJdbcDriver( orcasconfiguration.jdbcdriver );
    lParametersCall.getJdbcConnectParameters().setJdbcUrl( orcasconfiguration.jdbcurl );
    lParametersCall.getJdbcConnectParameters().setJdbcUser( orcasconfiguration.username );
    lParametersCall.getJdbcConnectParameters().setJdbcPassword( orcasconfiguration.password );

    lParametersCall.setTargetplsql( orcasconfiguration.targetplsql );

    lParametersCall.setScriptprefix( orcasconfiguration.scriptfolderPrefix );
    lParametersCall.setScriptpostfix( orcasconfiguration.scriptfolderPostfix );
    lParametersCall.setScriptfolderrecursive( orcasconfiguration.scriptfolderrecursive );

    lParametersCall.setSpoolfile( convertOutFile(orcasconfiguration.spoolfile) );
    lParametersCall.setSpoolfolder( convertOutFile(orcasconfiguration.spoolfolder) );
    lParametersCall.setLogname( getLogname() );
    lParametersCall.setLoglevel( orcasconfiguration.loglevel );

    lParametersCall.setFailOnErrorMode( failOnErrorMode != null ? failOnErrorMode : orcasconfiguration.failOnErrorMode );
    lParametersCall.setExecuteSqlErrorHandler( executeSqlErrorHandler != null ? executeSqlErrorHandler : orcasconfiguration.executeSqlErrorHandler );

    lParametersCall.setLogCompileErrors( orcasconfiguration.logCompileErrors );

    lParametersCall.setExcludewhereview( orcasconfiguration.excludewhereview );
    lParametersCall.setExcludewhereobjecttype( orcasconfiguration.excludewhereobjecttype );
    lParametersCall.setExcludewherepackage( orcasconfiguration.excludewherepackage );
    lParametersCall.setExcludewheretrigger( orcasconfiguration.excludewheretrigger );
    lParametersCall.setExcludewherefunction( orcasconfiguration.excludewherefunction );
    lParametersCall.setExcludewhereprocedure( orcasconfiguration.excludewhereprocedure );

    lParametersCall.setLogonly( orcasconfiguration.logonly );
    lParametersCall.setDropmode( orcasconfiguration.dropmode );
    lParametersCall.setIndexparallelcreate( orcasconfiguration.indexparallelcreate );
    lParametersCall.setIndexmovetablespace( orcasconfiguration.indexmovetablespace );
    lParametersCall.setTablemovetablespace( orcasconfiguration.tablemovetablespace );
    lParametersCall.setMviewlogmovetablespace( orcasconfiguration.mviewlogmovetablespace );
    lParametersCall.setCreatemissingfkindexes( orcasconfiguration.createmissingfkindexes );
    lParametersCall.setExcludewheretable( orcasconfiguration.excludewheretable );
    lParametersCall.setExcludewheresequence( orcasconfiguration.excludewheresequence );
    lParametersCall.setExcludewheremview( orcasconfiguration.excludewheremview );
    lParametersCall.setDateformat( orcasconfiguration.dateformat );
    lParametersCall.setExtensionParameter( orcasconfiguration.extensionparameter );
    lParametersCall.setAdditionsOnly( orcasconfiguration.additionsonly );
    lParametersCall.setLogIgnoredStatements( orcasconfiguration.logignoredstatements );
    lParametersCall.setXmlLogFile( convertOutFile( orcasconfiguration.xmllogfile ) );
    lParametersCall.setXmlInputFile( orcasconfiguration.xmlinputfile );
    lParametersCall.setSetUnusedInsteadOfDropColumn( orcasconfiguration.setunusedinsteadofdropcolumn );
    lParametersCall.setCreateIndexOnline( orcasconfiguration.indexonlinecreate );
    lParametersCall.setMinimizeStatementCount( orcasconfiguration.minimizestatementcount );
    lParametersCall.setCharsetName( orcasconfiguration.charsetname );
    lParametersCall.setCharsetNameSqlLog( orcasconfiguration.charsetnamesqllog );
    lParametersCall.setMultiSchema( orcasconfiguration.multischema );
    lParametersCall.setMultiSchemaDbaViews( orcasconfiguration.multischemadbaviews );
    lParametersCall.setMultiSchemaExcludewhereowner( orcasconfiguration.multischemaexcludewhereowner );
    lParametersCall.setViewExtractMode( orcasconfiguration.viewextractmode );
    lParametersCall.setIsOneTimeScriptLogonlyMode( orcasconfiguration.isOneTimeScriptLogonlyMode );
    orcasconfiguration.extensions.forEach{lParametersCall.addExtension(it)};
    orcasconfiguration.reverseExtensions.forEach{lParametersCall.addReverseExtension(it)};
    if (orcasconfiguration.isExtractViewCommnets == true) {
      lParametersCall.setExtractViewCommnets(true);
    } else {
      lParametersCall.setExtractViewCommnets(orcasconfiguration.isExtractViewCommnents);
    }

    lParametersCall.setUpdateEnabledStatus( orcasconfiguration.updateEnabledStatus );

    lParametersCall.setDbdocPlantuml( orcasconfiguration.dbdocPlantuml );

    if( orcasconfiguration.extensionHandler != null )
    {
      lParametersCall.setExtensionHandler( orcasconfiguration.extensionHandler );
    }

    if( !orcasconfiguration.usernameorcas.equals( "" ) )
    {
      lParametersCall.setOrcasDbUser( orcasconfiguration.usernameorcas );
    }
    else
    {
      if( orcasconfiguration.orcasusername != null )
      {
        lParametersCall.setOrcasDbUser( orcasconfiguration.orcasusername );
      }
      else
      {
        lParametersCall.setOrcasDbUser( orcasconfiguration.username );
      }
    }

    if( orcasconfiguration.orcasusername != null )
    {
      JdbcConnectParameters lOrcasJdbcConnectParameters = new JdbcConnectParameters();
      lOrcasJdbcConnectParameters.setJdbcDriver( orcasconfiguration.orcasjdbcdriver );
      lOrcasJdbcConnectParameters.setJdbcUrl( orcasconfiguration.orcasjdbcurl == null ? pParameters.getJdbcConnectParameters().getJdbcUrl() : orcasconfiguration.orcasjdbcurl );
      lOrcasJdbcConnectParameters.setJdbcUser( orcasconfiguration.orcasusername );
      lOrcasJdbcConnectParameters.setJdbcPassword( orcasconfiguration.orcaspassword );
      lParametersCall.setOrcasJdbcConnectParameters( lOrcasJdbcConnectParameters );
    }
    else
    {
      lParametersCall.setOrcasJdbcConnectParameters( lParametersCall.getJdbcConnectParameters() );
    }

    nologging = "nologging".equals( lParametersCall.getloglevel() );

    lParametersCall.setInfoLogHandler(
      new InfoLogHandler() 
      {
        void logInfo( String pLogMessage )
        {
          BaseOrcasTask.this.logInfo( pLogMessage );
        }
      }
    );
  
    logger.log( nologging ? LogLevel.QUIET : LogLevel.ERROR, "" );

    executeOrcasTaskWithParameters( lParametersCall );
  }

  @Internal
  protected boolean isMariadb()
  {
    return project.orcasconfiguration.jdbcurl.startsWith( "jdbc:mysql" ) || project.orcasconfiguration.jdbcurl.startsWith( "jdbc:mariadb" );
  }

  protected void logInfo( String pLogMessage )
  {
    LogLevel lLogLevel = nologging ? LogLevel.QUIET : LogLevel.ERROR;
    logger.log( lLogLevel, getLogname() + ": " + pLogMessage );
  }

  @Internal
  protected abstract String getLogname();

  protected abstract void executeOrcasTaskWithParameters( ParametersCall pParameters );

  protected void parameters( def pParameterModifier )
  {
    parameterModifier = pParameterModifier;
  }

  protected String convertOutFile( def pFile )
  {
    if( pFile == null )
    {
      return null;
    }

    return project.buildDir.toString() + "/" + pFile;
  }

  protected ParametersCall modifyParameters( ParametersCall pParameters )
  {
    if( parameterModifier != null )
    {
      parameterModifier.setDelegate( pParameters );
      parameterModifier();
    }

    return pParameters;
  }
}
