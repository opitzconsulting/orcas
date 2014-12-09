---
layout: docs
title: Unterstützte DB-Funktionen
prev_section: statics-syntax
next_section:
permalink: /docs/supported-db-functions/
---

#Unterstützte DB-Funktionen

![](/assets/error.png) **Diese Seite befindet sich noch im Aufbau**

Nachfolgend wird aufgelistet, welche Datenbankobjekte wie unterstüzt werden.

- Replaceable bedeutet dabei, dass diese Objekte bei jedem Lauf komplett neu aufgebaut werden. Dazu können beliebiege SQL*PLus-Scripte verwendet werden. Orcas bietet dazu die Funktionen XXX. Das ![](/assets/check.png) in der nachfolgenden Tabelle bedeutet dabei, dass auch die entsprechenden Löschfunktionen existieren XXX.
- Die als static merkierten Datenbankobjekte werden unterstützt, indem die Skripte mit den bestehenden Datenbankobjekten abgeglichen werden, und nur Änderungen ausgeführt werden. Hierbei bedeutet das ![](/assets/check.png), dass die Datenbankobjekte von der Skriptsprache untersützt werden, und die Abgleichsfunktionen vorhanden sind.

Einige Datenabnkobjekte sind in beiden Kategorien, bei diesen kann eine Einstufung in beide Kategorien in Einzelfall sinnvoll sein.

|Typ|Replacable|Static|
|---|----------|------|
|Table||![](/assets/check.png)|
|Constraint||![](/assets/check.png)|
|Index||![](/assets/check.png)|
|View|![](/assets/check.png)||
|Materialized View||![](/assets/error.png)|
|Synonym|![](/assets/check.png)||
|Function|![](/assets/check.png)||
|Procedure|![](/assets/check.png)||
|Package|![](/assets/check.png)||
|Trigger|![](/assets/check.png)||
|Type|![](/assets/check.png)|![](/assets/error.png)|
|Sequence||![](/assets/check.png)|
|Java|![](/assets/error.png)||
|DB Link|![](/assets/error.png)||
|User||![](/assets/error.png)|
|Grants||![](/assets/error.png)|
|Job|![](/assets/check.png)||
|AQ||![](/assets/error.png)|

`todo: seltsame Auflistung in der Quelle`
