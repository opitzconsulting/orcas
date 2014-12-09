---
layout: docs
title: Ant Tasks
prev_section: integrations-tests
next_section: statics-syntax
permalink: /docs/ant-tasks/
---

#Orcas ant-Tasks zur Ablaufsteuerung

Orcas wird über ant angesteuert. In weiteren Ausbaustufen ist auch eine Steuerung über Maven geplant. Die Ablaufsteuerung ist dabei sehr flexibel und bietet nachfolgende Funktionen an, die über ant-Tasks (Makros) realisiert sind. Es gibt einige Beispielprojekte in denen die Verwendung dieser ant-Tasks für typische Projek-Setups beschrieben ist. Diese Seite soll als Referenz dienen, um den Gesamtfunktionsumfang zu beschreiben.

`todo: Inhalt mit Links`

##Beispiel

```xml
<?xml version = '1.0' encoding = 'windows-1252'?>
<project name="database">
  <property name="oc_svw_dir" value=".../oc_svw_core"/>
  <import file="${oc_svw_dir}/oc_svw_default_tasks.xml"/>

  <property name="oc_svw.default_user" value="${username_schemaowner}"/>
  <property name="oc_svw.default_password" value="${password_schemaowner}"/>
  <property name="oc_svw.default_user_oc_svw" value="${username_oc_svw}"/>
  <property name="oc_svw.default_password_oc_svw" value="${password_oc_svw}"/>
  <property name="oc_svw.default_tnsname" value="${database}"/>
  <property name="oc_svw.default_tmpfolder" value="c:/temp/oc_svw"/>
  <property name="oc_svw.default_spoolfolder" value="${spoolfolder}"/>

  <target name="clean">
    <delete includeemptydirs="true">
      <fileset dir="${distributiondir}/target" includes="**/*" defaultexcludes="false"/>
    </delete>
  </target>

  <target name="oc_svw_install">
    <oc_svw_install user="${username_dba}" password="${password_dba}"/>
  </target>

  <target name="build_all" depends="">
    <oc_svw_initialize extensionfolder="${distributiondir}/../../oc-schemaverwaltung/oc_svw_extensions"/>
    <oc_svw_execute_one_time_scripts
      scriptfolder="skripts_pre"
      scriptfolderrecursive="true"
      logname="pre_skripts"/>
      <oc_svw_execute_statics
        scriptfolder="tables"
        dropmode="${dropmode}"
        logname="statics"/>
        <oc_svw_drop_replaceables
          logname="dropreplaceables"/>
          <oc_svw_execute_scripts
            scriptfolder="views"
            logname="views" />
            <oc_svw_execute_one_time_scripts
              scriptfolder="skripts_post"
              scriptfolderrecursive="true"
              logname="post_skripts"/>
            </target>
          </project>
```

##Initialisierung

Um die nachfolgenden Tasks nutzen zu können muss die Datei "oc_svw_default_tasks.xml" aus dem Verzeichnis "oc_svw_core" mittels ant includiert werden. Zusätzlich muss das property "oc_svw_dir" auf das "oc_svw_core" Verzeichnis gesetzt werden.

##Tasks für den Buildprozess

###Allgemeine Attribute

Jeder öffentliche ant-Task der OC-Schemaverwaltung hat folgende default Attribute. Diese, und nur diese, können auch über properties gesetzt werden und sind daher in dem ant-Task selbst immer optional.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|user     |Gibt den Benutzernamen an mit dem die Skripte ausgeführt werden sollen.|Yes|${oc_svw.default_user}|
|password |Gibt das Passwort zu dem Benutzer an.|Yes|${oc_svw.default_password}|
|userocsvw|Gibt den Benutzernamen an unter dem DB-Objekte abgelegt werden, die die OC-Schemaverwaltung für interne Aufgaben benötigt. Dabei muss es sich um einen anderen DB-User handeln als "user", dieser muss aber auf der gleichen DB-Instanz liegen.|Yes|${oc_svw.default_user_oc_svw}
|passwordocsvw|Gibt das Passwort zu dem SCS-Benutzer an.|Yes|${oc_svw.default_password_oc_svw}|
|tnsname  |Gibt den tnsnames Eintrag der Datenbank an.|Yes|${oc_svw.default_tnsname}|
|tmpfolder|Gibt ein Verzeichnis an, in dem die Skriptverwaltung temporäre Dateien ablegen soll.|Yes|${oc_svw.default_tmpfolder}|

