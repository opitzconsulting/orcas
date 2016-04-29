---
layout: page
title: Domain Extension
permalink: /docs/domain-extension/
categories: 
- en
---

Domain-Extension provides a "templating-mechanism" for tables und columns. This mechanism is also known as "Domain-concept".

In the following example you see what it is about:

## Table Domain

Every table should have a column called Id:
{% highlight sql %}
create table tab_a
(
  tab_a_id         number(10)          not null,
  somevalue        varchar2(100)
);

create table tab_b
(
  tab_b_id         number(10)          not null,
  somevalue        varchar2(100)
);
{% endhighlight %}

This can be simplified with a Domain-Extension. For this, a Table-Domain will be created, which defines, the table has to have an additional column called Id:
{% highlight sql %}
define table domain id_table
(
  add column column-name(table-name||"_"||column-name) ( id number(10) not null)
);
{% endhighlight %}

The new Domain can now be used for defining tables:
{% highlight sql %}
create table tab_a domain id_table
(
  somevalue        varchar2(100)
);

create table tab_b domain id_table
(
  somevalue        varchar2(100)
);
{% endhighlight %}
*Notice:* The column somevalue needs to be existing in this example because Orcas always needs at least one column for each table definition.

## Column Domain
So far so good, but how to handle foreign-keys? Now we gonna expend our example with a foreign-key:
{% highlight sql %}
create table tab_a domain id_table
(
  somevalue        varchar2(100),
  constraint tab_a_pk primary key (tab_a_id)
);

create table tab_b domain id_table
(
  somevalue      varchar2(100),
  tab_a_id       number(10),
  constraint fk_tab_a_id foreign key (tab_a_id) references tab_a (tab_a_id)
);
{% endhighlight %}
*Notice:* Now the table tab_a needs a PK. How to generate this, we'll see later, but now we'll concentrate on foreign-keys first.

Based on the fact that FKs always point on PKs in our schema, all FKs are from data type number(10). To unify that, we can create a column-domain:
{% highlight sql %}
define column domain fk_column
(
  number(10)
);
{% endhighlight %}

This one can now be used in the column definition:
{% highlight sql %}
create table tab_a domain id_table
(
  somevalue        varchar2(100),
  constraint tab_a_pk primary key (tab_a_id)
);

create table tab_b domain id_table
(
  somevalue      varchar2(100),
  tab_a_id       domain fk_column,
  constraint fk_tab_a_id foreign key (tab_a_id) references tab_a (tab_a_id)
);
{% endhighlight %}

Because all PK column names are unique, we are actually able to outsource our foreign-key definition into our column-domain:
{% highlight sql %}
define column domain fk_column
generate-foreign-key (constraint-name ("fk_" || column-name) pk-column-name(column-name))
(
  number(10)
);
{% endhighlight %}

So, our table definition would be shortened anymore:

{% highlight sql %}
create table tab_a domain id_table
(
  somevalue        varchar2(100),
  constraint tab_a_pk primary key (tab_a_id)
);

create table tab_b domain id_table
(
  somevalue      varchar2(100),
  tab_a_id       domain fk_column
);
{% endhighlight %}

To outsource the generation of our primary-keys, we have to define a column-domain and use it in our table-domain:

{% highlight sql %}
define column domain pk_column
(
  number(10) not null
);

define table domain id_table
(
  add column column-name(table-name||"_"||column-name) ( id domain pk_column )
);
{% endhighlight %}

First, there is no effect, but now we are able to add a primary-key to our new column-domain:

{% highlight sql %}
define column domain pk_column
generate-primary-key (constraint-name(table-name || "_pk"))
(
  number(10) not null
);
{% endhighlight %}

Now the definition of our table is obviously simplified (tab_b now also has a primary-key):
{% highlight sql %}
create table tab_a domain id_table
(
  somevalue        varchar2(100)
);

create table tab_b domain id_table
(
  somevalue      varchar2(100),
  tab_a_id       domain fk_column
);
{% endhighlight %}

Following you'll find a list of all included features. All these features are used in the [Domain-Extension-example]({{site.baseurl}}/docs/examples/#domain_extension_demo):

## Features

* Column Domains
  * Data type
  * Precision/Scale
  * not null
  * Default-value
  * Generate constraints:
    * Primary Key
      * with associated sequence
    * Foreign Key
    * Unique Key
      * Multiple columns possible
    * Check-Constraint 
* Table Domains
  * Add columns
  * Add history table
  * Inheritance for table domains
* Generate triggers for filling history tables


## How to use Domain-Extensions?

The extension-folder-parameter ([orcas_initialize]({{site.baseurl}}/docs/ant-tasks/#orcas_initialize)) has to point on orcas_domain_extension/extensions.
After this you are able to define and use domains in scripts, like you can do with tables and sequences. There is no specific file extension or directory structure. The order is also irrelevant, as usual in Orcas.

## Does Reverse-Engineering work?

Yes! But first, Orcas has to know the domain-definitions, which your want to use. To achieve this, you have to use a little trick:
<br/>You have to execute [orcas_execute_statics]({{site.baseurl}}/docs/ant-tasks/#orcas_execute_statics), namely with the domains. As you know, Orcas doesn't work without a table, you have to add a dummy table. Important: you should execute orcas_execute_statics with <code>logonly="true"</code> because if you don't, it will delete everything within your schema (Dropmode-check should prevent this, but orcas_execute_statics will terminate with an error).

As soon as orcas_execute_statics terminated successfully with your domain-definitions, you are able to use ([orcas_extract]({{site.baseurl}}/docs/ant-tasks/#orcas_extract)) as usual, whereas XSLT-File has to reference orcas_domain_extension/xslt_extract/orcas_domain_extract.xsl.

## Example
You''l find an example for the domain-extension right here: [Domain-Extension-Demo]({{site.baseurl}}/docs/examples/#domain_extension_demo).

## Extension
What to do if the domain-extension is not existant, but you need it in your project?

Basically you are able to combine your own extensions with the domain-extension. But here it is mandatory to pay attention to the order of execution. It is also possible to change the domain-extension (to use it as a copy template for an own extension).
Certainly it is more recommendable to create a completely new extension, because the domain-extension is quite complex. For a concrete project it is often much easier to code the definition directly into the extension.

The domain-extension is an 80% approach. If this is not enough, you usually have to use a completely own extension. 

Of course it is always useful to create an <a href="{{ site.github_issues }}">Issue</a> in the github page for a missing feature.
