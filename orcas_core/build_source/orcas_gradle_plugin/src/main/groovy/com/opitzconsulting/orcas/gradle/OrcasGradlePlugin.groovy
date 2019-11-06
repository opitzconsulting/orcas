package com.opitzconsulting.orcas.gradle

import de.opitzconsulting.orcas.diff.ExecuteSqlErrorHandler
import de.opitzconsulting.orcas.diff.ParameterDefaults
import org.gradle.api.Project
import org.gradle.api.Plugin

import de.opitzconsulting.orcas.diff.Parameters.FailOnErrorMode;

class OrcasGradlePlugin implements Plugin<Project> {
    void apply(Project pProject) {
        pProject.extensions.create("orcasconfiguration", OrcasGradlePluginExtension)

        pProject.task('cleanLog', type:  OrcasCleanLogTask)

        pProject.task('initializeOrcasDb', dependsOn: 'cleanLog', type: OrcasInitializeOrcasDbTask)

        pProject.task('preStatics', dependsOn: 'initializeOrcasDb', type: OrcasOneTimeScriptsPreStaticsTask)

        pProject.task('statics', dependsOn: 'preStatics', type:  OrcasUpdateStaticsTask)

        pProject.task('dropReplaceables', dependsOn: 'statics', type:  OrcasDropReplaceablesIfReplaceablesExistsTask)

        pProject.task('installReplaceables', dependsOn: 'dropReplaceables', type:  OrcasInstallReplaceablesTask)

        pProject.task('compileReplaceables', dependsOn: 'installReplaceables', type:  OrcasCompileAllInvalidIfReplaceablesExistsTask)

        pProject.task('postCompile', dependsOn: 'compileReplaceables', type:  OrcasOneTimeScriptsPostCompileTask)

        pProject.task('databaseDeployment', dependsOn: 'postCompile')


        pProject.task('checkConnection', type: OrcasCheckConnectionTask)

        pProject.task('extractStatics', type: OrcasExtractStaticsTask)
        pProject.task('extractReplaceables', type: OrcasExtractReplaceablesTask)
        pProject.task('extract')

        pProject.extract.dependsOn pProject.extractStatics,  pProject.extractReplaceables

        pProject.task('schemaSyncStatics', type: OrcasSchemaSyncStaticsTask)

        pProject.task('dbdoc', type: OrcasDbDocTask)
    }
}

class OrcasGradlePluginExtension {
  /**
   * JDBC-Driver class of the database.
   *
   * default: "oracle.jdbc.OracleDriver"
   */
  def String jdbcdriver = ParameterDefaults.jdbcdriver;
  /**
   * JDBC-URL of the database.
   */
  def String jdbcurl;
  /**
   * username of the database.
   */
  def String username;
  /**
   * password of the database.
   */
  def String password;
  /**
   * Ending for script-files.
   *
   * default: ".sql"
   */
  def String scriptfolderPostfix = ParameterDefaults.scriptfolderPostfix;
  /**
   * Start for script-files.
   *
   * default: "" meaning no filter
   */
  def String scriptfolderPrefix = ParameterDefaults.scriptfolderPrefix;
  /**
   * Search for script-files in subfolders?.
   *
   * default: true
   */
  def boolean scriptfolderrecursive = ParameterDefaults.scriptfolderrecursive;
  /**
   * spoolfile.
   *
   * default: "orcas_spoolfile.sql"
   */
  def String spoolfile = ParameterDefaults.spoolfile;
  /**
   * The Subfolder of the gradle build-folder for spoolfiles.
   *
   * default: "log/"
   */
  def String spoolfolder = ParameterDefaults.spoolfolder;
  /**
   * loglevel.
   *
   * Possible values: error, warn, info, debug
   *
   * default: "info"
   */
  def String loglevel = ParameterDefaults.loglevel;
  /**
   * Enum to control error handling.
   *
   * Only used if executeSqlErrorHandler is not set.
   *
   * default: FailOnErrorMode.ALWAYS
   */
  def FailOnErrorMode failOnErrorMode = ParameterDefaults.failOnErrorMode;
  /**
   * Extended control of error handling.
   *
   * default: An error-handler using the failOnErrorMode
   */
  def ExecuteSqlErrorHandler executeSqlErrorHandler = ParameterDefaults.executeSqlErrorHandler;
  /**
   * @deprecated
   */
  def String usernameorcas = ParameterDefaults.usernameorcas;
  /**
   * If set to true, the detected statics DDL-statements are only written to the log files.
   *
   * default: false
   */
  def boolean logonly = ParameterDefaults.logonly;
  /**
   * If set to true, Orcas will drop tables and columns even if they contain data.
   *
   * default: false
   */
  def boolean dropmode = ParameterDefaults.dropmode;
  /**
   * If set to true, Orcas will create indexes with the parallel option to improve performance.
   * Note that after the index is created the parallel option will be reset.
   *
   * default: true
   */
  def boolean indexparallelcreate = ParameterDefaults.indexparallelcreate;
  /**
   * If set to true, Orcas will move indexes to their new tablespaces.
   * If set to false, Orcas will not change indexes if they are stored in a diffenrent tablespace.
   *
   * default: true
   */
  def boolean indexmovetablespace = ParameterDefaults.indexmovetablespace;

