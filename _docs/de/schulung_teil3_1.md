---
layout: page
title: Teil 3.1 - Orcas Extensions
permalink: /docs/de/schulung_teil3_1/
categories: 
- de
---

Im dritten Teil der Schulung wollen wir uns nun mit Extensions auseinandersetzen. Extensions ermöglichen viele sinnvolle Zusatzfunktionen bei der Arbeit mit Orcas, die man normalerweise gar nicht in Erwägung ziehen würde. 

## Funktionsweise Extensions - Aufbau Orcas

Das folgende Ablaufdiagramm zeigt grob die Funktionsweise von Orcas. Eine genauere Beschreibung findet sich unter [Funktionsweise Orcas]({{site.baseurl}}/docs/de/how-it-works/)

![Funktionsweise von Orcas]({{site.baseurl}}/assets/funktion_orcas.gif)

Was an dieser Stelle für uns wichtig ist, ist das Orcas die beschriebenen Objekte in Java-Objekte und pl/sql liest. Dadurch ergeben sich für Extensions zwei Angriffspunkte, hier in Nr. 6 und Nr. 8 eingezeichnet. Durch die Übersetzung in Java und pl/sql Code, ist es in beiden Fällen möglich über diese Objekte zu iterieren und sie dabei gegebenenfalls zu erweitern oder zu verändern.
Das beudeutet man könnte beispielsweise über alle Spalten einer jeden Tabelle iterieren und sie mit dem Kürzel der Tabelle beginnen lassen um so projektspezifische Richtlinien einzuhalten.

Die Anwendung wird klarer wenn wir mit ein paar Beispielen beginnen.



