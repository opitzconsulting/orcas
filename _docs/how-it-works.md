---
layout: page
title: Funktionsweise von Orcas
permalink: /docs/how-it-works/
---

##Einleitung

Diese Seite beschreibt die grundsätzliche Funktionsweise von Orcas. Insbesondere die Arbeitsweise der Extensions wird hier dargestellt.

##Wer sollte das lesen?

Um Orcas einzusetzen, ist das Verständnis der Funktionsweise nicht notwendig. Es ist wichtig für die Entwicklung von Extensions und die Arbeit an Orcas selbst.

##Ablaufdiagramm

Das folgende Diagramm zeigt den groben Ablauf von Orcas. Die einzelnen Schritte, die mit Nummern versehen sind, werden nachfolgend genauer beschrieben:

![Funktionsweise von Orcas]({{site.baseurl}}/assets/funktion_orcas.gif)

##Beschreibung

1. Die BNF (Backus-Naur-Form) beschreibt die Syntax der Tabellen-Skripte (genauer: Statics-Skripte). Die BNF ist in einem xText-Format abgelegt (orcas_core/xtext/orcas/src/de/opitzconsulting/OrcasDsl.xtext). Im ersten Schritt wird daraus ein Satz an "PL/SQL Typen" (Oracle-Object-Types und Collection-Types) generiert, die Schemadaten aufnehmen können. Es gibt beispielsweise einen Object-Type für Tabellen, und dieser hat wiederum eine Collection von Object-Types mit Spaltendaten. Die Object-Types werden in das Schema von Orcas deployed.
2. Im Zweiten Schritt wird aus der Original-BNF eine BNF' gemacht. Dies ist die Aufgabe von Syntax-Extensions. Wenn in einem Projekt keine Syntax-Extensions benutzt werden, dann passiert hier nichts. Es können mehrere Syntax-Extensions benutzt werden um die endgültige BNF' zu erstellen. Die Alias-Syntax-Extension wird beispielsweise die BNF derart erweitern, dass nach Angabe des Tabellennamens noch der Alias folgen kann (oder muss). Die Syntax für die Tabellen-Skripte ergibt sich also aus der BNF'.
3. Aus der BNF' wird ein Java-Klassenmodell generiert, das die Daten aus den Tabellenskripten aufnehmen kann. Zudem werden viele weitere xText-Artefakte generiert, die hier nicht weiter relevant sind. Alle generierten Artefakte werden im angegebenen Temp-Verzeichnis abgelegt.
4. Aus der BNF' wird auch ein entsprechender Satz an "PL/SQL Typen' " generiert. Diese ähneln den "PL/SQL Typen" (also denen ohne den Strich " ' ") insofern, dass die Strich-Variante auch weitere Daten aufnehmen kann, die sich aus den Syntax-Extensions ergeben.
5. Bisher wurden die Schritte betrachtet, die beim orcas_initialize ablaufen. Ab Schritt fünf beginnt der eigentliche Abgleich, der über orcas_execute_statics gestartet wird. Der erste Teilschritt besteht darin, die Tabellen-Skripte zu parsen und die geparsten Daten in das Java-Klassenmodell zu überführen. Diese Aufgabe übernimmt xText. Falls nötig/sinnvoll, können natürlich weitere Schritte vorgeschaltet werden, die z.B. die Tabellen-Skripte aus einer anderen Quelle generieren. Dies wird über den Schritt "5a" angedeutet und liegt komplett außerhalb von Orcas.
6. Im Schritt sechs werden die Java-Extensions aktiv (falls vorhanden). Die Aufgabe der Extensions ist dabei in der Regel, die zusätzlichen Daten (die über die Syntax-Extensions ermöglicht bzw. erzwungen wurden) auszulesen und mit Hilfe dieser Daten Modifikationen an den eigentlichen Schemadaten durchzuführen.
7. Im nachfolgenden Schritt sieben werden die Daten aus den Java-Objekten in PL/SQL-Objekte umgewandelt. Sollte eine reine PL/SQL basierte Lösung erforderlich sein (z.B. um Alt-Projekte zu überführen, oder weil der Einsatz von Ant/Java/xText nicht möglich ist), dann kann ein direktes Erstellen der PL/SQL-Strich-Typen erfolgen. Dieser Weg (als 7a angedeutet) wird aber nicht empfohlen und sollte eine bewusste Ausnahme sein.
8. Die Ausführung der PL/SQL-Extensions folgt in Schritt acht. Diese haben genau die gleiche Aufgabe wie die Java-Extensions. Dass Extensions alternativ in Java oder PL/SQL existieren können, ist dadurch begründet, dass Extension-Entwickler oft nur Java oder nur PL/SQL beherrschen.
9. Schritt neun transformiert die Daten aus den PL/SQL-Strich-Typen in die "normalen" PL/SQL-(ohne-Strich)-Typen. Dies ist der SOLL-Stand des DB-Schemas.
10. Schritt zehn ermittelt den IST-Stand des DB-Schemas.
11. Der Abgleich der Daten im Schritt elf ermöglicht es, bestimmte Abweichungen zwischen SOLL und IST zu ignorieren (z.B. wenn Storage-Parameter abweichen). Dies wird dadurch erreicht, dass im ABGLEICH-Stand die zu ignorierenden SOLL-Daten mit den entsprechenden IST-Daten überschrieben werden.
12. Abschliessend wird im letzten Schritt der eigentliche Abgleich durchgeführt. In diesem Schritt passiert die "eigentliche" Logik. Er ist bei weitem der komplizierteste Schritt des ganzen Ablaufs. Beim Abgleich wird der IST-Stand mit dem ABGLEICH-Stand verglichen. Alle IST- / ABGLEICH-Differenzen werden im DB-Schema umgesetzt.

