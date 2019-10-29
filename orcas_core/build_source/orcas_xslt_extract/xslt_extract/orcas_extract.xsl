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

  <output method="text" />
  <strip-space elements="*" />

  <func:function name="myfunc:format-filename">
    <param name="string_param" />
    <func:result>
      <value-of select="java_string:toLowerCase(java_string:new($string_param))" />
    </func:result>
  </func:function>                                                  
  
 <func:function name="myfunc:format-mview-filename">
    <param name="string_param" />
    <func:result>
      <value-of select="java_string:concat('mv_',java_string:toLowerCase(java_string:new($string_param)))" />
    </func:result>
  </func:function>  

  <func:function name="myfunc:format-dbname">
    <param name="string_param" />
    <func:result>
      <choose>
        <when test="starts-with($string_param, '&quot;')">
          <value-of select="java_string:new($string_param)" />
        </when>
        <otherwise>
          <value-of select="java_string:toUpperCase(java_string:new($string_param))" />
        </otherwise>
      </choose>
    </func:result>
  </func:function>

  <func:function name="myfunc:escape-comment">
    <param name="string_param" />
    <func:result>
      <value-of select="java_string:replaceAll(java_string:replaceAll(java_string:new($string_param),'\\','\\\\'),&quot;'&quot;,&quot;\\'&quot;)" />
    </func:result>
  </func:function>

  <func:function name="myfunc:escape-constraint">
    <param name="string_param" />
    <func:result>
      <value-of select="java_string:replaceAll(java_string:replaceAll(java_string:new($string_param),'\\','\\\\'),'\&quot;','\\\&quot;')" />
    </func:result>
  </func:function>

  <func:function name="myfunc:escape-function-expression">
    <param name="string_param" />
    <func:result>
      <value-of select="java_string:replaceAll(java_string:new($string_param),'\\','\\\\')" />
    </func:result>
  </func:function>

  <func:function name="myfunc:escape-quote">
    <param name="string_param" />
    <func:result>
      <value-of select="java_string:replaceAll(java_string:new($string_param),'\&quot;','\\\&quot;')" />
    </func:result>
  </func:function>

  <func:function name="myfunc:is-number">
    <param name="string_param" />
    <func:result>
      <value-of select="java_string:matches(java_string:new($string_param),'[0-9]+')" />
    </func:result>
  </func:function>

  <template match="Sequence">
    <redirect:write file="{myfunc:format-filename(sequence_name)}.sql">
      <text>create sequence </text>
      <apply-templates select="sequence_name" />
      <apply-templates select="increment_by" />
      <apply-templates select="maxvalue" />
      <apply-templates select="minvalue" />
      <apply-templates select="cycle" />
      <apply-templates select="cache" />
      <apply-templates select="order" />
      <apply-templates select="max_value_select" />
      <text>;

</text>
    </redirect:write>
  </template>

  <template match="cache">
    <text> cache </text>
    <value-of select="." />
  </template>

  <template match="cycle">
    <text> </text>
    <value-of select="." />
  </template>

  <template match="increment_by">
    <text> increment by </text>
    <value-of select="." />
  </template>

  <template match="maxvalue[parent::Sequence]">
    <text> maxvalue </text>
    <value-of select="." />
  </template>

  <template match="minvalue">
    <text> minvalue </text>
    <value-of select="." />
  </template>

  <template match="order">
    <text> </text>
    <value-of select="." />
  </template>

  <template match="max_value_select">
    <!-- max_value_select nur uebernehmen, wenn es keine Zahl ist, denn sonst ist es nur der aktuelle wert der sequence -->
    <if test="not(myfunc:is-number(.))">
      <text> orcas_ext_max_value_select "</text>
      <value-of select="." />
      <text>"</text>
    </if>
  </template>

  <template match="Table">
    <redirect:write file="{myfunc:format-filename(name)}.sql">
      <text>create</text>
      <apply-templates select="permanentness" />
      <text> table </text>
      <apply-templates select="name" />
      <apply-templates select="object_type" />
      <if test="columns or primary_key or constraints or ind_uks or foreign_keys or comments">
      <text>
