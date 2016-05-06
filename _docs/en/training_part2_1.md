---
layout: page
title: Part 2.1 - Orcas ant-Tasks
permalink: /docs/training_part2_1/
categories: 
- en
---

The second part of the training is about how to build a Orcas build sequence to fit for your project.

## Create a custom project

To manage with Orcas a DB schema (DB User) we need first, of course, a scheme for our project:

{% highlight bash %}
SQL-Skript mit DBA-User auszuführen:
create user myschema identified by myschema;
grant connect to myschema;
grant resource to myschema;
{% endhighlight %}

Our new sample project is the simplicity created in the directory "examples":

{% highlight bash %}
Directory: /orcas/orcas
cd examples
mkdir myproject
cd myproject
{% endhighlight %}

Our tables are located in the directory "tables":

{% highlight bash %}
Directory: myproject
mkdir tables
{% endhighlight %}

In the directory "tables" the file "mytable.sql" should be created initially. This file should have the following contents:

{% highlight bash %}
create table mytable
(
  mycolumn         number(10)        
);

{% endhighlight %}

## Initial build.xml

Orcas is always controlled by [ant](http://ant.apache.org/). 
For this, a file with the name "build.xml" is required.

This file will be stored in the directory "myproject" and will initially have the following content:

{% highlight bash %}
<?xml version = '1.0' encoding = 'windows-1252'?>
<project name="myproject" default="build">
  <property name="orcas_dir" value="/orcas/orcas/orcas_core"/>
  <import file="${orcas_dir}/orcas_default_tasks.xml"/>

  <property name="orcas.default_user" value="myschema"/>
  <property name="orcas.default_password" value="myschema"/>
  <property name="orcas.default_user_orcas" value="myschema"/>
  <property name="orcas.default_password_orcas" value="myschema"/>
  <property name="orcas.default_tnsname" value="XE"/>
  <property name="orcas.default_jdbcurl" value="jdbc:oracle:thin:@localhost:1521:XE"/>
  <property name="orcas.default_tmpfolder" value="tmp"/>
  <property name="orcas.default_spoolfolder" value=""/>

  <target name="build" >
    <orcas_initialize/>
    <orcas_execute_statics scriptfolder="tables"/>
  </target>
</project>
{% endhighlight %}

With the file "build.xml" our project is basically executable and can be launched from ant:

{% highlight bash %}
Directory: myproject
ant
{% endhighlight %}

Then the table "mytable" should have been created in our database schema (additionally two ORCAS_ tables, which should not bother us in the moment).

## The initial build.xml in detail

After the obligatory first line of XML the definition of an ant-project follows. Here we set only the name of the project (not relevant for Orcas) and a default-target. The default-target ensures that we can start ant without additional parameters, otherwise we would call "ant build".

Within the ant project the property "orcas_dir" is first set. This must be set, because it is also used by Orcas. The value is the path to the directory "orcas_core". This path can be an absolute pathname (As in our example), or relative (in our example it would be "../../orcas_core").

*Note to ant paths: Within ant the path delimiter can both "/" (Unix standard) and* "\\" *(Windows standard) be used. However, it is always recommended to work with "/", because* "\\" *is interpreted in some cases as an escape character and thus can lead to errors. A "good" absolut Windows path would be for example "C:/orcas/orcas_core".* 

Subsequently, in the build.xml the file "orcas_default_tasks.xml" is imported, in which all required ant components for Orcas are contained.

In our example, we work with the orcas.default-properties that are set next (compare [General attributes]({{site.baseurl}}/docs/ant-tasks/#general)).
First Orcas requires the database parameters consisting of username, password, tnsname and jdbcurl.
For username and password there are two parameters, one for each of the to-manage scheme (default_user) and one for the Orcas schema (default_user_orcas). 
Orcas creates many custom database objects, because Orcas is implemented in the core in PL/SQL. It is recommended to create Orcas in a separate scheme, especially if your project itself contains PL/SQL components. In our simple example, we have not made this and so we set default_user=default_user_orcas.

The JDBC URL has been set for completeness, we will need it later.

The tmpfolder is the directory in which Orcas stored many of their own files, which are required for the execution of Orcas. Orcas creates this folder if necessary by itself.

We have disabled the spooling (more on that later) by setting to "".

*Note to ant properties: We have here the easiest way used to set ant properties. There are numerous other ways of doing this ([see also: ant manual](http://ant.apache.org/manual/properties.html)). Some will be used during the training. It is **not** necessary to write the **database passwords** into our **build.xml**.*

Then you can find an ant target in our "build.xml". An ant target can be started with the ant command line. As we set our ant target "build" as a default target in the ant project, we can start ant without any further parameters and our "build" target is executed.

Within our ant-targets we use two orcas ant tasks.

The first one is "orcas_initialize" and is intended to "set up" Orcas (on the one hand Orcas is compiled from source and on the other the Orcas database objects like PL/SQL packages must be imported into the database schema). "orcas_initialize" must be running before any other orcas ant task can be performed (with very few exceptions).

The second orcas ant task is "orcas_execute_statics". This starts the actual Orcas sequence and is thus also the one which created our table "mytable".

## Further steps

In the following exercises our sample project will be expanded step by step. That does not mean that any of these steps for each project makes sense.