---
layout: page
title: Example projects
permalink: /docs/examples/
categories: 
- en
---

The **Orderentry example** is the basis example for all other example projects. 
Thus you should initial get the Orderentry example running and then there should be fewer problems with the other example projects when working. 

The example implements the "location" concept, be handled with the different target environments (eg development, test, production) via locations.

This is essentially a setup for managing properties that can be used for other parts of the project.

##Setup Orderentry

Please check before that the required tools ([Installation]({{site.baseurl}}/docs/installation)) are working.

###Configure the database:

For this, change to the folder examples\orderentry\distribution\my_location.
<br/>This folder contains the fole "location.properties".

{% highlight properties %}
#Database
database              =XE
jdbc_host             =localhost
jdbc_sid              =XE
jdbc_port             =1521
username_dba          =system
password_dba          =my_system_password
{% endhighlight %}

The file must be adapted, at least "password_dba" must be changed (for the rest the configuration for a local default XE installation fits).

**Important**: The jdbc_XXX entries are only of secondary importance (for a simple test they are not needed). Especially important is "database". In the example, a *tnsping XE* must work (otherwise possibly configure TNS names).

**Important**: All examples should **not** be set up on **productive** 

*Notice*: It is certainly not necessary to run Orcas with DBA privileges. The sample projects are only for simplicity set up to create a database user automatically.

###Set up once Orcas and the example on the database:

With *ant install_all* the database users will set up once.

{% highlight bash %}
Directory: examples\orderentry\db
ant install_all
{% endhighlight %}

If completed successfully, on the target database there are two new users:

- ORCAS_ORDERENTRY (Schema owner, that contains the tables that are meant to be compared)
- ORCAS_ORDERENTRY_ORCAS (User, that contains the Orcas)

The passwords of the two users are defined in the file "examples\orderentry\distribution\default.properties" (standard for both identical with the scheme owner to lowercase).

If the *ant install_all* run terminates, then the two users may need to be deleted before, so that a new call of *install_all* works.

###How to launch Orcas:

{% highlight bash %}
Verzeichnis: examples\orderentry\db
ant
{% endhighlight %}

Result should be an output that reports a success in the end:

{% highlight bash %}
...

BUILD SUCCESSFUL
Total time: 12 seconds
{% endhighlight %}

The runtime of the first run will be much longer than 12 seconds (typically a few minutes). Firstly, Orcas will reload all required libraries from the Internet (Maven Central), on the other Orcas is assembled at the first round from the sources. The actual comparison is however very quickly. A new call of ant should therefore actually need only around 12 seconds. The runtime also should not increase by too much with increasing number of tables. So it's for example absolutely possible to match a scheme with 1,000 tables (including associated constraints) within a minute. The runtime will only increase significantly when many or lengthy database statements must be executed.

###Hot to use Orcas
If everything is successful, you can get here a brief introduction into the procedure, or switch diretly into other projects: [How to work with Orcas?]({{site.baseurl}}/docs/usage/)

##Other examples

To use another example, you can just copy the folder from the my_location of the Orderentry example into the respective distribution folder.

<a name="domain_extension_demo"/>

###domain_extension_demo

In this example the [domain extension]({{site.baseurl}}/docs/domain-extension/) is used.

<a name="extension_demo"/>

###extension_demo

This example shows how you can use your own [extensions]({{site.baseurl}}/docs/extensions/).

###liquibase_integration

This example shows how you can combine liquibase with Orcas.

###orderentry_one_schema

This example shows how Orcas can be used without a separate Orcas schema.

###sqlplus

In this example, the SQLPlus API is used. This should be made only in projects that don't want to or can't use ant, gradle or java, or in projects based on old versions of Orcas.

###target_plsql_demo

This example shows how you can use the table metadata from Orcas for own purposes (In the example to generate trigger).