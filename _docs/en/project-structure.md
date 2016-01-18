---
layout: page
title: Project structure
permalink: /docs/project-structure/
categories: 
- en
---

You'll find the following main directories in Orcas:

- examples
- orcas_core
- orcas_dbdoc
- orcas_domain_extension
- orcas_extensions
- orcas_integrationstest
- orcas_sqlplus_core
- orcas_vagrant

In **examples** you'll find a few example projects, which demonstrate the usage of Orcas. You can take an [example project]({{site.baseurl}}/docs/examples/) and use it as a template for your own project.

In **orcas_core** you'll find the main part of Orcas.  This is an essential directory to get Orcas running. You can either reference it from a concrete project or copy it into your project but you must not change the content of this directory. If required, please [contact](https://github.com/opitzconsulting/orcas/issues) the developers of Orcas, because with a high probability there is at least one of the following reasons, wherefore you need these changes:

- There is a comprehension gap and the functionality you need can be achieved otherwise.
- There is a bug. This should be fixed as soon as possible.
- You need an extension. Either this should be implemented into Orcas or separately be implemented from Orcas into a project specific directory.

In **orcas__dbdoc** you'll find the [dbdoc]({{site.baseurl}}/docs/dbdoc/) tool. With dbdoc you are able to plot database schemas.

In **orcas_domain_extension** you'll find all [Domain-Extensions]({{site.baseurl}}/docs/extensions/). The Domain-Extension offers a kind of templating-mechanism to you. With this it is easily possible to generate standard columns (e.g. ID or version). You also have the opportunity to generate PK, FK, UK and Check constraints.

In **orcas_extensions** you'll find example [Extensions]({{site.baseurl}}/docs/extensions/). It is possible to significantly extend Orcas with your own extensions. You can also copy the examples in this directory into your own project and adjust them, if necessary.

In **orcas_integrationtest** you'll find the [Integrationstest]({{site.baseurl}}/docs/integration-tests/). This tests whether Orcas is working correctly or not by using automatically controlled test sequences on different database systems. The integration test can also be started local and extended by own tests.

In **orcas_sqlplus_core** you'll find an API, which is intended to be directly called by SQL\*Plus. This possibility is available for old-projects and should not be used for new projects any more.

In **orcas_vagrant** you'll find a vagrant configuration. With this you are able to configure and start a VM, so Orcas is immediately runnable.
