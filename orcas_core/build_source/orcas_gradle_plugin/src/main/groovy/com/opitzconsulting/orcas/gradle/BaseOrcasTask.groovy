package com.opitzconsulting.orcas.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import de.opitzconsulting.orcas.diff.Parameters.FailOnErrorMode;
import de.opitzconsulting.orcas.diff.ParametersCall;

public abstract class BaseOrcasTask extends DefaultTask 
{
  @TaskAction
  def executeOrcasTask()
  {
    ParametersCall lParametersCall = new ParametersCall();

    lParametersCall.getJdbcConnectParameters().setJdbcDriver( project.orcasconfiguration.jdbcdriver );
    lParametersCall.getJdbcConnectParameters().setJdbcUrl( project.orcasconfiguration.jdbcurl );
    lParametersCall.getJdbcConnectParameters().setJdbcUser( project.orcasconfiguration.username );
    lParametersCall.getJdbcConnectParameters().setJdbcPassword( project.orcasconfiguration.password );

    lParametersCall.setOrcasDbUser( project.orcasconfiguration.usernameorcas );

    lParametersCall.setTargetplsql( project.orcasconfiguration.targetplsql );

    lParametersCall.setScriptprefix( project.orcasconfiguration.scriptfolderPrefix );
    lParametersCall.setScriptpostfix( project.orcasconfiguration.scriptfolderPostfix );
    lParametersCall.setScriptfolderrecursive( project.orcasconfiguration.scriptfolderrecursive );

    lParametersCall.setSpoolfile( project.file(project.orcasconfiguration.spoolfile).toString() );
    lParametersCall.setSpoolfolder( project.file(project.orcasconfiguration.spoolfolder).toString() );
    lParametersCall.setLogname( getLogname() );
    lParametersCall.setLoglevel( project.orcasconfiguration.loglevel );

    lParametersCall.setFailOnErrorMode( project.orcasconfiguration.failOnErrorMode );

    lParametersCall.setLogonly( project.orcasconfiguration.logonly );
    lParametersCall.setDropmode( project.orcasconfiguration.dropmode );
    lParametersCall.setIndexparallelcreate( project.orcasconfiguration.indexparallelcreate );
    lParametersCall.setIndexmovetablespace( project.orcasconfiguration.indexmovetablespace );
    lParametersCall.setTablemovetablespace( project.orcasconfiguration.tablemovetablespace );
    lParametersCall.setCreatemissingfkindexes( project.orcasconfiguration.createmissingfkindexes );
    lParametersCall.setExcludewheretable( project.orcasconfiguration.excludewheretable );
    lParametersCall.setExcludewheresequence( project.orcasconfiguration.excludewheresequence );
    lParametersCall.setDateformat( project.orcasconfiguration.dateformat );
    lParametersCall.setExtensionParameter( project.orcasconfiguration.extensionparameter );

    executeOrcasTaskWithParameters( lParametersCall );
  }

  protected abstract String getLogname();

  protected abstract void executeOrcasTaskWithParameters( ParametersCall pParameters );
}
