---
layout: page
title: Statics Syntax
permalink: /docs/statics-syntax/
categories: 
- en
---

## Syntax explanation

All table, sequence and index scripts using their own syntax, which is precisely explained and illustrated by an example in the following sections.

Declarations in curly brackets are mandatory details. In some cases, there is a default value, which will be used, if there is no declaration. Default values are characterized by <u>underlining</u>. If there is no default value, you have to use a value from the curly brackets. Options are separated by a vertical stroke. Variable declarations, like object names, are written in italics.

Declarations in square brackets are optional details. Values enclosed in quotation marks have to be written with quotation marks.

## Deviations from SQL Standard

- Only compact notations will be supported, in which all constraints and further specifications are included in the create table statement.
- Keywords will only be supported in lower case.
- Indizes will be expected in a create table statement, too. In this process, <code>create index index_name on tabellen_name ...</code><br/> will be transformed to <code>index index_name ...</code>
- 
- The order of constraints is defined as follows:
  1. primary key
  2. check constraint
  3. unique key
  4. foreign key
<br/> Index statements can be mixed with unique keys (e.g. to create a unique key which uses an explicitly created index).
  5. More reserved terms: quite every static term from the SQL syntax is reserved (e.g. "table", "create", "varchar2"). All reserved terms are not allowed to occur as a name. E.g. A column called timestamp is possible in SQl, but not in Orcas. You can easily avoid this limitation, by capitalize these terms (e.g. "TIMESTAMP"). Of course, terms reserved by SQL are not possible with this.

### Syntax

{% highlight sql %}
create {permanent|global temporary} table table_name [alias table_alias](
  column_name { [n]varchar2(char_length {BYTE|CHAR} ) | number[(precision[,scale])] | [n]clob | blob | xmltype | date | timestamp[(scale)][ with_time_zone] | rowid | raw(data_length) | long_raw | float[(scale)] | long | object } [default "default_value"] [not null]
  constraint constraint_name primary key ( primary_key_columns ) { enable | disable }
  constraint constraint_name check ( "check_statement" ) { enable | disable }
  constraint constraint_name { index | unique key } ( colums ) { enable | disable }
  index index_name { function_based | domain_index } ( colums ) { nonunique | unique } { logging | nologging} { noparallel | parallel}
  constraint constraint_name foreign key ( src_column ) references ref_table_name ( ref_column ) { on delete nothing | on delete cascade } { enable | disable }
  comment on { table | column } column_name is "comment_string";
);
{% endhighlight %}

### Beispiel

{% highlight sql %}
create table order_items
(
  orit_id   number(15)                          not null,
  version   number(15)       default "0"        not null,
  ordr_id   number(15)                          not null,
  item_id   number(15)                          not null,
  price     number(8,2)                         not null,
  text      varchar(50),
  quantity  number(4)                           not null,


  constraint orit_pk primary key (orit_id),
  constraint orit_pricecheck check ("price>0"),
  index      orit_text_upper_ix "UPPER(TEXT)",
  constraint orit_uc unique (ordr_id, item_id) disabled,
  index      orit_version_ix (version),
  constraint orit_item_fk foreign key (item_id) references items (item_id),
  constraint orit_ordr_fk foreign key (ordr_id) references orders (ordr_id) on delete cascade

  comment on table is "Detailed description of Order_Items";
  comment on column version is "Detailed description of column Order_Items.Version";
);
{% endhighlight %}

## Column

The following data types are supported in columns:

- number
- varchar2 - Usage of char or byte for length possible
{% highlight sql %}
... varchar2(50 BYTE) ...
{% endhighlight %}
- nvarchar2
- char
- date
- blob
- clob
- nclob
- xmltype
- rowid
- timestamp - Usage of length and/or time zone possible
- raw
- long raw
- float

You have to enclose default values in double quotation marks.

## Primary Key

Nothing special

## Check Constraint

You have to enclose the actual condition in double quotation marks. A constraint name has to be specified, too. But it is also possible to write an [Extension]({{site.baseurl}}/docs/extensions/), which automatically generates these names.

## Unique Key

Nothing special

## Index

An index can be specified within a table (recommended) or outside of the table definition (like SQL Standard). Column-indizes should not be mixed with function-based-indizes. Error are possible.

### Index example (internally)

{% highlight sql %}
index orit_price_ix (price,value)
{% endhighlight %}

{% highlight sql %}
index orit_ix (value) unique nologging parallel
{% endhighlight %}

{% highlight sql %}
index name_lastname_ix "upper(lastname),upper(name)"
{% endhighlight %}

**Attention**
<br/>To ensure, not to create a new index with every build, you have to define a function-based-index in capital letters!

{% highlight sql %}
index such_ix (orde_clob) domain_index "indextype is CTXSYS.CONTEXT PARAMETERS (''Wordlist GERMAN_STEM_PREF'')"
{% endhighlight %}

### Index Beispiel (extern)

{% highlight sql %}
create unique index orit_price_ix on order_items (price)
{% endhighlight %}

## Foreign Key

With foreign keys you have the opportunity to simplify the syntax by using [Extensions]({{site.baseurl}}/docs/extensions/) (e.g. omit a column specification, if you can define it by name conventions).

## Sequence

With sequences you can only specify the sequence name. In addition to this you can use a select, which return the highest value. A check on which value the sequence currently is will be performed and the sequence will be incremented if necessary.

### Syntax

{% highlight sql %}
create sequence sequence_name [orcas_ext_max_value_select 'select-statement']
{% endhighlight %}

### Sequence Beispiel

{% highlight sql %}
create sequence order_items_seq;

create sequence order_items_seq orcas_ext_max_value_select 'select nvl(max(orit_id),0) from order_items';
{% endhighlight %}

## Commentary

For commenting out content of table scripts, the usual syntax (/\* and \+/) of Java and PL/SQL can be used.

{% highlight sql %}
create table order_items
(
  orit_id   number(15)                          not null,
  version   number(15)       default "0"        not null
  /*  further contents will follow. */
);
{% endhighlight %}

## Materialized Views

Materialized view log purge clause: Normally you have to use <code>start with</code>, when using <code>next</code> or <code>repeat interval</code>.

## Complete syntax definition

The exact definition of syntax is described in xText. The BNF-similar definition is right hier: [xText Syntax Definition](https://github.com/opitzconsulting/orcas/blob/master/orcas_core/build_source/orcas/src/de/opitzconsulting/OrcasDsl.xtext)
