---
layout: page
title: Alternatives liquibase/flyway
permalink: /docs/liquibase/
categories: 
- en
---
[liquibase](http://www.liquibase.org/) and [flyway](http://flywaydb.org/) are two tools, which adress the same problem as Orcas does.

Basically both tools work with al list of database changes which needs to be maintained manually and gets executed automatically.

Both tools can be recommended and are also used in some projects of OPITZ CONSULTING. The big advantage of both tools is, you are not limited to work with Oracle databases. 

## So, why Orcas?

### Database model in form of scripts

Important: the database model itself and not the change log of the database model is maintained.
A change log in Orcas is only necessary for special cases and only in adition to the model.

In Orcas, version control merging conflicts normally occur, when you "need" them. So exactly when two developers applying changes to the database model at the same section. On the one hand, if you put the change log into the version control, usually you get to much merge conflicts (namely everytime two developers are applying changes to the database model at the same time, regardless of whether it is at the same section or not). On the other hand you can get too little merge conflicts (e.g. you outsource every change into a new file. Normally you don't get merge conflicts, which means problematical parallel changings in the data model will be recognized while installing or in the worst case never).

In the "special casese" whrer you need an change log in Orcas, there are basically the same problems regarding merges. But the advantage is, you don't need it that often, so the lieklyness of (unwanted) merge conflicts is significantly reduced. Furthermore a wrong merged change log will be detected with great reliability, because afterwards there will be a complete comparison. In addition to this, you also merge the data model itself, so you can precisely say, whether there is a problem with the change log or not.

The data model in form of scripts is also very suitable for ensuring the traceability of changes (e.g. to answer questions like "Who changed a table with his commit and when?").

The data model in form of scripts is also very useful as a reference, because you can easily check, which columns are included in a table.

### Data model will be compared

With this step you can ensure, the structure of a target schema is exactly like you defined it in the data model scripts.

While proceeding changes with change logs there is always the risk that changings "incidentally" will be directly executed on the one or the other database schema, which often causes in serious problems.

Obviously Orcas does not prevent directly executing changes "incidentally"  on the database schema, but with the next comparison those changings will immediately be detected and undone. Of course Orcas offers the possibility to log the changes that are to be made and also the possibility to only display these changes. According to this there is the possibility to recognize such changes and to update the data model scripts without rejecting them, if needed. 

### PL/SQL, Views, Trigger...

In addition to the normal comparison, Orcas has many ways to implement database objects. This is useful for database objects, which are able to be overwritten. These objects are normally Packages, Views, Triggers, Procedures, Functions and (restricted) Object-Types.
This happens with ant-Tasks for executing scripts (e.g. execute all script in a directory) and also for compiling and clearing up.

### Extensions

Extensions have many opportunities. The main task is to unify and simplify the definitions of the data model scripts. In addition to the opportunity to write [Extensions]({{site.baseurl}}/docs/extensions/) by yourself, there is the [Domain-Extension]({{site.baseurl}}/docs/domain-extension/), which already provides many functionality.

