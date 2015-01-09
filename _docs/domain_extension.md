---
layout: page
title: Domain Extension
permalink: /docs/domain-extension/
---

Die Domain-Extension ist eine Extension die einen "Templating-Mechanismus" für Tabellen und Spalten bereitstellt. Der Mechanismus wird auch "Domänen-Konzept" genannt. 

Das nachfolgende Beispiel zeigt worum es genau geht:

##Table Domain

Jede Tabelle soll eine Id-Spalte haben:
{% highlight sql %}
create table tab_a
(
  tab_a_id         number(10)
);

create table tab_b
(
  tab_b_id         number(10)
);
{% endhighlight %}

Dies kann mit der Domain-Extension vereinfacht werden. Dazu wird eine Table-Domain angelegt, die definiert, dass die zugehörigen Tabellen eine zusätzliche Id-Spalte haben sollen:
{% highlight sql %}
define table domain id_table
(
  add column column-name(table-name||"_"||column-name) ( id number(10) )
);
{% endhighlight %}

Die neue Domain kann jetzt bei der Definition der Tabellen verwendet werden:
{% highlight sql %}
create table tab_a domain id_table
(
);

create table tab_b domain id_table
(
);
{% endhighlight %}

##Column Domain
Soweit so gut, aber wie gehen wir mit foreign-keys um? Erweitern wir also das Beispiel um eine foreign-key-Spalte:
{% highlight sql %}
create table tab_a domain id_table
(
);

create table tab_b domain id_table
(
  tab_a_id       number(10),
  constraint fk_a_id foreign key (a_id) references tab_a (tab_a_id)
);
{% endhighlight %}

Wenn wir mal davon ausgehen, dass in unserem Schema FKs immer auf die primary-key-Spalten zeigen, dann haben alle FK-Spalten den Datentyp number(10). Um das zu vereinheitlichen können wir eine Spalten-Domain einführen:
{% highlight sql %}
define column domain fk_column
(
  number(10)
);
{% endhighlight %}

Diese kann dann bei der Spaltendefinition genutzt werden:
{% highlight sql %}
create table tab_a domain id_table
(
);

create table tab_b domain id_table
(
  tab_a_id       domain fk_column,
  constraint fk_tab_a_id foreign key (tab_a_id) references tab_a (tab_a_id)
);
{% endhighlight %}

Da in unserem Schema die PK-Spaltennamen eindeutig sind, können wir sogar die foreign-key-Definition in unsere Spalten-Domäne auslagern:
{% highlight sql %}
define column domain fk_column
generate-foreign-key (constraint-name ("fk_" || column-name) pk-column-name(column-name))
(
  number(10)
);
{% endhighlight %}
Somit würde die Tabellendefinition deutlich verkürzt:
{% highlight sql %}
create table tab_a domain id_table
(
);

create table tab_b domain id_table
(
  tab_a_id       domain fk_column
);
{% endhighlight %}


Column-Domains können auch in Table-Domains genutzt werden:

{% highlight sql %}
define column domain pk_column
(
  number(10)
);

define table domain id_table
(
  add column column-name(table-name||"_"||column-name) ( id domain pk_column )
);
{% endhighlight %}

Darüber können dann auch automatisch primary-key-constraints und Sequenzen generiert werden. Nachfolgend eine Liste der enthaltenen Funktionen, diese werden alle vom [Domain-Extension-Demo]({{site.baseurl}}/docs/examples/#domain_extension_demo) Beispielprojekt genutzt:

##Features

* Column Domains
  * Datentyp
  * Precision/Scale
  * not null
  * Default-Wert
  * Constraints erzeugen:
    * Primary Key
      * mit zugehöriger Sequenz
    * Foreign Key
    * Unique Key
      * auch mehrspaltig
    * Check-Constraint 
* Table Domains
  * Spalten hinzufügen
  * History-Tabelle hinzufügen
  * Vererbungsmechanismus für Table Domains
* Trigger zum befüllen von History-Tabellen generieren


##Wie verwendet man die Domain-Extension?
Dazu muss nur der Extension-Folder-Parameter ([orcas_initialize]({{site.baseurl}}/docs/ant-tasks/#orcas_initialize)) auf orcas_domain_extension/extensions verweisen.
Danach kann man die Domains genau wie Tabellen und Sequenzen in Skripten definieren und verwenden. Es ist keine besondere Dateiendung oder Verzeichnisstruktur notwendig. Auch die Reihenfolge ist, wie bei Orcas üblich, irrelevant.

##Funktioniert Reverse-Engineering?
Ja! Dazu muss Orcas zunächst einmal die Domain-Definitionen kennen, die verwendet werden sollen. Dazu ist ein kleiner Trick erforderlich:
<br/>[orcas_execute_statics]({{site.baseurl}}/docs/ant-tasks/#orcas_execute_statics) muss ausgeführt werden, und zwar mit den Domains. Da orcas_execute_statics leider nicht funktioniert, wenn nicht mindestens eine Tabelle angegeben ist, muss man also noch eine Dummy-Tabelle hinzufügen. Wichtig: orcas_execute_statics sollte auf jeden Fall mit <code>logonly="true"</code> ausgeführt werden, da sonst im Schema ja alles gelöscht werden würde (die Dropmode-Prüfung sollte dies zwar verhindern, aber dann bricht orcas_execute_statics mit einem Fehler ab). 

Sobald orcas_execute_statics mit den Domain-Definitionen erfolgreich durchgelaufen ist, kann man ganz normal ([orcas_extract]({{site.baseurl}}/docs/ant-tasks/#orcas_extract)) verwenden, wobei XSLT-File auf orcas_domain_extension/xslt_extract/orcas_domain_extract.xsl verweisen muss.

##Beispiel

Ein Beispielprojekt das die Domain-Extension verwendet findet sich hier: [Domain-Extension-Demo]({{site.baseurl}}/docs/examples/#domain_extension_demo).

##Erweiterung
Was wenn die Domain-Extension eine Funktionalität nicht bietet, die im Projekt gebraucht wird?

Man kann grundsätzlich eigene Extensions mit der Domain-Extension kombinieren, dabei muss aber sehr auf die Ausführungs-Reihenfolge geachtet werden. Es ist auch denkbar, die Domain-Extension abzuändern (als Kopiervorlage für eine eigene Extension verwenden).
Allerdings ist es ggf. ratsamer eine komplett eigene Extension zu erstellen, da die Domain-Extension relativ kompliziert ist. Für ein konkretes Projekt ist es oft viel einfacher die Definition direkt in der Extension auszuprogrammieren.

Die Domain-Extension ist ein 80%-Ansatz, wenn das nicht ausreicht, ist in der Regel eine komplett eigene Extension anzuraten. 

Es ist natürlich immer sinnvoll für eine fehlende Funktionalität einen <a href="{{ site.github_issues }}">Issue</a> auf der github Seite anzulegen.

