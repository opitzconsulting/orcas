---
layout: page
title: Generieren der Tabellenskripte
permalink: /docs/generate-scripts/
---

##Reverse Engineering

Für die Einführung von Orcas in einem bestehenden Datenbank-Projekt, müssen zunächst die Tabellenskripte initial erstellt werden.

Dazu steht orcas_extract zur Verfügung:

- Erweiterbar/anpassbar
- Nutzt [Extensions]({{site.baseurl}}/docs/extensions/)
- Basiert auf [XSLT](http://www.w3schools.com/xsl/)
- Kann keine Domain-Indizes (XML-Index, Oracle-Text)
- Quell-DB muss Orcas installiert haben.

Im [Orderentry-Beispiel]({{site.baseurl}}/docs/examples/) kann man das direkt ausprobieren, indem man **ant extract** ausführt. Die Tabellenskripte findet man dann unter **bin_orderentry\run\extract_output**.

##Warum tablespace XY nocompress logging noparallel?
Generell werden beim Generieren alle Informationen mit aufgenommen, auch wenn es sich um Default-Werte handelt.

Um diese Verhalten (oder auch andere Aspekte) zu beeinflussen kann man Extensions oder XSLT benutzen.

###Extension
Extensions kann man auch beim Reverse-Engineering einsetzen. Dann allerdings nur in PL/SQL (Java-Extensions sind *noch* nicht möglich).
Eine gute Vorlage dazu ist hier zu finden: orcas_domain_extension/extensions/pa_reverse_22_remove_defaults.sql.

###XSLT
Es besteht die Möglichkeit eine eigen XSLT-Datei mit anzugeben. Diese sollte die Original-XSLT-Datei (orcas_core/xslt_extract/orcas_extract.xsl) importieren (dazu <code>&lt;import href="orcas_extract.xsl"/&gt;</code> verwenden). Somit kann man die ganz normalen XSLT-Funktionalitäten nutzen, um die generierten Dateien anzupassen.
