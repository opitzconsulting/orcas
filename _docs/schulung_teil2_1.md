---
layout: page
title: Teil 2.1 - Orcas ant-Tasks
permalink: /docs/schulung_teil2_1/
---

Im zweiten Teil der Schulung soll es darum gehen, wie man einen Orcas-Build-Ablauf so aufbaut, dass er für das eigene Projekt passt.

## Eigenes Projekt anlegen

Um mit Orcas ein DB-Schema (DB-User) zu verwalten benötigen wir natürlich erst mal ein Schema für unser Projekt:

{% highlight bash %}
SQL-Skript mit DBA-User auszuführen:
create user myschema identified by myschema;
grant connect to myschema;
grant resource to myschema;
{% endhighlight %}

Unser neues Beispielprojekt wird der Einfachheit halber im Verzeichnis "examples" angelegt: 

{% highlight bash %}
Verzeichnis: /orcas/orcas
cd examples
mkdir myproject
cd myproject
{% endhighlight %}

Unsere Tabellen sollen im Verzeichnis "tables" liegen:

{% highlight bash %}
Verzeichnis: myproject
mkdir tables
{% endhighlight %}

Im Verzeichnis "tables" soll initial die Datei "mytable.sql" angelegt werden. Diese Datei soll folgenden Inhalt haben:

{% highlight bash %}
create table mytable
(
  mycolumn         number(10)        
);

{% endhighlight %}

## Initiale build.xml

Orcas wird immer über [ant](http://ant.apache.org/) gesteuert.
Dazu wird eine Datei mit dem Namen "build.xml" benötigt.

Diese Datei wird im Verzeichnis "myproject" abgelegt werden und soll initial folgenden Inhalt haben:

{% highlight bash %}
<?xml version = '1.0' encoding = 'windows-1252'?>
<project name="myproject" default="build">
  <property name="orcas_dir" value="/orcas/orcas/orcas_core"/>
  <import file="${orcas_dir}/orcas_default_tasks.xml"/>

  <property name="orcas.default_user" value="myschema"/>
  <property name="orcas.default_password" value="myschema"/>
  <property name="orcas.default_user_orcas" value="myschema"/>
  <property name="orcas.default_password_orcas" value="myschema"/>
  <property name="orcas.default_tnsname" value="XE"/>
  <property name="orcas.default_jdbcurl" value="jdbc:oracle:thin:@localhost:XE:1521"/>
  <property name="orcas.default_tmpfolder" value="tmp"/>
  <property name="orcas.default_spoolfolder" value=""/>

  <target name="build" >
    <orcas_initialize/>
    <orcas_execute_statics scriptfolder="tables"/>
  </target>
</project>
{% endhighlight %}

Mit der "build.xml"-Datei ist unser Projekt grundsätzlich mal lauffähig und kann über ant gestartet werden:

{% highlight bash %}
Verzeichnis: myproject
ant
{% endhighlight %}

Anschließend sollte in unserem Datenbankschema die Tabelle "mytable" angelegt worden sein (zusätzlich noch zwei ORCAS_ Tabellen, die uns erst mal nicht weiter stören sollen).

## Die initiale build.xml im Detail

Nach der obligatorischen ersten XML-Zeile folgt die Definition eines ant-projects. Hier geben wir nur den Namen des Projekts an (für Orcas irrelevant) und ein default-target. Das default-target sorgt dafür, dass wir ant ohne weiteren Parameter starten können, ansonsten müssten wir "ant build" aufrufen.

Innerhalb vom ant-project wird zunächst das property "orcas_dir" gesetzt. Dieses muss gesetzt werden, da es auch von Orcas verwendet wird. Der Wert ist der Pfad zum Verzeichnis "orcas_core". Dieser Pfad kann absolut angegeben werden (wie in unserem Beispiel), oder auch relativ (in unserem Beispiel wäre das: "../../orcas_core"). 

*Hinweis zu ant-Pfadangaben: Innerhalb von ant kann als Verzeichnis-Trennzeichen sowohl "/" (Unix-Standard) als auch* "\\" *(Windows-Standard) verwendet werden. Es empfiehlt sich aber grunsätzlich mit "/" zu arbeiten, weil* "\\" *in einigen Fällen als Escape-Zeichen interpretiert wird und somit zu Fehlern führen kann. Ein "guter" absoluter Windows-Pfad wäre also beispielsweise: "C:/orcas/orcas_core".*

Nachfolgend wird in der build.xml die "orcas_default_tasks.xml"-Datei importiert, in der alle benötigten ant-Komponenten von Orcas enthalten sind.

In unserem Beispiel arbeiten wir mit den orcas.default-properties, die als nächstes gesetzt werden (vgl. [Allgemeine Attribute]({{site.baseurl}}/docs/ant-tasks/#general)). 
Zunächst einmal benötigt Orcas die Datenbank-Parameter bestehend aus username, password, tnsname und jdbcurl.
Für username und password gibt es jeweils zwei Parameter, und zwar je einen für das zu verwaltende Schema (default_user) und einen für das Orcas-Schema (default_user_orcas).
Orcas legt viele eigene Datenbank-Objekte an, da Orcas im Kern in PL/SQL realisiert ist. Es wird daher empfohlen Orcas in einem separaten Schema anzulegen, insbesondere dann, wenn das eigene Projekt selbst PL/SQL-Komponenten enthält. In unserem einfachen Beispiel haben wir das aber nicht gemacht und somit default_user=default_user_orcas gesetzt.

Die JDBC-URL ist nur der Vollständigkeit halber gesetzt worden, wir werden sie erst später benötigen.

Der tmpfolder ist das Verzeichnis, in dem Orcas viele eigene Dateien abgelegt, die für die Ausführung von Orcas benötigt werden. Orcas legt diesen Ordner bei Bedarf selbst an. 

Das Spooling (dazu später mehr) haben wir durch das Setzen auf "" deaktiviert. 

*Hinweis zu ant-properties: Wir haben hier die einfachste Möglichkeit genutzt um ant properties zu setzen. Es gibt zahlreiche weitere Möglichkeiten dies zu tun ([siehe auch: ant manual](http://ant.apache.org/manual/properties.html)). Einige werden im Verlauf der Schulung noch genutzt werden. Es ist somit **nicht** nötig die **Datenbank-Passwörter** in unsere **build.xml**-Datei zu schreiben.*

Abschließend findet sich in unserer "build.xml" ein ant-target. Ein ant-target kann man gezielt über den ant-Kommandozeilen Befehl starten. Da wir unser ant-target "build" als default-target im ant-project gesetzt haben (siehe Zeile 2), können wir ant ohne weiteren Parameter starten und unser "build"-target wird ausgeführt.

Innerhalb unseres ant-targets benutzen wir zwei orcas-ant-tasks. 

Der Erste ist "orcas_initialize" und dient dazu Orcas "einzurichten" (Orcas wird zum einen aus den Sourcen kompiliert und zum anderen müssen die Orcas-Datenbankobjekte wie z.B. PL/SQL-Packages in dem Datenbankschema eingespielt werden). "orcas_initialize" muss immer ausgeführt werden bevor irgendein anderer orcas-ant-task ausgeführt werden kann (mit sehr wenigen Ausnahmen).

Der zweite orcas-ant-task ist "orcas_execute_statics". Dieser startet den eigentlichen Orcas-Ablauf und ist somit auch der, der unsere Tabelle "mytable" angelegt hat.

## Weitere Schritte

In den nachfolgenden Übungen wird unser Beispielprojekt Schritt für Schritt erweitert. Das bedeutet nicht, dass jeder dieser Schritte für jedes Projekt sinnvoll ist.

