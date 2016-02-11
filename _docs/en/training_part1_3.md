---
layout: page
title: Part 1.3 - one-time scripts
permalink: /docs/training_part1_3/
categories: 
- en
---

Sometimes it's necessary to execute additional scripts on the database. In ORCAS there are **one-time skripts**, which are scripts that are running (on any database schema) exactly once.


### Rename column (with data)

Voraussetzung:
In der Tabelle Items befinden sich nach wie vor Daten.


Now we want to rename the column **description** into **descriptions**.

First, the table definition must be adjusted accordingly:


{% highlight sql %}
create table items
(
  item_id         number(15)                    not null,
  version         number(15)      default "0"   not null,
  ctgr_id         number(15)                    not null,
  name            varchar2(30)                  not null,
  descriptions    varchar2(1000)                not null,
  image_location  varchar2(255),
  price           number(8,2)                   not null,

  constraint item_pk primary key (item_id),
  constraint item_uc unique (name),
  constraint item_ctgr_fk foreign key (ctgr_id) references categories (ctgr_id),

  comment on column version is 'default ist null';
  comment on table is 'items tabelle';  
);
{% endhighlight %}

Next run the build again with **ant**.

When in the build.xml the drop mode back is set to **false**, there is again an error message. Since ORCAS can not recognize that the column was only renamed, it's trying to remove the column **description** and adds the new column **descriptions**. Therefore the drop mode is active and prevents data loss. If the table is empty, the changes would be carried out.

Since we do not want to lose our sample data, setting **dropmode=true** isn't an alternative. Instead we can use an **one-time script**:

{% highlight sql %}
ALTER TABLE ITEMS RENAME COLUMN DESCRIPTION TO DESCRIPTIONS;
{% endhighlight %}

The script is saved in the folder **skripte** with any name. 
The table definiton from **items** corresponds already to the target scenario **descriptions**.

Now we can run **ant**. 

The build was successful and another column is renamed.
Important for this was that the script was executed before the tables were validated/created as described. Thus, the column was already named description **s** when it has been validated against the table definition.

### Pre-skripts in the build.xml

That the script executed BEFORE the table definitions is controlled via the build.xml:

{% highlight xml %}
<target name="pre_scripts" depends="show_location,orcas_initialize">
  <orcas_execute_one_time_scripts scriptfolder="skripte" logname="pre_skripte"/>
</target>
{% endhighlight %}

Here is also the folder **scripts** defined for our script.

By

{% highlight xml %}
<target name="build_all" depends="show_location,pre_scripts,build_tables">
</target>
{% endhighlight %}

the order of execution is set, therefore **pre_scripts** before **build_tables**.

### How does one-time scripts work

Similar to Flyway and Liquibase, a table also provides in ORCAS that each script runs only once. In **ORCAS_ORDERENTRY_ORCAS** the corresponding table **ORCAS_UPDATES** is found.
Here can be viewed when each script was run.