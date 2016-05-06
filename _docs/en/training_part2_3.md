---
layout: page
title: Part 2.3 - one_time_scripts
permalink: /docs/training_part2_3/
categories: 
- en
---

In Part 1 of the training, we have learned that the Orcas operation also includes scripts which necessary data migrations can be performed. How this could be fit, we can see now.

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
  <property name="orcas.default_spoolfolder" value=""/>

  <target name="setup" >
    <orcas_install user="system" password="sa"/>
  </target>

  <target name="build" >
    <orcas_initialize/>
    <orcas_execute_one_time_scripts scriptfolder="migrations_skripte" logname="migrations_skripte"/>
    <orcas_execute_statics scriptfolder="tables"/>
  </target>
</project>
{% endhighlight %}

A new addition is the ant-task "orcas_execute_one_time_scripts". This we have informed that our scripts should lie in the directory "migrations_skripte". This ant task we must give a clear logname (normally this is optional).

## Start Orcas

To use our project now, we still need to create the directory "migrations scripts":

{% highlight bash %}
Directory: myproject
mkdir migrations_skripte
{% endhighlight %}

Then we can normally start the adjustment:

{% highlight bash %}
Directory: myproject
ant 
{% endhighlight %}

This does not lead initially to a substantial change because our directory "migrations_skripte" is empty.

To verify that this works we create now in the "migrations_skripte" the file "test1.sql" with the following content:
{% highlight bash %}
select 'hello world' from dual
/

{% endhighlight %}

A new ant call should show somewhere in the log output "hello world". But this only the first time, each additional call should no longer include this issue.

Orcas logs the execution of one_time_scripts. This can be viewed as required:

{% highlight bash %}
Rin the SQL script with DBA-User:
select *
  from myschema_orcas.orcas_updates;
{% endhighlight %}
