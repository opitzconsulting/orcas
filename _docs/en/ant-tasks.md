---
layout: page
title: Ant Tasks
permalink: /docs/ant-tasks/
categories: 
- en
---

#Orcas ant-Tasks sequence control

Orcas is trigger by Ant. The control with Gradle and Maven is planned for additional expansions.
With this, the sequence control is very flexible and offers the following features, which are realised by Ant-Tasks (macros). There are a few example projects, in which the usage of Ant-Tasks for typical project setups is described. This site should serve as reference, to show the whole range of functions.

<ul class="no-list-style">
  <li><a href="#example">Example</a></li>
  <li><a href="#init">Initialisation</a></li>
  <li><a href="#tasks">Tasks for build process</a></li>
  <li>
    <ul class="ant-tasks-contents">
      <li><a href="#general">General attributes</a></li>
      <li><a href="#orcas_execute_script">orcas_execute_script</a></li>
      <li><a href="#orcas_execute_scripts">orcas_execute_scripts</a></li>
      <li><a href="#orcas_execute_one_time_scripts">orcas_execute_one_time_scripts</a></li>
      <li><a href="#orcas_execute_statics">orcas_execute_statics</a></li>
      <li><a href="#orcas_drop_replaceables">orcas_drop_replaceables</a></li>
      <li><a href="#orcas_drop_java">orcas_drop_java</a></li>
      <li><a href="#orcas_kill_jobs">orcas_kill_jobs</a></li>
      <li><a href="#orcas_compile_db_objects">orcas_compile_db_objects</a></li>
      <li><a href="#orcas_update_data">orcas_update_data</a></li>
      <li><a href="#orcas_clean_tables">orcas_clean_tables</a></li>
      <li><a href="#orcas_initialize">orcas_initialize</a></li>
      <li><a href="#orcas_grant">orcas_grant</a></li>
      <li><a href="#orcas_install">orcas_install</a></li>
      <li><a href="#orcas_check_connection">orcas_check_connection</a></li>
      <li><a href="#orcas_extract">orcas_extract</a></li>
    </ul>
  </li>
  <li><a href="#exclude_where">Special requirement at exclude_where_XXX attributes</a></li>
  <li><a href="#spool">Spooling</a></li>
</ul>

<a name="example"/>

##Example

{% highlight xml %}
<?xml version = '1.0' encoding = 'windows-1252'?>
<project name="database">
  <property name="orcas_dir" value=".../orcas_core"/>
  <import file="${orcas_dir}/orcas_default_tasks.xml"/>

  <property name="orcas.default_user" value="${username_schemaowner}"/>
  <property name="orcas.default_password" value="${password_schemaowner}"/>
  <property name="orcas.default_user_orcas" value="${username_orcas}"/>
  <property name="orcas.default_password_orcas" value="${password_orcas}"/>
  <property name="orcas.default_tnsname" value="${database}"/>
  <property name="orcas.default_tmpfolder" value="c:/temp/orcas"/>
  <property name="orcas.default_spoolfolder" value="${spoolfolder}"/>

  <target name="clean">
    <delete includeemptydirs="true">
      <fileset dir="${distributiondir}/target" includes="**/*" defaultexcludes="false"/>
    </delete>
  </target>

  <target name="orcas_install">
    <orcas_install user="${username_dba}" password="${password_dba}"/>
  </target>

  <target name="build_all" depends="">
    <orcas_initialize extensionfolder="${distributiondir}/../../orcas/orcas_extensions"/>
    <orcas_execute_one_time_scripts
      scriptfolder="skripts_pre"
      scriptfolderrecursive="true"
      logname="pre_skripts"/>
      <orcas_execute_statics
        scriptfolder="tables"
        dropmode="${dropmode}"
        logname="statics"/>
        <orcas_drop_replaceables
          logname="dropreplaceables"/>
          <orcas_execute_scripts
            scriptfolder="views"
            logname="views" />
            <orcas_execute_one_time_scripts
              scriptfolder="skripts_post"
              scriptfolderrecursive="true"
              logname="post_skripts"/>
            </target>
          </project>
{% endhighlight %}

