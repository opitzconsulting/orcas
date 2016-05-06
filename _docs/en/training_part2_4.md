---
layout: page
title: Part 2.4 - Logging
permalink: /docs/training_part2_4/
categories: 
- en
---

In this training we will activate the logging.

## Adapted build.xml

{% highlight bash %}
<?xml version = '1.0' encoding = 'windows-1252'?>
<project name="myproject" default="build">
  <property name="orcas_dir" value="/orcas/orcas/orcas_core"/>
  <import file="${orcas_dir}/orcas_default_tasks.xml"/>

  <property name="orcas.default_user" value="myschema"/>
  <property name="orcas.default_password" value="myschema"/>
  <property name="orcas.default_user_orcas" value="myschema_orcas"/>
  <property name="orcas.default_password_orcas" value="myschema_orcas"/>
  <property name="orcas.default_tnsname" value="XE"/>
  <property name="orcas.default_jdbcurl" value="jdbc:oracle:thin:@localhost:1521:XE"/>
  <property name="orcas.default_tmpfolder" value="tmp"/>
  <property name="orcas.default_spoolfolder" value="log"/>

  <target name="setup" >
    <orcas_install user="system" password="sa"/>
  </target>

  <target name="build" >
    <delete dir="${orcas.default_spoolfolder}"/>
    <orcas_initialize/>
    <orcas_execute_one_time_scripts scriptfolder="migrations_skripte" logname="migrations_skripte"/>
    <orcas_execute_statics scriptfolder="tables" logname="tables"/>
  </target>
</project>
{% endhighlight %}

The property "default_spoolfolder" is set to an actual value (namely to the name of the directory), so the logging is now active. With active logging each task needs which is able to log (these are almost all except for "orcas_install" and "orcas_initialize") a logname parameter. This should be unique. At the beginning of our build target the deletion of the spoolfolders is added.

## Start Orcas

We start our project as usual:

{% highlight bash %}
Directory: myproject
ant 
{% endhighlight %}

Result: The "log" directory was not even created. This is because Orcas performed no change to our scheme. That changes when we for example expand our table:

{% highlight bash %}
create table mytable
(
  mycolumn         number(10),
  mycolumn2        number(10)
);
{% endhighlight %}

A subsequent ant call is then lead to the new column is created and this will be logged in our "log" directory. The "log" directory now contains a file "master_install.sql". When you execute with SQL*Plus you get the same result in the respective schema as with ant. If we had previously copied our scheme (e.g. with import/export) and executed "master_install.sql" on it, then the scheme would have been also updated there.  