---
layout: page
title: Extensions
permalink: /docs/de/extensions/
categories: 
- de
---

## Was sollte man über Extensions wissen?

Extensions ermöglichen viele sinnvolle Zusatzfunktionen bei der Arbeit mit Orcas die man normalerweise gar nicht in Erwägung ziehen würde.
<br/>Deshalb ist es sinnvoll sich damit vor dem Einsatz von Orcas **kurz** zu beschäftigen, um zu **klären** ob man diese **Zusatzfunktionalität** nicht **im** eigenen **Projekt gut gebrauchen könnte**. Zum Beispiel könnte man die Konvention umsetzen, dass die erste Spalte immer der PK sein muss, indem man daraus die PK-Angabe generiert. Extensions sind immer projektspezifisch.

## Domain Extension

Die [Domain-Extension]({{site.baseurl}}/docs/de/domain-extension) ist eine vorgefertigte Extension, die in 80% der Fälle ausreicht. Da sie nur konfiguriert und benutzt werden muss, ist es immer ratsam die Domain-Extension zu nutzen und nicht selbst eine Extension zu erstellen, wenn die Funktionalität der Domain-Extension ausreichend ist.

## Wie funktionieren Extensions?

Eine Extension ist eine Programmkomponente, die die gesamte Datenstruktur aus den Tabellenskripten erhält. Diese Datenstruktur beinhaltet beispielsweise alle Tabellen mitsamt ihren Spalten, genauso wie sie in den Skripten definiert wurde. 
Eine Extension kann diese Datenstruktur beliebig verändern, z.B. kann man aus der Datenstruktur über alle Tabellen iterieren und zu jeder Tabelle eine neue Spalte mit dem Namen "ID" einfügen. Extensions können auch prüfen, ob bestimmte Konventionen eingehalten wurden und ggf. eine Exception werfen, wenn das nicht der Fall ist.

Extensions können auch für viele weiterführende Mechanismen eingesetzt werden. Eine PL/SQL-Extension kann z.B. relativ einfach die Datenstruktur komplett durch den Ist-Zustand im Datenbank-Schema ersetzen und daran Modifikationen durchführen (tablespace ändern, varchar2 von byte auf char umstellen...). Eine Java-Extension könnte bei Bedarf eine UML-XML-Datei oder JPA-Annotierte Java-Klassen auslesen und darüber direkt die Tabellen erzeugen. 
*Hinweis*: Aus technischen Gründen ist es notwendig, dass immer mindestens eine Tabelle in den Skripten vorhanden ist. Daher muss man im Zweifelsfall eine Dummy-Tabelle aufnehmen und diese ggf. in der Extension entfernen.

## Syntax-Extensions