  /**
   * If set to true, Orcas will move tables to their new tablespaces.
   * If set to false, Orcas will not change tables if they are stored in a diffenrent tablespace.
   *
   * default: true
   */
  def boolean tablemovetablespace = ParameterDefaults.tablemovetablespace;

  /**
   * If set to true, Orcas will recretae mview-logs if they are stored in a diffenrent tablespace.
   * If set to false, Orcas will not change mview-logs if they are stored in a diffenrent tablespace.
   *
   * default: false
   */
  def boolean mviewlogmovetablespace = ParameterDefaults.mviewlogmovetablespace;

  /**
   * If set to true, Orcas will create indexes for foreign-key columns that are not indexed.
   *
   * default: true
   */
  def boolean createmissingfkindexes = ParameterDefaults.createmissingfkindexes;

  /**
   * A where-fragment for excluding tables from the model read from the database.
   * The where-fragment is applied to the user_objects data dictionary view.
   *
   * default: "object_name like '%$%'"
   */
  def String excludewheretable = ParameterDefaults.excludewheretable;
  /**
   * A where-fragment for excluding sequences from the model read from the database.
   * The where-fragment is applied to the user_objects data dictionary view.
   *
   * default: "object_name like '%$%'"
   */
  def String excludewheresequence = ParameterDefaults.excludewheresequence;
  /**
   * A where-fragment for excluding materialized views from the model read from the database.
   * The where-fragment is applied to the user_objects data dictionary view.
   *
   * default: "object_name like '%$%'"
   */
  def String excludewheremview = ParameterDefaults.excludewheremview;
  /**
   * The data format.
   *
   * default: "dd.mm.yy"
   */
  def String dateformat = ParameterDefaults.dateformat;
  /**
   * Can be used to parameterize custom extensions.
   *
   * default: ""
   */
  def String extensionparameter = ParameterDefaults.extensionparameter;
  /**
   * @deprecated
   */
  def String targetplsql = ParameterDefaults.targetplsql;
  /**
   * The folder for replaceables used by the orcas-gradle-plugin.
   *
   * default: "src/main/sql/replaceables"
   */
  def String replaceablesfolder = ParameterDefaults.replaceablesfolder;
  /**
   * The folder for statics used by the orcas-gradle-plugin.
   *
   * default: "src/main/sql/statics"
   */
  def String staticsfolder = ParameterDefaults.staticsfolder;
  /**
   * If set to true Orcas will only apply statics changes that extend the database schema.
   * In this mode tables and columns are usually added, whereas constraints are usually dropped.
   * There are some database-changes that lead to an errror in this mode (e.g. changing column datatypes).
   *
   * default: false
   */
  def boolean additionsonly = ParameterDefaults.additionsonly;
  /**
   * If set to true Orcas will log ignored statements as comments in the spoolfolder.
   *
   * default: true
   */
  def boolean logignoredstatements = ParameterDefaults.logignoredstatements;
  /**
   * The log-file for statics updates within the gradle build-dir.
   *
   * default: "log.xml"
   */
  def String xmllogfile = ParameterDefaults.xmllogfile;
  def String xmlinputfile;
  /**
   * If set to true, Orcas will set colums to unused instead of dropping them.
   *
   * default: false
   */
  def boolean setunusedinsteadofdropcolumn = ParameterDefaults.setunusedinsteadofdropcolumn;
  /**
   * If set to true, Orcas will create indexes online.
   *
   * default: false
   */
  def boolean indexonlinecreate = ParameterDefaults.indexonlinecreate;
  /**
   * If set to true, Orcas will try to put multiple changes to tables in a single DDL-statement.
   *
   * default: false
   */
  def boolean minimizestatementcount = ParameterDefaults.minimizestatementcount;
  /**
   * Character set used for sql-files.
   *
   * default: "UTF-8"
   */
  def String charsetname = ParameterDefaults.charsetname;
  /**
   * Can be set to change the character set used for spool files only.
   */
  def String charsetnamesqllog = ParameterDefaults.charsetnamesqllog;
  /**
   * If set to false, compile errors are not shown.
   *
   * default: true
   */
  def boolean logCompileErrors = ParameterDefaults.logCompileErrors;
  /**
   * Driver class for the one-time-script ORCAS_UPDATES table.
   *
   * default: "oracle.jdbc.OracleDriver"
   */
  def String orcasjdbcdriver = ParameterDefaults.jdbcdriver;
  /**
   * JDBC-Url for the one-time-script ORCAS_UPDATES table.
   *
   * default: null so jdbcurl is used
   */
  def String orcasjdbcurl;
  /**
   * Username for the one-time-script ORCAS_UPDATES table.
   *
   * default: null so username is used
   */
  def String orcasusername;
  /**
   * Password for the one-time-script ORCAS_UPDATES table.
   *
   * default: null so password is used
   */
  def String orcaspassword;

