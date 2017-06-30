SET SERVEROUTPUT 	ON
SET VERIFY OFF
SET ECHO			ON
SET TERMOUT 		OFF
SET FEEDBACK		OFF
SET HEADING			ON
SET ESCAPE			ON
SET LINESIZE 		10000;
SET PAGESIZE		9999;
SET trimspool on
set long 1000000000 longc 60000


SPOOL &2
BEGIN
  DBMS_OUTPUT.ENABLE(9999999);
  DBMS_OUTPUT.NEW_LINE();
  FOR REC IN (SELECT TABLE_NAME, COLUMN_NAME FROM USER_TAB_COLUMNS WHERE TABLE_NAME NOT LIKE 'BIN$%' AND TABLE_NAME NOT LIKE '%$%'  AND DATA_TYPE NOT LIKE 'BLOB' AND DATA_TYPE NOT LIKE 'NCLOB' AND DATA_TYPE NOT LIKE 'CLOB' AND DATA_TYPE NOT LIKE 'XMLTYPE' AND DATA_TYPE NOT LIKE 'LONG' AND DATA_TYPE NOT LIKE 'LONG RAW' AND DATA_TYPE_OWNER IS NULL ORDER BY TABLE_NAME ASC, COLUMN_NAME ASC)
  LOOP
  	DBMS_OUTPUT.PUT_LINE ('SELECT '||REC.COLUMN_NAME ||' FROM '||REC.TABLE_NAME||' where &3 ORDER BY '||REC.COLUMN_NAME||' ASC;');
  END LOOP;
  FOR REC IN (SELECT TABLE_NAME, COLUMN_NAME FROM USER_TAB_COLUMNS WHERE TABLE_NAME NOT LIKE 'BIN$%' AND TABLE_NAME NOT LIKE '%$%'  AND (DATA_TYPE = 'NCLOB' OR DATA_TYPE LIKE 'CLOB') ORDER BY TABLE_NAME ASC, COLUMN_NAME ASC)
  LOOP
  	DBMS_OUTPUT.PUT_LINE ('SELECT dbms_lob.substr( '||REC.COLUMN_NAME ||', 4000, 1 ) FROM '||REC.TABLE_NAME||' where &3;');
  END LOOP;
  FOR REC IN (SELECT TABLE_NAME, COLUMN_NAME FROM USER_TAB_COLUMNS WHERE TABLE_NAME NOT LIKE 'BIN$%' AND TABLE_NAME NOT LIKE '%$%'  AND (DATA_TYPE LIKE 'XMLTYPE') ORDER BY TABLE_NAME ASC, COLUMN_NAME ASC)
  LOOP
  	DBMS_OUTPUT.PUT_LINE ('SELECT ('||REC.TABLE_NAME||'.'||REC.COLUMN_NAME ||').GetStringVal() as val FROM '||REC.TABLE_NAME||' where &3;');
  END LOOP;
  DBMS_OUTPUT.NEW_LINE();
END;
/
SPOOL OFF;


SPOOL &1

PROMPT ****************************************************************************************TABELLEN DER DATENBANK******************************************************************************************;
SELECT TABLE_NAME, logging, cache
FROM USER_TABLES
WHERE DROPPED = 'NO' AND TABLE_NAME NOT LIKE '%$%'
ORDER BY TABLE_NAME ASC;
PROMPT  ;

PROMPT ****************************************************************************************PARTITIONIERTE TABELLEN******************************************************************************************;
select table_name, partitioning_type, subpartitioning_type, interval
from user_part_tables
where table_name not like '%$%'
order by table_name;
PROMPT  ;

PROMPT ****************************************************************************************TABELLEN PARTITIONEN******************************************************************************************;
select table_name, composite, partition_name, 'MASTER' as subpartition_name, high_value, tablespace_name
from user_tab_partitions
where table_name not like '%$%'
union all
select table_name, 'SUB' as composite, partition_name, subpartition_name, high_value, tablespace_name
from user_tab_subpartitions
where table_name not like '%$%'
order by table_name, partition_name, subpartition_name;
PROMPT  ;

PROMPT ****************************************************************************************TABELLEN PAR KEY COLUMNS******************************************************************************************;
select name,object_type,column_name,column_position 
from user_part_key_columns
where name not like '%$%'
union all
select name,object_type,column_name,column_position 
from user_subpart_key_columns
where name not like '%$%'
order by name,object_type,column_position,column_name ;
PROMPT  ;

PROMPT **********************************************************************************************TRIGGER***************************************************************************************************;
SELECT TRIGGER_NAME,TRIGGER_TYPE,TRIGGERING_EVENT,BASE_OBJECT_TYPE,TABLE_NAME,COLUMN_NAME,REFERENCING_NAMES,WHEN_CLAUSE,STATUS,DESCRIPTION,ACTION_TYPE,TRIGGER_BODY 
FROM USER_TRIGGERS 
WHERE TABLE_NAME NOT LIKE 'BIN$%' AND TABLE_NAME NOT LIKE '%$%' 	
ORDER BY TRIGGER_NAME ASC;
PROMPT  ;

create or replace function orcas_get_cons_search_cond( p_table_name in varchar2, p_constraint_name in varchar2 ) return varchar2 is
  v_long long;  
begin
  select search_condition
    into v_long
    from user_constraints
   where table_name = p_table_name
     and constraint_name = p_constraint_name;  

  return substr(v_long,1,4000);
end; 
/

create or replace function orcas_get_name( p_name in varchar2 ) return varchar2 is
begin
  if( p_name not like 'SYS%' )
  then
    return p_name;
  else
    return 'GENERATED_NAME';
  end if;
end; 
/


drop function orcas_get_cons_search_cond;
drop function orcas_get_name;

PROMPT *******************************************************************************DATENSAETZE*************************************************************************************;
PROMPT  ;
@&2
SPOOL OFF
