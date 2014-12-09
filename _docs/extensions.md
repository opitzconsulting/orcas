---
layout: docs
title: Extensions
prev_section: dev-docs
next_section: features
permalink: /docs/extensions/
---

#Extensions

##Was sollte man über Extensions wissen?

Extensions ermöglichen viele sinnvolle Zusatzfunktionen bei der Arbeit mit der OC-Schemaverwaltung die man normalerweise gar nicht in erwägung ziehen würde.
<br/>Deshalb ist es sinnvoll sich damit vor Einsatz der OC-Schemaverwaltung kurz zu beschäftigen, um zu klären ob man diese Zusatzfunktionalität nicht im eigenen Projekt gut gebrauchen könnte. Zum Beispiel könnte man die Konvention umsetzen, dass die erste Spalte immer der PK sein muss, indem man daraus die PK-Angabe generiert. Extensions sind immer Projektspezifisch.

##Wie erstelle ich Extensions?

- In Java

  Extensions sind Java-Klassen (es besteht der Plan auch PL/SQL als "Extensionsprache" zu ermöglichen). Eine Extension-Java-Klasse implementiert ein spezielles Interface (OcSvwExtension oder BaseSyntaxExtension) und hat damit die Möglichkeit die Schemadefinition bzw. die Struktur der Schemadaten mehr oder weniger beliebig zu ändern.

- in PL/SQL

  Extensions in PL/SQL bestehen aus einem Package body. Das Skript dafür wird in das Extension-Verzeichnis als .sql-Datei abgelegt. Die Specification wird automatisch von der Schemaverwaltung generiert. Sie enthält eine einzige Function: "function run( p_input in ot_syex_model ) return ot_syex_model;".

Bevor man eine eigene Extension schreibt sollte man sich die Beispiel-Extensions ansehen, bei komplexeren Extensions sollte man vorher jemanden fragen der sich damit auskennt.

##Wie funktionieren Parameter mit Extensions?

Es besteht die Möglichkeit Extensions zu parametrisiern. Dazu gibt es die Möglichkeit eine Textvariable an alle Extensions zu übergeben. Dazu wird bei dem ant-Task `todo: Link zu den ant-Tasks`oc_svw_initialize das Attribut extensionparameter verwendet. Es gibt eine spezielle Basisklasse (OcSvwBaseExtensionWithParameter) für Parametrisierte Extensions. Diese ermöglicht die Nutzung von mehreren Parametern. Dazu wird die Testvariable in eine Map überführt, auf einen Parameter wird dann mit hilfe der Funktion getParameterAsMap zugegriffen. Beispiel: Um zu dem Parameter mit dem Namen "tablespace" den Wert "testtabs" auszulesen, könnte die Textvariable den folgenden Wert haben: "[xy:15,tablespace:testtabs]".

##Bestehende Extensions

Es gibt einige Extensions, die als Kopiervorlage für das eigene Projekt genutzt werden können. Einige Extensions werden, sofern sie eingebunden sind, über die Angabe eines Alias aktiviert.

```sql
create table business_partners alias busi (...);
```

##Alias to ID und PK

Mit der Angabe des Aliases wird automatisch eine Spalte ALIAS_ID (Number) und ein Primärschlüssel ALIAS_PK erstellt.

##Alias to Sequence

Mit der Angabe des Aliases wird automatisch zur Tabelle eine Sequence TABLE_NAME_SEQ erstellt.

##Alias to Benutzerstempel

Diese Vereinfachung kann aktiviert werden, wenn nach der Definition der Spalten im Tabellenskript, das Schlüsselwort "BENUTZERSTEMPEL" eingefügt wird. Dadurch werden vier zusätzliche Spalten eingefügt.

- ALIAS_INSERT_DATE
- ALIAS_INSERT_USER
- ALIAS_UPDATE_DATE
- ALIAS_UPDATE_USER

##Find Foreign Key

Die Extension ermöglicht es, über die Angabe des Alias, die Quell und Zielspalten des Constraints selbst zu ermitteln. Die Extension unterstützt nur einspaltige Primärschlüssel.

```sql
constraint BEISPIEL_FK foreign key references TESTTABELLE
```