  /**
   * A where-fragment for excluding views from the model read from the database.
   * The where-fragment is applied to the user_objects data dictionary view.
   *
   * default: "1 = 0" (nothing is excluded)
   */
  def String excludewhereview = ParameterDefaults.excludewhereview;
  /**
   * A where-fragment for excluding object types from the model read from the database.
   * The where-fragment is applied to the user_objects data dictionary view.
   *
   * default: "1 = 0" (nothing is excluded)
   */
  def String excludewhereobjecttype = ParameterDefaults.excludewhereobjecttype;
  /**
   * A where-fragment for excluding packages from the model read from the database.
   * The where-fragment is applied to the user_objects data dictionary view.
   *
   * default: "1 = 0" (nothing is excluded)
   */
  def String excludewherepackage = ParameterDefaults.excludewherepackage;
  /**
   * A where-fragment for excluding trigger from the model read from the database.
   * The where-fragment is applied to the user_objects data dictionary view.
   *
   * default: "1 = 0" (nothing is excluded)
   */
  def String excludewheretrigger = ParameterDefaults.excludewheretrigger;
  /**
   * A where-fragment for excluding functions from the model read from the database.
   * The where-fragment is applied to the user_objects data dictionary view.
   *
   * default: "1 = 0" (nothing is excluded)
   */
  def String excludewherefunction = ParameterDefaults.excludewherefunction;
  /**
   * A where-fragment for excluding procedures from the model read from the database.
   * The where-fragment is applied to the user_objects data dictionary view.
   *
   * default: "1 = 0" (nothing is excluded)
   */
  def String excludewhereprocedure = ParameterDefaults.excludewhereprocedure;
  /**
   * A where-fragment for excluding grants from the model read from the database.
   * The where-fragment is applied to the all_tab_privs_made data dictionary view.
   *
   * default: "not(owner = user)"
   */
  def String excludewheregrant = ParameterDefaults.excludewheregrant;
  /**
   * A where-fragment for excluding synonyms from the model read from the database.
   * The where-fragment is applied to the all_synonyms data dictionary view.
   *
   * default: "not((owner = user) or (owner = 'PUBLIC' and table_owner = user and db_link is null))"
   */
  def String excludewheresynonym = ParameterDefaults.excludewheresynonym;

