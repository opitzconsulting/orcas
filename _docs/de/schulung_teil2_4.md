---
layout: page
title: Teil 2.4 - Logging
permalink: /docs/de/schulung_teil2_4/
categories: 
- de
---

In dieser Übung werden wir das Logging aktivieren.

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
  <property name="orcas.default_jdbcurl" value="jdbc:oracle:thin:@localhost:1521:XE"/>
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
  </target>
</project>
{% endhighlight %}

Das property "default_spoolfolder" ist nun auf einen echten Wert gesetzt (nämlich auf einen Verzeichnis-Namen), damit ist das Logging aktiv. Bei aktivem Logging muss jeder Task der Loggen kann (dass sind fast alle abgesehen von "orcas_install" und "orcas_initialize"), einen logname-Parameter haben. Dieser sollte eindeutig sein. Am Anfang von unserem build-target ist noch das Löschen des spoolfolders hinzugekommen.

## Orcas starten

Wir starten unser Projekt wie gewohnt:

{% highlight bash %}
Verzeichnis: myproject
ant 
{% endhighlight %}

Ergebnis: Das "log"-Verzeichnis wurde nicht mal angelegt. Das liegt daran, dass Orcas keine Änderung an unserem Schema ausgeführt hat. Das ändert sich, wenn wir z.B. unser Tabelle erweitern:

{% highlight bash %}
create table mytable
(
  mycolumn         number(10),
  mycolumn2        number(10)
);
{% endhighlight %}

Ein nachfolgender ant-Aufruf wird dann dazu führen, dass die neue Spalte angelegt wird und das dies auch in unserem "log"-Verzeichnis protokolliert wird. Das "log"-Verzeichnis enthält danach eine "master_install.sql"-Datei. Wenn man diese mit SQL*Plus ausführt, wird in dem jeweiligen Schema genau das gemacht, was der ant-Aufruf auch gemacht hat. Hätten wir also vorher unser Schema kopiert (z.B. mit import/export), und das "master_install.sql" auf diesem Schema ausgeführt, dann wäre dort das Schema auch aktualisiert worden.

