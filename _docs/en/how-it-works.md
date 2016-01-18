---
layout: page
title: Functionality of Orcas
permalink: /docs/how-it-works/
categories: 
- en
---

##Introduction

This page describes the basic functionality of Orcas. In particular, the procedure of the extensions is shown here.

##Who should read it?

The understanding of the functionality of Orcas is not necessary to use it. It is important for the development of extensions and work on Orcas itself.

##Flowchart

The following chart shows the roughly sequence of Orcas. The individual steps, which are marked with numbers, are described in more detail below:

![Functionality of Orcas]({{site.baseurl}}/assets/funktion_orcas.gif)

##Description

1. The BNF (Backus-Naur-Form) describes the syntax of the tables scripts (specifically: statics scripts). The BNF is stored in the format xText (orcas_core/xtext/orcas/src/de/opitzconsulting/OrcasDsl.xtext). In the first step a set of "PL/SQL types" (Oracle Object types and Collection type) is generated, which habe the ability to absorb the schema information. There are, for example, an object type for tables, and this in turn has a collection of object types with column data. The object types are in the scheme of Orcas deployed.
2. In the second step a BNF' is made from the original BNF. This is the task of syntax extensions. It there is no syntax extension used in the project, nothing happens. It's possible to use multiple syntax extensions to create the final BNF'. The alias syntax extension for example expands the BNF expand so that after the table name the alias can follow (or should). The syntax for the table scripts results from the BNF'.
3. From BNF' a Java class model is generated, that can receive the data from the table scripts. In addition, many more xText artifacts are generated, which are of no further relevance. All generated artifacts are stored in the specified temp directory.
4. From the BNF' also a corresponding set of "PL/SQL types" is generated. These are similar to the "PL/SQL types" (without the dash " ' ") so far that the line variant can admit additional data that result from the syntax extensions.
5. So far, the steps were considered, which take place at the orcas_initialize. From step five of the actual adjustment starts, which is started via orcas_execute_statics. The first part step is to parse the table scripts and transfer the parsed data into the Java class model. xText is performed this task. If necessary or useful, of course, additional steps may be preceded, for example, generate the tables scripts from another source. This is indicated on the step "5a" and is completely outside of Orcas.
6. In step six, the Java extensions are activated (if available). The task of the extensions is normally read out the additional data and using this data to carry out modifications to the actual schema file.
7. In the following step seven the data from Java objects in PL/SQL objects are converted. If a pure PL/SQL based solution is required (eg to transfer old projects, or because of the use of Ant/Java/xText is not possible), then a direct creation of PL/SQL line types can take place. That way (As indicated in 7a) is not recommended and should be a conscious exception.
8. The execution of the PL/SQL extensions follows in step eight. These have exactly the same function as the Java extensions. Extensions may exist alternatively in Java or PL/SQL due to the fact that extension developers often familiar only with Java or PL/SQL.
9. Step Nine transforms the data from the PL/SQL line types into the "normal" PL/SQL (without line) types. This is the target state of the DB schema.
10. Step ten determines the actual state of the database schemas.
11. Adjustment of the data in step eleven makes it possible to ignore certain deviations between target and actual (for example, if storage parameters vary). This is achieved in overwriting the data to ignore with the corresponding actual data in the adjustment state.
12. Finally, the actual adjustment is performed in the last step. In this step, the "actual" logic happens. He is by far the most complicated step of the whole process. In the adjustment the actual state is compared with the target state. All differences are implemented in the database schema.

###7a row based SQL\*Plus scripts

This variant allows projects that are still using the SQL\*Plus script based table scripts to use Orcas.

The following points should be considered:

- The SQL\*Plus scripts have initial the API state of the latest Orcas version (GKN variant).
- The SQL\*Plus script based on one (or more) extensions (incl. syntax extension). This extension forms mainly the domain concept.
- The SQL\*Plus script fill in their run only one package variable (or possibly even a persistent variant) of the type OT_SYEX_MODEL.
- This is increasingly updated with each call of a SQL\*Plus script  time an SQLPlus script. Finally, created with this variable in step 9 of the normal procedure.
- The SQL\*Plus scripts are a sort of template and can/should be changed from concrete projects.
- Each project should copy theSQL\*Plus scripts into the project.
- If more syntax extensions to be used in a project, then the project must adapt typically the SQL\*Plus scripts.
- When new features (with new syntax options) are build into Orcas, then the SQL\*Plus script template should be expanded. If necessary, it may be here that this is then no longer backward compatible.
- The templates implementation of SQL\*Plus scripts should be robust to the extend of OT_SYEX_ data structures. It is, for example, not allowed to use the default constructor with all the parameters, but it must always the generated empty constructor be used followed by setting the attributes.
- The SQLPlus scripts be created manually in the first step. Possibly, it is meanwhile or afterwards possible to generate the SQLPlus scripts.

