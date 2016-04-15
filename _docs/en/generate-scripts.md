---
layout: page
title: Generating the table scripts
permalink: /docs/generate-scripts/
categories: 
- en
---

## Reverse Engineering

For the introduction of Orcas in an existing database project, the table scripts must be created initially.

For this orcas_extract is available:

- Expandable/adaptable
- Uses [extensions]({{site.baseurl}}/docs/extensions/)
- Based on [XSLT](http://www.w3schools.com/xsl/)
- Doesn't support domain indices (XML index, Oracle text)
- Source database must have installed Orcas.

In the [Orderentry example]({{site.baseurl}}/docs/examples/) you can try this by performing **ant extract**. The table scripts can be found then in the folder **bin_orderentry\run\extract_output**.

## Why tablespace XY nocompress logging noparallel?
While generating all information will be included, even if they are default values.

To influence this behavior (or other aspects) you can use extensions or XSLT.

### Extension
Extensions may also be used in reverse engineering. Then, however, only in PL/SQL (Java Extensions are *not* yet possible).
A good template for this can be found here:
orcas_domain_extension/extensions/pa_reverse_22_remove_defaults.sql.

### XSLT
There is the possibility to specify a custom XSLT file. This should import the original XSLT file (orcas_core/xslt_extract/orcas_extract.xsl) (use <code>&lt;import href="orcas_extract.xsl"/&gt;</code> for it). Thus, you can use the ordinary XSLT functions to customize the generated files.
