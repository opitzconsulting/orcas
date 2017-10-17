package com.opitzconsulting.orcas.gradle

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
    }
}

class OrcasGradlePluginExtension {
  def String jdbcdriver = "oracle.jdbc.OracleDriver";
  def String jdbcurl;
  def String username;
  def String password;
  def String scriptfolderPostfix = ".sql";
  def String scriptfolderPrefix = "";
  def boolean scriptfolderrecursive = true;
  def String spoolfile = "orcas_spoolfile.sql";
  def String spoolfolder = "log/";
  def String loglevel = "info";
  def FailOnErrorMode failOnErrorMode = FailOnErrorMode.ALWAYS;
  def String usernameorcas = "";
  def boolean logonly = false;
  def boolean dropmode = false;
  def boolean indexparallelcreate = true;
  def boolean indexmovetablespace = true;
  def boolean tablemovetablespace = true;
  def boolean createmissingfkindexes = true;
  def String excludewheretable = 'object_name like \'%$%\'';
  def String excludewheresequence = 'object_name like \'%$%\'';
  def String dateformat = "dd.mm.yy";
  def String extensionparameter = "";
  def String targetplsql = "";
  def String replaceablesfolder = "src/main/sql/replaceables";
  def String staticsfolder = "src/main/sql/statics" ;
  def boolean additionsonly = false;
  def boolean logignoredstatements = true;
  def String xmllogfile = "log.xml";
  def String xmlinputfile;
  def boolean setunusedinsteadofdropcolumn = false;
  def boolean indexonlinecreate = false;
  def boolean minimizestatementcount = false;
  def String charsetname = "UTF-8";
  def String charsetnamesqllog = null;

  def String orcasjdbcdriver = "oracle.jdbc.OracleDriver";
  def String orcasjdbcurl;
  def String orcasusername;
  def String orcaspassword;

  def String excludewhereview = 'object_name not like \'%\'';
  def String excludewhereobjecttype = 'object_name not like \'%\'';
  def String excludewherepackage = 'object_name not like \'%\'';
  def String excludewheretrigger = 'object_name not like \'%\'';
  def String excludewherefunction = 'object_name not like \'%\'';
  def String excludewhereprocedure = 'object_name not like \'%\'';

  def String extractstaticsoutfolder;
  def String extractmodelinputfolder;
  def boolean extractremovedefaultvaluesfrommodel = true;
  def String extractreplaceablesoutfolder;
  def String viewextractmode = "text";

  def String srcjdbcurl;
  def String srcusername;
  def String srcpassword;

  def de.opitzconsulting.orcas.diff.ExtensionHandler extensionHandler;
}