(</text>
      </if>
      <apply-templates select="columns" />
      <apply-templates select="primary_key" />
      <apply-templates select="constraints" />
      <apply-templates select="ind_uks" />
      <apply-templates select="foreign_keys" />
      <apply-templates select="comments" />
      <if test="columns or primary_key or constraints or ind_uks or foreign_keys or comments">
      <text>
)</text>
      </if>
      <apply-templates select="transactionControl" />
      <apply-templates select="lobStorages" />
      <apply-templates select="varrayStorages" />
      <apply-templates select="nestedTableStorages" />
      <apply-templates select="tablePartitioning" />
      <apply-templates select="tablespace" />
      <apply-templates select="indexOrganized" />
      <apply-templates select="pctthreshold" />
      <apply-templates select="includingColumn" />
      <apply-templates select="overflowTablespace" />
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

  <template match="constraints">
    <if test="../columns or ../primary_key">
      <text>,</text>
    </if>
    <text>
</text>
    <apply-templates />
  </template>

  <template match="ind_uks">
    <if test="../columns or ../primary_key or ../constraints">
      <text>,</text>
    </if>
    <text>
</text>
    <apply-templates />
  </template>

  <template match="foreign_keys">
    <if test="../columns or ../primary_key or ../constraints or ../ind_uks">
      <text>,</text>
    </if>
    <text>
</text>
    <apply-templates />
  </template>

  <template match="comments">
    <if test="../columns or ../primary_key or ../constraints or ../ind_uks or ../foreign_keys">
      <text>,</text>
    </if>
    <text>
