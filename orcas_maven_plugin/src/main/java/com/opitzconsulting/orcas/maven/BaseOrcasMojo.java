package com.opitzconsulting.orcas.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import de.opitzconsulting.orcas.diff.Parameters.FailOnErrorMode;
import de.opitzconsulting.orcas.diff.ParametersCall;
import de.opitzconsulting.orcas.diff.ParametersCommandline;
import de.opitzconsulting.orcas.diff.Parameters.JdbcConnectParameters;

public abstract class BaseOrcasMojo extends AbstractMojo
{
  /**
   * The JDBC-Driver to use. Always leave as default!
   */
  @Parameter( defaultValue = "oracle.jdbc.OracleDriver" )
  private String jdbcdriver;

  /**
   * The JDBC-URL to the database.
   */
  @Parameter
  private String jdbcurl;

  /**
   * The username of the database-schema.
   */
  @Parameter
  private String username;

  /**
   * The password of the database-schema.
   */
  @Parameter
  private String password;

  /**
   * Default filename-ending for script files.
   */
  @Parameter( defaultValue = ".sql" )
  private String scriptfolderPostfix;

  /**
   * Default filename-start for script files.
   */
  @Parameter( defaultValue = "" )
  private String scriptfolderPrefix;

  /**
   * Include subfolders of scriptfolder.
   */
  @Parameter( defaultValue = "true" )
  private boolean scriptfolderrecursive;

  @Parameter( defaultValue = "target/orcas_spoolfile.sql" )
  private File spoolfile;

  /**
   * The base-folder for spooling.
   */
  @Parameter( defaultValue = "target/log/" )
  private File spoolfolder;

  /**
   * The logleve. Possible values: info, error, debug.
   */
  @Parameter( defaultValue = "info" )
  private String loglevel;

  /**
   * How to handle errors while exceuting scripts: NEVER: do nothing. ALWAYS: fail. IGNORE_DROP: fail, but ignore failures of drop statements.
   */
  @Parameter( defaultValue = "ALWAYS" )
  private FailOnErrorMode failOnErrorMode;

  /**
   * The schema-name of the orcas user. Only needed if orcas should be installed into the database.
   */
  @Parameter( defaultValue = "" )
  private String usernameorcas;

  /**
   * If true statics updates only prduces log results.
   */
  @Parameter( defaultValue = "false" )
  private boolean logonly;

  /**
   * If true drop table and drop column-statements are always executed. If false they are only executed if the column or table is empty.
   */
  @Parameter( defaultValue = "false" )
  private boolean dropmode;

  /**
   * If true indexes are created in parallel (they are afterward set to noparalle if specified as noparallel).
   */
  @Parameter( defaultValue = "true" )
  private boolean indexparallelcreate;

  /**
   * If true tablespaces of indexes are adjusted if neede. If false index-tablespaces are only used when creating an index.
   */
  @Parameter( defaultValue = "true" )
  private boolean indexmovetablespace;

  /**
   * If true tablespaces of tables are adjusted if neede. If false index-tablespaces are only used when creating a table.
   */
  @Parameter( defaultValue = "true" )
  private boolean tablemovetablespace;

  /**
   * If true orcas will generate indexes for non-indexed foreign-key columns.
   */
  @Parameter( defaultValue = "true" )
  private boolean createmissingfkindexes;

  /**
   * Ignore existing tables which match the sql-where clause.
   */
  @Parameter( defaultValue = "object_name like '%$%'" )
  private String excludewheretable;

  /**
   * Ignore existing sequences which match the sql-where clause.
   */
  @Parameter( defaultValue = "object_name like '%$%'" )
  private String excludewheresequence;

  /**
   * Dateformat used in scripts (only used for materialized view logs).
   */
  @Parameter( defaultValue = "dd.mm.yy" )
  private String dateformat;

  /**
   * Parameter for your extension.
   */
  @Parameter( defaultValue = "" )
  private String extensionparameter;

  /**
   * Do not use this.
   */
  @Parameter( defaultValue = "" )
  private String targetplsql;

  /**
   * Default folder for replaceable scripts (scripts that contain objects that can be recreated without data-loss or performance issues).
   */
  @Parameter( defaultValue = "src/main/sql/replaceables" )
  protected File replaceablesfolder;
  
  /**
   * Default folder for tbale-scripts.
   */
  @Parameter( defaultValue = "src/main/sql/statics" )
  protected File staticsfolder;

  /**
   * The JDBC-Driver of the orcas user. Only needed if orcas should be installed into the database.
   */
  @Parameter( defaultValue = "oracle.jdbc.OracleDriver" )
  protected String orcasjdbcdriver;

  /**
   * The JDBC-URL of the orcas user. Only needed if orcas should be installed into the database.
   */
  @Parameter
  protected String orcasjdbcurl;

  /**
   * The schema-name of the orcas user. Only needed if orcas should be installed into the database.
   */
  @Parameter
  protected String orcasusername;

  /**
   * The schema-password of the orcas user. Only needed if orcas should be installed into the database.
   */
  @Parameter
  protected String orcaspassword;

  public void execute() throws MojoExecutionException
  {
    ParametersCall lParametersCall = new ParametersCall();

    lParametersCall.getJdbcConnectParameters().setJdbcDriver( jdbcdriver );
    lParametersCall.getJdbcConnectParameters().setJdbcUrl( jdbcurl );
    lParametersCall.getJdbcConnectParameters().setJdbcUser( username );
    lParametersCall.getJdbcConnectParameters().setJdbcPassword( password );

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

    if( usernameorcas != null && !usernameorcas.equals( "" ) )
    {
      lParametersCall.setOrcasDbUser( usernameorcas );
    }
    else
    {
      if( orcasusername != null )
      {
        lParametersCall.setOrcasDbUser( orcasusername );
      }
      else
      {
        lParametersCall.setOrcasDbUser( username );
      }
    }

    if( orcasusername != null )
    {
      JdbcConnectParameters lOrcasJdbcConnectParameters = new JdbcConnectParameters();
      lOrcasJdbcConnectParameters.setJdbcDriver( orcasjdbcdriver );
      lOrcasJdbcConnectParameters.setJdbcUrl( orcasjdbcurl == null ? lParametersCall.getJdbcConnectParameters().getJdbcUrl() : orcasjdbcurl );
      lOrcasJdbcConnectParameters.setJdbcUser( orcasusername );
      lOrcasJdbcConnectParameters.setJdbcPassword( orcaspassword );
      lParametersCall.setOrcasJdbcConnectParameters( lOrcasJdbcConnectParameters );
    }
    else
    {
      lParametersCall.setOrcasJdbcConnectParameters( lParametersCall.getJdbcConnectParameters() );
    }

    ParametersCommandline.setupLog4jLoglevel( lParametersCall );

    executeWithParameters( lParametersCall );
  }

  protected abstract String getLogname();

  protected abstract void executeWithParameters( ParametersCall pParameters );
}
