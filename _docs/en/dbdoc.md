---
layout: page
title: dbdoc
permalink: /docs/dbdoc/
categories: 
- en
---

dbdoc is a tool for documenting database schemas. It generates a visual display of all schema information in which you are able to see all tables and their relation to each other.
<br/>You can set an Ant script to configure the order and display of your tables.

To execute dbdoc you need to install [Graphviz](http://www.graphviz.org/Download_windows.php).

## 1. Structure of a dbdoc script

### 1.1. Including of dbdoc tasks

First you have to include the dbdoc tasks and Orcas default tasks.

{% highlight xml %}
<import file="${orcas_dir}/orcas_default_tasks.xml"/>
<import file="${orcas_dbdoc_dir}/orcas_dbdoc_tasks.xml"/>
{% endhighlight %}

### 1.2. Accessing the database

The database will be read out in the task **&lt;orcas_dbdoc&gt;**.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|jdbcurl  |Defines the database URL. <br/>Format: jdbc :oracle:thin:host:port:sid|Yes||
|user     |Defines the username of the schema.|Yes||
|password |Defines the password for the user. |Yes||
|outfolder|Defines the folder for the generated HTML pages.|Yes||
|tmpfolder|Defines the folder for storing temporary files. |Yes||

{% highlight xml %}
<orcas_dbdoc jdbcurl="${jdbc_url}" user="${demo_user}" password="${demo_password}" outfolder="${output}" tmpfolder="${tmpdir}/">
{% endhighlight %}

### 1.3. Configuration of the process

All table groups, the style, the diagrams and their structure will be defined in **&lt;config&gt;**.

#### 1.3.1. Configuration of table groups

All table groups will be defines in **&lt;tablerregistry&gt;**.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|tablesrcfolder|Defines the folder where to find the table scripts. <br/>Results in a display of SQL commands in a table.|No||

The task **&lt;tablegroup&gt;** will be created for every table group.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|name     |Defines the name of a table group.|Yes||

Both tasks **&lt;include&gt;** and **&lt;exclude&gt;** will be used in **&lt;tablegroup&gt;** for including and excluding tables.

Working with regular expressions is also possible, so you can create filters to select multiple tables.

Examples:

|Task|Value|Meaning|
|----|-----|---------|
|&lt;include name="MASTERDATA_.*/&gt;|MASTERDATA_.*|All tables with prefix "Masterdata_" will be assigned to table group "ADRESSE".|
|&lt;include name=".+TIER.*/&gt;|.+TIER.*|All tables which include "TIER" in their name(beginning, middle, end)|
|&lt;exclude name=".*_TIERMONITORING"/&gt;|.*_TIERMONITORING|All tables with suffix "_TIERMONITORING" will NOT be included into table group TIER|

{% highlight xml %}
<tableregistry tablesrcfolder="tables">

  <tablegroup name="TIERDISPATCH">
    <include name=".*DISPATCH.*"/>
    <include name="TIEROWNER"/>
  </tablegroup>

  <tablegroup name="SLAUGHTER">
    <include name="SLAUGHT.*"/>
    <include name="TYPE_GRADE"/>
    <include name="TYP_TIERCATEGORY"/>
</tablegroup>
{% endhighlight %}

#### 1.3.2. Style configuration

The output format of the tables and diagrams are defined in **&lt;styles&gt;**.

##### 1.3.2.1. Configuration of table presentation

You can adjust the table presentation for every single table group in **&lt;tables&gt;**.    
You only have to keep in mind not to assign a table to multiple table groups (the same applies to multiple selections by regular expressions!)

|Name|Description|Value|Default|
|----|-----------|--------|-------|
|color|Defines the table frame color|color name/hexadecimal value|black|
|fillcolor|Defines the filling color for the table|color name/hexadecimal value|lightgrey|
|font|Defines the font|font name||
|fontsize|Defines the font size|In dots|14|

You'll find many additional attributes in the [Graphviz documentation](http://www.graphviz.org/content/attrs).

{% highlight xml %}
<tables>
  <style name="fillcolor" value="#FFE500" tablegroup="TIER"/>
  <style name="fontsize"  value="18"      tablegroup="TIER"/>
  <style name="color"     value="green"   tablegroup="TIER"/>
  <style name="fillcolor" value="#FFE500" tablegroup="TIERNUMBERS"/>
  <style name="fillcolor" value="#FF6600" tablegroup="BUSINESSESTABLISHMENT"/>
  <style name="fillcolor" value="#FF9900" tablegroup="ADRESSE"/>
</tables>
{% endhighlight %}


##### 1.3.2.2. Configuration of table presentation

All style groups are defined in **&lt;diagrams&gt;** and can be selected in the next step (diagram generation) in **&lt;diagram&gt;**.
<br/>The task **&lt;stylegroup&gt;** will be created for every style group.

|Name|Description|Value|Default|
|----|-----------|--------|-------|
|dotexecutable|Style group template|Style group name|dot|

There are six different style groups:

<table>
  <tr>
    <td><b>dot</b></td>
    <td><b>fdp</b></td>
    <td><b>sfdp</b></td>
  </tr>
  <tr>
    <td><img src="{{site.baseurl}}/assets/Tables_dot.jpg"/></td>
    <td><img src="{{site.baseurl}}/assets/Tables_fdp.jpg"/></td>
    <td><img src="{{site.baseurl}}/assets/Tables_sfdp.jpg"/></td>
  </tr>
  <tr>
    <td><b>circo</b></td>
    <td><b>neato</b></td>
    <td><b>twopi</b></td>
  </tr>
  <tr>
    <td><img src="{{site.baseurl}}/assets/Tables_circo.jpg"/></td>
    <td><img src="{{site.baseurl}}/assets/Tables_neato.jpg"/></td>
    <td><img src="{{site.baseurl}}/assets/Tables_twopi.jpg"/></td>
  </tr>
</table>

Every style group can be adjusted with **&lt;style&gt;**.    
Possible parameters:

|Name|Description|Value|Default|
|----|-----------|-----|-------|
|overlap|Defines whether tables can be overlapping in a presentation or not|true/false|false|
|nodesep|Defines the horizontal space (inches) between tables. (Only valid with dot).|1-n|1|
|ranksep|Defines the vertical space (inches) between tables. (Only valid with dot and twopi)|1-n|2|
|splines|Defines whether and how table connections are displayed. |(empty), true, false, polyline|polyline|

You'll find many additional attributes in the [Graphviz documentation ](http://www.graphviz.org/content/attrs). Not all attributes are supported by all style groups.

{% highlight xml %}
<diagrams>
  <stylegroup name="style1" dotexecutable="dot">
    <style name="nodesep" value="1"/>
    <style name="ranksep" value="1"/>
    <style name="splines" value="polyline"/>
  </stylegroup>
</diagrams>
{% endhighlight %}

#### 1.3.3. Configuration of diagram structure

You can define the names and hierarchy of diagrams in **&lt;diagram&gt;**.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|label|Defines the diagram label.|Yes||
|stylegroup|Choose an existing style group.|No||
|subinnclude|Defines the presentation format of the diagram. <br/>(diagrams_only/diagrams_with_tables/tables)|No||
|tablegroup|Defines the tables and table groups belonging to the diagram.|No||

All three presentation formats of a diagram differ in size of the presented contents.

<table>
  <tr>
    <td>
      <b>diagrams_only</b>
      <br/>
      <img src="{{site.baseurl}}/assets/Diagrams_only.jpg"/>
    </td>
    <td>
--------------------------------------------------------------------------------------------------------------------------------------------------------
      Hierarchically subordinated diagrams with their table groups and linking between them will be displayed here.
      <br/>Single tables will not be displayed.
      <br/>(Mainly interesting for the main diagram)
    </td>
  </tr>
  <tr>
    <td>
    <b>diagrams_with_tables</b>
      <br/>
      <img src="{{site.baseurl}}/assets/Diagrams_with_tables.jpg"/>
    </td>
    <td>
      Hierarchically subordinated diagrams with their table groups and linking between them will be displayed here.
      <br/>(Error-prone presentation)
    </td>
  </tr>
  <tr>
    <td>
    <b>tables</b>
      <br/>
      <img src="{{site.baseurl}}/assets/Tables_dot.jpg"/>
    </td>
    <td>
      All hierarchically subordinated tables with linking between them will be displayed here.
      <br/>Subordinated diagram structures will not be considered.
      <br/>(Chaotic with many tables)
    </td>
  </tr>
</table>

The hierarchical structure of the a diagram will be achieved by nesting **&lt;diagram&gt;** tasks.    
The presentation form "diagrams_only" should be chosen for the main diagram (which includes all further diagrams) to achieve a clear presentation.    
To select all table groups, you have to set "tablegroup" to ".*".

{% highlight xml %}
<diagram label="Milk production" stylegroup="style1" subinclude="diagrams_only" tablegroup=".*"/>
{% endhighlight %}

If you are going to execute this, all table groups with all associated tables will be loaded and displayed because there are no hierarchically subordinated diagrams.
<br/>With a big amount of included tables, this can get complex very fast.
<br/>Because of this, there is the possibility to summarize table groups in separate diagrams, which are displayed hierarchically below the main diagram.

If you take the world as an example, there will be a diagram called "world". For this diagram you have a subordinate diagram for each continent. For a continent diagram you will have subordinate state diagrams and so on.

{% highlight xml %}
<diagram label="The world" subinnclude="diagrams_only" tablegroup=".*">

  <diagram label="Europe" subinnclude="diagrams_with_tables">
    <diagram label="Germany" subinnclude="tables">
      <diagram label="Baden_Wuerttemberg" tablegroup="Baden_Wuerttemberg"/>
      <diagram label="Bavaria" tablegroup="Bavaria"/>
    </diagram>
    <diagram label="Spain" tablegroup="Spain"/>
  </diagram>

  <diagram label="South America" subinnclude="tables">
    <diagram label="Brasil" tablegroup="Brasil"/>
    <diagram label="Chile" tablegroup="Chile"/>
    <diagram label="Columbia" tablegroup="Columbia"/>
  </diagram>

</diagram>

{% endhighlight %}

If you execute this code, there will be a diagram for "Europe" with subordinate diagrams "Spain" and "Germany" ("Germany" has two more subordinate diagrams for the federa states Bavaria and Baden-Württemberg).
![]({{site.baseurl}}/assets/Europa.jpg)

## Example project dbdoc_demo

You will find an example project at examples\dbdoc_demo\build.xml. You can execute it and use it as a template for your own projects.
<br/>The example is a data model für milk production.
<br/>Here you will find the main diagram (the gray rectangles are hierarchically subordinate diagrams):
![]({{site.baseurl}}/assets/Diagrams_only.jpg)


This link offers a presentation of the table group on an extra page.

![]({{site.baseurl}}/assets/Unterdiagramm.jpg)

Single tables can also be selected and will display associated SQL queries of included table directories.

![]({{site.baseurl}}/assets/abgang_hat_praemie.jpg)