</text>
    <apply-templates />
  </template>

  <template match="data_type | comment_object | parallel_degree | global ">
    <text> </text>
    <value-of select="." />
  </template>

  <template match="indexOrganized">
    <if test=". = 'true'">
      <text> organization index</text>
    </if>
  </template>

  <template match="overflowTablespace">
    <text> overflow tablespace </text>
    <value-of select="." />
  </template>

  <template match="pctthreshold">
    <text> pctthreshold </text>
    <value-of select="." />
  </template>

  <template match="includingColumn">
    <text> including </text>
    <value-of select="." />
  </template>

  <template match="object_type[parent::Column]">
    <text> type "</text>
    <value-of select="." />
    <text>"</text>
  </template>

  <template match="object_type[parent::Table]">
    <text> of </text>
    <value-of select="." />
  </template>

  <template match="PrimaryKey">
    <if test="../../columns">
      <text>,</text>
    </if>
    <text>
  </text>
    <if test="consName != ''">
      <text>constraint </text>
      <apply-templates select="consName" />
      <text> </text>
    </if>
    <text>primary key</text>
    <apply-templates select="pk_columns" />
    <apply-templates select="status" />
    <if test="tablespace != '' or reverse != '' or indexname != ''">
      <text> using index</text>
      <apply-templates select="reverse" />
      <if test="not(indexname) or indexname = ''">
        <apply-templates select="tablespace" />
      </if>
      <apply-templates select="indexname" />
    </if>
  </template>

  <template match="Column">
    <text>
  </text>
    <apply-templates select="name" />
    <apply-templates select="data_type" />
    <apply-templates select="precision" />
    <apply-templates select="unsigned" />
    <apply-templates select="with_time_zone" />
    <apply-templates select="object_type" />
    <apply-templates select="identity" />
    <if test="default_value">
      <variable name="quote">	
        <choose>
          <when test="contains(default_value, '&quot;')">
            <text>'</text>
          </when>
          <otherwise>
            <text>"</text>
          </otherwise>
        </choose>
      </variable>
      <choose>
        <when test="virtual">
          <text> as (</text><value-of select="$quote"/>
          <apply-templates select="default_value" />
          <value-of select="$quote"/><text>) </text>
          <apply-templates select="virtual" />
        </when>
        <otherwise>
          <text> default </text><value-of select="$quote"/>
          <apply-templates select="default_value" />
          <value-of select="$quote"/>
        </otherwise>
      </choose>
    </if>
    <apply-templates select="notnull" />
    <if test="position() != last()">
      <text>,</text>
    </if>
  </template>

  <template match="always">
    <text>always</text>
  </template>

  <template match="by_default">
    <text>by default</text>
  </template>

  <template match="unsigned">
    <if test=". = 'true'">
      <text> unsigned</text>
    </if>  
  </template>

  <template match="on_null">
    <text> on null</text>
  </template>

  <template match="status">
    <text> </text>
    <value-of select="." />
  </template>

  <template match="ColumnIdentity">
    <text> generated </text>
    <apply-templates select="always" />
    <apply-templates select="by_default" />
    <apply-templates select="on_null" />
    <text> as identity</text>
  </template>

  <template match="precision">
      <text>(</text>
      <value-of select="." />
      <apply-templates select="../scale" />
      <apply-templates select="../byteorchar" />
      <text>)</text>
  </template>

  <template match="scale">
    <if test=". != 0">
      <text>,</text>
      <value-of select="." />
    </if>
  </template>
  
  <template match="with_time_zone">
      <text> with_time_zone</text>
  </template>

  <template match="byteorchar">
    <if test=". = 'byte'">
      <text> byte</text>
    </if>
    <if test=". = 'char'">
      <text> char</text>
    </if>
  </template>

  <template match="reverse">
    <text> reverse</text>
  </template>

  <template match="notnull">
    <if test=". = 'true'">
      <text> not null</text>
    </if>  
  </template>

  <template match="consName | destTable | column_name | name | sequence_name | mview_name">
    <value-of select="myfunc:format-dbname(.)" />
  </template>

  <template match="Constraint">
    <text>
  </text>
    <text>constraint </text>
    <apply-templates select="consName" />
    <text> check</text>
    <apply-templates select="rule" />
    <apply-templates select="deferrtype" />
    <apply-templates select="status" />
    <if test="position() != last()">
      <text>,</text>
    </if>
  </template>

  <template match="rule">
    <text> ("</text>
    <value-of select="myfunc:escape-constraint(.)" />
    <text>")</text>
  </template>

  <template match="UniqueKey">
    <text>
  </text>
    <text>constraint </text>
    <apply-templates select="consName" />
    <text> unique</text>
    <apply-templates select="uk_columns" />
    <apply-templates select="deferrtype" />
    <if test="tablespace != '' or indexname != ''">
      <text> using index</text>
      <apply-templates select="tablespace" />
      <apply-templates select="indexname" />
    </if>
    <apply-templates select="status" />
    <if test="position() != last()">
      <text>,</text>
    </if>
  </template>

  <template match="Index">
    <text>
  </text>
    <text>index </text>
    <apply-templates select="consName" />
    <apply-templates select="index_columns" />
    <apply-templates select="function_based_expression" />
    <apply-templates select="bitmap" />
    <apply-templates select="unique" />
    <apply-templates select="logging" />
    <apply-templates select="tablespace" />
    <apply-templates select="compression" />
    <apply-templates select="parallel" />
    <apply-templates select="global" />
    <if test="position() != last()">
      <text>,</text>
    </if>
  </template>

  <template match="ForeignKey">
    <text>
  </text>
    <text>constraint </text>
    <apply-templates select="consName" />
    <text> foreign key</text>
    <apply-templates select="srcColumns" />
    <text> references </text>
    <apply-templates select="destTable" />
    <apply-templates select="destColumns" />
    <apply-templates select="delete_rule" />
    <apply-templates select="deferrtype" />
    <apply-templates select="status" />
    <if test="position() != last()">
      <text>,</text>
    </if>
  </template>  

  <template match="ColumnRef">
    <apply-templates select="column_name" />
    <if test="position() != last()">
      <text>,</text>
    </if>
  </template>

  <template match="parallel">
    <if test=". = 'parallel'">
      <text> parallel</text>
      <apply-templates select="../parallel_degree" />
    </if>
    <if test=". = 'noparallel'">
      <text> noparallel</text>
    </if>
  </template>
  
  <template match="logging">
    <if test=". = 'logging'">
      <text> logging</text>
    </if>
    <if test=". = 'nologging'">
      <text> nologging</text>
    </if>
  </template>  

  <template match="permanentness">
    <if test=". = 'global_temporary'">
      <text> global temporary</text>
    </if>
  </template>

  <template match="transactionControl[parent::Table]">
    <if test=". = 'on_commit_delete'">
      <text> on commit delete rows</text>
    </if>
    <if test=". = 'on_commit_preserve'">
      <text> on commit preserve rows</text>
    </if>
  </template>
  
  <template match="compressionFor">
    <if test=". = 'all'">
      <text> for all_operations</text>
    </if>
    <if test=". = 'direct_load'">
      <text> for direct_load_operations</text>
    </if>
    <if test=". = 'query low'">
      <text> for query_low</text>
    </if>
    <if test=". = 'query high'">
      <text> for query_high</text>
    </if>
    <if test=". = 'archive low'">
      <text> for archive_low</text>
    </if>
    <if test=". = 'archive high'">
      <text> for archive_high</text>
    </if>    
  </template>    
  
  <template match="compression">
    <if test=". = 'nocompress'">
      <text> nocompress</text>
    </if>
    <if test=". = 'compress'">
      <text> compress</text>
      <apply-templates select="compressionFor" />
    </if>
  </template>  

  <template match="lobCompressForType | lobDeduplicateType | compressType">
    <text> </text>
    <value-of select="." />
  </template>  

  <template match="index_columns | destColumns | srcColumns | uk_columns | pk_columns | columns[parent::RangePartitions] | mview_columns">
    <text> (</text>
    <apply-templates />
    <text>)</text>
  </template>

  <template match="function_based_expression">
    <text> "</text>
    <value-of select="myfunc:escape-function-expression(.)" />
    <text>"</text>
  </template>

  <template match="unique">
    <text> unique</text>
  </template>

  <template match="bitmap">
    <text> bitmap</text>
  </template>

  <template match="indexname[parent::UniqueKey] | indexname[parent::PrimaryKey]">
    <text> </text>
    <value-of select="myfunc:format-dbname(.)" />
  </template>  

  <template match="tablespace">
    <text> tablespace </text>
    <value-of select="myfunc:format-dbname(.)" />
  </template>
  
  <template match="pctfree">
    <text> pctfree </text>
    <value-of select="." />
  </template>  

  <template match="delete_rule">
    <if test=". = 'cascade'">
      <text> on delete cascade</text>
    </if>
    <if test=". = 'set_null'">
      <text> on delete set_null</text>
    </if>
  </template>

  <template match="deferrtype">
    <if test=". = 'deferred'">
      <text> deferrable initially deferred</text>
    </if>
    <if test=". = 'immediate'">
      <text> deferrable initially immediate</text>
    </if>
  </template>

  <template match="comment">
    <text> '</text>
    <value-of select="myfunc:escape-comment(.)" />
    <text>'</text>
  </template>

  <template match="InlineComment">
    <text>
  </text>
    <text>comment on</text>
    <apply-templates select="comment_object" />
    <if test="column_name">
      <text> </text>
    </if>
    <apply-templates select="column_name" />
    <text> is</text>
    <apply-templates select="comment" />
    <text>;</text>
  </template>

 <template match="VarrayStorage">
    <text>
</text>
    <text>varray </text>
    <apply-templates select="column_name" />
    <text> store as </text>
    <apply-templates select="lobStorageType" />
    <text> lob</text>
    <apply-templates select="lobStorageParameters" />
  </template>

 <template match="LobStorage">
    <text>
</text>
    <text>lob (</text>
    <apply-templates select="column_name" />
    <text>) store as </text>
    <apply-templates select="lobStorageParameters" />
  </template>

 <template match="LobStorageParameters">
    <text>(</text>
    <apply-templates select="tablespace" />
    <apply-templates select="lobDeduplicateType" />
    <apply-templates select="compressType" />
    <apply-templates select="lobCompressForType" />
    <text>)</text>
  </template>

  <template match="NestedTableStorage">
    <text>
</text>
    <text>nested table </text>
    <apply-templates select="column_name" />
    <text> store as </text>
    <apply-templates select="storage_clause" />
  </template>

  <template match="value[parent::ListPartitionValue] | value[parent::RangePartitionValue] ">
    <text>"</text>
    <value-of select="." />
    <text>"</text>
  </template>

  <template match="default">
    <text>default</text>
  </template>

  <template match="maxvalue[parent::ListPartitionValue] | maxvalue[parent::RangePartitionValue]">
    <text>maxvalue</text>
  </template>

  <template match="ListPartitionValue | RangePartitionValue">
    <apply-templates select="value" />
    <apply-templates select="default" />
    <apply-templates select="maxvalue" />
    <if test="position() != last()">
      <text>,</text>
    </if>
  </template>

  <template match="value[parent::ListPartition] | value[parent::ListSubPart] | value[parent::ListSubSubPart]">
    <text> values (</text>
    <apply-templates />
    <text>)</text>
  </template>

  <template match="value[parent::RangePartition] | value[parent::RangeSubPart] | value[parent::RangeSubSubPart]">
    <text> values less than (</text>
    <apply-templates />
    <text>)</text>
  </template>

  <template match="HashSubSubPart">
    <text>
  </text>
    <text>subpartition </text>
    <apply-templates select="name" />
    <apply-templates select="tablespace" />
    <if test="position() != last()">
      <text>,</text>
    </if>
  </template>
  
  <template match="RangeSubSubPart">
    <text>
  </text>
    <text>subpartition </text>
    <apply-templates select="name" />
    <apply-templates select="value" />
    <apply-templates select="tablespace" />
    <if test="position() != last()">
      <text>,</text>
    </if>
  </template>    
  
  <template match="ListSubSubPart">
    <text>
  </text>
    <text>subpartition </text>
    <apply-templates select="name" />
    <apply-templates select="value" />
    <apply-templates select="tablespace" />
    <if test="position() != last()">
      <text>,</text>
    </if>
  </template>  

  <template match="HashPartition | ListPartition | RangePartition | RefPartition">
    <text>
  </text>
    <text>partition </text>
    <apply-templates select="name" />
    <apply-templates select="value" />
    <apply-templates select="tablespace" />
    <if test="position() != last()">
      <text>,</text>
    </if>
  </template>

  <template match="ListSubPart | RangeSubPart">
    <text>
  </text>
    <text>partition </text>
    <apply-templates select="name" />
    <apply-templates select="value" />
    <apply-templates select="subPartList" />
    <if test="position() != last()">
      <text>,</text>
    </if>
  </template>

  <template match="partitionList | subPartitionList | subPartList">
    <text>
</text>
    <text>(</text>
    <apply-templates />
    <text>
</text>
    <text>)</text>
  </template>
  
  <template match="ListSubParts">
    <text>
    </text>
    <text>subpartition by list (</text>
    <apply-templates select="column" />
    <text>)</text>
  </template>  

  <template match="HashSubParts">
    <text>
    </text>
    <text>subpartition by hash (</text>
    <apply-templates select="column" />
    <text>)</text>
  </template>
  
  <template match="RangeSubParts">
    <text>
    </text>
    <text>subpartition by range (</text>
    <apply-templates select="columns" />
    <apply-templates select="intervalExpression" />
    <text>)</text>
  </template>  

  <template match="HashPartitions">
    <text>
</text>
    <text>partition by hash (</text>
    <apply-templates select="column" />
    <text>)</text>
    <apply-templates select="partitionList" />
  </template>

  <template match="ListPartitions">
    <text>
</text>
    <text>partition by list (</text>
    <apply-templates select="column" />
    <text>)</text>
    <apply-templates select="tableSubPart" />
    <apply-templates select="partitionList" />
    <apply-templates select="subPartitionList" />
  </template>

  <template match="intervalExpression">
    <text>
</text>
    <text> interval ("</text>
    <value-of select="." />
    <text>")</text>
  </template>

  <template match="RangePartitions">
    <text>
</text>
    <text>partition by range</text>
    <apply-templates select="columns" />
    <apply-templates select="intervalExpression" />
    <apply-templates select="tableSubPart" />
    <apply-templates select="partitionList" />
    <apply-templates select="subPartitionList" />
  </template>

  <template match="RefPartitions">
    <text>
</text>
    <text>partition by reference (</text>
    <apply-templates select="fkName" />
    <text>)</text>
    <apply-templates select="partitionList" />
  </template>
  
  <template match="MviewLog">
    <text> materialized view log </text>
    <apply-templates select="name" />
    <apply-templates select="tablespace" />
    <apply-templates select="parallel" />
    <apply-templates select="newValues" />
    <text> with </text>
    <apply-templates select="primaryKey" />
    <apply-templates select="rowid" />
    <apply-templates select="withSequence" />
    <apply-templates select="columns[parent::MviewLog]" />
    <apply-templates select="commitScn" /> 
    <apply-templates select="purge" />       
    <apply-templates select="synchronous" />    
    <apply-templates select="startWith" />     
    <apply-templates select="next" />     
    <apply-templates select="repeatInterval" />    
  </template>  
  
  <template match="primaryKey[parent::MviewLog]">
    <if test=". = 'primary'">
      <text> primary key</text>
    </if>
    <if test="position() != last()">
      <text>,</text>
    </if>
  </template>      
  
  <template match="purge[parent::MviewLog]">
    <if test=". = 'purge'">
      <text> purge</text>
    </if>
  </template>   
  
  <template match="synchronous[parent::MviewLog]">
    <text> immediate</text>
    <if test=". = 'synchronous'">
      <text> synchronous</text>
    </if>
    <if test=". = 'asynchronous'">
      <text> asynchronous</text>
    </if>
  </template>    
  
  <template match="startWith[parent::MviewLog]">
    <text> start with '</text>
    <value-of select="." />
    <text>'</text>
  </template>    
  
  <template match="next[parent::MviewLog]">
    <text> next '</text>
    <value-of select="." />
    <text>'</text>
  </template>    
  
  <template match="repeatInterval[parent::MviewLog]">
    <text> repeat interval </text>
    <value-of select="." />
  </template>  
  
  <template match="rowid[parent::MviewLog]">
    <if test=". = 'rowid'">
      <text> rowid</text>
    </if>
        <if test="position() != last()">
      <text>,</text>
    </if>
  </template>   
  
  <template match="withSequence">
    <if test=". = 'sequence'">
      <text> sequence</text>
    </if>
    <if test="position() != last()">
      <text>,</text>
    </if>
  </template>   
  
  <template match="columns[parent::MviewLog]">
    <text>
    </text>
    <text> (</text>
    <apply-templates select="ColumnRef" />
    <text>)</text>
    <if test="position() != last()">
      <text>,</text>
    </if>
  </template>  
  
  <template match="commitScn">
    <if test=". = 'commit_scn'">
      <text> commit_scn</text>
    </if>  
  </template> 
  
  <template match="newValues">
    <if test=". = 'including'">
      <text> including new values</text>
    </if>
    <if test=". = 'excluding'">
      <text> excluding new values</text>
    </if>
  </template>  
    
  <template match="Mview">
    <redirect:write file="{myfunc:format-mview-filename(mview_name)}.sql">
    <text>create materialized view </text>
    <apply-templates select="mview_name" />
    <apply-templates select="mview_columns" />
    <apply-templates select="tablespace" />
    <apply-templates select="compression" />
    <apply-templates select="compressionFor" />
    <apply-templates select="parallel" />
    <apply-templates select="buildMode" />
    <apply-templates select="refreshMethod" />
    <apply-templates select="refreshMode" />
    <apply-templates select="refreshWithPrimaryKey" />
    <apply-templates select="queryRewrite" />
    <apply-templates select="viewSelectCLOB" />
    <text>;

    </text>
    </redirect:write>
  </template>  
  
  <template match="buildMode[parent::Mview]">
    <if test=". = 'immediate'">
      <text> immediate</text>
    </if>
    <if test=". = 'deferred'">
      <text> deferred</text>
    </if>
    <if test=". = 'prebuilt'">
      <text> prebuilt</text>
    </if>
  </template>  
  
  <template match="queryRewrite">
    <if test=". = 'enable'">
      <text> enable query rewrite</text>
    </if>
    <if test=". = 'disable'">
      <text> disable query rewrite</text>
    </if>
  </template>  

  <template match="refreshMethod">
    <if test=". = 'complete'">
      <text> refresh_complete</text>
    </if>
    <if test=". = 'force'">
      <text> refresh_force</text>
    </if>
    <if test=". = 'fast'">
      <text> refresh_fast</text>
    </if>
    <if test=". = 'never'">
      <text> never_refresh</text>
    </if>
    <apply-templates select="refreshMode" />
  </template>  
  
  <template match="refreshMode[parent::Mview]">
    <if test=". = 'demand'">
      <text> on demand</text>
    </if>
    <if test=". = 'commit'">
      <text> on commit</text>
    </if>
  </template>

  <template match="refreshWithPrimaryKey">
    <if test=". = 'primary'">
      <text> with primary key</text>
    </if>
    <if test=". = 'rowid'">
      <text> with rowid</text>
    </if>
  </template>

  <template match="viewSelectCLOB">
    <text> as </text>
    <text>"</text>  
    <value-of select="myfunc:escape-quote(.)" />
    <text>"</text> 
  </template>      

</stylesheet>