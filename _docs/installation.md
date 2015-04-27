---
layout: page
title: Installation
permalink: /docs/installation/
---

##Git Projekt

Orcas wird in einem eigenen [Repository](https://github.com/opitzconsulting/orcas) auf GitHub verwaltet. Somit kann man sich einen lokalen git-Clone erstellen oder Orcas einfach als ZIP-Datei herunterladen.

##Vagrant

Orcas hat eine [Vagrant](https://de.wikipedia.org/wiki/Vagrant_%28Software%29)-Konfiguration, mit der man sehr einfach eine VM einrichten kann, in der Orcas direkt lauffähig (incl. Datenbank) ist. Für eine Installation der benötigten Tools auf Linux kann man sich hier auch einiges abgucken.

Die Vagrant-VM ist nur für Tests vorgesehen. Für den Einstaz von Orcas im eigenen Projekt müssen nachfolgende Tools installiert werden:

##Benötigte Tools

###ant

Zu finden unter [apache.org](http://ant.apache.org/). Bis auf weiteres kann die aktuellste Version verwendet werden.

###ant-contrib

Wichtig die **neueste** **Version** ist **nicht** die **richtige**! Die letzte funktionsfähige Version ist ant-contrib-1.0b3.jar.
Zu finden unter [sourceforge.net/ant-contrib-1.0b3.zip](http://sourceforge.net/projects/ant-contrib/files/ant-contrib/1.0b3/ant-contrib-1.0b3-bin.zip/download). 
Die Datei muss nach ANT_HOME/lib kopiert werden. **Die jar-Datei muss aus der zip-Datei extrahiert werden!**

###Gradle

Zu finden unter [gradle.org](http://www.gradle.org/). Bis auf weiteres kann die aktuellste Version verwendet werden.
<br/>Der Befehl gradle muss im PATH aufgenommen sein, oder GRADLE_HOME muss gesetzt sein.
<br/>Gradle baut ggf. eine Verbindung zum Maven-Central-Repository auf, es muss also eine Internet-Verbindung bestehen.

###Java

Benötigt wird mindestens Java 6 (1.6). **Wichtig**: Es muss ein **JDK** sein, kein JRE. Es reicht eine "Standard Edition" (SE), z.B. "Java SE 6u45". Nach der Installation sollte javac die richtige Version ausgeben:
{% highlight bash %}
javac -version
javac 1.6.0_12
{% endhighlight %}
Falls nicht, muss ggf. die PATH-Variable angepasst werden. Java wird bei Orcas von ant und gradle verwendet, daher evtl. auch die Konfigurationsmöglichkeiten beachten (z.B: wertet ant die Variable JAVA_HOME **vor** PATH aus).
Download-Link für Java 8: [Download](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

###ORACLE Client

SQL\*Plus muss an der Kommandozeile aufrufbar sein. Genauso sollte ein tnsping auf die Zieldatenbank funktionieren. Orcas benutzt im Kern immer TNS und SQL\*Plus, nur einige Zusatztools (z.B. dbdoc) nutzen JDBC.
ORACLE_HOME muss gesetzt sein.

####ORACLE Thin Client
Der [Instant-Client](http://www.oracle.com/technetwork/database/features/instant-client/index.html) kann verwendet werden. Dann muss ORACLE_HOME auf diesen zeigen.
ORACLE_HOME wird derzeit nur genutzt, um den JDBC-Treiber zu ermitteln.
Es werden das "Basic"-Package und das "SQL\*Plus"-Package benötigt. Der Instant Client kommt im Normalfall ohne tnsping, somit sollte das Setup über ein erfolgreichen SQL\*Plus connect getestet werden. 
Um eine tnsnames.ora mit dem Thin Client verwenden zu können muss diese manuelle im Unterverzeichnis ORACLE_HOME/network/admin angelegt werden (inklusive der Verzeichnisse) oder die TNS_ADMIN Variable muss entsprechend gesetzt sein.

