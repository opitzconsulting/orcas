---
layout: docs
title: Beispielprojekte
prev_section:
next_section:
permalink: /docs/examples/
---

#Beispielprojekte

##Orderentry

**Das Orderentry-Beispiel ist das am besten dokumentierte und gepflegte Beispiel.**

Dieses Beispiel zeigt wie das Orderentry Beipiel, das in einigen OC-Schulungen verwendet wird, mit der OC-Schemaverwaltung verwaltet wird.
<br/>Das Beispiel setzt zusätzlich das "location" Konzept um, mit dem verschiedene Zielumgebungen (z.B. oc_entwicklung, oc_test, kunde_test, kunde_produktiv) über locations gehandhabt werden.
<br/>Dabei handelt es sich im wesentlichen um ein Setup zur Verwaltung von properties, das auch für andere Teile des Projektes genutzt werden kann (und soll).

###Setup

Bitte vorher prüfen, dass die benötigten Tools (`todo: Link zu Getting Started`Getting Started) funktionieren.
<br/>Es gibt 2 Varianten mit welchem Datenbank-Setup das Orderentry Beispiel genutzt werden kann:

####Eigene (lokale) Datenbank

#####Dazu benötigt man eine eigene Konfigurationsdatei

Dazu wechselt man in den Ordner examples\orderentry\distribution.
<br/>Darin gibt es fuer jede Konfiguration einen Unterordner (der Name des Unterordners ist der Namen der location). Hier bitte einen neuen Ordner erstellen (z.:b oc_xy_lokal).
<br/>In dem Ordner muss eine "location.properties" Datei liegen. Am einfachtsen kopiert man eine Datei als Vorlage aus einer anderen location. In der Datei liegen die Datenbankzugangsdaten. Z.B.:

```
#Database
database              =XE
jdbc_host             =localhost
jdbc_sid              =XE
jdbc_port             =1521
username_dba          =system
password_dba          =test
username_schemaowner  =orderentry
```

**Wichtig**: Die jdbc_XXX Einträge werden hier nicht ausgelesen. Wichtig ist insbesondere "database". Im Beispiel muss ein "tnsping XE" funktionieren (ansonsten evtl. TNS-Names konfigurieren).
<br/>Damit die Konfiguration auch genutzt wird, muss der location-Name (Name des Ordners unter examples\orderentry\distribution) in die Datei examples\orderentry\distribution\default_location.properties eingetragen werden.
<br/>Ob die Konfiguration auch verwendet wird, kann man wie folgt testen:

```
Verzeichnis: examples\orderentry\db
ant show_location
```

Die Ausgabe sollte wie folgt sein:

```
Buildfile: D:\ocschemaverwaltung\src\trunk\examples\orderentry\db\build.xml
show_location:
[echo] ================= Location: oc_xy_lokal =================
BUILD SUCCESSFUL
Total time: 0 seconds
```

Wichtig dabei ist, dass dort "Location: oc_xy_lokal" steht.

#####Einmalig die Schemaverwaltung auf der Datenbank einrichten

Mit ant install_all wird die Schemaverwaltung eingerichtet. Zusätzlich wird auch der Schemaowner angelegt:

```
Verzeichnis: examples\orderentry\db
ant install_all
```

Wenn das erfolgreich durchgelaufen ist, gibt es auf der Zieldatenbank zwei neue User:
- OC_SVW_ORDERENTRY (Schemowner, der die abzugleichenden Tabellen enthält)
- OC_SVW_ORDERENTRY_OC_SVW (User der die Schemverwaltung enthält)

Wenn der ant install_all-Lauf abbricht, dann müssen die beiden User ggf. vorher wieder gelöscht werden, damit ein erneuter Aufruf von install_all funktioniert.

#####Schemaverwaltung starten

```
Verzeichnis: examples\orderentry\db
ant
```

Ergebnis sollte eine Ausgabe sein, die am Ende einen Erfolg meldet:

```
...

BUILD SUCCESSFUL
Total time: 12 seconds
```

##Andere Beispiele

###dbdoc_demo

Beispielprojekt, generiert eine grafische Darstellung des Datenbankschematas mit `todo: Link zu dbdoc`dbdoc.

###extensions_demo

Beispielprojekt, bindet die `todo: Link zu Extensions`Extensions ein.

###fahrzeugverwaltung

Beipielprojekt, einfaches ausführen von Tabellenskripten.

###Scriptgenerator_demo

Ausführen des Scriptgenerators für einen beliebigen Datenbankuser. Die Datenbankverbindung kann unter "Distribution" angelegt werden (dort finden sich auch Kopiervorlagen).
<br/>Die generierten SVW-Tabellenskripte werden in einem lokalen Ausgabeverzeichnis angelegt.

Mehr (aktuellere) Informationen zum Scriptgenerator findet sich hier: `todo: Link zu Generieren der Statics Tabellenskripte`PL/SQL-Package

###Sonnensystem

Beipielprojekt, das den Einsatz der OC-Schemverwaltung in ihrer einfachsten Form zeigt.