###oc_svw_execute_script

Dient zur Ausführung **eines** SQL*Plus Skripts.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|script   |Gibt das auszuführende Skript an.|Yes||
|parameter|Gibt mögliche Parameter für das SQL-Plus Skript an.|No||
|spoolparameter|Bietet die Möglichkeit andere Parameter zu speichern, als im aktiven Lauf verwendet wurden.|No|@{parameter}|
|failonerror|Gibt an, ob bei einem Fehler im Skript der Buildlauf abgebrochen werden soll.|No|false|
|loglevel |Gibt den Umfang der Ausgabe auf der Konsole an. Mögliche Werte: info, verbos.|Yes|info|
|executiondir|Gibt den Ordner an, von dem aus SQLPLUS gestartet wird.|No|${basedir}|
|spoolfolder|Durch setzen dieses Attributes wird das `todo: Link zum Spooling` Spooling aktiviert.|No|${oc_svw.default_spoolfolder}|
|logname  |Gibt einen Namen an, der für Logausgaben verwendet wird. Wenn die `todo: Link zum Spooling` Spooling Funktion genutz wird ist dieser Name eine Pflichtangabe und muss dann für jeden Taskaufruf unterschiedlich sein.|No||
|autotimestamp|Falls der Buildvorgang geloggt wird, kann mit der Angabe des automatischen Zeitstempels der Spooling-Folder mit einem Zeitstempel versehen werden. So werden ältere Spools nicht überschrieben.|No|false|

###oc_svw_execute_scripts

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
|spoolfolder|Durch setzen dieses Attributes wird das `todo: Link zum Spooling` Spooling aktiviert.|No|${oc_svw.default_spoolfolder}|
|logname  |Gibt einen Namen an, der für Logausgaben verwendet wird. Wenn die `todo: Link zum Spooling` Spooling Funktion genutz wird ist dieser Name eine Pflichtangabe und muss dann für jeden Taskaufruf unterschiedlich sein.|No||
|autotimestamp|Falls der Buildvorgang geloggt wird, kann mit der Angabe des automatischen Zeitstempels der Spooling-Folder mit einem Zeitstempel versehen werden. So werden ältere Spools nicht überschrieben.|No|false|


###oc_svw_execute_one_time_scripts

Dient zur Ausführung von mehreren SQL*Plus Skripten, die Besonderhiet liegt darin, dass diese Skripte nur ein einziges Mal ausgeführt werden. Dazu wird von der Schemaverwaltung nachgehalten, welche Skripte auf einem Schema schon ausgeführt wurden.

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
|spoolfolder|Durch setzen dieses Attributes wird das `todo: Link zum Spooling` Spooling aktiviert.|No|${oc_svw.default_spoolfolder}|
|logname  |Gibt einen Namen an, der für Logausgaben verwendet wird. Ist Plichtangabe, da er auch für die Prüfung ob ein Skript bereits ausgeführt wurde verwendet wird.|Yes||
|autotimestamp|Falls der Buildvorgang geloggt wird, kann mit der Angabe des automatischen Zeitstempels der Spooling-Folder mit einem Zeitstempel versehen werden. So werden ältere Spools nicht überschrieben.|No|false|

###oc_svw_execute_statics

