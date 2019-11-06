---
layout: page
title: First steps with Orcas
permalink: /docs/usage/
categories: 
- en
---
## Setup

This documentation describes how to work with Orcas. 

For this documentation, the Orderentry example is used, for information on how to install and run orcas refre to the correspponding instructions of your favorite build-tool:

- [Orcas for gradle]({{site.baseurl}}/docs/getting-started-gradle/)

- [Orcas for maven]({{site.baseurl}}/docs/getting-started-maven/)

## Orcas

With Orcas all tables (and other database objects) are stored in plain text files. These then place typically be placed in a version control along with the other sources of a project.

The task of Orcas is to read these files, compare them with an existing database schema and execute the necessary schema changes.

For example in the orderentry example there is a table named "categories". 

It is defined in a file called "categories.sql":

![system overview]({{site.plantumlbaseurl}}/table_categories_intial.iuml)

If the new column "image_location" is inserted:

![system overview]({{site.plantumlbaseurl}}/table_categories_added_column.iuml)

Orcas will add the new column to the database:

![system overview]({{site.plantumlbaseurl}}/table_categories_apply.iuml)

So finally the database looks like:

![system overview]({{site.plantumlbaseurl}}/table_categories_applied.iuml)


In the same way you would perform other changes like:

- Change default values
- Add or delete constraints
- set "not null" or remove it
- Create tables
- Create foreign keys
- Change data type/length.

## Data loss protection

There are two changes that are blocked by default:

- Delete Column (unless the column is empty)
- Delete Table (unless the table is empty)

Both changes are applied, if the table is empty (or if the column only conatins null values). In the Orderentry example it will initially runwithout errors (because all the tables are empty initially). But if you write data into the tables, then this leads to an error message.

*Notices*: If an error like this occurres no changes are made to the schema, thus it can't happen that a transfer was only "half" performed.

This lock can be avoided with the so-called "dropmode". In the Orderentry example you need to change the entry *dropmode="false"* into *dropmode="true"* in the build.xml.

**It's generally not recommended to activate the "dropmode", because in some instances (import of an old version/ Merge error / renaming) it can lead to data loss. 

## Extension of the data model
In typical products 90-95% of the changes to the data model are extensions, which can simply be handled by Orcas.

## Modification of the data model
Changes that do not directly refer to data (eg to expand the Index by a column) are usually also handled by Orcas.

But as soon as a **data migration** is required, Orcas can no longer recognize the necessary changes by a simple comparison. These include the use cases **Rename a table** and **Rename a column**.

Orcas provides one-time-scripts for these scenarios. These are scripts which are only executed once on any database schema.


