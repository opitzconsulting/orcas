---
layout: page
title: About Orcas
permalink: /
categories: 
- en
---

Orcas is an open source deployment framework for transforming an existing **database schema** into a **target state** described by **text files**. 
By using simple text files, an **integration** into existing projects using **version control** is very easy. Orcas also offers the possibility to integrate complex crossovers, and it features an extension mechanism that allows the definition of the database schema to be simplified and unified.
Orcas is an acronym for **Or**a**c**le **a**daptive **s**chemas”. It is only compatible with Oracle RDBMS.


## Problem

The following problems can frequently occur during development of database applications:

* Why is the column size on the testing environment smaller than on the development database?
* Why is an index missing on the production database?
* Why does my table not have a primary key?
* Why do I always have to write `ALTER TABLE mytable ADD COLUMN...`?  In Java, it is much simpler than `ALTER CLASS myclass.java ADD INSTANCE VARIABLE...`!
* Which SVN or Git revision corresponds to the version of my development database?
* Who overwrote my recent changes to a DB view?

And if you are not busy trying to keep the GO-LIVE date or explaining why the budget was insufficient, you probably have some of the following questions:

* How to synchronize the versions of the development, test and productive databases?
* How to manage versioning, branching and merging of database code?
* How to implement uniform database conventions?
* How to manage concurrent development with several developers on the same development database?
In short:
* How to correctly develop databases?

Orcas is certainly no panacea, but by using it, you get a powerful tool to solve all those problems.

## Introduction

Orcas is a deployment framework for transforming an existing database schema into a target state. The state of the existing schema is irrelevant in most cases. If needed, unnecessary indexes, constraints, columns and tables will be deleted and necessary tables and columns will be added. Data types will be changed if possible. The target state will be provided in the form of SQL files, which are based on the CREATE / ALTER TABLE syntax. The use of Orcas has many advantages. One huge advantage is the possibility of versioning table creation scripts, which is a great help when working in a team, because changes can easily be recognized and undone if needed. An additional benefit is the ease of deployment on different databases without hassle so you have the same version of your source code on any number of databases.

## Documentation

Table of contents

* [How to work with Orcas?]({{site.baseurl}}/docs/usage/)
* [Installation]({{site.baseurl}}/docs/installation/) - What to do to get Orcas working within my own project
* [Examples]({{site.baseurl}}/docs/examples/) - Example projects
* [Ant tasks]({{site.baseurl}}/docs/ant-tasks/) - How to setup the necessary Ant processes
* [Table syntax]({{site.baseurl}}/docs/statics-syntax/) - What do table creation / alteration scripts look like?
* [Domain extension]({{site.baseurl}}/docs/domain-extension/) - How to integrate project specific extensions
* [Extensions]({{site.baseurl}}/docs/extensions/) - How to integrate special project specific extensions?
* [Functionality of Orcas]({{site.baseurl}}/docs/how-it-works/) - How does Orcas work?

## Advantages and Disadvantages

### Advantages

* The target state is managed in simple text-based script files. Because of this, you can use all benefits of a version control system (provide versions, change log, uniform versions, merge support etc.).
* The scripts are a reference in themselves, so you don’t have to search different schemas to get the latest version of a DB package or have to set a default scheme for references.
* You don’t need any complicated or error prone DB release scripts.
* You can create as many schemas for development or testing purposes as you want without any cumbersome comparison effort.

### Disadvantages

* When using unsupported database functions, these parts have to be managed manually.
* Project associates need to know how to work with Orcas.