Dient zur Ausführung des Abgleichs von statischen Objekten mit den bestehenden Datenbankobjekten. Die statischen Objekte müssen in der speziellen `todo: Link zu statics syntax` Notation der OC-Schemaverwaltung für statische Objekte definiert sein.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|scriptfolder|Gibt den Verzeichnisnamen an, in dem die Skripte mit den statischen Objekten liegen.|Yes||
|scriptprefix|Gibt einen Prefix an, der die auszuführenden Dateien einschränkt.|No||
|scriptpostfix|Gibt einen Postfix an, der die auszuführenden Dateien einschränkt.|No|".sql"|
|scriptfolderrecursive|Gibt an, ob auch die Skripte aus den Unterverzeichnissen ausgeführt werden sollen.|No|false|
|dropmode|Gibt an, ob auch Spalten und Tabellen gedropt werden sollen, was im Fehlerfall zu ernsthaftem Datenverlust führen kann.|No|false|
|indexparallelcreate|Gibt an ob Indexe parallel erstellt werden sollen. Diese Angabe bezieht sich nur auf das Anlegen, nach dem Anlegen wird der Index gemäß parallel bzw. noparallel (default) angabe eingestellt. <br/>Es handelt sich heirbei also nur um eine Performanceoptimierung bei der Indexanlage.|No|true|
|createmissingfkindexes|Gibt an, ob für jeden Foreign-Key ein Index angelegt werden soll, falls kein passender Index vorliegt. <br/>Hintergrund: Aus Performancegründen (Laufzeit und Locking), ist ein Index pro Foreign-Key fast immer notwendig.|No|true|
|excludewheretable|Gibt eine `todo: Link zu Besonderheiten`exclude_where-Bedingung an, mit der Tabellen von der Abschliessenden Bereinigung ausgenommen werden können.|No|"object_name like '%$%'"|
|excludewheresequence|Gibt eine `todo: Link zu Besonderheiten`exclude_where-Bedingung an, mit der Sequenzen von der Abschliessenden Bereinigung ausgenommen werden können.|No|"object_name like '%$%'"|
|loglevel|Gibt den Umfang der Ausgabe auf der Konsole an. Mögliche Werte: info, verbose|Yes|info|
|targetplsql|Mit diesem Attribut kann die Schemaverwaltung für andere Zwecke als einen Tabellenabgleich eingesetzt werden. Wenn es gesetzt ist, dann muss es einen Package-Namen enthalten. Dieses Package wird dann an Stelle der eigentlichen Schemaverwaltung ausgeführt. Das Package muss die folgende Prozedur beiinhalten und das Package muss auch schon in dem User-Schema enthalten sein: "procedure run( p_model in ot_syex_model )". <br/>Mit diesem Mechanismus kann z.B. ein Triggergenerator angesprochen werden (Siehe Beispiel: target_plsql_demo).|No||
|logonly|Gibt an ob nur protokolliert werden soll, wenn true, dann wird keine Änderung am Datenmodell durchgeführt, sondern nur das Spooling ausgeführt.|No|false|
|spoolfolder|Durch setzen dieses Attributes wird das `todo: Link zum Spooling` Spooling aktiviert.|No|${oc_svw.default_spoolfolder}|
|logname|Gibt einen Namen an, der für Logausgaben verwendet wird. Wenn die `todo: Link zum Spooling`Spooling Funktion genutz wird ist dieser Name eine Pflichtangabe und muss dann für jeden Taskaufruf unterschiedlich sein.|No||
|autotimestamp|Falls der Buildvorgang geloggt wird, kann mit der Angabe des automatischen Zeitstempels der Spooling-Folder mit einem Zeitstempel versehen werden. So werden ältere Spools nicht überschrieben.|No|false|

###oc_svw_drop_replaceables

Durch diesen ant-Task werden alle replaceable-Objekte in der Datenbank gelöscht. Replaceable Objekte sind dabei die folgenden:

- Views
- Object-Types (incl. Collections)
- Packages
- Trigger
- Functions
- Procedures

`todo: Link zu Besonderheiten vor jedem exclude`

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|excludewhereview|Gibt eine exclude_where-Bedingung an, mit der Views vor dem Löschen geschützt werden.|No|"object_name like '%'"|
|excludewhereobjecttype|Gibt eine exclude_where-Bedingung an, mit der Object-Types und Collections vor dem Löschen geschützt werden.|No|"object_name like '%'"|
|excludewherepackage|Gibt eine exclude_where-Bedingung an, mit der Packages vor dem Löschen geschützt werden.|No|"object_name like '%'"|
|excludewheretrigger|Gibt eine exclude_where-Bedingung an, mit der Trigger vor dem Löschen geschützt werden.|No|"object_name like '%'"|
|excludewherefunction|Gibt eine exclude_where-Bedingung an, mit der Functions vor dem Löschen geschützt werden.|No|"object_name like '%'"|
|excludewhereprocedure|Gibt eine exclude_where-Bedingung an, mit der Procedures vor dem Löschen geschützt werden.|No|"object_name like '%'"|

###oc_svw_drop_java

