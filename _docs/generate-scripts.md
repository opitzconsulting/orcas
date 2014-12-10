---
layout: docs
title: Generieren der Statics-Tabellenskripte
prev_section:
next_section:
permalink: /docs/generate-scripts/
---

#Generieren der Statics-Tabellenskripte

##Übersicht

Für die Einführung der Schemaverwaltung in einer bestehenden Datenbank, müssen zunächst vor allem die Tabellenbeschreibungen initial erstellt werden.
<br/>Dabei gibt es verschiedene Möglichkeiten:
- oc_svw_extract
  - Sollte nach Möglichkeit immer genutzt werden
  - Erweiterbar/Anpassbar
  - Nutzt Extensions
  - Basiert auf XSLT
  - Kann keine Domain-Indizes (XML-Index, Oracle-Text)
  - Kann (noch) keine Partitionierung
  - Quell-DB muss OC-Schemaverwaltung installiert haben.
- Skripte von Hand neu schreiben
  - Aufwändig bei vielen Tabellen
  - Sehr flexibel
  - Fehleranfällig
  - Praktikabel für Projekte mit wenigen Tabellen, die sehr "speziell" sind
- SQL-Skripte nutzen
  - Z.B. mit TOAD generieren
  - Manuelle Anpassungen erfolderlich, das Syntax abweicht
  - Praktikabel auch bei vielen Tabellen, wenn es nur bei wenigen Tabellen Syntax abwecihungen gibt
  - ggf. mit Bereingungsskript kombinieren (z.B. UNIX sed)
- OC_SVW-Skripte generieren
  - Automatisierbar (mehrfach durchführbar)
  - Auf verwendete Extensions optimierbar
  - Projektspezifisch

In Orcas gibt es unter trunk/oc_svw_scriptgenerator/static einen Bereich in dem Projektspezifische Skriptgeneratoren gesammelt werden können.
<br/>Aus diesem Grund gibt es auf dieser Seite eine kurze Übersicht über die verschiedenen Versionen.
