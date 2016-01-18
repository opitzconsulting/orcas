---
layout: page
title: Ant Tasks
permalink: /docs/ant-tasks/
categories: 
- de
---

#Orcas ant-Tasks zur Ablaufsteuerung

Orcas wird über Ant angesteuert. In weiteren Ausbaustufen ist auch eine Steuerung über Gradle und Maven geplant. 
Die Ablaufsteuerung ist dabei sehr flexibel und bietet nachfolgende Funktionen an, die über Ant-Tasks (Makros) realisiert sind. Es gibt einige Beispielprojekte, in denen die Verwendung dieser Ant-Tasks für typische Projek-Setups beschrieben ist. Diese Seite soll als Referenz dienen, um den Gesamtfunktionsumfang zu beschreiben.

<ul class="no-list-style">
  <li><a href="#example">Beispiel</a></li>
  <li><a href="#init">Initialisierung</a></li>
  <li><a href="#tasks">Tasks für den Buildprozess</a></li>
  <li>
    <ul class="ant-tasks-contents">
      <li><a href="#general">Allgemeine Attribute</a></li>
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
  <li><a href="#exclude_where">Besonderheiten bei exclude_where_XXX Attributen</a></li>
  <li><a href="#spool">Spooling</a></li>
</ul>

<a name="example"/>

##Beispiel

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

##Initialisierung

Um die nachfolgenden Tasks nutzen zu können muss die Datei "orcas_default_tasks.xml" aus dem Verzeichnis "orcas_core" mittels ant includiert werden. Zusätzlich muss das property "orcas_dir" auf das "orcas_core" Verzeichnis gesetzt werden.

<a name="tasks"/>

##Tasks für den Buildprozess

<a name="general"/>

###Allgemeine Attribute

Jeder öffentliche ant-Task von Orcas hat folgende default Attribute. Diese, und nur diese, können auch über properties gesetzt werden und sind daher in dem ant-Task selbst immer optional.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|user     |Gibt den Benutzernamen an mit dem die Skripte ausgeführt werden sollen.|Yes|${orcas.default_user}|
|password |Gibt das Passwort zu dem Benutzer an.|Yes|${orcas.default_password}|
|userocsvw|Gibt den Benutzernamen an unter dem DB-Objekte abgelegt werden, die Orcas für interne Aufgaben benötigt. Dabei muss es sich um einen anderen DB-User handeln als "user", dieser muss aber auf der gleichen DB-Instanz liegen.|Yes|${orcas.default_user_orcas}
|passwordocsvw|Gibt das Passwort zu dem SCS-Benutzer an.|Yes|${orcas.default_password_orcas}|
|tnsname  |Gibt den tnsnames Eintrag der Datenbank an.|Yes|${orcas.default_tnsname}|
|tmpfolder|Gibt ein Verzeichnis an, in dem die Skriptverwaltung temporäre Dateien ablegen soll.|Yes|${orcas.default_tmpfolder}|
|jdbcurl  |Wird für JDBC-Zugriffe verwendet, ist in allen Tasks vorhanden, wird aber bisher nur von einigen genutzt. Wenn der angegebene Default-Wert auch nicht gesetzt ist, dann wird die URL über den tnsname gesetzt, dabei wird der OCI-Treiber verwendet ("jdbc:oracle:oci:@tnsname") der aber oft zu Problemen führt. Daher wird empfohlen den Parameter auf eine ULR mit thin-Treiber zu setzen. |No|${orcas.default_jdbcurl}|

<a name="orcas_execute_script"/>

###orcas_execute_script

