package com.opitzconsulting.orcas.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import de.opitzconsulting.orcas.diff.Parameters.FailOnErrorMode;
import de.opitzconsulting.orcas.diff.ParametersCall;

public abstract class BaseOrcasMojo extends AbstractMojo
{
  @Parameter( defaultValue = "oracle.jdbc.OracleDriver" )
  private String jdbcdriver;

  @Parameter
  private String jdbcurl;

  @Parameter
  private String username;

  @Parameter
  private String password;

  @Parameter( defaultValue = ".sql" )
  private String scriptfolderPostfix;

  @Parameter( defaultValue = "" )
  private String scriptfolderPrefix;

  @Parameter( defaultValue = "true" )
  private boolean scriptfolderrecursive;

  @Parameter( defaultValue = "target/orcas_spoolfile.sql" )
  private File spoolfile;

  @Parameter( defaultValue = "target/log/" )
  private File spoolfolder;

  @Parameter( defaultValue = "info" )
  private String loglevel;

  @Parameter( defaultValue = "ALWAYS" )
  private FailOnErrorMode failOnErrorMode;

  @Parameter( defaultValue = "" )
  private String usernameorcas;

  @Parameter( defaultValue = "false" )
  private boolean logonly;

  @Parameter( defaultValue = "false" )
  private boolean dropmode;

  @Parameter( defaultValue = "true" )
  private boolean indexparallelcreate;

  @Parameter( defaultValue = "true" )
  private boolean indexmovetablespace;

  @Parameter( defaultValue = "true" )
  private boolean tablemovetablespace;

  @Parameter( defaultValue = "true" )
  private boolean createmissingfkindexes;

  @Parameter( defaultValue = "object_name like '%$%'" )
  private String excludewheretable;

  @Parameter( defaultValue = "object_name like '%$%'" )
  private String excludewheresequence;

  @Parameter( defaultValue = "dd.mm.yy" )
  private String dateformat;

  @Parameter( defaultValue = "" )
  private String extensionparameter;

  @Parameter( defaultValue = "" )
  private String targetplsql;

  @Parameter( defaultValue = "src/main/sql/replaceables" )
  protected File replaceablesfolder;
  
  @Parameter( defaultValue = "src/main/sql/statics" )
  protected File staticsfolder;

  public void execute() throws MojoExecutionException
  {
    ParametersCall lParametersCall = new ParametersCall();

    lParametersCall.getJdbcConnectParameters().setJdbcDriver( jdbcdriver );
    lParametersCall.getJdbcConnectParameters().setJdbcUrl( jdbcurl );
    lParametersCall.getJdbcConnectParameters().setJdbcUser( username );
    lParametersCall.getJdbcConnectParameters().setJdbcPassword( password );

    lParametersCall.setOrcasDbUser( usernameorcas );

    lParametersCall.setTargetplsql( targetplsql );

    lParametersCall.setScriptprefix( scriptfolderPrefix );
    lParametersCall.setScriptpostfix( scriptfolderPostfix );
    lParametersCall.setScriptfolderrecursive( scriptfolderrecursive );

    lParametersCall.setSpoolfile( spoolfile.toString() );
    lParametersCall.setSpoolfolder( spoolfolder.toString() );
    lParametersCall.setLogname( getLogname() );
    lParametersCall.setLoglevel( loglevel );

    lParametersCall.setFailOnErrorMode( failOnErrorMode );

    lParametersCall.setLogonly( logonly );
    lParametersCall.setDropmode( dropmode );
    lParametersCall.setIndexparallelcreate( indexparallelcreate );
    lParametersCall.setIndexmovetablespace( indexmovetablespace );
    lParametersCall.setTablemovetablespace( tablemovetablespace );
    lParametersCall.setCreatemissingfkindexes( createmissingfkindexes );
    lParametersCall.setExcludewheretable( excludewheretable );
    lParametersCall.setExcludewheresequence( excludewheresequence );
    lParametersCall.setDateformat( dateformat );
    lParametersCall.setExtensionParameter( extensionparameter );

    executeWithParameters( lParametersCall );
  }

  protected abstract String getLogname();

  protected abstract void executeWithParameters( ParametersCall pParameters );
}
