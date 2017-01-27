---
layout: page
title: Getting Started Maven
permalink: /docs/getting-started-maven/
categories: 
- en
---

## Tools needed

### Maven

To find at [maven.apache.org](http://maven.apache.org/). Until further notice you can use the newest version. Orcas rquires at least maven 3.

### Java

Java 8 (1.8) needed at least. 

### Orcale jdbc driver

You need to provide the oracle jdbc driver.
			  please make sure you find a way that works with the oracle license terms.

- add to local maven-repository:

  Execute the following command, make sure you replace the location of the ojdbc6.jar.
  `mvn install:install-file "-Dfile=C:\oracle\product\11.1.0\client_32\jdbc\lib\ojdbc6.jar" -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.1.0.7 -Dpackaging=jar`

- direct path in pom.xml:

  add scope and system tags to the dependency-section of your pom.xml (see next section on wherer to find the pom.xml for the orderentry example):
  `<scope>system</scope>`
  `<systemPath>C:\oracle\product\11.1.0\client_32\jdbc\lib\ojdbc6.jar</systemPath>`

- form maven.oracle.com:

  The driver is also accessible via http://maven.oracle.com, note that this requires a login-configuration

## The orderentry example

##  Obtain orcas
Download and extract [orcas](https://github.com/opitzconsulting/orcas/archive/master.zip).

##  Configure Orderentry example
Edit examples\maven\pom.xml and setup your database connection (and additionally the jdbc-driver setup if needed).

The default database user is created like this:

```
create user orcas_orderentry identified by orcas_orderentry;
grant connect to orcas_orderentry;
grant resource to orcas_orderentry;
grant create sequence to orcas_orderentry;
grant create view to orcas_orderentry;
```

## Start orcas
Exceute `mvn database-deployment` at a command line located at `examples\maven`.
