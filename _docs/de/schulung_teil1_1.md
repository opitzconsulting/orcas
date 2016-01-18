---
layout: page
title: Teil 1.1 - Das Beispiel zum Laufen bekommen
permalink: /docs/de/schulung_teil1_1/
categories: 
- de
---

Zu Beginn der Schulung wollen wir erst mal das Beispiel zum Laufen bekommen.

## Setup

Die Schulung basiert auf der über [Vagrant](https://www.vagrantup.com/) bereitgestellten Umgebung. Die Schulung kann auch in einer eigenen Umgebung ausgeführt werden, dazu bitte auch "Setup ohne Vagrant" beachten.

### Vagrant Setup 

Dazu müssen zunächst [Vagrant](https://www.vagrantup.com/) und [Virtual-Box](https://www.virtualbox.org/) installiert werden. 
Anschließend  muss im Verzeichnis orcas_vagrant Die Vagrant-Umgebung gestartet werden:

{% highlight bash %}
Verzeichnis: orcas_vagrant
vagrant up
vagrant ssh
{% endhighlight %}

Der Befehl "vagrant up" wird beim ersten Ausführen einige Zeit brauchen, da dabei die Basis-VM ("danmikita/centos") heruntergeladen werden muss. Zudem werden noch innerhalb der vm die benötigten Tools automatsich installiert, die wiederum heruntergeladen werden müssen.
Der Befehl "vagrant ssh" sollte dann sehr schnell dazu führen, dass man in der Shell der VM landet. Das Orcas-Verzeichnis findet sich innerhalb der VM unter /orcas/orcas.

{% highlight bash %}
cd /orcas/orcas
{% endhighlight %}

Innerhalb der VM läuft eine Datenbank mit folgenden Connect-Daten:

- TNS-NAME : XE
- ORACLE_SID: XE
- ORACLE_HOST: localhost
- PORT: 1521 (der Port 1521 wird über Vagrant auf Port 1531 gemappt, somit ist die Datenbank vom Host Rechner auch erreichbar, dann aber auf diesem Port).

DBA-User:

- DBA-User: system
- Passwort-DBA-User: sa

### Setup ohne Vagrant
Alternativ dazu kann die Schulung auch in einer eigenen Umgebung durchgeführt werden. Dazu müssen dann aber die benötigten Tools ([siehe]({{site.baseurl}}/docs/de/installation/) installiert werden. Zudem ist dann zu beachten, dass die Schulung von den oben genannten Connect-Daten ausgeht. Wenn diese in der eigenen Umgebung nicht übereinstimmen müssen diese dann in den Beispielen entsprechend angepasst werden.

Zudem ist zu beachten, dass die Schulung in einer Linux-Umgebung läuft, ggf. muss man also OS-spezifische Befehle an die eigene Umgebung anpassen.

## Orderentry-Beispiel

Nach Abschluss der Installation soll das Orderentry Beispiel zum Laufen gebracht werden. 
Eine detaillierte Beschreibung dazu findet sich hier: [Orderentry]({{site.baseurl}}/docs/de/examples/).

Innerhalb der Vagrant-VM sind alle Beispiele bereits vorkonfiguriert, somit reichen folgende Befehle aus um das Orderentry Beispiel zu starten:

{% highlight bash %}
Verzeichnis: /orcas/orcas
cd examples/orderentry/db
ant install_all
ant
{% endhighlight %}

# Das Beispiel im Toad/SQLDeveloper ansehen

Jetzt kann vom Hostrechner aus wie oben beschrieben (Port 1531) auf das Beispiel zugegriffen werden. Unter Users/Andere Benutzer fondet sich u.a. das Schema ORCAS_ORDERENTRY in dem bereits einige Tabellen zu sehen sind.


