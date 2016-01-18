---
layout: page
title: Integrationtests
permalink: /docs/de/integration-tests/
categories: 
- de
---

##Spooltests

###Funktionsweise Spooling

Die Integrationstests nutzen einen Spooling Mechanismus, der im Wesentlichen die IST-Schema-Daten aus der DB lädt und via XML-Export in eine Datei ausgibt (dieser Weg wird auch für das Reverse-Engineering verwendet).
<br/>Das Spooling macht noch ein paar weitere Abfragen auf dem Data-Dictionary (dies sollte bald entfernt werden). Und es werden auch die Inhalte der Tabellen mit ausgegeben (dabei nur Datentypen, die problemlos via sqlplus gespoolt werden könne, also z.B. keine LOB-Daten).

###Tests

Die Spooltests selbst liegen unter orcas_integrationstest\testspool\tests. In dem Ordner befinden sich jeweils Unterordner mit jeweils einem Testfall. Der Testfall besteht aus zwei Dateien (a.sql und b.sql). Der Test läuft wie folgt ab:
![Bildliche Darstellung des Spoolings]({{site.baseurl}}/assets/spooling.GIF "spooling.GIF")

1. a.sql wird im einem (zuvor geleerten) Schema ausgeführt.
2. Das Spooling wird auf diesem Schema ausgeführt und in die Datei a.log geschrieben.
3. b.sql wird im dem (erneut zuvor geleerten) Schema ausgeführt.
4. Das Spooling wird auf diesem Schema erneut ausgeführt und in die Datei b.log geschrieben.
5. Es wird überprüft, dass a.log und b.log unterschiedlich sind, wenn nicht, ist der Testfall fehlgeschlagen.

Bei diesem Vorgehen ist es sehr wichtig, dass jeder einzelne Testfall nur genau ein Detail testet, da sonst eine ungewollte Oder-Verknüpfung durchgeführt würde ("Das Spooling erkennt eine Abweichung im Datentyp ODER in der Precision").

##Integrationstests

Die eigentlichen Integrationstest befinden sich im Ordner orcas_integrationstest/tests. Wie schon beim spooltest gibt es hier wieder Unterordner, und jeder davon ist ein eigener Testfall. Es ist hier aber nicht nötig, für jedes Detail einen eigenen Testfall zu erstellen (Im Gegenteil: Jeder Testfall dauert ca. 3 Minuten, daher soll die Anzahl der Tests nicht zu groß werden). Mit jedem Testfall werden verschiedene Testszenarios durchgeführt Diese werden nachfolgend beschrieben:

###Testszenario "normal":
![Bildliche Darstellung Testszenario "normal"]({{site.baseurl}}/assets/testszenario-normal.GIF "testszenario-normal.GIF")

1. In einem (leeren) Schema wird das Skript erzeuge_zielzustand.sql eingespielt.
2. Mit dem Spool-Skript wird das Schema in eine Datei geschrieben.
3. Auf einem anderen (leeren) Schema wird das Skript erzeuge_ausgangszustand.sql eingespielt.
  Hinweis: Das Skript darf bei einem Testfall auch leer sein.
4. Über Orcas wird der eigentliche Abgleich ausgeführt.
  Dabei wird mitprotokolliert, und zwar in das Verzeichnis "Protokoll" (ist erst für den nächsten Tesfall relevant).
5. Das abgeglichene Schema wird auch in eine Datei geschrieben.
6. Es wird geprüft, ob die beiden Schemata gleich sind.
7. Orcas wird erneut ausgeführt. Diesmal wird in das Verzeichnis "Protokoll_svw_test" protokolliert.
8. Das Protokoll des zweiten Abgleichs muss leer sein.
  Dadurch wird verifiziert, dass Orcas nicht unnötig Objekte im Schema dropt und neu anlegt (was ohne diesen Test nicht auffallen würde, aber zu teilweise erheblichen Performance-Problemen führen könnte).

###Testszenario "protokoll"
![Bildliche Darstellung Testszenario "protokoll"]({{site.baseurl}}/assets/testszenario-protokoll.GIF "testszenario-protokoll.GIF")

1. Auf dem (leeren) protokoll-Schema wird das Skript erzeuge_ausgangszustand.sql eingespielt.
2. Das beim vorangegangenen Testfall erzeugte Protokoll-Skript wird jetzt auf dem protokoll-Schema ausgeführt.
  Hinweis: Das erfolgt nur über SQL*Plus, ohne Orcas.
3. Das protokoll-Schema wird auch in eine Datei geschrieben.
4. Auch die protokoll-log-Datei muss gleich der zielskript-log-Datei sein.

###Testszenario "extract"
![Bildliche Darstellung Testszenario "extract"]({{site.baseurl}}/assets/testszenario-extract.GIF "testszenarion-extract.GIF")

Dieses Testszenario wird nur ausgeführt, wenn das Property "test_extract" nicht auf false gesetzt wurde.

1. Beim extract-Test (Reverse-Engineering-Test) dürfen keine Tabelleninhalte betrachtet werden. Daher wird beim Spool-Skript der Daten-Export deaktiviert.
2. Mit orcas_extract werden die Tabellenskripte zu dem zielskript-Schema erzeugt und im Verzeichnis extract_output abgelegt.
3. In dem (zuvor geleerten) ueberfuehrung-Schema wird der Abgleich gestartet.
  Da das Schema vorher leer war, wird hier implizit auch immer der "neu-anlegen" Fall mitgetestet.
4. Das Schema wird wieder in eine Datei gespoolt.
5. Die gespoolten-Dateien müssen übereinstimmen.

###Testszenario "sqlplus-api"
![Bildliche Darstellung Testszenario sqlplus-api]({{site.baseurl}}/assets/testszenario-sqlplus-api.GIF "testszenario-sqlplus-api.GIF")

Dieses Testszenario wird nur ausgeführt, wenn das Verzeichnis tabellen_sqlplus existiert.

1. Auf dem (zuvor geleerten) ueberfuehrung-Schema wird das Skript erzeuge_ausgangszustand.sql eingespielt.
2. Mit Orcas wird wieder der Abgleich ausgeführt. Diesmal aber mit der SQL*Plus-API Variante.
3. Das Schema wird wieder gespoolt.
4. Auch hier müssen wieder die Dateien gleich sein.