###7a Zeilenbasierte SQL\*Plus Skripte

Diese Variante ermöglicht Projekten, die noch die SQL\*Plus-Skript basierten Tabellenskripte verwenden, Orcas zu nutzen.

Dabei sollen folgende Punkte gelten:

- Die SQL\*Plus Skripte haben initial den API-Stand der aktuellsten Orcas-Version (GKN-Variante).
- Die SQL\*Plus Skripte basieren auf einer (oder mehrerer) Extensions (inkl. Syntax Extension). Diese Extension bildet im Wesentlichen das Domänen-Konzept ab.
- Die SQL\*Plus Skripte befüllen bei ihrem Lauf nur eine Package-Variable (oder ggf. auch eine persistente Variante) vom Typ OT_SYEX_MODEL.
- Diese wird bei jedem Aufruf eines SQL\*Plus Skripts immer weiter fortgeschrieben. Abschliessend wird dann mit dieser Variable im Schritt 9 des normalen Ablaufs aufgesetzt.
- Die SQL\*Plus Skripte sind eine Art Vorlage und können/sollen von konkreten Projekten geändert werden.
  - Jedes Projekt sollte sich die SQL\*Plus Skripte ins Projekt kopieren
  - Wenn weitere Syntax-Extensions in einem Projekt eingesetzt werden sollen, dann muss das Projekt typischerweise auch die SQL\*Plus Skripte anpassen
  - Wenn in Orcas neue Funktionen (mit neuen Syntax-Möglichkeiten) eingebaut werden, dann soll auch die SQL\*Plus Skript Vorlage erweitert werden. Ggf. kann es dabei sein, dass diese dann nicht mehr abwärtskompatibel ist.
  - Die Vorlagen-Implementierung der SQL\*Plus Skripte soll möglichst robust gegenüber dem Erweitern der OT_SYEX_ Datenstrukturen sein. Es darf z.B. nicht der Defaultkonstruktor mit allen Parametern genutzt werden, sondern es muss immer der generierte leere Konstruktor verwendet werden mit anschliessendem Setzen der Attribute.
- Die SQL\*Plus Skripte werden im ersten Schritt manuell erstellt. Evtl. ist es danach oder dabei möglich die SQL\*Plus Skripte zu generieren.

##Extract (reverse Engineering)

![Extract - Reverse Engineering]({{site.baseurl}}/assets/funktion_orcas.gif)

1. Die IST-Daten werden aus dem Schema ausgelesen.
2. Die Daten werden von den PL/SQL-Strich-Typen in die "normalen" PL/SQL-(ohne-Strich)-Typen transformiert.
3. Die reverse-Extensions werden angewendet. Diese sollten die Daten analysieren und ggf. Extension-Daten erzeugen und dabei die Original-Daten entfernen. Zusätzlich können die Extensions Daten entfernen die unnötig sind (z.B. Tablespace-Angaben).
4. Im letzten Schritt werden die Daten in XML umgewandelt und mit einer Stylesheet-Transformation die eigentlichen Skripte erzeugt.

##PL/SQL Komponenten

###Object/Collection - Types

Alle Object-Types sind generiert und sollten nie direkt geändert werden. Die Objekttypen dienen dazu die Modelldaten aufzunehmen. Es gibt z.B. den Typ "ot_orig_table". Ausprägungen (Instanzen) von diesem Typ repräsentieren die Tabellen, die im Schema angelegt/abgeglichen werden sollen.