<a name="init"/>

##Initialisation

To use the following tasks you have to include the file "orcas_default_tasks.xml from the directory "orcas_core" by using ant. In addition to this you have to set the property "orcas_dir" to the "orcas_core" directory.

<a name="tasks"/>

##Tasks for build process

<a name="general"/>

###General attributes

Every public ant-Task of Orcas got the following default attributes. These ones, and only these ones, can be set by properties and therefore they are always optional in the ant-Task itself.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|user     |Defines the user, by whom the scripts should be executed.|Yes|${orcas.default_user}|
|password |Defines the password for the user.|Yes|${orcas.default_password}|
|userocsvw|Defines the username by whom database objects will be stored, which Orcas needs for internal tasks. This has to be a different db-user than "user" but has to be on the same db-instance as "user".|Yes|${orcas.default_user_orcas}
|passwordocsvw|Defines the password for the SCS user.|Yes|${orcas.default_password_orcas}|
|tnsname  |Defines the tnsnames entry for the database.|Yes|${orcas.default_tnsname}|
|tmpfolder|Defines the directory, in which temporary files should be stored for the script management.|Yes|${orcas.default_tmpfolder}|
|jdbcurl  |Is used for JDBC accesses. This is existing in every task, but will only be used by a few tasks so far. If the default value is not set, the url will be set by tnsname by using the OCI driver. |No|${orcas.default_jdbcurl}|

<a name="orcas_execute_script"/>

###orcas_execute_script

Is used for executing exactly **one** SQL*Plus script.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|script   |Defines the script which needs to be executed.|Yes||
|parameter|Defines possible parameters for the SQL*Plus script.|No||
|spoolparameter|Provides the opportunity to save different parameters, than used in the active run before.|No|@{parameter}|
|failonerror|Defines whether to abort the build process if an error occured in the script, or not.|No|false|
|loglevel |Defines the extent of the console output. Possible values: info, verbos.|Yes|info|

|executiondir|Defines the directory, from which SQLPLUS will be started.|No|${basedir}|
|spoolfolder|By setting this attribute, [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) will be activated.|No|${orcas.default_spoolfolder}
|logname  |Defines a name, which will be used for log outputs. If [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) is used, this name is a mandatory detail and has to be different for every task call.|No||
|autotimestamp|In case of logging the building process, with this you are able to set a time stamp for every single spooling folder. As a result, older spools will not be overwritten.|No|false|

<a name="orcas_execute_scripts"/>

###orcas_execute_scripts

Is used for executing **more than one** SQL*Plus script.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|scriptfolder|Defines the directory, where to find the scripts which need to be executed.|Yes||
|scriptprefix|Defines a prefix to limit the files, which need to be executed.|No||
|scriptpostfix|Defines a postfix to limit the files, which need to be executed.|No|".sql"|
|scriptfolderrecursive|Defines, whether to execute scripts in subdirectories or not.|No|false|
|failonerror|Defines whether to abort the build process if an error occured in the script, or not.|No|false|
|loglevel |Defines the extent of the console output. Possible values: info, verbos.|Yes|info|
|executiondir|Defines the directory, from which SQLPLUS will be started.|No|${basedir}|
|spoolscriptname|If set "true", while spooling, prompts with script names will be created.|No|false|
|spoolfolder|By setting this attribute, [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) will be activated.|No|${orcas.default_spoolfolder}|
|logname  |Defines a name, which will be used for log outputs. If [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) is used, this name is a mandatory detail and has to be different for every task call.|No||
|autotimestamp|In case of logging the building process, with this you are able to set a time stamp for every single spooling folder. As a result, older spools will not be overwritten.|No|false|

<a name="orcas_execute_one_time_scripts"/>

