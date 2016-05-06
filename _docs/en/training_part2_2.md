---
layout: page
title: Part 2.2 - Custom Orcas scheme
permalink: /docs/training_part2_2/
categories: 
- en
---

As already said, the recommended procedure is not to install Orcas in the same schema which is to be adjusted. Therefore, we will change our project to the use of a separate Orcas schema.

First of all, we should newly set up our existing schema to get rid of the Orcas database objects (In principle we can make use of the Ocras resources, but we don't "know" them yet).

{% highlight bash %}
Run the SQL script with DBA-User:
drop user myschema cascade;
create user myschema identified by myschema;
grant connect to myschema;
grant resource to myschema;
{% endhighlight %}

## Custom build.xml

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
    <orcas_execute_statics scriptfolder="tables"/>
  </target>
</project>
{% endhighlight %}

The important change is that as "user_orcas" we no longer use "myschema". Instead we set "myschema_orcas". 

To create this user we will use the ant task "orcas_install". For this we have created an ant target called "setup". To call "orcas_install" we need to specify a user (including password) with the permission to create other users. After our "myschema" user has not these rights, we use the "system" user.

To the problem that the password (exactly like the others) should not be placed in the build.xml, we take care of later.

It is also possible to manually create this user (without "orcas_install") or let create by a DBA.

## Start Orcas

To use our project now, we need to call once our setup target:

{% highlight bash %}
Directory: myproject
ant setup 
{% endhighlight %}

Then we can normally start the adjustment:

{% highlight bash %}
Directory: myproject
ant 
{% endhighlight %}

The result is now that in myproject only our "mytable" exists, the two ORCAS_ tables are now in the scheme "myschema_orcas".