Durch diesen ant-Task werden alle Java-Klassen aus dem Schema entfernt.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|excludewherejava|Gibt eine `todo: Link zu Besonderheiten`exclude_where-Bedingung an, mit der Java-Klassen vor dem Löschen geschützt werden.|No|"name like '%'"|

###oc_svw_kill_jobs

Durch diesen ant-Task werden alle Jobs auf der Datenbank entfernt. Dabei wird wie folgt vorgegangen:
- Job auf broken setzen
- Ggf. aktive Job Session killen
- Job löschen

Dabei werden folgende besonderen Rechte nötig:
- select on sys.v_$session
- select on sys.v_$lock
- alter system

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|excludewherejobwhat|Gibt eine `todo: Link zu Besonderheiten`exclude_where-Bedingung an, mit der Jobs vor dem Löschen geschützt werden. Der object_name bezieht sich dabei auf die "what"-Angabe des Jobs|No|"what like '%'"|

###oc_svw_compile_db_objects

Kompiliert alle invaliden DB-Objekte.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|logsinglecompile|Wenn gesetzt, und Spooling verwendet wird, dann wird jeder einzelne compile Befehl protokolliert. Wenn nicht gesetzt, wird ein compile-all Skript protokolliert.|No|false|
|spoolfolder|Durch setzen dieses Attributes wird das `todo: Link zum Spooling`Spooling aktiviert.|No|${oc_svw.default_spoolfolder}|
|logname|Gibt einen Namen an, der für Logausgaben verwendet wird. Wenn die `todo: Link zum Spooling`Spooling Funktion genutz wird ist dieser Name eine Pflichtangabe und muss dann für jeden Taskaufruf unterschiedlich sein.|No||
|autotimestamp|Falls der Buildvorgang geloggt wird, kann mit der Angabe des automatischen Zeitstempels der Spooling-Folder mit einem Zeitstempel versehen werden. So werden ältere Spools nicht überschrieben.|No|false|

###oc_svw_upate_data

Mit diesem ant-Task können Stammdaten mit der Datenbank abgeglichen werden.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|scriptfolder|Gibt den Verzeichnisnamen an, in dem die auszuführenden Skripte liegen.|Yes||
|scriptprefix|Gibt einen Prefix an, der die auszuführenden Dateien einschränkt.|No||
|scriptpostfix|Gibt einen Postfix an, der die auszuführenden Dateien einschränkt.|No|".sql"|
|scriptfolderrecursive|Gibt an, ob auch die Skripte aus den Unterverzeichnissen ausgeführt werden sollen.|No|false|
|checkmode|In diesem Modus werden nur die Daten mit den vorhandenen Daten verglichen. Dabei wird nach Abweichungen bei den nur-Insert Spalten gesucht. Die gefundenen Abweichungen werden ausgegeben.|No|false|
|spoolfolder|Durch setzen dieses Attributes wird das `todo: Link zum Spooling`Spooling aktiviert.|No|${oc_svw.default_spoolfolder}|
|logname|Gibt einen Namen an, der für Logausgaben verwendet wird. Wenn die `todo: Link zum Spooling`Spooling Funktion genutz wird ist dieser Name eine Pflichtangabe und muss dann für jeden Taskaufruf unterschiedlich sein.|No||
|autotimestamp|Falls der Buildvorgang geloggt wird, kann mit der Angabe des automatischen Zeitstempels der Spooling-Folder mit einem Zeitstempel versehen werden. So werden ältere Spools nicht überschrieben.|No|false|

###oc_svw_clean_tables

Diese Funktion entfernt alles bis auf die Daten von einer Tabelle.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|excludewheretable|Gibt eine `todo: Link zu Besonderheiten`exclude_where-Bedingung an, mit der Tabellen von der Abschliessenden Bereinigung ausgenommen werden können.|No|"object_name like '%$%'"|

###oc_svw_initialize

Dieser ant-Task muss vor jedem ant-Lauf augerufen werden um die Schemaverwaltung zu initialisieren. Dies wird benötigt, um die DB-Objekte der Schemaverwaltung zu aktualisieren und um das temporäre Verzeichnis zu initialisieren.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|extensionfolder|Gibt das Verzeichnis an, in dem die `todo: Link zu Extensions`Extensions liegen. Wenn keine Extensions genutzt werden muss extensionfolder auf ein leeres Verzeichnis verweisen.|Yes||
|extensionparameter|Damit können Parameter an die Extensions (ein Text-Parameter für alle Extensions) übergeben werden.|No|||

