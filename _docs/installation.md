---
layout: page
title: Installation
permalink: /docs/installation/
---

#Installation

##Git Projekt

Orcas wird in einem eigenen [Repository](https://github.com/opitzconsulting/orcas) auf GitHub verwaltet. Somit kann man sich einen lokalen git-Clone erstellen oder Orcas einfach als ZIP-Datei herunterladen.

##Vagrant

Orcas hat eine Vagrant Konfiguration mit der man sehr einfach eine VM einrichten kann in der Orcas direkt lauffähig (inc. Datenbank) ist. Für eine Installation der benötigten Tools auf Linux kann man sich hier auch einiges abgucken.

Die Vagrant VM ist nur für Tests vorgesehen, für den Einstaz von Orcas im eigenen Projekt müssen nachfolgende Tools installiert werden:

##Benötigte Tools

###ant

Zu finden unter [apache.org](http://ant.apache.org/). Bis auf weiteres kann die aktuellste Version verwendet werden.

###ant-contrib

Zu finden unter [sourceforge.net](http://sourceforge.net/projects/ant-contrib/) z.B: ant-contrib-1.0b3.jar. Die Datei muss nach ANT_HOME/lib kopiert werden.

###Gradle

Zu finden unter [gradle.org](http://www.gradle.org/). Bis auf weiteres kann die aktuellste Version verwendet werden.
<br/>Der Befehl gradle muss im PATH aufgenommen sein, oder GRADLE_HOME muss gesetzt sein.
<br/>Gradle baut ggf. eine Verbindung zum Maven-Central-Repository auf, es muss als eine Internet-Verbindung bestehen.

###Java

Benötigt wird mindestens Java 6 (1.6). **Wichtig**: Es muss ein **JDK** sein, kein JRE. Es reicht eine "Standard Edition" (SE), z.B. "Java SE 6u45". Nach der Installation sollte javac die richtige Version ausgeben:
{% highlight bash %}
javac -version
javac 1.6.0_12
{% endhighlight %}
Falls nicht, muss ggf. die PATH-Variable angepasstw erden. Java wird bei Orcas von ant und gradle verwendet, daher evtl. auch die Konfigurationsmöglichkeiten beachten (z.B: wird JAVA_HOME von ant **vor** PATH ausgewertet).

###ORACLE Client

SQL\*Plus muss an der Kommandozeile aufrufbar sein. Genauso sollte ein tnsping auf die Zieldatenbank funktionieren. Orcas benutzt im Kern immer TNS und SQL\*Plus, nur einige Zusatztools (z.B. dbdoc) nutzen JDBC.
ORACLE_HOME muss gesetzt sein.
Der Instant-Client kann verwendet werden, dann muss ORACLE_HOME auf diesen zeigen.
ORACLE_HOME wir derzeit nur genutzt um den JDBC-Treiber zu ermitteln.

