---
layout: docs
title: Entwicklerdokumentation
prev_section: participate
next_section:
permalink: /docs/dev-docs/
---

#Entwicklerdokumentation

##Funktionsweise

Für die Beschreibung der Funktionsweise siehe: `todo: Link zu Funktionsweise ...`Funktionsweise der Schemaverwaltung.

##Integrationstests

Für die Beschreibung der Integrationstests siehe: `todo: Link zu Integrationstests`Integrationstests

##Git

`todo: Formatierung`
Änderungen an der OC-Schemaverwaltung sollten in einem eigenen Entwickler-Branch erfolgen. Jeder OC-Mitarbeiter sollte bereits das Recht haben einen neuen Entwickler-Branch anzulegen und zu pushen.
<br/>Wenn eine Änderung fertig ist, dann soll von dem Entwickler-Branch ein neuer Feature-Branch erstellt werden, für diesen wird dann in stash ein Pull-Request erstellt.
<br/><br/>Beispiel:
<br/>Entwickler FSA möchte eine Änderung machen. FSA erstellt den Branch:
<br/>developer-fsa
<br/><br/>Der Branch wird immer von master-Branch erstellt (wenn er schon existiert, z.B.: weil FSA schon vorher Änderungen gemacht hat, dann wird der mittels merge auf den aktuellen master Stand gebracht).
<br/><br/>Entwickler FSA committed und pushed in den Branch bis die Entwicklung soweit fertig ist, dass sie in die OC Schemaverwaltung übernommen werden kann/soll. Dazu erstellt fsa einen neuen Branch:
<br/><br/>Wobei OCSVERW-78 ein Verweis auf einen JIRA-Punkt sein kann, oder ein anderer halbwegs verständlicher Name. Der Branch pullrequest-fsa-OCSVERW-78 wird von dem Branch developer-fsa gemacht. Anschliessend stellt FSA einen pull-request für den Branch pullrequest-fsa-OCSVERW-78 in stash ein (https://git.opitz-consulting.de/projects/OCFRW/repos/oc-schemaverwaltung). WICHTIG: auf dem Branch pullrequest-fsa-OCSVERW-78 darf nicht weiterentwickelt werden, weil jede (gepushte) Änderung automatisch mit in dem Pull-Request landet. FSA sollte auf dem Branch developer-fsa weiterarbeiten. Es bleibt FSA überlassen, ob bei der Weiterentwicklung auf dem master-Branch-Stand oder dem pullrequest-fsa-OCSVERW-78 -Branch-Stand aufgesetzt wird.
<br/>pullrequest-fsa-OCSVERW-78
<br/><br/>Im Branch pullrequest-fsa-OCSVERW-78 werden ggf. Korrekturen durchgeführt, die dann bei Bedarf in den developer-fsa Branch gemerged werden können.

##Vorgehensweise SQL*Plus Skripte

Um die unter `todo: Link zu Funktionsweise ...`Funktionsweise der Schemaverwaltung Schritt 7a umzusetzen, ist folgende Vorgehnweise vorgesehen:

1. Mini-Beispiel erstellen
2. Mini-Beispiel umsetzen
3. Vollständiges Beispiel erstellen (ohne Domänen-Konzept)
4. Ermitteln was noch zu tun ist um Vollständiges Beispiel umsetzen zu können
  - Welche Funktionalität fehlt im "Core"
  - Was fehlt bei den SQL*Plus Skripten?
5. Fehlende Punkte umsetzen
6. Vollständiges Domänen-Konzept-Beispiel erstellen
7. Ermitteln was noch zu tun ist um Vollständiges Domänen-Konzept-Beispiel umsetzen zu können
  - Welche Funktionalität fehlt im "Core"?
  - Welche Funktionalität wird in der Domain-Extension noch benötigt?
8. Fehlende Domänen-Punkte umsetzen
9. Test mit erstem echten Projekt durchführen

##Vorgehensweise Reverse Engineering

Reverse Engineering soll über die Modelldaten erfolgen.

##SOLL-IST-Abgleich

In `todo: Link zur Funktionsweise ...`Funktionsweise der Schemaverwaltung Schritt 10, 11 und 12 wird geziegt wie der Abgleich mit der DB laufen sollte. Derzeit ist das Verahalten noch nicht (überall) so. In einem ersten Realisierungsschritt soll der SOLL-IST-Abgleich ohne den ABGLEICH (Schritt 11) erfolgen.Die Umsetzung soll Schrittweise erfolgen (wie z.B: bei Sequenzen).

##SQL*Plus Standalone Variante

Es soll eine Möglichkeit geschaffen werden, die OC-Schemaverwaltung ohne ant und Java zu nutzen.
