---
layout: docs
title: Getting Started
permalink: /docs/getting-started/
prev_section:
next_section: examples
---

#Getting Started

##Git Projekt

- Orcas wird in einem eigenen [Repository](https://github.com/opitzconsulting/orcas) auf GitHub verwaltet.
- Die Dokumentation wird ebenfalls von GitHub gehostet (als [GitHub Page](https://pages.github.com/))
  - Um den Code der Dokumentation zu sehen im Repository auf den [gh-pages Branch](https://github.com/opitzconsulting/orcas/tree/gh-pages) umstellen
- Bei Interesse bei der Weiterentwicklung zu unterstützen bitte den Anweisungen unter [Hilf uns bei der Weiterentwicklung!](http://localhost:4000/docs/participate/) folgen

<a id="tools"/>

##Benötigte Tools

###ant

Zu finden unter [apache.org](http://ant.apache.org/). Bis auf weiteres kann die aktuellste Version verwendet werden.

###ant-contrib

Zu finden unter [sourceforge.net](http://sourceforge.net/projects/ant-contrib/) z.B: ant-contrib-1.0b3.jar. Die Datei muss nach ANT_HOME/lib kopiert werden.

###Gradle

Zu finden unter [gradle.org](http://www.gradle.org/). Bis auf weiteres kann die aktuellste Version verwendet werden.
<br/>Der Befehl gradle muss im PATH aufgenommen sein, oder GRADLE_HOME muss gesetzt sein.
<br/>Gradle baut ggf. eine Verbindung zum Maven-Central-Repository auf, es muss als eine Internet-Verbindung bestehen.

###Java

Benötigt wird mindestens Java 6 (1.6). **Wichtig**: Es muss ein **JDK** sein, kein JRE. Es reicht eine "Standard Edition" (SE), z.B. "Java SE 6u45". Nach der Installation sollte javac die richtige Version ausgeben:
{% highlight bash %}
javac -version
javac 1.6.0_12
{% endhighlight %}
Falls nicht, muss ggf. die PATH-Variable angepasstw erden. Java wird bei Orcas nur von ant verwendet, daher evtl. auch ant-Konfigurationsmöglichkeiten beachten (z.B: wird JAVA_HOME von ant **vor** PATH ausgewertet).

###ORACLE Client

SQL\*Plus muss an der Kommandozeile aufrufbar sein. Genauso sollte ein tnsping auf die Zieldatenbank funktionieren. Orcas benutzt im Kern immer TNS und SQL\*Plus, nur einige Zusatztools (z.B. dbdoc) nutzen JDBC.

###Projektsetup

``todo: Doku stimmt nicht mit Realität überein!``

Orcas hat sechs Hauptverzeichnisse:

- examples
- orcas_core
- orcas_dbdoc
- orcas_extensions
- orcas_integrationstest
- orcas_scriptgenerator

Unter **examples** befinden sich eine Reihe von Beispielprojekten die die Nutzung von Orcas zeigen. Hier kann man sich ein [Beispielprojekt]({{site.baseurl}}/docs/examples/) aussuchen und als Kopiervorlage für das eigene Projekt verwenden.

Unter **orcas_core** liegt der Hauptbestandsteil von Orcas. Dieses Verzeichnis wird benötigt um Orcas laufen zu lassen. Das Verzeichnis kann aus einem konkreten Projekt heraus referenziert oder in ein Projekt kopiert werden. Der Inhalt des Verzeichnisses darf jedoch nicht geändert werden. Sollten solche Änderungen gewünscht sein, bitte immer mit den Entwicklern von Orcas [abstimmen](https://github.com/opitzconsulting/orcas/issues), da mit großer Wahrscheinlichkeit einer der folgenden Gründe vorliegt:

- Es gibt ein Verständnisproblem und die gewünschte Funktionalität kann auf einem anderen, dafür vorgesehenen, Weg erreicht werden.
- Es gibt einen Bug, dieser sollte für Orcas immer behoben werden.
- Es wird eine Erweiterung benötigt, diese sollte entweder in Orcas aufgenommen werden oder projektspezifisch, dann aber in einem anderen projekteigenen Verzeichnis.

Unter **orcas_dbdoc** befindet sich das Tool [dbdoc]({{site.baseurl}}/docs/dbdoc/). Mit dbdoc lässt sich eine grafische Darstellung der Datenbankschemata erstellen.

Unter **orcas_extensions** befinden sich die [Extensions]({{site.baseurl}}/docs/extensions/). Hier kann man Extensions auswählen, die in einem Projekt eingesetzt werden sollen. Diese Dateien sollten ins eigene Projekt kopiert werden und können dort angepasst werden. Natürlich ist es auch erlaubt eigene Extensions zu schreiben. Auf Extensions kann aber auch ganz verzichtet werden.

Unter **orcas_integrationstest** ist der [Integrationstest]({{site.baseurl}}/docs/integration-tests/) abgelegt. Dieser überprüft die korrekte Arbeitsweise von Orcas durch automatisch gesteuerte Testabläufe auf verschiedenen Datenbanksystemen. Der Integrationstest kann auch lokal ausgeführt und um eigene Tests erweitert werden.

Unter **orcas_scriptgenerator** liegt das [PL/SQL-Package]({{site.baseurl}}/docs/generate-scripts/) zur automatischen Generierung der Tabellenbeschreibung.

###DB-Objekte extrahieren

Wenn das Projekt bereits Datenbankobjekte enthält, die in Orcas übernommen werden sollen, müssen diese in eine passende Skriptform gebracht werden.
Das kann wahlweise von Hand geschehen oder mittels eines Skriptes ([Details]({{site.baseurl}}/docs/generate-scripts/)), das zur Generierung der Tabellenbeschreibungen verwendet werden kann.

####Tabellen und Sequences

Diese können am einfachsten manuell über deren Quellskripte extrahiert werden. Dazu sind die Hinweise zur [Statics Syntax]({{site.baseurl}}/docs/statics-syntax/)) zu beachten.

![]({{site.baseurl}}/assets/error.png) Andere DB-Objekte

###Aufruf des ant-Skriptes

Empfohlen wird das ant-Skript im Quiet-Modus zu starten:
{% highlight bash %}
ant -q build_all
{% endhighlight %}
Allerdings sieht man dann auch nicht, welche ant-Targets in welcher Reihenfolge ausgeführt werden.
