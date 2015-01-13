---
layout: page
title: Wie arbeitet man mit Orcas?
permalink: /docs/usage/
---

Diese Dokumentation beschriebt wie man mit Orcas arbeitet. Dazu wird das Orderentry-Beispiel verwendet, dass man idealerweise bereits [installiert]({{site.baseurl}}/docs/examples/) hat.

Mit Orcas werden alle Tabellen (und andere Datenbankobjekte) in normalen Textdateien abgelegt. Diese wird man dann typischerweise in einer Versionsverwaltung zusammen mit den andren Sourcen eines Projektes ablegen. Somit kann man sie weitestgehend problemlos mergen oder branchen.

Die Aufgabe von Orcas besteht darin die Dateien zu lesen, mit einem bestehden Datenbank-Schema zu vergleichen und die ggf. notwendigen Änderungen in dem Schema auszuführen.

Im Orderentry-Beispiel kann man also z.B. in der Datei orderentry\db\tabellen\categories.sql
{% highlight sql %}
create table categories
(
  ctgr_id      number(15)                  not null,
  version      number(15)      default "0" not null,
  name         varchar2(30)                not null,
  description  varchar2(1000)              not null,

  constraint ctgr_pk primary key (ctgr_id),
  constraint ctgr_uc unique (name)
);
{% endhighlight %}

Eine neue Spalte einfügen:

{% highlight sql %}
create table categories
(
  ctgr_id        number(15)                  not null,
  version        number(15)      default "0" not null,
  name           varchar2(30)                not null,
  description    varchar2(1000)              not null,
  image_location varchar2(1000),

  constraint ctgr_pk primary key (ctgr_id),
  constraint ctgr_uc unique (name)
);
{% endhighlight %}

Orcas starten, und die neue Spalte ist in der Datenbank.

Auf die gleiche Art und Weise würde man auch andere Änderungen durchführen:

- Default-Werte ändern
- Constarinst hinzufügen oder löschen
- "not null" setzen oder entfernen ("not null" setzen geht natürlich nur, wenn in der jeweiligen Tabellen-Spalte durchgängig Daten enthalten sind)
- Tabellen anlegen (sinnvollerweise in einer neuen Datei)
- Foreign-Keys anlegen, dabei sorgt Orcas für die richtige Reihenfolge beim Anlegen
- Datentyp/Länge ändern. Dabei ist das vergrössern einer Spalte realtiv problemlos möglich, dass Verkleinern oder Typ-Änderungen werden nur erfolgreich durchlaufen, wenn die enthaltenen Daten in der Tabelle dazu passen.

## Löschen von Teilen des Datenmodells

Es gibt zwei Änderungen die standardmässig **gesperrt** sind und dementsprechend nicht fehlerfrei durchlaufen:
- Spalte löschen
- Tabelle löschen
Beide Änderungen laufen durch wenn die Tabelle leer ist (bzw. bei Spalten auch wenn nur null-Werte vorhanden sind). In dem Orderentry-Beispiel wird das also erst mal funktionieren (da alle Tabellen initial leer sind), wenn man in die Tabellen aber Daten schreibt, dann führt diese zu einer Fehlermeldung. 
<br/>*Hinweise*: Im Fehlerfall wird gar keine Änderung am Schema durchgeführt, somit kann es nicht passieren, dass eine Überführung nur "halb" durchgeführt wurde.

Diese Sperre lässt sich mit dem sogenannten "dropmode" umgehen. Im Orderentry-Beispiel müsste man dazu im build.xml den Eintrag *dropmode="false"* in *dropmode="true"* ändern. 

**Den "dropmode" zu aktivieren wird aber generell nicht empfohlen, da es in einigen Fällen (einspielen alter Version / Merge-Fehler / Umbenennung) zu Datenverlust kommen kann, somit sollte zummindest auf Produktions-Datenbanken der "dropmode" nicht aktiviert werden.**

## Erweiterung des Datenmodells
In typischen Projekten sind 90-95% der Änderungen am Datenmodell Erweiterungen, die normalerweise Problmelos mit Orcas eingespielt werden können.

## Veränderung des Datenmodells
Veränderungen die sich nicht direkt auf Daten beziehen (z.B. Index um eine Spalte erweitern) sind in der Regel auch noch problemlos durch Orcas zu verarbeiten.

Sobald aber eine **Datenmigration** erforderlich ist, kann Orcas nicht mehr durch einen einfachen Abgleich die notwendigen Änderungen erkennen. Dazu gehören auch die beiden Anwendungsfälle **Tabelle umbenennen** und **Spalte umbenennen**.

Orcas bietet zwei Möglichkeiten an solche Änderungen am Datenmodell durchzuführen.

###1. one_time_scripts
Bei dieser Möglichkeit werden SQL*Plus-Skripte auf jedem Datenbankschema nur genau ein mal ausgeführt. Das Orderentry-Beispiel ist so eingerichtet, dass alle Skripte unter orderentry\db\skripte so gehandhabt werden. 

###2. liquibase
Mit [liquibase](http://www.liquibase.org/) ist normalerweise für jede Änderung an einem Schema eine Änderungsanweisung im database-change-log notwendig. Orcas bietet die Möglichkeit diese Änderungsanweisungen auf ein Minimum zu reduzieren.

Bei beiden Varianten ist zu beachten, dass Entwickler **vergessen** können ein entsprechendes one-time-script bzw. einen database-change-log-Eintrag zu erstellen. Für diese Fälle ist der **dropmode** sehr wichtig, da damit **verhindert** wird, dass es in solchen Fällen zu **Datenverlust** kommen kann.

Zudem müssen die Tabellenänderungen immer auch in den Tabellen-Skripten erfolgen.

### [Warum nimmt man dann nicht gleich liquibase (ohne Orcas)?]({{site.baseurl}}/docs/liquibase/)
        




