---
layout: page
title: Teil 2.5 - Packages, Views & Co
permalink: /docs/schulung_teil2_5/
---

In Orcas wir zwischen **statics** und **replaceables** unterschieden.

- **replaceables**: Sind Datenbankobjekte die bei jedem Build-Durchlauf ersetzt (also im Zweifel gelöscht und neu angelegt) werden können.

- **statics**: sind die Datenbankobjekte bei denen das nicht geht. Primär weil das Datenverlust bedeuten würde (z.B. bei Tabellen) und sekundär weil das aus Performance-Gründen nicht akzeptable wäre (z.B. bei Indices).

Bisher haben wir uns ausschließlich mit statics beschäftigt. Nun wollen wir unser Beispiel auch um PL/SQL-Packages und Views erweitern.

## Angepasste build.xml

{% highlight bash %}
<?xml version = '1.0' encoding = 'windows-1252'?>
<project name="myproject" default="build">
  <property name="orcas_dir" value="/orcas/orcas/orcas_core"/>
  <import file="${orcas_dir}/orcas_default_tasks.xml"/>

  <property name="orcas.default_user" value="myschema"/>
  <property name="orcas.default_password" value="myschema"/>
  <property name="orcas.default_user_orcas" value="myschema_orcas"/>
  <property name="orcas.default_password_orcas" value="myschema_orcas"/>
  <property name="orcas.default_tnsname" value="XE"/>
  <property name="orcas.default_jdbcurl" value="jdbc:oracle:thin:@localhost:XE:1521"/>
  <property name="orcas.default_tmpfolder" value="tmp"/>
  <property name="orcas.default_spoolfolder" value="log"/>

  <target name="setup" >
    <orcas_install user="system" password="sa"/>
  </target>

  <target name="build" >
    <delete dir="${orcas.default_spoolfolder}"/>
    <orcas_initialize/>
    <orcas_execute_one_time_scripts scriptfolder="migrations_skripte" logname="migrations_skripte"/>
    <orcas_execute_statics scriptfolder="tables" logname="tables"/>
    <orcas_drop_replaceables logname="drop_replaceables"/>
    <orcas_execute_scripts scriptfolder="views" logname="views"/>
    <orcas_execute_scripts scriptfolder="packages" scriptprefix="h_" logname="package_specs"/>
    <orcas_execute_scripts scriptfolder="packages" scriptprefix="b_" logname="package_bodies"/>
    <orcas_compile_db_objects logname="compile_db_objects"/>
  </target>
</project>
{% endhighlight %}

Neu hinzugekommen ist diesmal der Block ab "orcas_drop_replaceables".

Zunächst mal sollen alle replaceables aus unserem Schema entfernt werden. Dazu dient der ant-task "orcas_drop_replaceables". Diesem könnte man weitere Parameter mit angeben, die bestimmte replaceables von der Löschung ausschließen, in unserem Setup soll aber alles gelöscht werden.

Danach führen wir unsere View-Skripte im Verzeichnis "views" aus.
Anschließend kommen unsere PL/SQL-Packages die im Verzeichnis "packages" liegen sollen. Hier sollen zuerst die specifications eingespielt werden (die in unserem Projekt-Setup am Dateianfang "h_" zu erkennen sind), Danach die Bodies ("b_").

Prinzipiell könnten wir alle Datenbank-Objekt-Skripte in ein Verzeichnis legen und ausführen. Dadurch dass wir die Objekte anschließend noch compilieren ist die Reihenfolge relativ irrelevant. Die Aufteilung in verschiedene Ordner und getrenntes Einspieln von specification und body dient eher der Übersichtlichkeit.

Abschließend compilierne wir alle Datenbankobjekte mit "orcas_compile_db_objects". Dieser Task bricht mit einem build-Fehler ab, wenn nicht alle Objekte compiliert werden konnten.

## Orcas starten

Zunächst mal müssen wir die benötigten Verzeichnisse anlegen:

{% highlight bash %}
Verzeichnis: myproject
mkdir views
mkdir packages
{% endhighlight %}

Wir starten unser Projekt wie gewohnt:

{% highlight bash %}
Verzeichnis: myproject
ant 
{% endhighlight %}


Erst mal passiert wieder nichts, aber sobald wir in den neuen Verzeichnissen Skripte ablegen, werden diese verarbeitet.