Es gibt zwei "Sätze" von Objekttypen:

- Zum einen die OT_ORIG_*/CT_ORIG_* Typen, diese enthalten die Daten aus der BNF, und damit arbeitet der Kern von Orcas.
- Der andere Satz (OT_SYEX_*/CT_SYEX_*) enthält auch die Daten, die durch Syntaxextensions in das Modell gekommen sind.

###Ablauf

Der Ablauf ist so, dass dem Package **pa_orcas_xtext_model**  Modellinhalte über **pa_orcas_model_holder.add_model_element** hinzugefügt werden, um anschliessend das Model über **get_model** auszulesen, in ORIG-Typen zu transformieren und an **pa_orcas_ddl_call.update_schema** zu übergeben.

Nachfolgend ein Beispiel (call_orcas.sql):

{% highlight sql %}
declare
  v_syex_model ot_syex_model;
  v_orig_model ot_orig_model;
begin
  pa_orcas_xtext_model.build();                                                              -- 2. Teil von Schritt 7
  v_syex_model := pa_orcas_extensions.call_extensions( pa_orcas_model_holder.get_model() );  -- Schritt 8 (call_extensions)

  v_orig_model := pa_orcas_trans_syex_orig.trans_syex_orig( v_syex_model );                  -- Schritt 9
  pa_orcas_ddl_call.update_schema( v_orig_model );                                           -- Schritt 10, 11 und 12  
end;
/
{% endhighlight %}

###Packages

**pa_orcas_checksum**

Dient nur dazu eine "Version" in der Datenbank abzulegen, um ggf. das Neuaufbauen des Orcas-Users überspringen zu können. Der Packagebody (mit der checksum) wird im Ant-build-Ablauf generiert.

**pa_orcas_compare**

Dieses Package führt den Abgleich durch (Schritt 11 und 12). Dabei werden zuerst alle benötigten Statements gesammelt (Schritt 11), und erst am Ende ausgeführt (Schritt 12).

**pa_orcas_ddl**

Hilfspackage um pa_orcas_compare zu starten.

**pa_oc_exec_log**

Dieses Package bietet Hilfsfunktionen für das protokollierte Ausführen von dynamischen SQL und Logging.

**pa_orcas_xml_syex**

Dieses Package bietet die Möglichkeit, einen SYEX-Typen nach JSON auszugeben. Wird beim Reverse Engineering verwendet.

**pa_orcas_load_ist**

Dieses Package lädt die IST-Daten aus dem Data-Dictinonary in eine Instanz von ot_orig_model (Schritt10).

**pa_orcas_model_holder**

Das Package dient nur dazu, eine Packagevariable (privat: **pv_model**) zu halten, in der das Zielmodell vorgehalten wird. Das Package arbeitet auf den SYEX-Typen.

**pa_orcas_extension_parameter**

Dieses Package verwaltet die Extension-Parameter.

**pa_orcas_extensions**

Dieses Package ruft die PL/SQL-Extensions auf. Der Body wird generiert.

**pa_orcas_run_parameter**

Dieses Package verwaltet die Parameter, die beim execute_statics-Lauf übergeben werden.

**pa_orcas_trans_syex_orig**

Das Package ist generiert und ermöglicht die Transformation von SYEX-Typen nach ORIG-Typen.

**pa_orcas_trans_orig_syex**

Das Package ist generiert und ermöglicht die Transformation von ORIG-Typen nach SYEX-Typen.

**pa_orcas_updates**

Das Package dient nur dazu, die one-time-scripts zu verwalten und hat mit dem eigentlichen Abgleich nichts zu tun.

**pa_orcas_xtext_model**

Das Package hat die Aufgabe, das Modell, das in **pa_orcas_model_holder** gehalten wird, über die **add_model_element** Prozedur zu befüllen. Dies tut es in der Prozedur build. Der Body von diesem Package wird bei jedem Durchlauf neu generiert. Er enthält nur Aufrufe der **pa_orcas_xtext_\*** Packages.

**pa_orcas_xtext_* (pa_orcas_xtext_1,pa_orcas_xtext_2...)**

Die **pa_orcas_xtext_\*** Packages werden komplett (Specification und Body) im Ant-build-Ablauf generiert und enthalten die Modelldaten. Meistens gibt es nur eins. Mehrere Packages werden automatisch generiert, wenn es ein sehr großes Model (Datenbankschema) abzugleichen gilt. Dann werden wegen der PL/SQL / SQL\*Plus Größenbeschränkungen mehrere Packages generiert. Die Packages werden in Schritt 7 generiert und enthalten die echten Modelldaten (Tabellennamen, Spaltennamen, ...).
