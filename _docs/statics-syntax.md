---
layout: docs
title: Statics Syntax
prev_section: ant-tasks
next_section: extensions
permalink: /docs/statics-syntax/
---

#Statics Syntax

`todo: Inhalt mit Links`

##Syntax Erklärung

Die Tabellen-, Sequenzen- und Index-Skripte besitzen alle ihre eigene Syntax, die in den einzelnen Abschnitten durch eine genaue Beschreibung und jeweils ein Beispiel erklärt wird.
<br/><br/>Angaben in den geschweiften Klammern sind Pflichtangaben. In einigen Fällen ist ein Standardwert definiert, der verwendet wird, wenn keine Angabe gemacht wird. Standardwerte sind durch <u>Unterstreichung</u> gekennzeichnet. Falls kein Standardwert definiert ist, muss ein Wert aus den geschweiften Klammern verwendet werden. Die Möglichkeiten sind durch einen Senkrecht-Strich getrennt. Variable Angaben, wie etwa Objektnamen, sind *kursiv* geschrieben.
<br/><br/>Angaben in den eckigen Klammern sind optional. Bei Werten innerhalb von Anführungszeichen müssen die Anführungszeichen mitgeschrieben werden.

##Abweichungen vom SQL Standard

- Es wird nur die kompakte Schreibweise unterstützt, bei der alle constraints um weitere Angaben in dem create table enthalten sind.
- Auch Indizes werden innerhalb des create table Statements erwartet, dabei wird aus dem <code>create index index_name on tabellen_name ...</code><br/>ein <code>~~create~~ index index_name ~~one tabellen_name~~ ...</code>
- Die Reihenfolge der constraints ist wie folgt:
  1. primary key
  2. check constraint
  3. unique key
  4. foreign key
<br/>Die index Statements können mit den unique keys gemischt werden (um z.B. einen unique key anzulegen der einen explizit angelegten index nutzt).
  5. Mehr reservierte Begriffe: so ziemlich alle statischen Begriffe aus der Syntax sind reserviert (z.B.: "table","create","varchar2"). Alle reservierten Begriffe dürfen nicht als Namen vorkommen. Z.B. ist timestamp in SQL als Spaltenname möglich, in Orcas nicht. Diese Limitierung kann man sehr leicht umgehen, wenn man die Begriffe groß schreibt (z.B. "TIMESTAMP"). Natürlich sind in SQL reservierte Bergiffe damit auch nicht möglich.

###Syntax

{% highlight sql %}
create {permanent|global temporary} table table_name [alias table_alias](
  column_name { varchar2(scale {BYTE|CHAR} ) | number(scale[,precision]) | clob(scale) | blob | xmltype | date } [default "default_value"] {null | not null}
  constraint constraint_name primary key ( primary_key_columns ) { enable | disable }
  constraint constraint_name check ( "check_statement" ) { enable | disable }
  constraint constraint_name { index | unique key } ( colums ) { enable | disable }
  index index_name { function_based | domain_index } ( colums ) { nonunique | unique } { logging | nologging} { noparallel | parallel}
  constraint consrtaint_name foreign key ( _src_column ) references des_table_name ( dest_column ) { on delete nothing | on delete cascade } { enable | disable }
  comment on { table | column } {column_name}_ is "comment_string";
);
{% endhighlight %}

###Beispiel

```sql
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
  index      orit_text_upper_ix (upper(text)),
  constraint orit_uc unique (ordr_id, item_id) disabled,
  index      orit_version_ix (version),
  constraint orit_item_fk foreign key (item_id) references items (item_id),
  constraint orit_ordr_fk foreign key (ordr_id) references orders (ordr_id) on delete cascade

  comment on table is "Ausführliche Beschreibung von Order_Items";
  comment on column version is "Ausführliche Beschreibung der Spalte Order_Items.Version";
);
```

##Column

Bei Spalten werden folgende Datentypen unterstützt:

- number
- varchar2 - Angabe von char oder byte bei der Längenangabe ist möglich
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

Bei Defaultwerten müssen doppelte Anführungszeichen um den Ausdruck gesetzt werden.

##Primary Key

Keine Besonderheiten

##Check Constraint

Um die eigentliche Bedingung müssen doppelte Anführungszeichen gesetzt werden. Ein Constraintname muss mit angegeben werden, es ist aber möglich eine [Extension]({{site.baseurl}}/docs/extensions/) zu schreiben, die Namen automatisch vergibt.

##Unique Key

Keine Besonderheiten

##Index

Ein Index kann innerhalb einer Tabelle angegeben werden (dies wird empfohlen) oder extern ausserhalb der Tabellendefinition (wie im SQL Standard). Spalten-Indizes sollten nicht mit funcion-based-Indizes gemischt werden. Es kann dabei zu Fehlern kommen.

###Index Beispiel (intern)

{% highlight sql %}
index orit_price_ix (price,value)
{% endhighlight %}

{% highlight sql %}
index orit_ix (value) unique nologging parallel
{% endhighlight %}

{% highlight sql %}
index name_lastname_birthdate_ix "upper(lastname),upper(name)"
{% endhighlight %}

**Achtung**
<br/>Möchte man sicherstellen, dass der Index nicht bei jedem Build Neu angelegt wird, muss die Definition eines Function-Based-Index in Großbuchstaben erfolgen!

{% highlight sql %}
index such_ix (orde_clob) domain_index "indextype is CTXSYS.CONTEXT PARAMETERS (''Wordlist GERMAN_STEM_PREF'')"
{% endhighlight %}

###Index Beispiel (extern)

{% highlight sql %}
create unique index orit_price_ix on order_items (price)
{% endhighlight %}

##Foreign Key

Bei Foreign Keys gibt es einige Möglichkeiten über [Extension]({{site.baseurl}}/docs/extensions/) die Syntax zu vereinfachen (z.B. weglassen der Spaltenangabe wenn diese über Namenskonventionen bestimmt werden können).

##Sequence

Bei Sequences kann nur der Sequence Name angegeben werden. Zusätzlich kann ein Select angegeben werden, was den größten verwendeten Wert zurückliefert. Wenn dies geschieht, wird geprüft, auf welchen Wert die Sequence aktuell steht und ggf. die Sequence hochgezählt.

###Syntax

{% highlight sql %}
create sequence sequence_name [orcas_ext_max_value_select 'select-statement']
{% endhighlight %}

###Sequence Beispiel

{% highlight sql %}
create sequence order_items_seq;

create sequence order_items_seq orcas_ext_max_value_select 'select nvl(max(orit_id),0) from order_items';
{% endhighlight %}

##Kommentare

Zum auskommentieren von Inhalten der Tabellen-Skripte kann die in Java übliche Syntax /\* und \*/ verwendet werden.

{% highlight sql %}
create table order_items
(
  orit_id   number(15)                          not null,
  version   number(15)       default "0"        not null
  /*  weitere Inhalte mussen noch ergänzt werden */
);
{% endhighlight %}

##Materialized Views

Materialized view log purge clause: Es gilt die Regel, das start with angegeben werden muss, wenn next oder repeat interval gesetzt werden.

##Vollständige Syntax Definition

Die genaue Definition der Syntax ist in xText beschrieben. Die BNF-ähnliche Definition findet sich hier: [xText Syntax Definition](https://github.com/opitzconsulting/orcas/blob/master/orcas_core/build_source/orcas/src/de/opitzconsulting/OrcasDsl.xtext)
