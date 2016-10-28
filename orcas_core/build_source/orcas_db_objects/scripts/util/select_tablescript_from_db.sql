set linesize 32000;
set heading off;
set feedback off;
set pagesize 0;
set trimspool on;
set trim on;

select decode(instr(script,'/'), 0, trim(replace(replace(replace(script, chr(13) || chr(10),' '), chr(10),' '), chr(13),' ')), script) from orcas_table_script_source order by id;

