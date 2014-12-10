---
layout: default
title: Getting Started
permalink: /docs/getting-started/
---

#Getting Started

##Git Projekt

`todo: GitHub Stuff; evtl. Link zu participate`

##Benötigte Tools

###ant

Zu finden unter http://ant.apache.org/. Bis auf weiteres kann die aktuellste Version verwendet werden.

###ant-contrib

Zu finden unter http://sourceforge.net/projects/ant-contrib/ z.B: ant-contrib-1.0b3.jar. Die Datei muss nach ANT_HOME/lib kopiert werden.

###Gradle

Zu finden unter http://www.gradle.org/. Bis auf weiteres kann die aktuellste Version verwendet werden.
Der Befehl gradle muss im PATH aufgenommen sein, oder GRADLE_HOME muss gesetzt sein.
Gradle baut ggf. eine Verbindung zum Maven-Central-Repository auf, es muss als eine Internet-Verbindung bestehen.

###Java

Benötigt wird mindestens Java 6 (1.6). Wichtig es muss ein **JDK** sein, kein JRE. Es reicht eine "Standard Edition" (SE) z.B. "Java SE 6u45". Nach der Installation sollte javac die richtige Version ausgeben:
```
javac -version
javac 1.6.0_12
```
Falls nicht, muss ggf. die PATH-Variable angepasstw erden. Java wird bei Orcas nur von ant verwendet, daher evtl. auch ant-Konfigurationsmöglichkeiten beachten (z.B: wird JAVA_HOME von ant vor PATH ausgewertet).

###ORACLE Client

sqlplus muss an der Kommandozeile aufrufbar sein. Genauso sollte ein tnsping auf die Zieldatenbank funktionieren. Die OC-Schemaverwaltung benutzt im Kern immer TNS und sqlplus, nur einige Zusatztools (z.B. dbdoc) nutzen jdbc.

###Projektsetup

Die OC-Schemaverwaltung hat sechs Hauptverzeichnisse:
- examples
- oc_svw_core
- oc_svw_dbdoc
- oc_svw_extensions
- oc_svw_integrationstest
- oc_svw_scriptgenerator

Unter **examples** befinden sich eine Reihe von Beispielprojekten die die Nutzung von Orcas zeigen. Hier kann man sich ein `todo: Link zu Beispielprojekt`Beispielprojekt aussuchen und als Kopiervorlage für das eigene Projekt verwenden.

Unter **oc_svw_core** liegt die eigentliche OC-Schemaverwaltung. Dieses Verzeichnis wird benötigt um die OC-Schemaverwaltung laufen zu lassen. Das Verzeichnis kann aus einem konkreten Projekt heraus referenziert, oder in ein Projekt kopiert werden. Der Inhalt des Verzeichnisses darf jedoch nicht geändert werden. Sollten solche Änderungen gewünscht sein, bitte immer mit den Entwicklern der OC-Schemaverwaltung abstimmen, da mit großer Wahrscheinlichkeit einer der folgenden Gründe vorliegt:
- Es gibt ein Verständnisproblem und die gewünschte Funktionalität kann auf einem anderen, dafür vorgesehenen, Weg erreicht werden.
- Es gibt einen Bug, dieser sollte immer in der OC-Schemaverwaltung behoben werden.
- Es wird eine Erweiterung benötigt, diese sollte entweder in die OC-Schemaverwaltung aufgenommen werden oder Projektspezifisch, dann aber in einem anderen projekteigenen Verzeichnis.

Unter **oc_svw_dbdoc** befindet sich das Tool `todo: Link zu dbdoc`dbdoc. Mit dbdoc lässt sich eine grafische Darstellung der Datenbankschemata erstellen.

Unter **oc_svw_extensions** befinden sich die `todo: Link zu Extensions`Extensions. Hier kann man Extensions auswählen, die in einem Projekt eingesetzt werden sollen. Diese Dateien sollten ins eigene Projekt kopiert werden und können dort angepasst werden. Natürlich ist es auch erlaubt eigene Extensions zu schreiben. Auf Extensions kann aber auch ganz verzichtet werden.

Unter **oc_svw_integrationstest** ist der `todo: Link zu Integrationstest`Integrationstest abgelegt. Dieser überprüft die korrekte Arbeitsweise der Schemaverwaltung durch automatisch gesteuerte Testabläufe auf verschiedenen Datenbanksystemen. Der Integrationstest kann auch lokal ausgeführt und um eigene Tests erweitert werden.

Unter **oc_svw_scriptgenerator** liegt das `todo: Link zu Generieren der Statics-Tabellenskripte`PL/SQL-Package zur automatischen Generierung der Tabellenbeschreibung.

###DB-Objekte extrahieren

Wenn das Projekt bereits Datenbankobjekte enthält, die in die OC-Schemaverwaltung übernommen werden sollen, müssen diese in eine passende Skriptform gebracht werden.
Das kann wahlweise von Hand geschehen oder mittels eines Skriptes (`todo: Link zu Generieren der Statics-Tabellenskripte`Details), das zur Generierung der Tabellenbeschreibungen verwendet werden kann.

####Tabellen und Sequences

Diese können am einfachsten manuell über deren Quellskripte extrahiert werden. Dazu sind die Hinweise zur `todo: Link zu Statics Syntax`Statics Syntax zu beachten.

![](/assets/error.png) Andere DB-Objekte

###Aufruf des ant-Skriptes

Empfohlen wird das ant-Skript im Quiet-Modus zu starten:
```
> ant -q build_all
```
Allerdings sieht man dann auch nicht, welche ant-Targets in welcher Reihenfolge ausgeführt werden.
