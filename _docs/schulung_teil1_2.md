---
layout: page
title: Teil 1.2 - Ein paar einfache Experimente
permalink: /docs/schulung_teil1_2/
---

## Tabellendefinitionen ansehen

Im Verzeichenis **orcas\examples\orderentry\db\tabellen** findet ihr die bisherigen Tabellendefinitionen in "Orcas-Syntax". Um herauszufinden wie etwas in ORCAS beschrieben werden muss, könnt ihr entweder die [Tabellen Syntax]({{site.baseurl}}/docs/statics-syntax) nachlesen, oder via reverse engineering aus einer bestehenden Datenbank konkret die Dinge ansehen die ihr sucht.

Hier als Beispiel die Definition der Tabelle **items**


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

### Eine Spalte hinzufügen
Wir wollen nun eine Spalte **currency** hinzufügen, dafür erweitern wir die Tabellen-Definition einfach um die entsprechende Zeile:

{% highlight sql %}
  currency     varchar2(3)           not null,
{% endhighlight %}

Um nun das Schema neu abzugleichen und die neue Spalte in der DB zu sehen, müsst ihr euch wieder auf die Vagrant-VM verbinden

{% highlight bash%}
vagrant ssh
{% endhighlight %}
dort in das Verzeichnis **/orcas/orcas/examples/orderenty/db** wechseln, in der das build.xml liegt und erneut
{% highlight bash%}
ant
{% endhighlight %}
ausführen.

Nun solltet ihr in der Tabelle **items** die neue Spalte sehen.

### Eine Spalte wegnehmen (ohne Daten)

Ebenso könnt ihr die Spalte

{% highlight sql %}
  currency     varchar2(3)           not null,
{% endhighlight %}

wieder aus der Definition entfernen. Nach erneutem Ausführen von **ant** ist die Spalte wieder weg. 

### Eine Spalte wegnehmen (mit Daten)

Nun wollen wir das Beispiel erneut etwas abgewandelt ausführen, also zunächst kommt die Spalte wieder hinzu:


{% highlight sql %}
  currency     varchar2(3)           not null,
{% endhighlight %}

Nun befüllen wir die Tabelle mit einem Datensatz. Da es einen Foreignkey auf **Categories** gibt, müssen wir zunächst hier einen Datensatz eingeben.

Das Einfügen der Daten erfolgt manuell durch entsprechende Insert-Statements oder DB-Tool.

Als nächstes nehmen wir die Spalte wieder aus der Definition und führen erneut **ant** aus.

Auf der Konsole erscheint nun die folgende Meldung:

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
 
 Sobald durch ein **drop** Daten verloren gehen würden, wird dieses nicht ausgeführt. Eingestellt wird das über die build.xml:
 
  {% highlight xml %}
<target name="build_tables" depends="show_location,orcas_initialize">
  <orcas_execute_statics scriptfolder="tabellen" scriptfolderrecursive="true" spoolfolder="${binrundir}/log" logname="tables" dropmode="false"/>
</target>
  {% endhighlight %}
 
 Im target **build_tables** ist der **dropmode=false**, wir stellen ihn nun auf **true** speichern und führen erneut **ant** aus. Diesmal ist der build wieder erfolgreich:
 
 {% highlight bash %}
  build_all:
  
  BUILD SUCCESSFUL
 {% endhighlight %}
 
  und die Spalte ist nicht mehr vorhanden. Vorsichtshalber stellen wir den Paramter in der build.xml nun wieder zurück ;). 
  Das Thema ant-Tasks folgt ausführlicher in Teil 2 der Schulung.
  

### Neue Tabelle hinzufügen
Übung: Ein Business Partner kann mehrere Adressen haben 

Füge eine neue Tabelle **ADDRESSES** hinzu, die die Adressteile aus **BUSINESS_PARTNERS** übernimmt und über einen Foreignkey aus **BUSINESS_PARTNERS** heraus refrenziert wird.
  
### Weitere Übungen

Bei den Übungen soll jeweils das generierte SQL beobachtet werden, insbesondere ob "drop und create" oder "alter" verwendet wird.

1. Erweiterung des item_uc um die Spalte ctgr_id.
1. Setze item_ctgr_fk auf "on delete cascade".
1. Setze den Datentyp von description auf varchar2(500)
 -  ohne Daten
 -  mit Daten die passen (nicht länger als 500)
 -  mit Daten die nicht passen (länger als 500)
1. Setze den Datentyp von description auf varchar2(2000)
1. Es soll ein Index auf den Spalten price und version erstellt werden
1. Es soll ein Index auf upper(name) erstellt werden.

   
  
  
  
 
