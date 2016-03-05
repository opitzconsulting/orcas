-- Parameters:
-- 1:  Filename for temp-file
-- 2:  fileprefix for filenames (with path)
-- 3:  objekt typname (e.g. PACKAGE_BODY not PACKAGE BODY)
-- 4:  objekt name (with wildcards)

set pagesize 0
set long 32000
SET linesize 10000
set trimspool on
set verify off
set termout off

spool &1

select text
  from
       (
       select 'spool ' ||  
              '&2' ||
              lower( object_name ) ||
              '.sql' as text,
              -1 as line,
              object_name as name,
              object_type as type
         from user_objects
       union all
       select 'prompt LINE_BEGINdrop ' || lower(object_type) || ' ' || lower(object_name) || ';;' as text,
              1000001 as line,
              object_name as name,
              object_type as type
         from user_objects
       union all
       select 'spool off' as text,
              1000002 as line,
              object_name as name,
              object_type as type
         from user_objects         
       )
 where type = decode( '&3', 'PACKAGE_BODY', 'PACKAGE BODY', '&3' )    
   and name like '&4'
 order by name, line;

spool off

@&1

quit