Dient zur Ausführung **eines** SQL*Plus Skripts.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|script   |Gibt das auszuführende Skript an.|Yes||
|parameter|Gibt mögliche Parameter für das SQL-Plus Skript an.|No||
|spoolparameter|Bietet die Möglichkeit andere Parameter zu speichern, als im aktiven Lauf verwendet wurden.|No|@{parameter}|
|failonerror|Gibt an, ob bei einem Fehler im Skript der Buildlauf abgebrochen werden soll.|No|false|
|loglevel |Gibt den Umfang der Ausgabe auf der Konsole an. Mögliche Werte: info, verbos.|Yes|info|
|executiondir|Gibt den Ordner an, von dem aus SQLPLUS gestartet wird.|No|${basedir}|
|spoolfolder|Durch setzen dieses Attributes wird das [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) aktiviert.|No|${orcas.default_spoolfolder}|
|logname  |Gibt einen Namen an, der für Logausgaben verwendet wird. Wenn die [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) Funktion genutz wird ist dieser Name eine Pflichtangabe und muss dann für jeden Taskaufruf unterschiedlich sein.|No||
|autotimestamp|Falls der Buildvorgang geloggt wird, kann mit der Angabe des automatischen Zeitstempels der Spooling-Folder mit einem Zeitstempel versehen werden. So werden ältere Spools nicht überschrieben.|No|false|

<a name="orcas_execute_scripts"/>

###orcas_execute_scripts

Dient zur Ausführung von **mehreren** SQL*Plus Skripten.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|scriptfolder|Gibt den Verzeichnisnamen an, in dem die auszuführenden Skripte liegen.|Yes||
|scriptprefix|Gibt einen Prefix an, der die auszuführenden Dateien einschränkt.|No||
|scriptpostfix|Gibt einen Postfix an, der die auszuführenden Dateien einschränkt.|No|".sql"|
|scriptfolderrecursive|Gibt an, ob auch die Skripte aus den Unterverzeichnissen ausgeführt werden sollen.|No|false|
|failonerror|Gibt an, ob bei einem Fehler im Skript der Buildlauf abgebrochen werden soll.|No|false|
|loglevel |Gibt den Umfang der Ausgabe auf der Konsole an. Mögliche Werte: info, verbose|Yes|info|
|executiondir|Gibt den Ordner an, von dem aus SQLPLUS gestartet wird|No|${basedir}|
|spoolscriptname|Wenn true werden beim spooling Prompts mit den Skriptnamen generiert|No|false|
|spoolfolder|Durch setzen dieses Attributes wird das [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) aktiviert.|No|${orcas.default_spoolfolder}|
|logname  |Gibt einen Namen an, der für Logausgaben verwendet wird. Wenn die [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) Funktion genutz wird ist dieser Name eine Pflichtangabe und muss dann für jeden Taskaufruf unterschiedlich sein.|No||
|autotimestamp|Falls der Buildvorgang geloggt wird, kann mit der Angabe des automatischen Zeitstempels der Spooling-Folder mit einem Zeitstempel versehen werden. So werden ältere Spools nicht überschrieben.|No|false|

<a name="orcas_execute_one_time_scripts"/>

###orcas_execute_one_time_scripts

Dient zur Ausführung von mehreren SQL*Plus Skripten, die Besonderhiet liegt darin, dass diese Skripte nur ein einziges Mal ausgeführt werden. Dazu wird von Orcas nachgehalten, welche Skripte auf einem Schema schon ausgeführt wurden.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|scriptfolder|Gibt den Verzeichnisnamen an, in dem die auszuführenden Skripte liegen.|Yes||
|scriptprefix|Gibt einen Prefix an, der die auszuführenden Dateien einschränkt.|No||
|scriptpostfix|Gibt einen Postfix an, der die auszuführenden Dateien einschränkt.|No|".sql"|
|scriptfolderrecursive|Gibt an, ob auch die Skripte aus den Unterverzeichnissen ausgeführt werden sollen.|No|false|
|failonerror|Gibt an, ob bei einem Fehler im Skript der Buildlauf abgebrochen werden soll.|No|true|
|noexecute|Gibt an, dass die Skripte nur auf ausgeführt gesetzt werden, dabei aber nicht ausgeführt werden. Das ist zum Beispiel dann sinnvoll, wenn das SCS-Schema neu aufgebaut wurde.|No|false|
|loglevel |Gibt den Umfang der Ausgabe auf der Konsole an. Mögliche Werte: info, verbose|Yes|info|
|spoolscriptname|Wenn true werden beim spooling Prompts mit den Skriptnamen generiert|No|false|
|spoolfolder|Durch setzen dieses Attributes wird das [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) aktiviert.|No|${orcas.default_spoolfolder}|
|logname  |Gibt einen Namen an, der für Logausgaben verwendet wird. Ist Plichtangabe, da er auch für die Prüfung ob ein Skript bereits ausgeführt wurde verwendet wird.|Yes||
|autotimestamp|Falls der Buildvorgang geloggt wird, kann mit der Angabe des automatischen Zeitstempels der Spooling-Folder mit einem Zeitstempel versehen werden. So werden ältere Spools nicht überschrieben.|No|false|

