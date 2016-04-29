---
layout: page
title: Part 1.1 - Getting the example to work
permalink: /docs/training_part1_1/
categories: 
- en
---

At the beginning of the training, we want to get the sample to work.

## Setup

The training is based on the environment provided with [Vagrant](https://www.vagrantup.com/). The training can be performed in a separate environment, please note therefore also "Setup without Vagrant".

### Vagrant Setup

For this purpose, first [Vagrant](https://www.vagrantup.com/) and [Virtual-Box](https://www.virtualbox.org/) must be installed. 
Then the Vagramt environment must be started in the directory orcas_vagrant:

{% highlight bash %}
Verzeichnis: orcas_vagrant
vagrant up
vagrant ssh
{% endhighlight %}

The command "vagrant up" will need some time on the first run, because the base VM ("danmikita/centos") needs to be downloaded. In addition, the required tools are installed automatically even within the VM, which in turn must be downloaded.
The command "vagrant ssh" should cause, that you end up in the shell of the VM. The Orcas directory can be found within the VM under /orcas/orcas.

{% highlight bash %}
cd /orcas/orcas
{% endhighlight %}

Within the VM a database is running with the following connect data:

- TNS-NAME : XE
- ORACLE_SID: XE
- ORACLE_HOST: localhost
- PORT: 1521 (the Port 1521 is mapped through Vagrant to port 1531, thus the database from the host computer is also available, but then on this port).

DBA-User:

- DBA user: system
- Password DBA user: sa

### Setup without Vagrant
Alternatively, the training can be performed in a dedicated environment. For this purpose then the required tools ([see]({{site.baseurl}}/docs/installation/) must be installed. In addition, then to note that the training is based on the connect data mentioned above. If they do not match in their own environment, they must then be adjusted accordingly in the examples.

In addition, it should be noted that the training runs in a Linux environment, possibly so you need to adapt OS-specific commands to the own environment.

## Orderentry example

After installation is complete, the Orderentry example should be made to work.
A detailed description can be found here: [Orderentry]({{site.baseurl}}/docs/examples/).

Within the Vagrant VM all examples are preconfigured, thus use the following commands in order to run the Orderentry sample:

{% highlight bash %}
Directory: /orcas/orcas
cd examples/orderentry/db
ant install_all
ant
{% endhighlight %}

# View the example with Toad/SQLDeveloper

Now you can access the example from the host computer as described earlier (port 1531). Under Users/Other Users there is amongst other things the scheme ORCAS_ORDERENTRY, where some tables can already be seen.