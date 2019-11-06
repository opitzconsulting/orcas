---
layout: page
title: Getting Started Gradle
permalink: /docs/getting-started-gradle/
categories: 
- en
---

## Tools needed

### Java

Java 8 needed at least. 

### Gradle

Use the gradle wrapper (type gradlew.bat [on Windows] or ./gradlew [on Unix] instead of gradle).

Otherwise download and install gradle from [gradle.org](https://gradle.org/).

## Using Orcas

Provide a buildscript dependency for Orcas:


```
buildscript {
    repositories {
       mavenCentral()
    }
    dependencies {
        classpath group: 'com.opitzconsulting.orcas', name: 'orcas-gradle-plugin',
                  version: '7.5.2'
    }
}
```

## Orcale jdbc driver

Add the Oracle jdbc driver:

```
buildscript {
    repositories {
       mavenCentral()
    }
    dependencies {
        classpath group: 'com.opitzconsulting.orcas', name: 'orcas-gradle-plugin',
                  version: '7.5.2'

        classpath group: 'com.oracle.ojdbc', name: 'ojdbc8', version: '19.3.0.0'
    }
}
```

## The Orcas-Gradle-Plugin

Orcas provides a plugin for gradle which assumes a default project layout.
This plugin is suitable for simple Orcas usages.

Note that you are free to use Orcas much more flexible if you set up your gradle-project yourself (see: examples/gradle_custom_build).

### Tasks

- databaseDeplyoyment

- extract

### project layout

```
.
├── buidl.gradle
└── src
    └── main
      ├── scripts
      |   ├── post-compile
      |   └── pre-statics
      └── sql
          ├── replaceables
          └── statics
```

## Configure Orcas

Orcas uses the "orcasconfiguration" object to provide access to various parameters.
The most basic configuration is used to set up the database connection:

```
orcasconfiguration {
          jdbcurl = "jdbc:oracle:thin:@localhost:1521:XE"
          username = "orcas_orderentry"
          password = "orcas_orderentry"
}

```

The complete List of available parameters can be found here: [OrcasGradlePluginExtension]({{site.baseurl}}/orcas_gradle_plugin/groovydoc/com/opitzconsulting/orcas/gradle/OrcasGradlePluginExtension.html)

## The orderentry example

##  Obtain orcas
Download and extract [orcas](https://github.com/opitzconsulting/orcas/archive/master.zip).

##  Configure Orderentry example
Edit examples/gradle_simple/build.gradle and setup your database connection.

The default database user needs to be cretaed created like this:

```
create user orcas_orderentry identified by orcas_orderentry;
grant connect to orcas_orderentry;
grant resource to orcas_orderentry;
```

## Start orcas
Exceute `gradlew databaseDeployment` at a command line located at `examples/gradle_simple`.