###oc_svw_grant

Dieser ant-Task dient dazu mehreren User mit dem selben Schemverwaltungs-User zu nutzen. Der erste User wird über oc_svw_initialize berechtigt, weiter können mit diesem Task berechtigt werden. Die OC-Schemverwaltung darf in dieser Konstellation nicht parallel gestartet werden (jeder parallelel abgleich braucht einen eigenen Schemverwaltungs-User).

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|user     |Gibt den User an, der die Rechte auf die OC-Schemaverwaltung erhalten soll.|Yes|||

###oc_svw_install

Dieser ant-Task muss ein einziges Mal aufgerufen werden um den SCS-Datenbankuser einzurichten. Dieser wird mit dem "normalen" User angelegt. Dabei ist es durchaus möglich für diesen Aufruf einen speziellen DB-User als "user" zu verwenden, der die entsprechenden Anlage-Berechtigungen hat.

Der SCS-DB-User erhalt dabei folgende Rechte:
- connect
- resource
- unlimited tablespace
- select any table

`todo: empty Table in source`

###oc_svw_check_connection

Dieser ant-Task prüft, ob mit den angegebenen Daten eine Connection aufgebaut werden kann. Die normalen execute_script ant-Tasks prüfen dies nicht (auch nicht wenn failonerror auf true gesetzt wurde). Dieser Task kann aufgerufen werden, ohne vorher die Schemaverwaltung zu installieren oder zu initialisieren.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|outputfolder|Gibt den Verzeichnisnamen an, in dem die Skripte abgelegt werden sollen. der Ordner wird gelöscht und neu angelegt.|Yes||
|xsltfile|`todo`|No|oc_svw_core/xslt_extract/oc_svw_extract.xsl|
|excludewheretable|Gibt eine `todo: Link zu Besonderheiten`exclude_where-Bedingung an, mit der Tabellen von der Abschliessenden Bereinigung ausgenommen werden können.|No|"object_name like '%$%'"|
|excludewheresequence|Gibt eine `todo: Link zu Besonderheiten`exclude_where-Bedingung an, mit der Sequenzen von der Abschliessenden Bereinigung ausgenommen werden können.|No|"object_name like '%$%'"|

###oc_svw_delete_directory

Mit diesem Target können Ordner gelöscht werden. Es besteht auch die Möglichkeit, den Inhalt des Ordners vor dem Löschen in einer anderen Ordner zu kopieren. So ist es beispielsweise Möglich, alte Log-Dateien in einem Ordner zu Lagern. Die Log-Ordner werden dort über einen Zeitstempel identifiziert.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|autoTimeStamp|Mit der Angabe wird dem Ordnernamen des Backup-Folders ein Zeitstempel angehangen|No|false|
|folder   |Der zu löschende Ordner|Yes||
|backup-to-folder|Mit der Angabe wird der Inhalt des zu löschenden Ordner vorher in diesen Ordner kopiert|No|""|

##Besonderheiten bei exclude_where_XXX Attributen

Dabei handelt es sich um eine Möglichkeit DB-Objekte des entsprechenden typs (XXX) von der Verarbeitung auszuschliessen. Dabei wird eine SQL-where-Bedingung formuliert, und alle DB-Objekte, die dieser where-Bedingung entsprechen werden nicht beachtet. Dabei darf in der where-Bedingung eine Spalte verwendet werden, um den Objektnamen zu referenzieren. Diese Spalte heisst immer object_name, unabhängig davon, um welchen Typ von Objekt es sich handelt. Beginnt die where-Bedingung mit einem "and ", dann wird die evtl. angegebene default-where-Bedingung erweitert, ansonsten wird sie überschrieben.

##Spooling
Mit der Spooling Funktionalität wird in einem speziellen Spooling Verzeichnis ein Satz von SQL*Plus Skripten erzeugt, womit die komplette Abarbeitung protokolliert und wiederholbar gemacht wird. Dabei werden die beiden ant-Tasks oc_svw_install und oc_svw_initialize nicht mit protokolliert.
