---
layout: page
title: Orcas for Maven lifecycle
permalink: /docs/lifecycle-maven/
categories: 
- en
---

The default lifecycle can be used with `<packaging>orcas-database-deployment</packaging>`.

- clean-log
- initialize-orcas-db
- pre-statics
- statics
- drop-replaceables
- install-replaceables
- compile-replaceables
- post-compile
- database-deployment


## clean-log
Removes the spool-folder. The default location of that folder is: `target/log/`.

## initialize-orcas-db
Usually does nothing. It is only doing somesthing if a pl/sql extension is used.

## pre-statics
Executes one-time-scripts in `src/main/scripts/pre-statics`. 

## statics
Updates the database-schemas statics (tables, sequences,...). Uses orcas-scripts `src/main/sql/statics` in.

## drop-replaceables
Drops all replaceables (triggers, packages,...). Note that this is the default for handling replaceables but there are other options.

## install-replaceables
Executes sql-scripts in `src/main/sql/replaceables`. 

## compile-replaceables
Compiles all pl/sql code in the database-schema.

## post-compile
Executes one-time-scripts in `src/main/scripts/post-compile`. 

## database-deployment
This is the main thing to exceute. Does nothing on its, but is set up to exceute all others.

<iframe width="480" height="360" src="{{site.baseurl}}/orcas_maven_plugin/site/plugin-info.html" frameborder="0"> </iframe>


## Soon to come...

## 

