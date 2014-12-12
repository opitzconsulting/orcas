---
layout: docs
title: Unterstützte DB-Funktionen
prev_section: features
next_section: ant-tasks
permalink: /docs/supported-db-functions/
---

#Unterstützte DB-Funktionen

![]({{site.baseurl}}/assets/error.png) **Diese Seite befindet sich noch im Aufbau**

Nachfolgend wird aufgelistet, welche Datenbankobjekte wie unterstüzt werden.

- Replaceable bedeutet dabei, dass diese Objekte bei jedem Lauf komplett neu aufgebaut werden. Dazu können beliebiege SQL*PLus-Scripte verwendet werden. Orcas bietet dazu die Funktionen XXX. Das ![]({{site.baseurl}}/assets/check.png) in der nachfolgenden Tabelle bedeutet dabei, dass auch die entsprechenden Löschfunktionen existieren XXX.
- Die als static markierten Datenbankobjekte werden unterstützt, indem die Skripte mit den bestehenden Datenbankobjekten abgeglichen werden, und nur Änderungen ausgeführt werden. Hierbei bedeutet das ![]({{site.baseurl}}/assets/check.png), dass die Datenbankobjekte von der Skriptsprache untersützt werden, und die Abgleichsfunktionen vorhanden sind.

Einige Datenabnkobjekte sind in beiden Kategorien, bei diesen kann eine Einstufung in beide Kategorien in Einzelfall sinnvoll sein.

|Typ|Replacable|Static|
|---|----------|------|
|Table||![]({{site.baseurl}}/assets/check.png)|
|Constraint||![]({{site.baseurl}}/assets/check.png)|
|Index||![](/{{site.baseurl}}assets/check.png)|
|View|![](/{{site.baseurl}}assets/check.png)||
|Materialized View||![]({{site.baseurl}}/assets/error.png)|
|Synonym|![]({{site.baseurl}}/assets/check.png)||
|Function|![]({{site.baseurl}}/assets/check.png)||
|Procedure|![]({{site.baseurl}}/assets/check.png)||
|Package|![]({{site.baseurl}}/assets/check.png)||
|Trigger|![]({{site.baseurl}}/assets/check.png)||
|Type|![]({{site.baseurl}}/assets/check.png)|![]({{site.baseurl}}/assets/error.png)|
|Sequence||![]({{site.baseurl}}/assets/check.png)|
|Java|![]({{site.baseurl}}/assets/error.png)||
|DB Link|![]({{site.baseurl}}/assets/error.png)||
|User||![]({{site.baseurl}}/assets/error.png)|
|Grants||![]({{site.baseurl}}/assets/error.png)|
|Job|![]({{site.baseurl}}/assets/check.png)||
|AQ||![]({{site.baseurl}}/assets/error.png)|

`todo: seltsame Auflistung in der Quelle`
