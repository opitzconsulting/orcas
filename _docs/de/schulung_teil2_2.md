---
layout: page
title: Teil 2.2 - Eigenes Orcas Schema
permalink: /docs/de/schulung_teil2_2/
categories: 
- de
---

Wie schon gesagt, die empfohlene Vorgehensweis ist nicht Orcas im selben Schema zu installieren das auch abgeglichen werden soll. Daher werden wir unser Projekt auf die Nutzung eines separaten Orcas-Schemas umstellen.

Zunächst einmal sollten wir dazu unser bestehendes Schema neu aufsetzen, um die Orcas-DB-Objekte wieder los zu werden (prinzipiell könnten wir dazu auch Orcas-Mittel nutzen, die "kennen" wir aber momentan noch gar nicht).

{% highlight bash %}
SQL-Skript mit DBA-User auszuführen:
drop user myschema cascade;
create user myschema identified by myschema;
grant connect to myschema;
grant resource to myschema;
{% endhighlight %}

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
  <property name="orcas.default_spoolfolder" value=""/>

  <target name="setup" >
    <orcas_install user="system" password="sa"/>
  </target>

  <target name="build" >
    <orcas_initialize/>
    <orcas_execute_statics scriptfolder="tables"/>
  </target>
</project>
{% endhighlight %}

Die wichtige Änderung ist die, dass wir als "user_orcas" jetzt nicht mehr "myschema" verwenden. Stattdessen haben wir "myschema_orcas" gesetzt. 

Um diesen User anzulegen verwenden wird den ant-task "orcas_install". Für diesen haben wir ein ant-target mit dem Namen "setup" erstellt. Für den Aufruf von "orcas_install" müssen wir einen User (samt Passwort) angeben, der die Berechtigung hat andere User anzulegen. Nachdem unser "myschema"-User diese Rechte nicht hat verwenden wir hier den "system"-User. 

Um das Problem, das dessen Passwort (genau wie die anderen auch) nicht in der build.xml stehen sollte, kümmern wir uns später. 

Es besteht auch die Möglichkeit diesen User manuell (ohne "orcas_install") anzulegen bzw. durch einen DBA anlegen zu lassen.

## Orcas starten

Um unser Projekt jetzt nutzen zu können, müssen wir einmalig unser setup-target aufrufen:

{% highlight bash %}
Verzeichnis: myproject
ant setup 
{% endhighlight %}

Danach können wir wie gewohnt den Abgleich starten:

{% highlight bash %}
Verzeichnis: myproject
ant 
{% endhighlight %}

Das Ergebnis ist jetzt, dass im myproject nur noch unsere "mytable" existiert, die beiden ORCAS_-Tabellen liegen jetzt im Schema "myschema_orcas".


