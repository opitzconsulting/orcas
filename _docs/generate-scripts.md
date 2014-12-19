---
layout: docs
title: Generieren der Tabellenskripte
prev_section: dbdoc
next_section: supported-db-functions
permalink: /docs/generate-scripts/
---

#Generieren der Tabellenskripte

Für die Einführung von Orcas in einem bestehenden Datenbank-Projekt, müssen zunächst die Tabellenskripte initial erstellt werden.

Dazu steht orcas_extract zur Verfügung:

- Sollte nach Möglichkeit immer genutzt werden
- Erweiterbar/anpassbar
- Nutzt [Extensions]({{site.baseurl}}/docs/extensions/)
- Basiert auf [XSLT](http://www.w3schools.com/xsl/)
- Kann keine Domain-Indizes (XML-Index, Oracle-Text)
- Quell-DB muss Orcas installiert haben.

Im [Orderentry-Beispiel]({{site.baseurl}}/docs/examples/) kann man das direkt ausprobieren, in dem man **ant extract** ausführt. Die Tabellenskripte findet man dann unter **bin_orderentry\run\extract_output**.

