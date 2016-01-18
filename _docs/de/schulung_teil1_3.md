---
layout: page
title: Teil 1.3 - one-time-skripte
permalink: /docs/de/schulung_teil1_3/
categories: 
- de
---

Manchmal ist es nötig zusätzliche Skripte auf der DB auszuführen. Dafür gibt es in ORCAS die **one-time-skripte**, also Skripte die (auf jedem Datenbankschema) genau einmal ausgeführt werden.


### Spalte umbenennen (mit Daten)

Voraussetzung:
In der Tabelle Items befinden sich nach wie vor Daten. 


Nun wollen wir die Spalte **description** in **descriptions** umbenennen.

Zunächst muss die Tabellendefinition entsprechend angepasst werden:


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

Im Anschluss wieder per **ant** der build ausgeführt werden.

Wenn in der build.xml der Drop-Modus wieder auf **false** gesetzt wurde, kommt es erneut zu einer Fehlermeldung. Da ORCAS nicht erkennen kann, dass die Spalte nur umbenannt wurde, 
wird versucht die Spalte **description** zu entfernen und eine neuen Spalte **descriptions** hinzufügen. 
Deswegen zieht hier wieder der Drop-Modus und verhindert, dass Daten verloren gehen. Wäre die Tabelle leer, würden die Änderungen ausgeführt.
  
Da wir unsere Besipieldaten nicht verlieren wollen ist das Setzen des **dropmode=true** keine Alternative. Stattdessen können wir hierfür ein **one-time-script** nutzen:

{% highlight sql %}
ALTER TABLE ITEMS RENAME COLUMN DESCRIPTION TO DESCRIPTIONS;
{% endhighlight %}

Das Skript wird im Ordner **skripte** unter einem beliebigen Namen gespeichert.
Die Tabellendefinition von **items** entspricht bereits dem Zielscenario **descriptions**

Nun können wir wieder **ant** ausführen.

Der build war erfolgreich und unsere Spalte ist umbenannt.
Wichtig hierfür war, dass das Skript ausgeführt wurde bevor die Tabellen gemäß Beschreibung validiert/erstellt wurden. Somit hieß die Spalte bereits description**s** als sie gegen die Tabellendefinition validiert wurde.

### Pre-Skripte in der build.xml

Dass das Skript VOR den Tabellendefinitionen ausgeführt wurde wird über die build.xml gesteuert:

{% highlight xml %}
<target name="pre_scripts" depends="show_location,orcas_initialize">
  <orcas_execute_one_time_scripts scriptfolder="skripte" logname="pre_skripte"/>
</target>
{% endhighlight %}

hier wurde auch der Ordner **skripte** für unser Skript definitiert. 

Mit:

{% highlight xml %}
<target name="build_all" depends="show_location,pre_scripts,build_tables">
</target>
{% endhighlight %}

Wird die Reihenfolge der Ausführung festgelgt, also **pre_scripts** vor **build_tables**

### Wie funktionieren Einmalskripte

Ähnlich zu Flyway und Liquibase, sorgt auch in ORCAS eine Tabelle dafür, dass jedes Skript nur einmal ausgeführt wird. Im User **ORCAS_ORDERENTRY_ORCAS** ist die entsprechenden Tabelle **ORCAS_UPDATES** zu finden.
Hier kann eingesehen werden, wann welches Skript ausgeführt wurde.



