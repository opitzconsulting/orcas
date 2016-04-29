---
layout: page
title: Part 1.2 - A few simple experiments
permalink: /docs/training_part1_2/
categories: 
- en
---

## View table definitions

In the directory **orcas\examples\orderentry\db\tabellen** you can find the previous table definitions in "Orcas syntax". To find out how something has to be described in ORCAS, you can either read the [table syntax]({{site.baseurl}}/docs/statics-syntax), or see things you seek via reverse engineering from an existing database actually.

Here as an example the definition of the table **items**


{% highlight sql %}
create table items
(
  item_id         number(15)                    not null,
  version         number(15)      default "0"   not null,
  ctgr_id         number(15)                    not null,
  name            varchar2(30)                  not null,
  description     varchar2(1000)                not null,
  image_location  varchar2(255),
  price           number(8,2)                   not null,

  constraint item_pk primary key (item_id),
  constraint item_uc unique (name),
  constraint item_ctgr_fk foreign key (ctgr_id) references categories (ctgr_id),

  comment on column version is 'default ist null';
  comment on table is 'items tabelle';  
);
{% endhighlight %}

### Add a column
We want to add only one column **currency**, but we are expanding the table definition simply to the appropriate line:

{% highlight sql %}
  currency     varchar2(3)           not null,
{% endhighlight %}

In order to synchronize the schema again and to see the new column in the database, you have to connect you back to the Vagrant VM

{% highlight bash%}
vagrant ssh
{% endhighlight %}
There switch to the directory **/orcas/orcas/examples/orderenty/db**, where the build.xml is and run again
{% highlight bash%}
ant
{% endhighlight %}

Now you should see in the table **items* the new column.

### Remove a column (no data)

Also you can remove the column 

{% highlight sql %}
  currency     varchar2(3)           not null,
{% endhighlight %}

again from the definition. After another run of **ant** the column is gone.

### Remove a column (with data)

Now let the example rerun slightly modified, so initially the column is added again:


{% highlight sql %}
  currency     varchar2(3)           not null,
{% endhighlight %}

Now we want to add the table with one record. There's a foreign key for categories, we must first enter a record here.

The insertion of data is done manually by corresponding insert statements or database tool.

Next, use the column again from the definition and rerun **ant** .

Now the following message appears on the console:

 {% highlight bash %}
[echo] ERROR at line 1:
[echo] ORA-20000: drop mode ist nicht aktiv, daher kann folgendes statement nicht
[echo] ausgefuehrt werden: alter table ITEMS drop column CURRENCY
[echo] ORA-06512: at "ORCAS_ORDERENTRY_ORCAS.PA_ORCAS_COMPARE", line 407
[echo] ORA-06512: at "ORCAS_ORDERENTRY_ORCAS.PA_ORCAS_COMPARE", line 2309
[echo] ORA-06512: at "ORCAS_ORDERENTRY_ORCAS.PA_ORCAS_COMPARE", line 2576
[echo] ORA-06512: at "ORCAS_ORDERENTRY_ORCAS.PA_ORCAS_COMPARE", line 2771
[echo] ORA-06512: at "ORCAS_ORDERENTRY_ORCAS.PA_ORCAS_DDL_CALL", line 10
[echo] ORA-06512: at line 9
 {% endhighlight %}

As soon a the data get lost through a **drop**, this is not performed. It is determined by the build.xml:

  {% highlight xml %}
<target name="build_tables" depends="show_location,orcas_initialize">
  <orcas_execute_statics scriptfolder="tabellen" scriptfolderrecursive="true" spoolfolder="${binrundir}/log" logname="tables" dropmode="false"/>
</target>
  {% endhighlight %}
  
In the target **build_tables** is the **dropmode=false**, we set it to **true**, save and run again **ant**. This time the build is successful again:
  
 {% highlight bash %}
  build_all:
  
  BUILD SUCCESSFUL
 {% endhighlight %}
 
and the column is no longer available. As a precaution, we put the parameters in build.xml now back again ;).
The topic ant tasks follows in more detail in part 2 of the training.

### Add new table
Exercise: A business partner can have several addresses

Now add a new table **ADDRESSES** that accepts the address line of **BUSINESS_PARTNERS** and is referenced by a foreign key from **BUSINESS_PARTNERS**.

### More exercises

For the exercises respectively the generated SQL is to be observed, especially if "drop and create" or "alter" is used.

1. Expansion of item_uc the column ctgr_id.
1. Set item_ctgr_fk to "on delete cascade".
1. Set the data type from description to varchar2(500)
 -  no data
 -  with data that fit (not longer than 500)
 -  with data that does not fit (longer than 500)
1. Set the data type from description to varchar2(2000)
1. You want to create an index on the column price and version
1. An index on upper(name) should be created.