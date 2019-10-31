package com.opitzconsulting.orcas.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.logging.LogLevel

import de.opitzconsulting.orcas.diff.Parameters.FailOnErrorMode;
import de.opitzconsulting.orcas.diff.ExecuteSqlErrorHandler;
import de.opitzconsulting.orcas.diff.ParametersCall;
import de.opitzconsulting.orcas.diff.Parameters.InfoLogHandler;
import de.opitzconsulting.orcas.diff.Parameters.JdbcConnectParameters;

public abstract class BaseOrcasTask extends DefaultTask 
{
  FailOnErrorMode failOnErrorMode
  ExecuteSqlErrorHandler executeSqlErrorHandler
  private def parameterModifier;
  def boolean nologging;

  @TaskAction
  def executeOrcasTask()
  {
    ParametersCall lParametersCall = new ParametersCall();

    lParametersCall.getJdbcConnectParameters().setJdbcDriver( project.orcasconfiguration.jdbcdriver );
    lParametersCall.getJdbcConnectParameters().setJdbcUrl( project.orcasconfiguration.jdbcurl );
    lParametersCall.getJdbcConnectParameters().setJdbcUser( project.orcasconfiguration.username );
    lParametersCall.getJdbcConnectParameters().setJdbcPassword( project.orcasconfiguration.password );

    lParametersCall.setTargetplsql( project.orcasconfiguration.targetplsql );

    lParametersCall.setScriptprefix( project.orcasconfiguration.scriptfolderPrefix );
    lParametersCall.setScriptpostfix( project.orcasconfiguration.scriptfolderPostfix );
    lParametersCall.setScriptfolderrecursive( project.orcasconfiguration.scriptfolderrecursive );

    lParametersCall.setSpoolfile( convertOutFile(project.orcasconfiguration.spoolfile) );
    lParametersCall.setSpoolfolder( convertOutFile(project.orcasconfiguration.spoolfolder) );
    lParametersCall.setLogname( getLogname() );
    lParametersCall.setLoglevel( project.orcasconfiguration.loglevel );

    lParametersCall.setFailOnErrorMode( failOnErrorMode != null ? failOnErrorMode : project.orcasconfiguration.failOnErrorMode );
    lParametersCall.setExecuteSqlErrorHandler( executeSqlErrorHandler != null ? executeSqlErrorHandler : project.orcasconfiguration.executeSqlErrorHandler );

    lParametersCall.setLogCompileErrors( project.orcasconfiguration.logCompileErrors );

    lParametersCall.setExcludewhereview( project.orcasconfiguration.excludewhereview );
    lParametersCall.setExcludewhereobjecttype( project.orcasconfiguration.excludewhereobjecttype );
    lParametersCall.setExcludewherepackage( project.orcasconfiguration.excludewherepackage );
    lParametersCall.setExcludewheretrigger( project.orcasconfiguration.excludewheretrigger );
    lParametersCall.setExcludewherefunction( project.orcasconfiguration.excludewherefunction );
    lParametersCall.setExcludewhereprocedure( project.orcasconfiguration.excludewhereprocedure );

    lParametersCall.setLogonly( project.orcasconfiguration.logonly );
    lParametersCall.setDropmode( project.orcasconfiguration.dropmode );
    lParametersCall.setIndexparallelcreate( project.orcasconfiguration.indexparallelcreate );
    lParametersCall.setIndexmovetablespace( project.orcasconfiguration.indexmovetablespace );
    lParametersCall.setTablemovetablespace( project.orcasconfiguration.tablemovetablespace );
    lParametersCall.setMviewlogmovetablespace( project.orcasconfiguration.mviewlogmovetablespace );
    lParametersCall.setCreatemissingfkindexes( project.orcasconfiguration.createmissingfkindexes );
    lParametersCall.setExcludewheretable( project.orcasconfiguration.excludewheretable );
    lParametersCall.setExcludewheresequence( project.orcasconfiguration.excludewheresequence );
    lParametersCall.setExcludewheremview( project.orcasconfiguration.excludewheremview );
    lParametersCall.setDateformat( project.orcasconfiguration.dateformat );
    lParametersCall.setExtensionParameter( project.orcasconfiguration.extensionparameter );
    lParametersCall.setAdditionsOnly( project.orcasconfiguration.additionsonly );
    lParametersCall.setLogIgnoredStatements( project.orcasconfiguration.logignoredstatements );
    lParametersCall.setXmlLogFile( convertOutFile( project.orcasconfiguration.xmllogfile ) );
    lParametersCall.setXmlInputFile( project.orcasconfiguration.xmlinputfile );
    lParametersCall.setSetUnusedInsteadOfDropColumn( project.orcasconfiguration.setunusedinsteadofdropcolumn );
    lParametersCall.setCreateIndexOnline( project.orcasconfiguration.indexonlinecreate );
    lParametersCall.setMinimizeStatementCount( project.orcasconfiguration.minimizestatementcount );
    lParametersCall.setCharsetName( project.orcasconfiguration.charsetname );
    lParametersCall.setCharsetNameSqlLog( project.orcasconfiguration.charsetnamesqllog );
    lParametersCall.setMultiSchema( project.orcasconfiguration.multischema );
    lParametersCall.setMultiSchemaDbaViews( project.orcasconfiguration.multischemadbaviews );
    lParametersCall.setMultiSchemaExcludewhereowner( project.orcasconfiguration.multischemaexcludewhereowner );
    lParametersCall.setViewExtractMode( project.orcasconfiguration.viewextractmode );
    if (project.orcasconfiguration.isExtractViewCommnets == true) {
      lParametersCall.setExtractViewCommnets(true);
    } else {
      lParametersCall.setExtractViewCommnets(project.orcasconfiguration.isExtractViewCommnents);
    }

    lParametersCall.setUpdateEnabledStatus( project.orcasconfiguration.updateEnabledStatus );

    lParametersCall.setDbdocPlantuml( project.orcasconfiguration.dbdocPlantuml );

    if( project.orcasconfiguration.extensionHandler != null )
    {
      lParametersCall.setExtensionHandler( project.orcasconfiguration.extensionHandler );
    }

    if( !project.orcasconfiguration.usernameorcas.equals( "" ) )
    {
      lParametersCall.setOrcasDbUser( project.orcasconfiguration.usernameorcas );
    }
    else
    {
      if( project.orcasconfiguration.orcasusername != null )
      {
        lParametersCall.setOrcasDbUser( project.orcasconfiguration.orcasusername );
      }
      else
      {
        lParametersCall.setOrcasDbUser( project.orcasconfiguration.username );
      }
    }

    if( project.orcasconfiguration.orcasusername != null )
    {
      JdbcConnectParameters lOrcasJdbcConnectParameters = new JdbcConnectParameters();
      lOrcasJdbcConnectParameters.setJdbcDriver( project.orcasconfiguration.orcasjdbcdriver );
      lOrcasJdbcConnectParameters.setJdbcUrl( project.orcasconfiguration.orcasjdbcurl == null ? pParameters.getJdbcConnectParameters().getJdbcUrl() : project.orcasconfiguration.orcasjdbcurl );
      lOrcasJdbcConnectParameters.setJdbcUser( project.orcasconfiguration.orcasusername );
      lOrcasJdbcConnectParameters.setJdbcPassword( project.orcasconfiguration.orcaspassword );
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

  protected boolean isMariadb()
  {
    return project.orcasconfiguration.jdbcurl.startsWith( "jdbc:mysql" ) || project.orcasconfiguration.jdbcurl.startsWith( "jdbc:mariadb" );
  }

  protected void logInfo( String pLogMessage )
  {
    LogLevel lLogLevel = nologging ? LogLevel.QUIET : LogLevel.ERROR;
    logger.log( lLogLevel, getLogname() + ": " + pLogMessage );
  }

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