###orcas_execute_one_time_scripts

Is used for executing more than one SQL*Plus script, but with the restriction that scripts will be executed only once. In addition to this, Orcas recognized which scripts have already been executed on a schema.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|scriptfolder|Defines the directory, where to find the scripts which need to be executed.|Yes||
|scriptprefix|Defines a prefix to limit the files, which need to be executed.|No||
|scriptpostfix|Defines a postfix to limit the files, which need to be executed.|No|".sql"|
|scriptfolderrecursive|Defines, whether to execute scripts in subdirectories or not.|No|false|
|failonerror|Defines whether to abort the build process if an error occured in the script, or not.|No|false|
|noexecute|Defines whether to set scripts to "executed" without executing, or not. This makes sense when the SCS schema is completely built up new.|No|false|
|loglevel |Defines the extent of the console output. Possible values: info, verbos.|Yes|info|
|spoolscriptname|If set "true", while spooling, prompts with script names will be created.|No|false|
|spoolfolder|By setting this attribute, [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) will be activated.|No|${orcas.default_spoolfolder}|
|logname  |Defines a name, which will be used for log outputs. If [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) is used, this name is a mandatory detail and has to be different for every task call.|No||
|autotimestamp|In case of logging the building process, with this you are able to set a time stamp for every single spooling folder. As a result, older spools will not be overwritten.|No|false|

<a name="orcas_execute_statics"/>

###orcas_execute_statics

