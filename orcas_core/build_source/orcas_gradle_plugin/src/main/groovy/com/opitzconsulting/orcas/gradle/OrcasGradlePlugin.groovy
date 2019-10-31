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
  def String jdbcdriver = ParameterDefaults.jdbcdriver;
  def String jdbcurl;
  def String username;
  def String password;
  def String scriptfolderPostfix = ParameterDefaults.scriptfolderPostfix;
  def String scriptfolderPrefix = ParameterDefaults.scriptfolderPrefix;
  def boolean scriptfolderrecursive = ParameterDefaults.scriptfolderrecursive;
  def String spoolfile = ParameterDefaults.spoolfile;
  def String spoolfolder = ParameterDefaults.spoolfolder;
  def String loglevel = ParameterDefaults.loglevel;
  def FailOnErrorMode failOnErrorMode = ParameterDefaults.failOnErrorMode;
  def ExecuteSqlErrorHandler executeSqlErrorHandler = ParameterDefaults.executeSqlErrorHandler;
  def String usernameorcas = ParameterDefaults.usernameorcas;
  def boolean logonly = ParameterDefaults.logonly;
  def boolean dropmode = ParameterDefaults.dropmode;
  def boolean indexparallelcreate = ParameterDefaults.indexparallelcreate;
  def boolean indexmovetablespace = ParameterDefaults.indexmovetablespace;
  def boolean tablemovetablespace = ParameterDefaults.tablemovetablespace;
  def boolean mviewlogmovetablespace = ParameterDefaults.mviewlogmovetablespace;
  def boolean createmissingfkindexes = ParameterDefaults.createmissingfkindexes;
  def String excludewheretable = ParameterDefaults.excludewheretable;
  def String excludewheresequence = ParameterDefaults.excludewheresequence;
  def String excludewheremview = ParameterDefaults.excludewheremview;
  def String dateformat = ParameterDefaults.dateformat;
  def String extensionparameter = ParameterDefaults.extensionparameter;
  def String targetplsql = ParameterDefaults.targetplsql;
  def String replaceablesfolder = ParameterDefaults.replaceablesfolder;
  def String staticsfolder = ParameterDefaults.staticsfolder;
  def boolean additionsonly = ParameterDefaults.additionsonly;
  def boolean logignoredstatements = ParameterDefaults.logignoredstatements;
  def String xmllogfile = ParameterDefaults.xmllogfile;
  def String xmlinputfile;
  def boolean setunusedinsteadofdropcolumn = ParameterDefaults.setunusedinsteadofdropcolumn;
  def boolean indexonlinecreate = ParameterDefaults.indexonlinecreate;
  def boolean minimizestatementcount = ParameterDefaults.minimizestatementcount;
  def String charsetname = ParameterDefaults.charsetname;
  def String charsetnamesqllog = ParameterDefaults.charsetnamesqllog;
  def boolean logCompileErrors = ParameterDefaults.logCompileErrors;

  def String orcasjdbcdriver = ParameterDefaults.jdbcdriver;
  def String orcasjdbcurl;
  def String orcasusername;
  def String orcaspassword;

  def String excludewhereview = ParameterDefaults.excludewhereview;
  def String excludewhereobjecttype = ParameterDefaults.excludewhereobjecttype;
  def String excludewherepackage = ParameterDefaults.excludewherepackage;
  def String excludewheretrigger = ParameterDefaults.excludewheretrigger;
  def String excludewherefunction = ParameterDefaults.excludewherefunction;
  def String excludewhereprocedure = ParameterDefaults.excludewhereprocedure;
  def String excludewheregrant = ParameterDefaults.excludewheregrant;
  def String excludewheresynonym = ParameterDefaults.excludewheresynonym;

  def boolean updateEnabledStatus = ParameterDefaults.updateEnabledStatus;

  def String extractgrantsfile;
  def String extractsynonymsfile;
  def String extractstaticsoutfolder;
  def String extractmodelinputfolder;
  def boolean extractremovedefaultvaluesfrommodel = ParameterDefaults.extractremovedefaultvaluesfrommodel;
  def String extractreplaceablesoutfolder;
  def String viewextractmode = ParameterDefaults.viewextractmode;
  def boolean isExtractViewCommnets;
  def boolean isExtractViewCommnents = ParameterDefaults.isExtractViewCommnents;

  def String srcjdbcurl;
  def String srcusername;
  def String srcpassword;

  def boolean multischema;
  def boolean multischemadbaviews;
  def String multischemaexcludewhereowner;

  def de.opitzconsulting.orcas.diff.ExtensionHandler extensionHandler;

  def boolean dbdocPlantuml = ParameterDefaults.dbdocPlantuml;
}


