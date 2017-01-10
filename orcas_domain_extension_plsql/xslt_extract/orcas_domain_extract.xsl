<?xml version="1.0"?>

<stylesheet 
  xmlns="http://www.w3.org/1999/XSL/Transform" 
  version="1.0" 
  xmlns:lxslt="http://xml.apache.org/xslt" 
  xmlns:myfunc="http://myfunc"
  xmlns:func="http://exslt.org/functions" 
  xmlns:redirect="http://xml.apache.org/xalan/redirect"
  xmlns:java_string="http://xml.apache.org/xalan/java/java.lang.String"
  extension-element-prefixes="redirect myfunc java_string" 
  xml:space="default">

  <import href="orcas_extract.xsl"/>

  <template match="Table">
    <redirect:write file="{myfunc:format-filename(name)}.sql">
      <text>create</text>
      <apply-templates select="permanentness" />
      <text> table </text>
      <apply-templates select="name" />
      <apply-templates select="alias" />
      <apply-templates select="domain" />
      <text>
(</text>
      <apply-templates select="columns" />
      <apply-templates select="primary_key" />
      <apply-templates select="constraints" />
      <apply-templates select="ind_uks" />
      <apply-templates select="foreign_keys" />
      <apply-templates select="comments" />
      <text>
)</text>
      <apply-templates select="transactionControl" />
      <apply-templates select="tablePartitioning" />
      <apply-templates select="tablespace" />
      <apply-templates select="pctfree" />      
      <apply-templates select="compression" />
      <apply-templates select="compressionFor" />
      <apply-templates select="logging" />
      <apply-templates select="parallel" />
      <apply-templates select="mviewLog" />
      <text>;

</text>
    </redirect:write>
  </template>

  <template match="Column">
    <text>
  </text>
    <apply-templates select="name" />
    <apply-templates select="domain" />     
    <apply-templates select="data_type" />
    <apply-templates select="precision" />
    <apply-templates select="with_time_zone" />
    <apply-templates select="object_type" />
    <apply-templates select="identity" />
    <apply-templates select="default_value" />
    <apply-templates select="notnull" />
    <if test="position() != last()">
      <text>,</text>
    </if>
  </template>

  <template match="alias">
    <text> alias </text>
    <value-of select="myfunc:format-dbname(.)" />
  </template>

  <template match="domain">
    <text> domain </text>
    <value-of select="myfunc:format-dbname(.)" />
  </template>

</stylesheet>



