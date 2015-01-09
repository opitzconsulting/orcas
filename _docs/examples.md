---
layout: page
title: Beispielprojekte
permalink: /docs/examples/
---

Das **Orderentry-Beispiel** ist das Basis-Beispiel für alle anderen Beispiel-Projekte.
Somit sollte man inital das Orderentry-Beispiel zum laufen bekommen und dann wenig Probleme haben mit den andren Beispiel-Projekten zu arebiten.

Das Beispiel setzt das "location" Konzept um, mit dem verschiedene Zielumgebungen (z.B. entwicklung, test, produktion) über locations gehandhabt werden.

Dabei handelt es sich im wesentlichen um ein Setup zur Verwaltung von properties, das auch für andere Teile des Projektes genutzt werden kann.

##Setup Orderentry

Bitte vorher prüfen, dass die benötigten Tools ([Installation]({{site.baseurl}}/docs/installation)) funktionieren.

###Datenbank konfigurieren:

Dazu wechselt man in den Ordner examples\orderentry\distribution\my_location.
<br/>In dem Ordner liegt eine "location.properties" Datei.

{% highlight properties %}
#Database
database              =XE
jdbc_host             =localhost
jdbc_sid              =XE
jdbc_port             =1521
username_dba          =system
password_dba          =my_system_password
username_schemaowner  =orderentry
{% endhighlight %}

Die Datei muss angepasst werden, mindestens "password_dba" muss geändert werden (ansonsten passt die Konfiguration für eine lokale-default XE-Installation).

**Wichtig**: Die jdbc_XXX Einträge sind nur von zweitrangiger Bedeutung (für einen eifahcne Test werden sie nicht benötigt). Wichtig ist insbesondere "database". Im Beispiel muss ein *tnsping XE* funktionieren (ansonsten evtl. TNS-Names konfigurieren).

**Wichtig**: Alle Beispiele sollten nicht auf **Produktiv**-Datenbanken eingerichtet werden!

*Hinweis*: Es ist keinesfalls notwenig Orcas mit DBA-Rechten laufen zu lassen. Die Beispiel-Projkete sind nur der einfachheit halber so aufgesetzt, dass benötige Datenbank-User automatisch angelegt werden.

###Einmalig Orcas und das Beispiel auf der Datenbank einrichten:

Mit *ant install_all* werden die benötigten Datenbnak-User einmalig eingerichtet.

{% highlight bash %}
Verzeichnis: examples\orderentry\db
ant install_all
{% endhighlight %}

Wenn das erfolgreich durchgelaufen ist, gibt es auf der Zieldatenbank zwei neue User:

- ORDERENTRY (Schemaowner, der die abzugleichenden Tabellen enthält)
- ORDERENTRY_ORCAS (User der Orcas enthält)

Wenn der *ant install_all*-Lauf abbricht, dann müssen die beiden User ggf. vorher wieder gelöscht werden, damit ein erneuter Aufruf von *install_all* funktioniert.

###Orcas starten:

{% highlight bash %}
Verzeichnis: examples\orderentry\db
ant
{% endhighlight %}

Ergebnis sollte eine Ausgabe sein, die am Ende einen Erfolg meldet:

{% highlight bash %}
...

BUILD SUCCESSFUL
Total time: 12 seconds
{% endhighlight %}

Die Laufzeit beim aller ersten Lauf wird deutlich länger als 12 Sekunden sein (typischerweise einige Minuten). Zum einen wird Orcas alle benötigten Bibliotheken aus dem Internet (Maven-Central) nachladen, zum anderen wird Orcas beim ersten Lauf aus den Sourcen zusammengebaut. Der eigentliche Abgleich geht dagegen sehr schnell, ein erneuter Aufruf von ant sollte also tatsächlich nur um die 12 Sekunden benötigen. Die Laufzeit sollte auch mit steigender Anzahl an Tabellen nicht zu sehr ansteigen, so ist es z.B. durchaus möglich ein Schema mit 1000 Tabellen (samt zugehöriger Constraints) innerhalb einer Minute abzugleichen. Die Laufzeit wird erst dann signifikant setigen, wenn viele oder langweirige Datenbank-Statements ausgeführt werden müssen.

###Orcas benutzen
Wenn alles woeit erfolgreich verlaufen ist, kann man unter: [Wie arbeitet man mit Orcas?]({{site.baseurl}}/docs/usage/) eine kurze Einführung in die Arbeitsweise erhalten, oder direkt zu den andrene Projekten übergehen.

##Andere Beispiele

Um die anderen Beispiele zu nutzen, kann man einfach den my_location Ordner aus dem Orderentry-Beispiel in den jeweiligen distribution-Ordner kopieren.

<a name="domain_extension_demo"/>

###domain_extension_demo

In diesem Beispiel wird die [Domain-Extension]({{site.baseurl}}/docs/domain-extension/) verwendet.

<a name="extension_demo"/>

###extension_demo

In diesem Beispiel wird gezeigt wie man eigene [Extensions]({{site.baseurl}}/docs/extensions/) verwenden kann.

###liquibase_integration

In diesem Beispiel wird gezeigt wie man liquibase mit Orcas kombinieren kann.

###orderentry_one_schema

Dieses Beispiel zeigt, wie Orcas ohne ein eigenes Orcas-Schema genutzt werden kann.

###sqlplus

In diesem Beispiel wird die SQL*Plus-API verwendet, dies sollte nur in Projekten gemacht werden, die kein ant, gradle oder java nutzen können/wollen, oder in Projekten die auf alten Versionen von Orcas basieren.

###target_plsql_demo

In diesem Beispiel wird gezeigt wie man die Tabellen-Metadaten aus Orcas für eigene Zwecke nutzen kann (Im Beispiel um Trigger zu generieren).

