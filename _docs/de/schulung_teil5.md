---
layout: page
title: Teil 5 - Einsatzszenarien
permalink: /docs/de/schulung_teil5/
categories: 
- de
---

Orcas unterstützt viele verschiedene Einsatzszenarien. In diesem Teil sollen einige davon exemplarisch vorgestellt werden. Die Einsatzszenarien beziehen sich dabei auf den Tabellenabgleich, die Handhabung von "Replaceables" (z.B. Views oder Packages) wird hier nicht berücksichtigt.
Auch steht primär der Einsatz auf Produktionsumgebungen im Fokus, mit orcas sollte der Einsatz auf Entwicklungs- und Test-Umgebungen generell sehr einfach sein.

## Direkt auf der Produktion
Bei diesem Scenario wird orcas auf der Produktions-DB installiert und ausgeführt.

### Vorteile: 

* Die Produktions-DB entsprich sicher dem Soll-Zustand.
* Wenig Aufwand.

### Nachteile: 

* Relativ unsicher, da Fehler ggf. erst bei Produktivnahme auftreten.
* Keine vorherige (manuelle) Kontrolle der ausgeführten SQL-Statements möglich.
* Orcas muss in der Produktionsumgebung installiert sein.

## Direkt auf der Produktion mit "logonly"

Bei diesem Scenario wird orcas auf der Produktions-DB installiert und ausgeführt. Allerdings im logonly-Modus, der nur die SQL-Skripte erzeugt. Dies werden dann anschließend manuell ausgeführt

### Vorteile: 

* Die Produktions-DB entsprich sicher dem Soll-Zustand.
* Relativ wenig Aufwand.

### Nachteile: 

* Etwas unsicher, da Fehler im build-Ablauf oder in orcas ggf. doch Auswirkungen auf die Produktion haben könnten.
* Orcas muss in der Produktionsumgebung installiert sein.

## Über eine Produktions-Export

Bei diesem Scenario wird die Produktions-DB auf eine Test-Datenbank kopiert (z.B. via imp/exp).
Auf dieser Kopie wird dann orcas ausgeführt, dabei wird das Logging aktiviert. Die Log-Skripte werden dann bei der Produktiv-Installation verwendet.

### Vorteile: 

* Die Änderungen an der Produktionsumgebung sind vorher getestet.

### Nachteile: 

* Bei großen Produktionsdatenbanken (im TB-Bereich) ist das Kopieren Zeitaufwändig.

## Über eine Referenz-Datenbank

Bei diesem Scenario wird eine Datenbank benötigt, die (zumindest strukturell) mit der Produktions-Datenbank identisch ist. 
Auf dieser Datenbank wird dann orcas ausgeführt, wiederum mit aktiviertem Logging. 

### Vorteile: 

* Es wird keine Datenbank mit Produktionsdaten benötigt. Entwicklungs- und Produktionsumgebung können sehr stark getrennt werden.

### Nachteile: 

* Es muss sichergestellt werden, dass die Referenz-Datenbank und die Produktions-Datenbank synchron bleiben.
* Etwaige Problem mit Datenkonstellationen könne nicht frühzeitig erkannt werden.
* Die Laufzeit der Datenbankskripte kann nicht ermittelt werden.





