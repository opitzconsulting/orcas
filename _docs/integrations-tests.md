---
layout: default
title: Integrationstests
prev_section: how-it-works
next_section: ant-tasks
permalink: /docs/integration-tests/
---

#Integrationstests

##Spooltests

###Funktionsweise Spooling

Die Integrationstests nutzen einen Spooling Mechanismus, der im Wesentlichen die IST-Schema-Daten aus der DB lädt und via XML-Export in eine Datei ausgibt (dieser Weg wird auch für das Reverse-Engineering verwendet).
<br/>Das Spooling macht noch ein paar weitere Abfragen auf dem Data-Dictionary (dies sollte bald entfernt werden). Und es werden auch die Inhalte der Tabellen mit ausgegeben (dabei nur Datentypen, die problemlos via sqlplus gespoolt werden könne, also z.B. keine LOB-Daten).

###Tests

Die Spooltests selbst liegen unter oc_svw_integrationstest\testspool\tests. In dem Ordner befinden sich jeweils Unterordner mit jeweils einem Testfall. Der Testfall besteht aus zwei Dateien (a.sql und b.sql). Der Test läuft wie folgt ab:
![Bildliche Darstellung des Spoolings](/assets/spooling.GIF "spooling.GIF")

1. a.sql wird im einem (zuvor geleertem) Schema ausgeführt.
2. Das Spooling wird auf diesem Schema ausgeführt und in die Datei a.log geschrieben.
3. b.sql wird im dem (erneut zuvor geleertem) Schema ausgeführt.
4. Das Spooling wird auf diesem Schema erneut ausgeführt und in die Datei b.log geschrieben.
5. Es wird überprüft, dass a.log und b.log unterschiedlich sind, wenn nicht, ist der Testfall fehlgeschlagen.

Bei diesem Vorgehen ist es sehr wichtig, dass jeder einzelne Testfall nur genau ein Detail testet, da sonst eine ungewollte Oder-Verknüpfung durchgeführt würde ("Das Spooling erkennt eine Abweichung im Datentyp ODER in der Precision").

##Integrationstests

Die eigentlichen Integrationstest befinden sich im Ordner oc_svw_integrationstest/tests. Wie schon beim spooltest gibt es hier wieder Unterordner, und jeder davon ist ein eigener Testfall. Es ist hier aber nicht nötig, für jedes Detail einen eigenen Testfall zu erstellen (Im Gegenteil: Jeder Testfall dauert ca. 3 Minuten, daher sollen die Anzahl der Tests nicht zu groß werden). Mit jedem Testfall werden verschiedene Testszenarios durchgeführt, diese werden nachfolgend beschrieben:

###Testszenario "normal":
![Bildliche Darstellung Testszenario "normal"](/assets/testszenario-normal.GIF "testszenario-normal.GIF")

1. In einem (leeren) Schema wird das Skript erzeuge_zielzustand.sql eingespielt.
2. Mit dem Spool-Skript wird das Schema in eine Datei geschrieben.
3. Auf einem anderen (leeren) Schema wird das Skript erzeuge_ausgangszustand.sql eingespielt.
  Hinweis: Das Skript darf bei einem Testfall auch leer sein.
4. Über die OC-Schemaverwaltung wird der eigentliche Abgleich ausgeführt .
  Dabei wird mitprotokolliert und zwar in das Verzeichnis "Protokoll" (ist erst für den nächsten Tesfall relevant).
5. Das abgeglichene Schema wird auch in eine Datei geschrieben.
6. Es wird geprüft, das die beiden Schemata gleich sind.
7. Orcas wird erneut ausgeführt, diesmal wird in das Verzeichnis "Protokoll_svw_test" protokolliert.
8. Das Protokoll des zweiten Abgleichs muss leer sein.
  Dadurch wird verifiziert, dass die OC-Schemaverwaltung nicht unnötig Objekte im Schema dropt und neu anlegt (was ohne diesen Test nicht auffallen würde, aber zu teilweise erheblichen Performance-Problemen führen könnte).

###Testszenario "protokoll"
![Bildliche Darstellung Testszenario "protokoll"](/assets/testszenario-protokoll.GIF "testszenario-protokoll.GIF")

1. Auf dem (leeren) protokoll-Schema wird das Skript erzeuge_ausgangszustand.sql eingespielt.
2. Das beim vorangegangenen Testfall erzeugte Protokoll-Skript wird jetzt auf dem protokoll-Schema ausgeführt.
  Hinweis das erfolgt nur über SQL*Plus, ohne die OC-Schemaverwaltung.
3. Das protokoll-Schema wird auch in eine Datei geschrieben.
4. Auch die protokoll-log-Datei muss gleich der zielskript-log-Datei sein.

###Testszenario "extract"
![Bildliche Darstellung Testszenario "extract"](/assets/testszenario-extract.GIF "testszenarion-extract.GIF")

Dieses Tesszenario wird nur ausgeführt, wenn das Property "test_extract" nicht auf false gesetzt wurde.
1. Beim extract-Test (Reverse-Engineering-Test) dürfen keine Tabelleninhalte betrachtet werden, daher wird beim Spool-Skript der Daten-Export deaktiviert.
2. Mit der oc_svw_extract werden die Tabellenskripte zu dem zielskript-Schema erzeugt und im Verzeichnis extract_output abgelegt.
3. In dem (zuvor geleertem) ueberfuehrung-Schema wird der Abgleich gestartet.
  Da das Schema vorher leer war, wird hier implizit auch immer der "neu-anlegen" Fall mitgetestet.
4. Das Schema wird wieder in eine Datei gespoolt.
5. Die gespoolten-Dateien müssen übereinstimmen.

###Testszenario "sqlplus-api"
![Bildliche Darstellung Testszenario sqlplus-api](/assets/testszenario-sqlplus-api.GIF "testszenario-sqlplus-api.GIF")

Dieses Tesszenario wird nur ausgeführt, wenn das Verzeichnis tabellen_sqlplus existiert.
1. Auf dem (zuvor geleertem) ueberfuehrung-Schema wird das Skript erzeuge_ausgangszustand.sql eingespielt.
2. Mit der OC-Schemaverwaltung wird wieder der Abgleich ausgeführt, diesmal aber mit der SQL*Plus-API Variante.
3. Das Schema wird wieder gespoolt.
4. Auch hier müssen wieder die Dateien gleich sein.
