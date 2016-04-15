---
layout: page
title: Was ist Orcas?
permalink: /de/
categories: 
- de
---

Orcas ist ein Open Source Deploymentframework mit dem ein bestehendes **Datenbankschema** in einen mit **Textdateien** beschriebenen Soll-Zustand **überführt** werden kann.  
Durch die Verwendung einfacher Textdateien ist eine **Integration** in bestehendende Projekte mit **Versionsverwaltung** denkbar einfach. Orcas bietet dazu auch die Möglichkeit **kompliziertere Überführungen** zu **integrieren**.  
Orcas hat zudem einen **Extension**-Mechanismus, mit dem die **Definition** des Datenbankschemas deutlich **vereinfach** und **vereinheitlicht** werden kann.

Orcas steht für **Or**a**c**le **a**daptive **s**chemas und ist nur mit Oracle RDBMS kompatibel.

## Problemstellung

Folgende Problemfälle treten bei der Datenbankentwicklung immer wieder auf, meist genau dann, wenn man sie am wenigsten gebrauchen kann:

- Wieso ist die Spaltengröße auf dem Testsystem kleiner als auf der Entwicklungsdatenbank?
- Wieso fehlt auf der Produktionsdatenbank ein Index?
- Wer hat bei der Tabelle den PK vergessen?
- Warum muss ich "ALTER TABLE mytable ADD COLUM" schreiben? Ich schreibe ja auch nicht "ALTER CLASS myclass.java ADD INSTANCE VARIABLE"
- Welche SVN/Git-Version passt eigentlich noch zu meiner Entwicklungsdatenbank?
- Wer hat gerade meine View-Änderungen überschrieben?

Wenn man nicht gerade damit beschäftigt ist den GO-LIVE-Termin zu retten oder zu erklären warum das Budget doch nicht gereicht hat, dann kommt man oft zu folgenden Fragestellungen:

- Wie kann man die Entwicklungs-, Test- und Produktionsdatenbank auf dem gleichen Versionsstand halten?
- Wie kann man Datenbanken versionieren, branchen oder mergen?
- Wie kann man einheitliche Datenbankkonventionen umsetzen?
- Wie kann man agile Datenbankentwicklung betreiben?
- Wie können mehrere Entwickler auf der Entwicklungsdatenbank parallel entwickeln?

Oder kurz:

- Wie macht man das mit der Datenbankentwicklung eigentlich richtig?

Orcas ist zwar kein Allheilmittel. Richtig eingesetzt kann es aber bei all diesen Fragestellungen eine praktikable Lösung anbieten.

## Einführung

Orcas ist ein Deploymentframework mit dem ein bestehendes Schema in einen in Orcas beschriebenen Soll-Zustand überführt werden kann. Der Zustand des bestehenden Schemas ist dabei größtenteils irrelevant. Bei Bedarf werden "überflüssige" Indizes, Constraints, Spalten oder Tabellen verworfen bzw. neue Tabellen oder Spalten hinzugefügt. Änderungen von Datentypen werden, sofern möglich, durchgeführt. Der Soll-Zustand wird dabei in Form von einfachen SQL Skriptdateien vorgehalten, die in ihrer Syntax stark an die "CREATE TABLE" Syntax angelehnt sind.
Die Nutzung von Orcas hat viele Vorteile. Ein großer Vorteil ist, dass die Tabellenskripte versioniert werden können, was bei einem Projektteam eine enorme Erleichterung ist, da Änderungen nachvollzogen und auch rückgängig gemacht werden können. Ein weiterer Vorteil ist, dass ohne große Umstände auf verschiedenen Datenbanken deployed werden und somit ein einheitlicher Datenbankstand auf beliebig vielen Schemata hergestellt werden kann.

## Dokumentation

Hier sind die wichtigsten Bereiche der Dokumentation mit kurzer Beschreibung:

- [Wie arbeitet man mit Orcas?]({{site.baseurl}}/docs/de/usage/)
- [Installation]({{site.baseurl}}/docs/de/installation/) - Was muss ich tun, um Orcas in meinem Projekt einsetzen zu können?
- [Examples]({{site.baseurl}}/docs/de/examples/) - Beispielprojekte
- [ant Tasks]({{site.baseurl}}/docs/de/ant-tasks/) - Wie erstelle ich einen Gesamtablauf mit ant?
- [Tabellen Syntax]({{site.baseurl}}/docs/de/statics-syntax/) - Wie sehen die Tabellenskripte aus?
- [Domain-Extension]({{site.baseurl}}/docs/de/domain-extension/) - Wie kann ich projektspezifische Erweiterungen einfach integrieren?
- [Extensions]({{site.baseurl}}/docs/de/extensions/) - Wie kann ich spezielle projektspezifische Erweiterungen integrieren?
- [Funktionsweise Orcas]({{site.baseurl}}/docs/de/how-it-works/) - Wie funktioniert Orcas?

## Welche Vorteile/Nachteile gibt es?

### Vorteile

- Der Soll-Zustand wird in einfachen Textskriptdateien verwaltet. Damit kann man alle Vorteile einer Versionsverwaltung nutzen (Versionen nachhalten, nachvollziehen wer wann welche Änderung gemacht hat, einheitliche Versionsstände, Mergeunterstützung, ...).
- Die Skripte sind eine echte "Referenz", man muss also nicht in diversen Schemata suchen wenn man eine aktuelle Package-Version haben will bzw. man braucht keine organisatorische Festlegungen welches Schema als Referenzschema verwendet wird.
- Umständliche und fehleranfällige DB-Releaseskripte werden nicht benötigt.
- Es können beliebig viele Schemata für Entwicklungs- und Testzwecke erstellt werden, ohne dass ein gewaltiger, fehleranfälliger Abgleichaufwand entsteht.

### Nachteile

- Wenn Datenbank-Funktionen verwendet werden, die Orcas nicht unterstützt, müssen diese Teilbereiche "manuell" verwaltet werden.
- Projektmitarbeiter müssen wissen, wie man mit Orcas arbeitet.
