---
layout: page
title: Teil 2.2 - one_time_scripts
permalink: /docs/schulung_teil2_3/
---

Im 1. Teil der Schulung haben wir gelernt, dass zur Orcas-Arbeitsweise auch Skripte gehören, mit denen notwenige Datenmigrationen durchgeführt werden können. Wie diese aufgesetzt werden könne sehen wir jetzt.

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
  <property name="orcas.default_spoolfolder" value=""/>

  <target name="setup" >
    <orcas_install user="system" password="sa"/>
  </target>

  <target name="build" >
    <orcas_initialize/>
    <orcas_execute_one_time_scripts scriptfolder="migrations_skripte" logname="migrations_skripte"/>
    <orcas_execute_statics scriptfolder="tables"/>
  </target>
</project>
{% endhighlight %}

Neu hinzugekommen ist der ant-task "orcas_execute_one_time_scripts". Diesem haben wir mitgetielt, dass unsere Skripte im Verzeichnis "migrations_skripte" liegen sollen. Diesem ant-task müssen wir einen eindeutigen logname geben (normalerweise ist dieser Optional).

## Orcas starten

Um unser Projekt jetzt nutzen zu können, müssen das "migrations_skripte" Verzeichnis noch anlegen:

{% highlight bash %}
Verzeichnis: myproject
mkdir migrations_skripte
{% endhighlight %}

Danach können wir wie gewohnt den Abgleich starten:

{% highlight bash %}
Verzeichnis: myproject
ant 
{% endhighlight %}

Dies führt erst mal nicht zu eienr wesentlichen Veränderung, da unser "migrations_skripte" Verzeichnis ja leer ist.

Um zu verifizieren, dass das auch funktioniert erstellen wir jetzt im Verzeichnis "migrations_skripte" die Datei "test1.sql" mit folgendem Inhalt:
{% highlight bash %}
select 'hello world' from dual
/

{% endhighlight %}

Ein erneuter ant-Aufruf sollte irgendwo in den log-Ausgaben "hello world" anzeigen. Dies aber nur beim ersten Aufruf, jeder weitere Aufruf sollte diese Ausgabe nicht mehr beinhalten.

Orcas protokolliert die Ausführung der one_time_scripts. Diese kann bei Bedarf eingesehen werden:

{% highlight bash %}
SQL-Skript mit DBA-User auszuführen:
select *
  from myschema_orcas.orcas_updates;
{% endhighlight %}

