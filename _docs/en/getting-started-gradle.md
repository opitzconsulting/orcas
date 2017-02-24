---
layout: page
title: Getting Started Gradle
permalink: /docs/getting-started-gradle/
categories: 
- en
---

## Tools needed

### Java

Java 8 (1.8) needed at least. 

### Gradle

Use the gradle wrapper (type gradlew or ./gradlew instead of gradle).

Otherwise download and install gradle from [gradle.org](https://gradle.org/). Until further notice you can use the newest version. Orcas rquires at least gradle 3.3.

### Orcale jdbc driver

You need to provide the oracle jdbc driver.
			  please make sure you find a way that works with the oracle license terms.

- add to local maven-repository (yes maven-repository although this is the gradle guide):

  Execute the following command, make sure you replace the location of the ojdbc6.jar.
  `mvn install:install-file "-Dfile=C:\oracle\product\11.1.0\client_32\jdbc\lib\ojdbc6.jar" -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.1.0.7 -Dpackaging=jar`

- direct path in build.gradle:

  add direct classpath to the dependencies-section of your build.gradle (see next section on wherer to find the build.gradle for the orderentry example):
  `classpath files('C:\oracle\product\11.1.0\client_32\jdbc\lib\ojdbc6.jar')`

- form maven.oracle.com:

  The driver is also accessible via http://maven.oracle.com, note that this requires a login-configuration

## The orderentry example

##  Obtain orcas
Download and extract [orcas](https://github.com/opitzconsulting/orcas/archive/master.zip).

##  Configure Orderentry example
Edit examples\gradle\build.gradle and setup your database connection (and additionally the jdbc-driver setup if needed).

The default database user is created like this:

```
create user orcas_orderentry identified by orcas_orderentry;
grant connect to orcas_orderentry;
grant resource to orcas_orderentry;
grant create sequence to orcas_orderentry;
grant create view to orcas_orderentry;
```

## Start orcas
Exceute `gradle databaseDeployment` at a command line located at `examples\gradle`.

For gradle wrapper use `gradlew databaseDeployment` or `./gradlew databaseDeployment` instead.