  /**
   * If set to true, Orcas will enable or disable constraints as given in the statics scripts.
   * If set to false, Orcas will not cnahe the enabled state of constraints.
   *
   * default: false
   */
  def boolean updateEnabledStatus = ParameterDefaults.updateEnabledStatus;

  /**
   * The name of the extracted grants (not used by the orcas-gradle-plugin).
   *
   * default: "extract/scripts/grants/grants.sql"
   */
  def String extractgrantsfile;

  /**
   * The name of the extracted synonyms (not used by the orcas-gradle-plugin).
   *
   * default: "extract/scripts/synonyms/synonyms.sql"
   */
  def String extractsynonymsfile;

  /**
   * The name of the extracted statics base folder.
   *
   * default: "extract/sql/statics"
   */
  def String extractstaticsoutfolder;

  /**
   * Used mainly for the domain-extension to specify the domains to apply during extract.
   */
  def String extractmodelinputfolder;

  /**
   * If set to true, Orcas removes all default values from extracted statics.
   *
   * default: true
   */
  def boolean extractremovedefaultvaluesfrommodel = ParameterDefaults.extractremovedefaultvaluesfrommodel;

  /**
   * The name of the extracted replaceables base folder.
   *
   * default: "extract/sql/replaceables"
   */
  def String extractreplaceablesoutfolder;

  /**
   * If set to "text", Orcas will only extract the source of the views.
   * Can be "text" or "full".
   *
   * default: "text"
   */
  def String viewextractmode = ParameterDefaults.viewextractmode;
  /**
   * @deprecated
   */
  def boolean isExtractViewCommnets;
  /**
   * If set to false, Orcas will extract comments for views.
   *
   * default: false
   */
  def boolean isExtractViewCommnents = ParameterDefaults.isExtractViewCommnents;

  /**
   * Can be used to do a static comparison with another schema instead of script files.
   */
  def String srcjdbcurl;
  /**
   * Can be used to do a static comparison with another schema instead of script files.
   */
  def String srcusername;
  /**
   * Can be used to do a static comparison with another schema instead of script files.
   */
  def String srcpassword;

  /**
   * If set to true, Orcas can handle multiple schemas in a single statics run.
   * Object names may need to be prefixed with a schema name in this mode.
   *
   * default: false
   */
  def boolean multischema;
  /**
   * If multischema is used and set to true, Orcas will use the dba_ views instead of the all_views for extracting data-dictinary data.
   * Note that multischemaexcludewhereowner is required if set to true.
   *
   * default: false
   */
  def boolean multischemadbaviews;
  /**
   * If multischema is used, Orcas uses this as an where-fragment to exclude data-dictinary.
   * The where-fragment may only use the "owner"-column.
   */
  def String multischemaexcludewhereowner;

  /**
   * A custom extension-handler.
   */
  def de.opitzconsulting.orcas.diff.ExtensionHandler extensionHandler;

  /**
   * If set to true, orcas will generat platuml-sytle diagrams instead of using graphiz and html.
   *
   * default: false
   */
  def boolean dbdocPlantuml = ParameterDefaults.dbdocPlantuml;
}