Is used for executing the comparison between static objects and existing database objects. Static objects have to be defined in the special [Spooling]({{site.baseurl}}/docs/statics-syntax/) notation for static objects of Orcas.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|scriptfolder|Defines the directory, where to find the scripts including the static objects which need to be executed.|Yes||
|scriptprefix|Defines a prefix to limit the files, which need to be executed.|No||
|scriptpostfix|Defines a postfix to limit the files, which need to be executed.|No|".sql"|
|scriptfolderrecursive|Defines, whether to execute scripts in subdirectories or not.|No|false|
|dropmode|Defines whether to drop columns and tables, or not. This can result in a serious loss of data in case of errors.|No|false|
|indexparallelcreate|Defines whether to create parallel indexes, or not. This only has an effect on creations. After creating, the index will be set to parallel or noparallel (default), depending on what is set. <br/>So this is only a performance optimization for creating indexes.|No|true|
|createmissingfkindexes|Defines whether to create an index for every foreign key if there is no mathing index, or not. <br/>Background: Because of performance reasons (runtime and locking), an index for every foreign key is almost always necessary.|No|true|
|excludewheretable|Defines an [exclude_where-reason]({{site.baseurl}}/docs/ant-tasks/#paricularity), to except tables from being finally adjusted.|No|"object_name like '%$%'"|
|excludewheresequence|Defines an [exclude_where-reason]({{site.baseurl}}/docs/ant-tasks/#paricularity), to except sequences from being finally adjusted.|No|"object_name like '%$%'"|
|loglevel |Defines the extent of the console output. Possible values: info, verbos.|Yes|info|
|targetplsql|With this attribute Orcas can be used for other purposes than table comparison. If set, it has to contain a package name. Instead of usual Orcas, this package will be executed then. The package has to contain the following procedure and also be implemented in the user schema: "procedure run( p_model in ot_syex_model )". <br/>With this mechanism you are able to adress things like a trigger generator (example:target_plsql_demo).|No||
|logonly|Defines whether only to log, or not. If "true", there won't be any changes at the data model, but instead only spooling will be executed.|No|false|
|spoolfolder|By setting this attribute, [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) will be activated.|No|${orcas.default_spoolfolder}|
|logname  |Defines a name, which will be used for log outputs. If [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) is used, this name is a mandatory detail and has to be different for every task call.|No||
|autotimestamp|In case of logging the building process, with this you are able to set a time stamp for every single spooling folder. As a result, older spools will not be overwritten.|No|false|

<a name="orcas_drop_replaceables"/>

###orcas_drop_replaceables

With this ant-Task all replaceable objects in the database will be deleted. Replaceable objects in this case are the following:

- Views
- Object-Types (incl. Collections)
- Packages
- Trigger
- Functions
- Procedures

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|excludewhereview|Defines an [exclude_where-reason]({{site.baseurl}}/docs/ant-tasks/#exclude_where), which prevents views from being deleted.|No|"object_name like '%'"|
|excludewhereobjecttype|Defines an [exclude_where-reason]({{site.baseurl}}/docs/ant-tasks/#exclude_where), which prevents object types and collections from being deleted.|No|"object_name like '%'"|
|excludewherepackage|Defines an [exclude_where-reason]({{site.baseurl}}/docs/ant-tasks/#exclude_where), which prevents packages from being deleted.|No|"object_name like '%'"|
|excludewheretrigger|Defines an [exclude_where-reason]({{site.baseurl}}/docs/ant-tasks/#exclude_where), which prevents triggers from being deleted.|No|"object_name like '%'"|
|excludewherefunction|Defines an [exclude_where-reason]({{site.baseurl}}/docs/ant-tasks/#exclude_where), which prevents functions from being deleted.|No|"object_name like '%'"|
|excludewhereprocedure|Defines an [exclude_where-reason]({{site.baseurl}}/docs/ant-tasks/#exclude_where), which prevents procedures from being deleted.|No|"object_name like '%'"|

<a name="orcas_drop_java"/>

###orcas_drop_java

With this ant-Task, all Java classes will be deleted from the schema.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|excludewherejava|Defines an [exclude_where-reason]({{site.baseurl}}/docs/ant-tasks/#exclude_where), which prevents Java classes from being deleted.|No|"name like '%'"|

<a name="orcas_kill_jobs"/>

###orcas_kill_jobs

With this ant-Task, all jobs will be deleted from the schema. This will be achieved by the following steps:
- Set job to broken
- Kill active job session if necessary
- Delete job

The following permissions are necessary for this:
- select on sys.v_$session
- select on sys.v_$lock
- alter system

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|excludewherejobwhat|Defines an [exclude_where-reason]({{site.baseurl}}/docs/ant-tasks/#exclude_where), which prevents jobs from being deleted. In this case object_name refers to the "what" specification of the job.|No|"what like '%'"|

<a name="orcas_compile_db_objects"/>

###orcas_compile_db_objects

Compiles every invalid db object.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|logsinglecompile|If set and spooling is in use, every single compile command will be logged. If not set, a compile-all script will be logged.|No|false|
|spoolfolder|By setting this attribute, [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) will be activated.|No|${orcas.default_spoolfolder}|
|logname  |Defines a name, which will be used for log outputs. If [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) is used, this name is a mandatory detail and has to be different for every task call.|No||
|autotimestamp|In case of logging the building process, with this you are able to set a time stamp for every single spooling folder. As a result, older spools will not be overwritten.|No|false|

<a name="orcas_upate_data"/>

###orcas_upate_data

With this ant-Task, master data can be compared with the database.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|scriptfolder|Defines the directory, where to find the scripts which need to be executed.|Yes||
|scriptprefix|Defines a prefix to limit the files, which need to be executed.|No||
|scriptpostfix|Defines a postfix to limit the files, which need to be executed.|No|".sql"|
|scriptfolderrecursive|Defines, whether to execute scripts in subdirectories or not.|No|false|
|checkmode|In this mode, data will be compared to existing data only. In this process, differences in only-insert columns will be searched. The differences which were found will be displayed.|No|false|
|spoolfolder|By setting this attribute, [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) will be activated.|No|${orcas.default_spoolfolder}|
|logname  |Defines a name, which will be used for log outputs. If [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) is used, this name is a mandatory detail and has to be different for every task call.|No||
|autotimestamp|In case of logging the building process, with this you are able to set a time stamp for every single spooling folder. As a result, older spools will not be overwritten.|No|false|

<a name="orcas_clean_tables"/>

###orcas_clean_tables

This feature deletes everything except the data of a table.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|excludewheretable|Defines an [exclude_where-reason]({{site.baseurl}}/docs/ant-tasks/#paricularity), to except tables from being finally adjusted.|No|"object_name like '%$%'"|

<a name="orcas_initialize"/>

###orcas_initialize

This ant-Task has to be called before every ant-run, to initialize Orcas. This is necessary to update all db objects and to initialize the temporary directory.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|extensionfolder|Defines the directory where the [Extensions]({{site.baseurl}}/docs/extensions/) are located. If you don't use extensions, extensionfolder has to refer an empty directory.|Yes|| 
|extensionparameter|With this, you can pass parameters to extensions (one single text parameter for all extentions).|No||
|prebuildmode|Allows to store or use orcas-installation to or from a prebuildfile, allows to use orcas without internet connection and gradle. Possible values: 1. build: Creates a prebuildfile, containing orcas, its dependencies and extensions (also initializes orcas normally). ; 2. use: Skips the gradle-build and uses the prebuildfile instead. ; 3. none: Don't use prebuildmode.|No|none|
|prebuildfile|Filename for the prebuildfiles (required if prebuildmode != none). The file may not reside in the tmpfolder. |No||


<a name="orcas_grant"/>

###orcas_grant

This ant-Task is used to handle several Users with the same schema management user. The first user will be authorized by orcas_initialize, more users can be authorized with this task. You must not start the OC-schema-management in parallel with this constellation (every comparison parallel to this needs an own schema management user). 

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|user     |Returns the user, who should be authorized to Orcas.|Yes||

<a name="orcas_install"/>

###orcas_install

This ant-Task has to be called only once to set up the SCS database user. This one will be created by the "normal" user. Although it is possible to use a special db user, who granted the necessary rights, as "user" for this call.

The SCS-DB-User grants the following rights:
- connect
- resource
- unlimited tablespace
- select any table

`todo: empty Table in source`

<a name="orcas_check_connection"/>

###orcas_check_connection

This ant-Task tests, if a connection with specified data is successful, or not. Regular execute_script ant-Tasks don't test this (also not if failonerror has been set "true"). This task can be called without having Orcas installed or initialized.

<a name="orcas_extract"/>

###orcas_extract

Is used for generating table scripts from an existing schema (reverse engineering). See also; [Generating statics table scripts]({{site.baseurl}}/docs/generate-scripts/).

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|outputfolder|Defines the directory in which the scripts will be saved. The folder is gonna be deleted and created new.|Yes||
|xsltfile|`todo`|No|orcas_core/xslt_extract/orcas_extract.xsl|
|excludewheretable|Defines an [exclude_where-reason]({{site.baseurl}}/docs/ant-tasks/#paricularity), to except tables from being finally adjusted.|No|"object_name like '%$%'"|
|excludewheresequence|Defines an [exclude_where-reason]({{site.baseurl}}/docs/ant-tasks/#paricularity), to except sequences from being finally adjusted.|No|"object_name like '%$%'"|

<a name="exclude_where"/>

##Special features with exclude_where_XXX attributes

There is a possibility to exclude database objects with a specific type (XXX) from processing. In this process, a SQL-WHERE statement will be used to not consider database objects which apply to this statement. You may use a column in this where-condition to reference the object name. The name of this column is always object_name, doesn't matter which type of object it is. If the where-condition starts with "and" the existing default-where-condition will be expanded. If not it will be overwritten.

<a name="spool"/>

##Spooling
With Spooling SQL*Plus scripts will be generated in a specific Spooling directory. With these scripts the complete processing will be logged and repeatable. The ant-Tasks orcas_install and orcas_initialize will not be logged within this process.
