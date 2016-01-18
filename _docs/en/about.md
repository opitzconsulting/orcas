---
layout: page
title: About Orcas
permalink: /
categories: 
- en
---

Orcas is an Open Source Deployment framework for transferring an existing **database schema** into a **target state** which is specified by **text files**.
Because of the usage of easy text files the **integration** into existing project with **version control** is quiet easy. Orcas also has the opportunity to integrate complex transfers.
In addition to this Orcas got an extension mechanism to substantially simplify and unify the definition of the database scheme.

Orcas stands for **Or**a**c**le **a**daptive **s**chemas” and is only compatible with Oracle RDBMS.

##Problem

The following problem cases will mostly occur while database developing and always at the worst time:

- Why is the column size on the testing system smaller than on the development database?
- Why is an index missing on the productive database?
- Who forgot the PK at the table?
- Why do I always have to write “ALTER TABLE mytable ADD COLUMN”?  Also I don’t write “ALTER CLASS myclass.java ADD INSTANCE VARIABLE”
- Which SVN/Git-Version matches my development database, too?
- Who overwrote my view changing?

If you are not busy saving the GO-LIVE date or declaring why the budget unfortunately wasn’t enough, you probably have some of the following questions:

- How to synchronize the versions of the development database, the test database and the productive database?
- How to manage versioning, branching and merging of databases?
- How to implement uniform database conventions?
- How to manage simultaneously developing with more than one developer at the same time on the development database?
In short:
- How to develop databases correctly?

Orcas is no panacea but used right it can be a practicable solution for all these questions.


##Introduction

Orcas is a Deployment framework for transferring an existing database schema into a target state. The state of the existing scheme is mostly irrelevant for this. If needed, unnecessary indexes, constraints, columns and tables will be rejected or new tables and columns will be added. Data types will be changed if possible. The target state will be provided in the form of SQL script files, which are based on the “CREATE TABLE” syntax. The use of Orcas has many advantages. One huge advantage is the possibility of versioning table scripts, which is a great relief when working in a project team, because changes can easy be recognized and be undone if needed. An additional benefit is the deploying on different database without circumstances so you have the same version of your databases on any number of schemea.

##Documentation

These are the most important chapters of this documentation with a short description:

- [How to work with Orcas?]({{site.baseurl}}/docs/usage/)
- [Installation]({{site.baseurl}}/docs/installation/) - What to do to get Orcas working within my own project?
- [Examples]({{site.baseurl}}/docs/examples/) - BExample projects
- [ant tasks]({{site.baseurl}}/docs/ant-tasks/) - How to create the complete process with ant?
- [Table syntax]({{site.baseurl}}/docs/statics-syntax/) - What do table scripts look like?
- [Domain extension]({{site.baseurl}}/docs/domain-extension/) - How to integrate project specific extensions the easy way?
- [Extensions]({{site.baseurl}}/docs/extensions/) - How to integrate special project specific extensions?
- [Functioning of Orcas]({{site.baseurl}}/docs/how-it-works/) - How does Orcas work?


##Advantages and Disadvantages

### Advantages

- The target state is managed by easy text script files. With this you can use all benefits of a version control system (provide versions, understand who did when which changing, uniform versions, merge-support, …).
- The scripts are a real “reference”, so you don’t have to search all schemes to get the latest package version or have to set a default scheme for references.
- You don’t need any complicate or prone DB release scripts.
- You can create as many schemes for development or test purposes as you want without having any disproportionate effort getting vulnerable to errors.


###Disadvantages

- When using unsupported database functions, these parts have to be managed manually.
- Project associates need to know how to work with Orcas.
