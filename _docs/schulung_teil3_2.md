---
layout: page
title: Teil 3.2 - Domain Extensions
permalink: /docs/schulung_teil3_2/
---

Die Domain-Extension ist eine vorgefertigte Extension, die in 80% der Fälle ausreicht. Da sie nur konfiguriert und benutzt werden muss, ist es immer ratsam die Domain-Extension zu nutzen und nicht selbst eine Extension zu erstellen, wenn die Funktionalität der Domain-Extension ausreichend ist.
Dabei stellen Domain-Extensions eine Art Template für Spalten oder Tabellen dar, ähnlich der Vererbung in JAVA.

## Beispiel - Tabellen mit einer ID Spalte
So kann beispielsweise eine Table-Domain angelegt werden, die definiert, dass die zugehörigen Tabellen eine zusätzliche Id-Spalte besitzen sollen.
Ohne die Domain-Extension würde unsere Tabelle beispielsweise so aussehen:

{% highlight sql %}
create table address
(
  address_id       number(10) not null,
  firstname        varchar2(30),
  lastname         varchar2(30),
  street           varchar2(50),
  city             varchar2(30)
);
{% endhighlight %}

Die Extension befolgt dabei ein simples Schema: 

{% highlight sql %}
define table domain id_table
(
  add column column-name(table-name||"_"||column-name) ( id number(10) not null)
);
{% endhighlight %}

**define table domain** legt dabei fest, dass es sich um eine Table-Domain handelt.
**id_table** ist der Name mit dem ich die Domain ansprechen möchte.
**add column** bedeutet dass die Spalte hinzugefügt wird.
Der Name der neuen Spalte wird hierbei als pattern angegeben.
Die Spalte selbst soll (ohne Pattern) **id** heißen.

Um das "Template" anzuwenden reicht die Angabe der domain mit Namen im create-statement:

{% highlight sql %}
create table address domain id_table
(
  firstname        varchar2(30),
  lastname         varchar2(30),
  street           varchar2(50),
  city             varchar2(30)
);
{% endhighlight %}

## Das Beispiel zum Laufen bringen

Um die Domainextensions nutzen zu können, muss abermals die build.xml angepasst werden, indem der ([orcas_initialize]({{site.baseurl}}/docs/ant-tasks/#orcas_initialize)) gesetzt wird. Er muss auf den Ordner ../../../orcas_domain_extension/extensions verweisen, bzw. einen Ordner mit dessen Inhalt. Hier liegen die nötigen packages sowie die Java-Klasse um die Domainextensions zu verwenden.


{% highlight xml %}
<target name="orcas_initialize" depends="show_location">
  <orcas_initialize extensionfolder="../../../orcas_domain_extension/extensions"/>
</target>
{% endhighlight %}

Die eigentliche Extension (wie oben id_table) kann nun mit einer beliebigen Dateiendung neben den Tabellen in beliebiger Ordnerstruktur abgespeichert werden.

## Weitere Domain-Extensions

Nachfolgend eine Liste der enthaltenen Funktionen. Diese werden alle vom [Domain-Extension-Demo]({{site.baseurl}}/docs/examples/#domain_extension_demo) Beispielprojekt genutzt:

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

## Übungen

Schaue dir die Beispiele aus der **domain_extension_demo** an (~\examples\domain_extension_demo\db\tabellen\domains). 

Löse mit deren Hilfe die folgenden Aufgaben innerhalb des Orderentry-Beispiels: 

1. Ersetze alle **not** **null** Spalten im Beispiel durch eine ColumnDomain
2. Füge allen Tabellen aliase hinzu und verwende im Nachhinein die Domain-Extensions zum referenzieren von Foreign Keys
3. Erstelle eine TableDomain, die automatisch zwei neue Datums-Spalten hinzufügt: **Modification_dt** und **Creation_dt**





