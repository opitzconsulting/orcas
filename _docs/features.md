// disabled
/*---
layout: docs
title: Feature Liste
prev_section: generate-scripts
next_section: supported-db-functions
permalink: /docs/features/
---*/

#Feature Matrix

Die folgende Tabelle ist eine Kopie von der [Flyway Homepage](http://flywaydb.org/#features) mit kleinen Korrekturen:

||**Orcas**|Flyway|Liquibase|c5-db-migration|dbdeploy|mybatis|MIGRATEdb|migrate4j|dbmaintain|AutoPatch|
|-|-|-|-|-|-|-|-|-|-|-|
|**Migration Types**|||||||||||
|Plain Old SQL migrations|true|true|true <span class="legend">1,5</span>|true|true|true|false|false|true|true|
|Java migrations|true <span class="legend">6</span>|true|false <span class="legend">1</span>|false|false|false|false|true|false|true|
|Groovy Migrations|true <span class="legend">6</span>|false|true|true|false|false|false|false|false|false|
|XML Migrations|true <span class="legend">6</span>|false|true|false|false|false|true|false|false|false|
|DDL abstraction DSL|true <span class="legend">6</span>|false|true|false|false|false|false|false|false|false|
|Requires specification of migration scripts|true <span class="legend">6</span>|true|true||true||true||||
|Allows to specify target state|true|false|false||false||false||||
|Can combin diff and migration scripts|true|false|false||false||false||||
|**Execution**|||||||||||
|CLI|true|true|true|false|true|true|true|true|true|false|
|API (Java)|false|true|true|true|false|true|false|true|true|true|
|API (Android)|false|true|false|false|false|false|false|false|false|false|
|Maven|false|true|true|true|false|true|false|false|true|false|
|Gradle|false|true|true <span class="legend">2</span>|false|true <span class="legend">2</span>|true <span class="legend">2</span>|false|false|false|false|
|Ant|true|true|true|false|true|false|true|true|true|false|
|SBT|false|true|true <span class="legend">2</span>|false|true <span class="legend">2</span>|false|false|false|false|false|
|**Databases**|||||||||||
|Oracle|true|true|true|false|true|true|true|false|true|true|
|SQL Server|false|true|true|true|true|true|true|false|true|true|
|DB2|false|true|true|false|true|true|true|false|true|true|
|MySQL|false|true|true|true|true|true|true|true|true|true|
|PostgreSQL|false|true|true|true|true|true|true|false|true|true|
|H2|false|true|true|true|true|true|true|true|false|true|
|Hsql|false|true|true|true|true|true|true|false|true|true|
|Derby|false|true|true|false|true|true|true|true|true|true|
|SQLite|false|true|true|false|true|true|true|true|true|true|
|**SQL Parser**|||||||||||
|Oracle PL/SQL||true|false <span class="legend">3</span>|false|false|false|false|false|true|false|
|SQL Server T-SQL||true|false  <span class="legend">3</span>|false|false|false|false|false|false|false|
|DB2 SQL PL||true|false  <span class="legend">3</span>|false|false|false|false|false|false|false|
|MySQL stored procedures||true|false  <span class="legend">3</span>|true|false|false|false|false|false  <span class="legend">4</span>|false|
|PostgreSQL stored procedures||true|false  <span class="legend">3</span>|true|false|false|false|false|false|false|
|**Other**|||||||||||
|Auto creation of schema||true|false|false|false|false|false|false|false|
|Auto creation of metadata table|true|true|true|true|true|true|false|false|true|true|
|Cluster-safe|true|true|true|false|false|false|false|false|false|true|
|Checksum validation|true|true|true|false|false|false|false|false|true|false|
|Placeholder replacement|true  <span class="legend">8</span>|true|true|false|true|false|false|false|false|false|
|Multiple schema support|false  <span class="legend">7</span>|true|false|false|false|false|false|false|true|false|
|Clean existing schema|true|true|false|false|false|false|false|false|true|false|
|Output to SQL file|true|false|true|false|true|true|true|false|false|false|
|Available on Maven Central|false|true|true|false|false|false|false|false|true|false|
|License||Apache v2|Apache v2|Apache v2|LGPL|Apache v2|BSD|LGPL v3|Apache v2|Apache|

<br/>

<div class="legend">
  <ol>
    <li>
      Sql files and Java classes can be used indirectly through references in xml migrations.
    </li>
    <li>
      Not out of the box. Available through a 3rd party. May be outdated.
    </li>
    <li>
    Only a single statement at a time is supported. No mixed delimiters.
    </li>
    <li>
    Only Oracle-style PL/SQL delimeters. No MySQL standard syntax using the DELIMITER statement.
    </li>
    <li>
    SQL Migrations require specific comments, but beside that they are plain sql
    </li>
    <li>
    Migration im eigentlichen Sinne ist mit Orcas nicht nötig, es reicht den Zielzustand mittels vorhandenem Reverse-Engineering abzuspeichern.
    </li>
    <li>
    FK-Beziehungen Schemaübergreifend müssen noch realisiert werden.
    </li>
    <li>
    True wegen Extension-Mechanismus, der wesentlich mehr kann als nur "Placeholder replacement"
    </li>
  </ol>
</div>