<a name="orcas_execute_statics"/>

###orcas_execute_statics

Dient zur Ausführung des Abgleichs von statischen Objekten mit den bestehenden Datenbankobjekten. Die statischen Objekte müssen in der speziellen [Spooling]({{site.baseurl}}/docs/statics-syntax/) Notation von Orcas für statische Objekte definiert sein.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|scriptfolder|Gibt den Verzeichnisnamen an, in dem die Skripte mit den statischen Objekten liegen.|Yes||
|scriptprefix|Gibt einen Prefix an, der die auszuführenden Dateien einschränkt.|No||
|scriptpostfix|Gibt einen Postfix an, der die auszuführenden Dateien einschränkt.|No|".sql"|
|scriptfolderrecursive|Gibt an, ob auch die Skripte aus den Unterverzeichnissen ausgeführt werden sollen.|No|false|
|dropmode|Gibt an, ob auch Spalten und Tabellen gedropt werden sollen, was im Fehlerfall zu ernsthaftem Datenverlust führen kann.|No|false|
|indexparallelcreate|Gibt an, ob Indexe parallel erstellt werden sollen. Diese Angabe bezieht sich nur auf das Anlegen. Nach dem Anlegen wird der Index gemäß parallel bzw. noparallel (default) Angabe eingestellt. <br/>Es handelt sich hierbei also nur um eine Performanceoptimierung bei der Indexanlage.|No|true|
|createmissingfkindexes|Gibt an, ob für jeden Foreign-Key ein Index angelegt werden soll, falls kein passender Index vorliegt. <br/>Hintergrund: Aus Performancegründen (Laufzeit und Locking), ist ein Index pro Foreign-Key fast immer notwendig.|No|true|
|excludewheretable|Gibt eine [exclude_where-Bedingung]({{site.baseurl}}/docs/ant-tasks/#paricularity) an, mit der Tabellen von der abschliessenden Bereinigung ausgenommen werden können.|No|"object_name like '%$%'"|
|excludewheresequence|Gibt eine [exclude_where-Bedingung]({{site.baseurl}}/docs/ant-tasks/#paricularity) an, mit der Sequenzen von der abschliessenden Bereinigung ausgenommen werden können.|No|"object_name like '%$%'"|
|loglevel|Gibt den Umfang der Ausgabe auf der Konsole an. Mögliche Werte: info, verbose|Yes|info|
|targetplsql|Mit diesem Attribut kann Orcas für andere Zwecke als einen Tabellenabgleich eingesetzt werden. Wenn es gesetzt ist, dann muss es einen Package-Namen enthalten. Dieses Package wird dann an Stelle des eigentlichen Orcas ausgeführt. Das Package muss die folgende Prozedur beinhalten und das Package muss auch schon in dem User-Schema enthalten sein: "procedure run( p_model in ot_syex_model )". <br/>Mit diesem Mechanismus kann z.B. ein Triggergenerator angesprochen werden (Siehe Beispiel: target_plsql_demo).|No||
|logonly|Gibt an, ob nur protokolliert werden soll. Wenn true, dann wird keine Änderung am Datenmodell durchgeführt, sondern nur das Spooling ausgeführt.|No|false|
|spoolfolder|Durch Setzen dieses Attributes wird das [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) aktiviert.|No|${orcas.default_spoolfolder}|
|logname|Gibt einen Namen an, der für Logausgaben verwendet wird. Wenn die [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) Funktion genutzt wird, ist dieser Name eine Pflichtangabe und muss dann für jeden Taskaufruf unterschiedlich sein.|No||
|autotimestamp|Falls der Buildvorgang geloggt wird, kann mit der Angabe des automatischen Zeitstempels der Spooling-Folder mit einem Zeitstempel versehen werden. So werden ältere Spools nicht überschrieben.|No|false|

<a name="orcas_drop_replaceables"/>

###orcas_drop_replaceables

Durch diesen ant-Task werden alle replaceable-Objekte in der Datenbank gelöscht. Replaceable Objekte sind dabei die folgenden:

- Views
- Object-Types (incl. Collections)
- Packages
- Trigger
- Functions
- Procedures

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|excludewhereview|Gibt eine [exclude_where-Bedingung]({{site.baseurl}}/docs/ant-tasks/#exclude_where) an, mit der Views vor dem Löschen geschützt werden.|No|"object_name like '%'"|
|excludewhereobjecttype|Gibt eine [exclude_where-Bedingung]({{site.baseurl}}/docs/ant-tasks/#exclude_where) an, mit der Object-Types und Collections vor dem Löschen geschützt werden.|No|"object_name like '%'"|
|excludewherepackage|Gibt eine [exclude_where-Bedingung]({{site.baseurl}}/docs/ant-tasks/#exclude_where) an, mit der Packages vor dem Löschen geschützt werden.|No|"object_name like '%'"|
|excludewheretrigger|Gibt eine [exclude_where-Bedingung]({{site.baseurl}}/docs/ant-tasks/#exclude_where) an, mit der Trigger vor dem Löschen geschützt werden.|No|"object_name like '%'"|
|excludewherefunction|Gibt eine [exclude_where-Bedingung]({{site.baseurl}}/docs/ant-tasks/#exclude_where) an, mit der Functions vor dem Löschen geschützt werden.|No|"object_name like '%'"|
|excludewhereprocedure|Gibt eine [exclude_where-Bedingung]({{site.baseurl}}/docs/ant-tasks/#exclude_where) an, mit der Procedures vor dem Löschen geschützt werden.|No|"object_name like '%'"|

<a name="orcas_drop_java"/>

###orcas_drop_java

Durch diesen ant-Task werden alle Java-Klassen aus dem Schema entfernt.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|excludewherejava|Gibt eine [exclude_where-Bedingung]({{site.baseurl}}/docs/ant-tasks/#exclude_where) an, mit der Java-Klassen vor dem Löschen geschützt werden.|No|"name like '%'"|

<a name="orcas_kill_jobs"/>

###orcas_kill_jobs

Durch diesen ant-Task werden alle Jobs auf der Datenbank entfernt. Dabei wird wie folgt vorgegangen:
- Job auf broken setzen
- Ggf. aktive Job Session killen
- Job löschen

Dabei sind folgende besonderen Rechte nötig:
- select on sys.v_$session
- select on sys.v_$lock
- alter system

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|excludewherejobwhat|Gibt eine [exclude_where-Bedingung]({{site.baseurl}}/docs/ant-tasks/#exclude_where) an, mit der Jobs vor dem Löschen geschützt werden. Der object_name bezieht sich dabei auf die "what"-Angabe des Jobs|No|"what like '%'"|

<a name="orcas_compile_db_objects"/>

###orcas_compile_db_objects

Kompiliert alle invaliden DB-Objekte.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|logsinglecompile|Wenn gesetzt, und Spooling verwendet wird, dann wird jeder einzelne compile Befehl protokolliert. Wenn nicht gesetzt, wird ein compile-all Skript protokolliert.|No|false|
|spoolfolder|Durch setzen dieses Attributes wird das [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) aktiviert.|No|${orcas.default_spoolfolder}|
|logname|Gibt einen Namen an, der für Logausgaben verwendet wird. Wenn die [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) Funktion genutzt wird, ist dieser Name eine Pflichtangabe und muss dann für jeden Taskaufruf unterschiedlich sein.|No||
|autotimestamp|Falls der Buildvorgang geloggt wird, kann mit der Angabe des automatischen Zeitstempels der Spooling-Folder mit einem Zeitstempel versehen werden. So werden ältere Spools nicht überschrieben.|No|false|

<a name="orcas_upate_data"/>

###orcas_upate_data

Mit diesem ant-Task können Stammdaten mit der Datenbank abgeglichen werden.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|scriptfolder|Gibt den Verzeichnisnamen an, in dem die auszuführenden Skripte liegen.|Yes||
|scriptprefix|Gibt einen Prefix an, der die auszuführenden Dateien einschränkt.|No||
|scriptpostfix|Gibt einen Postfix an, der die auszuführenden Dateien einschränkt.|No|".sql"|
|scriptfolderrecursive|Gibt an, ob auch die Skripte aus den Unterverzeichnissen ausgeführt werden sollen.|No|false|
|checkmode|In diesem Modus werden nur die Daten mit den vorhandenen Daten verglichen. Dabei wird nach Abweichungen bei den nur-Insert Spalten gesucht. Die gefundenen Abweichungen werden ausgegeben.|No|false|
|spoolfolder|Durch setzen dieses Attributes wird das [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) aktiviert.|No|${orcas.default_spoolfolder}|
|logname|Gibt einen Namen an, der für Logausgaben verwendet wird. Wenn die [Spooling]({{site.baseurl}}/docs/ant-tasks/#spool) Funktion genutzt wird, ist dieser Name eine Pflichtangabe und muss dann für jeden Taskaufruf unterschiedlich sein.|No||
|autotimestamp|Falls der Buildvorgang geloggt wird, kann mit der Angabe des automatischen Zeitstempels der Spooling-Folder mit einem Zeitstempel versehen werden. So werden ältere Spools nicht überschrieben.|No|false|

<a name="orcas_clean_tables"/>

###orcas_clean_tables

Diese Funktion entfernt alles bis auf die Daten von einer Tabelle.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|excludewheretable|Gibt eine [exclude_where-Bedingung]({{site.baseurl}}/docs/ant-tasks/#exclude_where) an, mit der Tabellen von der abschliessenden Bereinigung ausgenommen werden können.|No|"object_name like '%$%'"|

<a name="orcas_initialize"/>

###orcas_initialize

Dieser ant-Task muss vor jedem ant-Lauf aufgerufen werden um Orcas zu initialisieren. Dies wird benötigt, um die DB-Objekte von Orcas zu aktualisieren und um das temporäre Verzeichnis zu initialisieren.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|extensionfolder|Gibt das Verzeichnis an, in dem die [Extensions]({{site.baseurl}}/docs/extensions/) liegen. Wenn keine Extensions genutzt werden, muss extensionfolder auf ein leeres Verzeichnis verweisen.|Yes||
|extensionparameter|Damit können Parameter an die Extensions (ein Text-Parameter für alle Extensions) übergeben werden.|No||
|prebuildmode|Ermöglicht die orcas-Initialisierung "abzuspeichern". Mögliche Werte: 1. build: Erzeugt (neben der normalen Initialisierung) eine Datei(prebuildfile), die orcas sowie die dependencies und Extensions enthält. 2. use: Überspringt den gradle-build und verwendet stattdessen das angegebene prebuildfile. Im prebuildfile sind auch die Extensions und Extensionparameter integriert, es ist daher nicht möglich diese bei Verwendung eines prebuildfiles zu ändern. ; 3. none: Deaktiviert die Funktion.|No|none|
|prebuildfile|Muss den Dateinamen des prebuildfiles haben, wenn prebuildmode nicht auf none steht. Die Datei darf nicht im tmpfolder liegen. |No||

<a name="orcas_grant"/>

###orcas_grant

Dieser ant-Task dient dazu, mehrere User mit dem selben Schemverwaltungs-User zu nutzen. Der erste User wird über orcas_initialize berechtigt, weitere können mit diesem Task berechtigt werden. Die OC-Schemverwaltung darf in dieser Konstellation nicht parallel gestartet werden (jeder parallelel abgleich braucht einen eigenen Schemverwaltungs-User).

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|user     |Gibt den User an, der die Rechte auf Orcas erhalten soll.|Yes||

<a name="orcas_install"/>

###orcas_install

Dieser ant-Task muss ein einziges Mal aufgerufen werden, um den SCS-Datenbankuser einzurichten. Dieser wird mit dem "normalen" User angelegt. Dabei ist es durchaus möglich, für diesen Aufruf einen speziellen DB-User als "user" zu verwenden, der die entsprechenden Anlage-Berechtigungen hat.

Der SCS-DB-User erhalt dabei folgende Rechte:
- connect
- resource
- unlimited tablespace
- select any table

`todo: empty Table in source`

<a name="orcas_check_connection"/>

###orcas_check_connection

Dieser ant-Task prüft, ob mit den angegebenen Daten eine Connection aufgebaut werden kann. Die normalen execute_script ant-Tasks prüfen dies nicht (auch nicht wenn failonerror auf true gesetzt wurde). Dieser Task kann aufgerufen werden ohne vorher Orcas zu installieren oder zu initialisieren.

<a name="orcas_extract"/>

###orcas_extract

Dient zur Generierung der Tabellenskripte aus einem bestehenden Schema (Reverse-Engineering). Siehe auch: [Generieren der Statics-Tabellenskripte]({{site.baseurl}}/docs/generate-scripts/).

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|outputfolder|Gibt den Verzeichnisnamen an, in dem die Skripte abgelegt werden sollen. Der Ordner wird gelöscht und neu angelegt.|Yes||
|xsltfile|`todo`|No|orcas_core/xslt_extract/orcas_extract.xsl|
|excludewheretable|Gibt eine [exclude_where-Bedingung]({{site.baseurl}}/docs/ant-tasks/#exclude_where) an, mit der Tabellen von der abschliessenden Bereinigung ausgenommen werden können.|No|"object_name like '%$%'"|
|excludewheresequence|Gibt eine [exclude_where-Bedingung]({{site.baseurl}}/docs/ant-tasks/#exclude_where) an, mit der Sequenzen von der abschliessenden Bereinigung ausgenommen werden können.|No|"object_name like '%$%'"|

<a name="exclude_where"/>

##Besonderheiten bei exclude_where_XXX Attributen

Dabei handelt es sich um eine Möglichkeit, DB-Objekte des entsprechenden typs (XXX) von der Verarbeitung auszuschliessen. Dabei wird eine SQL-where-Bedingung formuliert, und alle DB-Objekte, die dieser where-Bedingung entsprechen, werden nicht beachtet. Dabei darf in der where-Bedingung eine Spalte verwendet werden, um den Objektnamen zu referenzieren. Diese Spalte heisst immer object_name, unabhängig davon, um welchen Typ von Objekt es sich handelt. Beginnt die where-Bedingung mit einem "and ", dann wird die evtl. angegebene default-where-Bedingung erweitert, ansonsten wird sie überschrieben.

<a name="spool"/>

##Spooling
Mit der Spooling Funktionalität wird in einem speziellen Spooling Verzeichnis ein Satz von SQL*Plus Skripten erzeugt, womit die komplette Abarbeitung protokolliert und wiederholbar gemacht wird. Dabei werden die beiden ant-Tasks orcas_install und orcas_initialize nicht mitprotokolliert.
