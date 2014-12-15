---
layout: docs
title: Warum Orcas?
prev_section:
next_section: getting-started
permalink: /docs/why-orcas/
---

#Warum Orcas?

##Problemstellung

Folgende Problemfälle treten bei der Datenbankentwicklung immer wieder auf, meist genau dann, wenn man sie am wenigsten gebrauchen kann:

- Wieso ist die Spaltengröße auf dem Testsystem kleiner als auf der Entwicklungsdatenbank?
- Wieso fehlt auf der Produktionsdatenbank ein Index?
- Wer hat bei der Tabelle den PK vergessen?
- Warum muss ich „ALTER TABLE mytable ADD COLUM …“ schreiben? ich schreibe ja auch nicht „ALTER CLASS myclass.java ADD INSTANCE VARIABLE …“
- Welche SVN/Git-Version passt eigentlich noch zu meiner Entwicklungsdatenbank?
- Wer hat gerade meine View-Änderungen überschrieben?
- …

Wenn man nicht gerade damit beschäftigt ist den GO-LIVE-Termin zu retten oder zu erklären warum das Budget doch nicht gereicht hat, dann kommt man oft zu folgenden Fragestellungen:

- Wie kann man die Entwicklungs-, Test- und Produktionsdatenbank auf dem gleichen Versionsstand halten?
- Wie kann man Datenbanken versionieren, branchen oder mergen?
- Wie kann man einheitliche Datenbankkonventionen umsetzen?
- Wie kann man agile Datenbankentwicklung betreiben?
- Wie können mehrere Entwickler auf der Entwicklungsdatenbank parallel entwickeln?
<br/>Oder kurz:
- Wie macht man das mit der Datenbankentwicklung eigentlich richtig?

Orcas ist zwar kein Allheilmittel. Richtig eingesetzt, kann es aber bei all diesen Fragestellungen eine praktikable Lösung anbieten.

##Einführung

Orcas ist ein Deploymentframework mit dem ein bestehendes Schema in einem in Orcas beschriebenen Soll-Zustand überführt werden kann. Der Zustand des bestehenden Schemas ist dabei größtenteils irrelevant. Bei Bedarf werden „überflüssige" Indizes, Constraints, Spalten oder Tabellen verworfen bzw. neue Tabellen oder Spalten hinzugefügt. Änderungen von Datentypen werden, sofern möglich, durchgeführt. Der Soll-Zustand wird dabei in Form von einfachen SQL Skriptdateien vorgehalten, die in ihrer Syntax stark an die „CREATE TABLE" Syntax angelehnt sind.
Die Nutzung von Orcas hat viele Vorteile. Ein großer Vorteil ist, dass die Tabellenskripte versioniert werden können, was bei einem Projektteam eine enorme Erleichterung ist, da Änderungen nachvollzogen und auch rückgängig gemacht werden können. Ein weiterer Vorteil ist, dass ohne große Umstände auf verschiedenen Datenbanken deployed werden und somit ein einheitlicher Datenbankstand auf beliebig viele Schemata hergestellt werden kann.

##Dokumentation

Hier die wichtigsten Bereiche der Dokumentation mit kurzer Beschreibung:

- [Getting Started]({{site.baseurl}}/docs/getting-started/) - Wie kann ich Orcas in meinem Projekt einsetzen?
- [ant Tasks]({{site.baseurl}}/docs/ant-tasks/) - Wie erstelle ich einen Gesamtablauf mit ant?
- [Statics Syntax]({{site.baseurl}}/docs/statics-syntax/) - Wie sehen die Tabellenskripte aus?
- [Extensions]({{site.baseurl}}/docs/extensions/) - Wie kann ich Projektspezifische Syntaxerweiterungen integrieren?
- [Funktionsweise Orcas]({{site.baseurl}}/docs/how-it-works/) - Wie funktioniert Orcas?
- [Entwickler Dokumentation]({{site.baseurl}}/docs/dev-docs/) - Was ist bei der Entwicklung von Orcas zu beachten?

Wir empfehlen für den Beginn das [Getting Started]({{site.baseurl}}/docs/getting-started/) Kapitel und zusätlich die [Ant Tasks]({{site.baseurl}}/docs/ant-tasks/) und die [Statics Syntax]({{site.baseurl}}/docs/statics-syntax/).

Anleitungen für die Weiterentwicklung und Pflege von Orcas und dieser Dokumentation, bitte die Anleitungen im letzten Bereich der Dokumentation beachten (unter "Bei der Weiterentwicklung helfen").

##Welche Vorteile/Nachteile gibt es?

### Vorteile

- Der Soll-Zustand wird in einfachen Textskriptdateien verwaltet, damit kann man alle Vorteile einer Versionsverwaltung nutzen (Versionen nachhalten, nachvollziehen wer wann welche Änderung gemacht hat, einheitliche Versionsstände, Mergeunterstützung, ...).
- Die Skripte sind eine echte „Referenz", man muss also nicht in diversen Schemata suchen wenn man eine aktuelle Package-Version haben will bzw. man braucht keine organisatorische Festlegungen welches Schema als Referenzschema verwendet wird.
- Umständliche und fehleranfällige DB-Releaseskripte werden nicht benötigt.
- Es können beliebig viele Schemata für Entwicklungs- und Testzwecke erstellt werden, ohne dass ein gewaltiger Abgleichaufwand entsteht, was zudem noch sehr fehleranfällig ist.

###Nachteile

- Wenn Funktionen verwendet werden, die Orcas nicht unterstützt, müssen diese Teilbereiche „manuell" verwaltet werden.
- Projektmitarbeiter müssen wissen wie man mit Orcas arbeitet.
