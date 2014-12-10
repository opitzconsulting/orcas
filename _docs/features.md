---
layout: default
title: Feature Liste
prev_section: extensions
next_section: how-it-works
permalink: /docs/features/
---

#Feature Matrix

Die folgende Tabelle ist ein shameless copy von der flyway Seite mit kleinen Korrekturen.

||Orcas|Flyway|Liquibase|c5-db-migration|dbdeploy|mybatis|MIGRATEdb|migrate4j|dbmaintain|AutoPatch|
|-|-|-|-|-|-|-|-|-|-|-|
|**Migration Types**|||||||||||
|Plain Old Sql migrations|true|true|true 1,5|true|true|true|false|false|true|true|
|Java migrations|true 6|true|false 1|false|false|false|false|true|fals|true|

`todo: Tabelle ausfüllen`

1. Sql files and Java classes can be used indirectly through references in xml migrations.
2. Not out of the box. Available through a 3rd party. May be outdated.
3. Only a single statement at a time is supported. No mixed delimiters.
4. Only Oracle-style PL/SQL delimeters. No MySQL standard syntax using the DELIMITER statement.
5. SQL Migrations require specific comments, but beside that they are plain sql
6. Migration im eigentlichen Sinne ist mit der OC-Schemaverwaltung nicht nötigt, es reicht den Zielzustand mittels vorhandenem Reverse-Engineering abzuspeichern.
7. FK-Beziehungen Schemaübergreifend müssen noch realisiert werden.
8. True wegen Extension-Mechanismus, der wesentlich mehr kann als nur "Placeholder replacement"
