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

- [Orcas for maven]({{site.baseurl}}/docs/getting-started-maven/)

- [Orcas for gradle]({{site.baseurl}}/docs/getting-started-gradle/)

- [Orcas for ant]({{site.baseurl}}/docs/examples/)

## Orcas

With Orcas all tables (and other database objects) are stored in plain text files. These then place typically be placed in a version control along with the other sources of a project.

The task of Orcas is to read these files, compare them with an existing database schema and execute the necessary schema changes.

For example in the orderentry example there is a table named "categories". 

It is defined in a file called "categories.sql":
{% highlight sql %}
create table categories
(
  ctgr_id      number(15)                  not null,
  version      number(15)      default "0" not null,
  name         varchar2(30)                not null,
  description  varchar2(1000)              not null,

  constraint ctgr_pk primary key (ctgr_id),
  constraint ctgr_uc unique (name)
);
{% endhighlight %}

If the new column "image_location" is inserted:

{% highlight sql %}
create table categories
(
  ctgr_id        number(15)                  not null,
  version        number(15)      default "0" not null,
  name           varchar2(30)                not null,
  description    varchar2(1000)              not null,
  image_location varchar2(1000),

  constraint ctgr_pk primary key (ctgr_id),
  constraint ctgr_uc unique (name)
);
{% endhighlight %}

Orcas will add the new column to the database.

In the same way you would perform other changes like:

- Change default values
- Add or delete constraints
- set "not null" or remove it
- Create tables
- Create foreign keys
- Change data type/length.

## Deleting parts of the data model

There are two changes that are blocked by default:

- Delete Column (unless the column is empty)
- Delete Table (unless the table is empty)

Both changes run through, if the table is empty (or in columns although if only null values are present). In the Orderentry example it will work for now (because all the tables are empty initially). But if you write data into the tables, then this leads to an error message.

*Notices*: If an error occurred no changes are made to the schema, thus it can't happen that a transfer was only "half" performed.

This lock can be avoided with the so-called "dropmode". In the Orderentry example you need to change the entry *dropmode="false"* into *dropmode="true"* in the build.xml.

**It's generally not recommended to activate the "dropmode", because in some instances (import of an old version/ Merge error / renaming) it can lead to data loss. Therefore "dropmode" should at least not be enabled on production databases.**

## Extension of the data model
In typical products 90-95% of the changes to the data model are extensions, which can usually be easily implemented with Orcas.

## Modification of the data model
Changes that do not directly refer to data (eg to expand the Index by a column) are usually also easily to handle with Orcas.

But as soon as a **data migration** is required, Orcas can no longer recognize the necessary changes by a simple adjustment. These include the use cases **Rename a table** and **Rename a column**.

Orcas offers two ways to perform such changes to the data model.

### 1. one_time_scripts
In this possibility SQLPlus scripts on any database schema only be executed exactly once. The Orderentry example is so arranged, that all scripts under orderentry\db\skripte are handled like this.

### 2. liquibase
With [liquibase](http://www.liquibase.org/) a change statement in the database change log is usually necessary for any change to a scheme. Orcas offers the possibility to reduce  these change instructions to a minimum.

With both variants you have to mind, there is a possibility developers **forget** to create a specificone-time-script or database-change-log-entry. In these cases the **dropmode** is very important to **prevent** a loss of data.

Furthermore you always have to change the table scripts, too.

[So why don't we take liquibase (without Orcas)?]({{site.baseurl}}/docs/liquibase/)
