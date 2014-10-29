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

  <template match="Column">
    <text>
  </text>
    <apply-templates select="name" />
    <apply-templates select="domain" />
    <apply-templates select="data_type" />
    <apply-templates select="precision" />
    <apply-templates select="default_value" />
    <apply-templates select="notnull" />
    <if test="position() != last()">
      <text>,</text>
    </if>
  </template>

  <template match="domain">
    <text> domain </text>
    <value-of select="." />
  </template>

</stylesheet>



