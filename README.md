Orcas
=====

At this place, we have released our open-source schema management framework orcas.

Documentation
-------------

The documentation can be found here: [http://opitzconsulting.github.io/orcas](http://opitzconsulting.github.io/orcas).
(Deutsche Version: [http://opitzconsulting.github.io/orcas/de](http://opitzconsulting.github.io/orcas/de).)

Introduction
------------

Orcas is a schema management and deployment framework for Oracle Databases. Orcas migrates an existing schema into a new to-be state as described by the framework.
The current state of the schema is mostly irrelevant. As required, the framework will remove no longer needed indices, constraints, columns or tables or will create new tables or columns as required.
If possible, the framework will also automatically perform changes to data types.
The to-be-state is described by simple SQL scripts (a DSL). The script syntax resembles the "create table" syntax.
Using orcas allows to version control table scripts. Changes can be tracked and reverted.
