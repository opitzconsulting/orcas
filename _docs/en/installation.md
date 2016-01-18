---
layout: page
title: Installation
permalink: /docs/installation/
categories: 
- en
---

##Git Project

Orcas is managed in an own [Repository](https://github.com/opitzconsulting/orcas) on GitHub. So you can get your own local git-Clone or just download as ZIP file.

##Vagrant

Orcas comes with a [Vagrant](https://de.wikipedia.org/wiki/Vagrant_%28Software%29) configuration to easily set up a VM, in which Orcas is directly runnable (including database). For an installation of the required tools on Linux you can have a look at this to get an idea.

The Vagrant-VM is only intended for tests. To use Orcas in your own project you have to install the following tools:

##Tools needed

###Java

Java 6 (1.6) needed at least. **Important**: It has to be a **JDK** and not a JRE. The "Standard Edition" (SE), e.g. "Java SE 6u45", is enough for this. After the installation you should get the right version with javac:
{% highlight bash %}
javac -version
javac 1.6.0_12
{% endhighlight %}
If not, you maybe have to adapt the PATH variable. In Orcas, Java is used by ant and gradle, so maybe you have to consider the configuration possibilities (e.g. ant interprets JAVA_HOME **before** PATH).
Download link for Java 8: [Download](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

###ant

To find at [apache.org](http://ant.apache.org/). Until further notice you can use the newest version (to find at "Download Binary Distributions").
<br/>Download, extract (e.g. to "C:\Program Files\apache-ant-1.9.4"), create system variable ANT_HOME and take path as value, create user variable PATH (or extend) with "%ANT_HOME%\bin;%JAVA_HOME%\bin;" as value (without quotation marks).
<br/>Test of ant: use command line with **ant -version**. Now you should be able to read something like "Apache Ant(TM) version 1.9.4 compiled on April 29 2014".

###ant-contrib

**Important**: The **newest** **version** is **not** the **right** version! The last working version is ant-contrib-1.0b3.jar.
To find at [sourceforge.net/ant-contrib-1.0b3.zip](http://sourceforge.net/projects/ant-contrib/files/ant-contrib/1.0b3/ant-contrib-1.0b3-bin.zip/download).
Download ZIP, extract, copy "ant-contrib-1.0b3.jar" (other files are not relevant!) to ANT_HOME/lib.

###Gradle

To find at [gradle.org](http://www.gradle.org/). Until further notice you can use the newest version.
<br/>Download, extract (e.g. to "C:\Program Files\gradle-2.3").
<br/>The command gradle has to be included in PATH, or GRADLE_HOME has to be set.
<br/>Maybe Gradle connects to Maven Central repository, which means there has to be a working internet connection.

###ORACLE Client

SQL\*Plus has to be executable on the command line. tnsping on the target database has so work, too. Orcas essentially always uses TNS and SQL\*Plus, just a few addition features (e.g. dbdoc) are using JDBC.
ORACLE_HOME has to be set.

####ORACLE Thin Client
It is possible to use the [Instant-Client](http://www.oracle.com/technetwork/database/features/instant-client/index.html). If you do, ORACLE_HOME has to be set to the instant client directory.
ORACLE_HOME is currently only used to get the JDBC driver.
You need the "Basic"-Package and the "SQL\*Plus"-Package. Usually there is no tnsping in the Instant Client. So you need to test the setup with a successful SQL\*Plus connect.
To use a tnsnames.ora with the Thin Client you manually have to  create this in the directory ORACLE_HOME/network/admin (inclusive of the directory) or the TNS_ADMIN variable has to be set appropriately.