Es ist auch möglich das Metamodell der Datenstruktur zu erweitern (also z.B. den Typ "Table" (Java-Klasse bzw. object-type) um ein Feld zu erweitern). Häufig wird beispielsweise die Alias-Syntax-Extension(orcas_extensions/AliasSyntaxExtension.java) eingesetzt, die es einfach nur ermöglicht, einer Tabelle einen Alias zu geben. Der Alias ist dabei eine eindeutige Kurzform (3-4 Buchstaben). 
<br>Beispiel: <code> create table MYTABLE alis MYAL ( ...</code>
<br>Dieser Alias wird dann in den eigentlichen Extensions genutzt um z.B. Namen für Constraints, Sequenzen und/oder Spalten zu generieren. Syntax-Extensions können nur in Java implementiert werden.

## Wie erstelle ich Extensions?

Extensions sind Dateien, die in den Extension-Folder gelegt werden (siehe auch: [orcas_initialize]({{site.baseurl}}/docs/de/ant-tasks/#orcas_initialize)).
Beispiele sind im Ordner orcas_extensions zu finden.
Ein Beispielprojekt, das Extensions verwendet, findet sich hier: [Extension-Demo]({{site.baseurl}}/docs/de/examples/#extension_demo):

- Java Extension

  Extensions sind Java-Klassen. Diese müssen das Interface (OrcasExtension) implementieren. Typischerweise erben die Extension-Klassen von OrcasBaseExtensionWithParameter oder TableVisitorExtension. Die Java-Klassen müssen im Package de.opitzconsulting.orcas.extensions sein. Die Dateien müssen direkt (ohne Unterverzeichnis) liegen.

- Java Syntax-Extension

  Syntax-Extensions sind Java-Klassen. Diese müssen von BaseSyntaxExtension abgeleitet sein. Die Java-Klassen müssen im Package de.opitzconsulting.orcas.syntax_extensions sein. Die Dateien müssen direkt (ohne Unterverzeichnis) liegen.

- PL/SQL Extension

  Extensions in PL/SQL bestehen aus einem Package body. Das Skript dafür wird in das Extension-Verzeichnis als .sql-Datei abgelegt. Die Specification wird automatisch von Orcas generiert. Sie enthält eine einzige Function: "function run( p_input in ot_syex_model ) return ot_syex_model;", diese muss im body implementiert werden.

- PL/SQL Reverse-Extension

  Werden genau so definiert, wie normale PL/SQL-Extensions, werden aber nicht beim normalen Ablauf, sondern nur beim [Reverse-Engineering]({{site.baseurl}}/docs/de/generate-scripts) ausgeführt. Eine PL/SQL Reverse-Extension muss das Schlüsselwort "reverse" im Namen haben.

- PL/SQL Helper-Package

  Es ist auch möglich, ein eigenes Package im Orcas-Schema zu haben. Dazu legt man einfach die Dateien (specification und body) im Extension-Folder ab. Diese müssen das Schlüsselwort "helper" im Namen haben.

Bevor man eine eigene Extension schreibt, sollte man sich die Beispiel-Extensions ansehen. Bei komplexeren Extensions sollte man vorher jemanden fragen, der sich damit auskennt.

## Wie funktionieren Parameter mit Extensions?

Es besteht die Möglichkeit, Extensions zu parametrisieren. Dazu gibt es die Möglichkeit, eine Textvariable an alle Extensions zu übergeben. Dazu wird bei dem ant-Task `todo: Link zu den ant-Tasks`orcas_initialize das Attribut extensionparameter verwendet. Es gibt eine spezielle Basisklasse (OrcasBaseExtensionWithParameter) für parametrisierte Extensions. Diese ermöglicht die Nutzung von mehreren Parametern. Dazu wird die Testvariable in eine Map überführt, auf einen Parameter wird dann mit Hilfe der Funktion getParameterAsMap zugegriffen. Beispiel: Um zu dem Parameter mit dem Namen "tablespace" den Wert "testtabs" auszulesen, könnte die Textvariable den folgenden Wert haben: "[xy:15,tablespace:testtabs]".

## Bestehende Extensions

Es gibt einige Extensions, die als Kopiervorlage für das eigene Projekt genutzt werden können. Einige Extensions werden, sofern sie eingebunden sind, über die Angabe eines Alias aktiviert.

{% highlight sql %}
create table business_partners alias busi (...);
{% endhighlight %}

## Alias to ID und PK

Mit der Angabe des Aliases wird automatisch eine Spalte &lt;ALIAS&gt;_ID (Number) und ein Primärschlüssel &lt;ALIAS&gt;_PK erstellt.

## Alias to Sequence

Mit der Angabe des Aliases wird automatisch zur Tabelle eine Sequence &lt;TABLE_NAME&gt;_SEQ erstellt.

## Alias to Benutzerstempel

Diese Vereinfachung kann aktiviert werden, wenn nach der Definition der Spalten im Tabellenskript, das Schlüsselwort "BENUTZERSTEMPEL" eingefügt wird. Dadurch werden vier zusätzliche Spalten eingefügt.

- &lt;ALIAS&gt;_INSERT_DATE
- &lt;ALIAS&gt;_INSERT_USER
- &lt;ALIAS&gt;_UPDATE_DATE
- &lt;ALIAS&gt;_UPDATE_USER

## Find Foreign Key

Die Extension ermöglicht es, über die Angabe des Alias die Quell und Zielspalten des Constraints selbst zu ermitteln. Die Extension unterstützt nur einspaltige Primärschlüssel.

{% highlight sql %}
constraint BEISPIEL_FK foreign key references TESTTABELLE
{% endhighlight %}