##Extract (reverse engineering)

![Extract - Reverse engineering]({{site.baseurl}}/assets/funktion_reverse.gif)

1. The actual data is read from the schema.
2. The data is transformed from the PL/SQL line types to the "normal" PL/SQL (without line) types.
3. The reverse extensions are applied. These should analyze the data and possibly create extension data and removing the original data. In addition, the extensions can remove data that are unnecessary (eg tablespace information).
4. In the final step, the data are converted into XML and with a stylesheet transformation the actual scripts are generated.

##PL/SQL components

###Object/Collection - types

All Object types are generated and should never be directly modified. The object types are used to receive the model data. There is for example the type "ot_orig_table". Occurrences (instances) of this type represent the tables that are to be created/adjusted in the schema.

There are two "sets" of object types:

- Firstly, the OT_ORIG_*/CT_ORIG_* types. These contain the data from the BNF, and the core of Orcas works with it.
- The other set (OT_SYEX_*/CT_SYEX_*) contains  also the data that have come into the model through syntax extensions.

###Procedure

The procedure is that the package **pa_orcas_xtext_model** model content will be added over **pa_orcas_model_holder.add_model_element**, to then read out the model on **get_model**, transform into ORIG types and hand over to **pa_orcas_ddl_call.update_schema**.

Here is an example (call_orcas.sql):

{% highlight sql %}
declare
  v_syex_model ot_syex_model;
  v_orig_model ot_orig_model;
begin
  pa_orcas_xtext_model.build();                                                              -- 2. Teil von Schritt 7
  v_syex_model := pa_orcas_extensions.call_extensions( pa_orcas_model_holder.get_model() );  -- Schritt 8 (call_extensions)

  v_orig_model := pa_orcas_trans_syex_orig.trans_syex_orig( v_syex_model );                  -- Schritt 9
  pa_orcas_ddl_call.update_schema( v_orig_model );                                           -- Schritt 10, 11 und 12  
end;
/
{% endhighlight %}

###Packages

**pa_orcas_checksum**

Only serves a "version" to store in the database in order to possibly skip the rebuild of Orcas user. The package body (with the checksum) is generated in the Ant build process.

**pa_orcas_compare**

This package performs the comparison (step 11 and 12). There first all needed statements are collected (step 11), and runs at the end (step 12).

**pa_orcas_ddl**

Utility package to start pa_orcas_compare.

**pa_oc_exec_log**

This package provides utility functions for the logged execution of dynamic SQL and logging.

**pa_orcas_xml_syex**

This package offers the opportunity to output a SYEX types to JSON. Used in reverse engineering.

**pa_orcas_load_ist**

This package loads the actual data from the data dictionary in an instance of ot_orig_model (Step 10).

**pa_orcas_model_holder**

The package only serves to keep a package variable (private: **pv_model**), with the maintained target model. The package works on SYEX types.

**pa_orcas_extension_parameter**

This package manages the extension parameters.

**pa_orcas_extensions**

This package calls the PL/SQL extensions. The body is generated.

**pa_orcas_run_parameter**

This package manages the parameters that are delivered in execute_statics run.

**pa_orcas_trans_syex_orig**

The package is generated and allows the transformation of SYEX types to ORIG types.

**pa_orcas_trans_orig_syex**

The package is generated and allows the transformation of ORIG types to SYEX types.

**pa_orcas_updates**

The package only serves to manage the one-time scripts and has nothing to do with the actual adjustment.

**pa_orcas_xtext_model**

The package has the task to fill the model, which is hold in **pa_orcas_model_holder**, through the procedure **add_model_element**. It does this in the procedure build. The body of this package is regenerated at each iteration. It contains only views of the packages **pa_orcas_xtext_\***.

**pa_orcas_xtext_* (pa_orcas_xtext_1,pa_orcas_xtext_2...)**

The **pa_orcas_xtext_\*** packages are generated completely (specification and body) in the Ant build process and contains the model data. Usually there is only one. Multiple packages are automatically generated if it is to adjust a very large model (database schema). Then several packages be generated due to the PL/SQL / SQL\*Plus size limitations. The packages are generated in step 7 and contain the real model data (table names, column names, ...).
