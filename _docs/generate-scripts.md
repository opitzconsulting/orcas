---
layout: docs
title: Generieren der Statics-Tabellenskripte
prev_section: dbdoc
next_section: supported-db-functions
permalink: /docs/generate-scripts/
---

#Generieren der Statics-Tabellenskripte

##Übersicht

Für die Einführung von Orcas in einer bestehenden Datenbank, müssen zunächst vor allem die Tabellenbeschreibungen initial erstellt werden.
<br/>
Dabei gibt es verschiedene Möglichkeiten:

- orcas_extract
  - Sollte nach Möglichkeit immer genutzt werden
  - Erweiterbar/anpassbar
  - Nutzt [Extensions]({{site.baseurl}}/docs/extensions/)
  - Basiert auf [XSLT](http://www.w3schools.com/xsl/)
  - Kann keine Domain-Indizes (XML-Index, Oracle-Text)
  - Kann (noch) keine Partitionierung
  - Quell-DB muss Orcas installiert haben.
- Skripte von Hand neu schreiben
  - Aufwändig bei vielen Tabellen
  - Sehr flexibel
  - Fehleranfällig
  - Praktikabel für Projekte mit wenig Tabellen, die sehr "speziell" sind
- SQL-Skripte nutzen
  - Z.B. mit Toad generieren
  - Manuelle Anpassungen erforderlich, da Syntax abweicht
  - Praktikabel auch bei vielen Tabellen, wenn es nur bei wenigen Tabellen Syntaxabweichungen gibt
  - Ggf. mit Bereingungsskript kombinieren (z.B. UNIX sed)
- ORCAS-Skripte generieren
  - Automatisierbar (mehrfach durchführbar)
  - Auf verwendete Extensions optimierbar
  - Projektspezifisch

In Orcas gibt es unter trunk/orcas_scriptgenerator/static einen Bereich in dem projektspezifische Skriptgeneratoren gesammelt werden können.
