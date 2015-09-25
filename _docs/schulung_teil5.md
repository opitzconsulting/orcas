---
layout: page
title: Teil 5 - Einsatzscenarien
permalink: /docs/schulung_teil5/
---

## Direkt auf der Produktion
Bei diesem Scenario wird orcas auf der Produktions-DB installiert und ausgeführt.

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




