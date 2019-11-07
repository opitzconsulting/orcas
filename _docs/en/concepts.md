---
layout: page
title: Basic Orcas concepts
permalink: /docs/concepts/
categories: 
- en
---
## Orcas-Statics

The most import feature of Orcas is the capability to compare a database with a set of files and apply only the neccessary changes to the database.

However this feature is limted to:

* tables (including most details like columns and constraints, but not triggers)
* indexes (indexes are usually considered as table details as well)
* sequences 
* materialized views

These objects are called "statics" within the Orcas framework, because they cannot be dropped and recreated without data-loss or a signification performance impact.

## Orcas-scripts

So what about oracle database objects like:

* functions
* procedures
* triggers
* packages
* views

These are considered "replaceables" because they can be handled wit "create or replace" statements (not completely true for object-types though).

So basically all Orcas needs to do is run a set of scripts which execute these "create or replace" statements.

Orcas offers some support for those objects:

### Orcas compile replaceables

Orcas can run a compile-step, that compiles all of these objects.
This is done in an incremetnal way, so dependencies between those objects should not matter (this requires views to be created with the "force" option).

### Orcas drop replaceables

The "clean" way to handle replaceables is to drop them and then to completely recreate them during a database deplyoment.

This is what the gardle and maven plugins do by default (if you specify replaceable at all).

However this is not mandatory and can be skipped.

## Orcas one-time-scripts

There are several times when little "tweaks" are needed to deploy to a database in a fully automated way.

Orcas tracks where those one-time-scripts have been executed already and only executes each of them once on every target database.


## What about object-types?

Oracle object-types can be used to bring obejct-oriented programming features to the PL/SQL world.
Used this way objects-types can be handled as replaceables, which is the default in Orcas.

But if object-types are used as column or table types, they cannot be replaced anymore.
The object-types used this way should be handled solely with one-time-scripts, they must be ignored for drop-tasks with the "excludewhereobjecttype" parameter.







