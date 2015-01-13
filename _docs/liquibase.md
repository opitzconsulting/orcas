---
layout: page
title: Abgrenzung zu liquibase/flyway
permalink: /docs/liquibase/
---
[liquibase](http://www.liquibase.org/) und [flyway](http://flywaydb.org/) sind zwei Tools die eine ähnliche Problemstellung adressieren wie Orcas.

Beide arbeiten im Prinzip mit einer Liste von auszuführenden Datenbankänderungen (teilweise kann diese "Liste" auch hierarchisch geordnet sein).

Beide Tools sind grundsätzlich zu empfehlen und werden auch bei Opitz Consulting in einigen Projekten genutzt. Der große Vorteil beider Tools ist, dass sie nicht nur mit Oracle-Datenbanken arbeiten können.

##Warum also Orcas?

## Datenmodell in Skriptform
Wichtig: es wird das Datenmodell selbst in Skriptform, nicht das Änderungslog des Datenmodell.
Ein Änderungslog ist bei Orcas nur für Spezialfälle erforderlich und ist immer nur zusätzlich zum Datenmodell vorhanden.

- Versionsverwaltung-Merge-Konflikte treten bei Orcas in der Regel genau dann auf, wenn man sie "braucht". Also dann wenn zwei Entwickler parallel an der gleichen Stelle im Datenmodell Änderungen durchführen. Legt man dagegen das Änderungslog in der Versionsverwaltung ab, kommt es meist zu zu-vielen Merge-Konflikten (nämlich jedes Mal dann wenn zwei Entwickler parallel Änderungen am Datenmodell durchführen, unabhängig davon ob das an der gleichen Stelle war oder nicht). Oder es kommt zu zu-wenigen Merge-Konflikten (z.B. dann, wenn man jede Änderung in eine eigene Datei auslagern, dann kommt es in der Regel nie zu einem Merge-Konflikten, was bedeutet, dass problematische parallele Änderungen am Datenmodell erst bei der Installation oder gar nicht auffallen). 

- Die gleiche Art von Problem gibt es natürlich auch bei Orcas, da ja auch hier für einzelne Spezialfälle ein Änderungslog-Mechanismus genutzt werden muss. Dann besteht aber der Vorteil, dass diese nicht sonderlich oft gebraucht werden, und sich somit das Merge-Konflikt-Potential erheblich absenkt. Zum anderen wird ein falsch gemergtes Änderungslog mit großer Sicherheit bei der nächsten Installation auffallen, da anschließend ja noch ein vollständiger Abgleich erfolgt. Dazu kommt noch, dass man beim Merge ja auch das Datenmodell selbst mergt, und dabei schon sehr genau sagen kann, ob es im Änderungslog ein Problem gibt oder nicht.

- Die Skriptform des Datenmodells eignet sich auch sehr gut um die Nachvollziehbarkeit von Änderungen zu gewährleisten (also z.B. die Frage: "Wer hat wann mit welchem commit eine Tabelle geändert?" beantworten zu könne).

- Die Skriptform des Datenmodells ist auch als Referenz nützlich, da man hier einfach nachsehen kann welche Spalten eine Tabelle hat.

## Datenmodell wird abgeglichen

Damit ist sichergestellt, dass ein Zielschema auch wirklich so aussieht, wie es in den Datenmodell-Skripten definiert ist.

Bei einer Vorgehensweise mit Änderungslog besteht immer die Gefahr, dass Änderungen "nebenher" direkt auf dem eine oder anderen Datenbankschema ausgeführt werden, was immer wieder zu teils gravierenden Problemen führen kann.

Orcas verhindert natürlich nicht, dass Änderungen "nebenher" direkt auf einem Datenbankschema durchgeführt werden, aber mit dem nächsten Abgleich fallen solche Änderungen sofort auf und werden zurückgenommen. Natürlich bietet Orcas die Möglichkeit die durchzuführenden Änderungen zu protokollieren und auch die Möglichkeit nur die durchzuführenden Änderungen auszugeben. Damit besteht die Möglichkeit solche Änderungen zu bemerken und bei Bedarf in den Datenmodell-Skripten nachzuziehen ohne sie zu verwerfen. 

## PL/SQL, Views, Trigger...
Orcas bietet neben dem eigentlichen Abgleich auch einige Möglichkeiten um Datenbankobjekte einzuspielen. Das ist für Datenbankobjekte sinnvoll, die einfach überschrieben werden können. Dazu gehören in der Regel Packages, Views, Trigger, Procedures, Functions und (eingeschränkt) Object-Types. 
Diese geschieht über ant-Tasks zum Ausführen von Skripten (z.B. alle Skripte in einem Verzeichnis ausführen), sowie ant-Tasks zum compilieren und bereinigen. 

## Extensions
Extensions bieten vielfältige Möglichkeiten. Die Haupt-Aufgabe besteht sicher darin, die Definition der Datenmodell-Skripte zu vereinheitlichen und zu vereinfachen. Neben der Möglichkeit [Extensions]({{site.baseurl}}/docs/extensions/) selbst zu schreiben gibt es auch die [Domaion-Extension]({{site.baseurl}}/docs/domain-extension/), die bereits viel Funktionalität mitbringt.

