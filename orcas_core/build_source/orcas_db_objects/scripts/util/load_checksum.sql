set serveroutput on
set feedback off
set verify off
set term off
set linesize 250;
begin
  dbms_output.put_line('orcas_internal_total_checksum_db='||pa_orcas_checksum.get_total_checksum());      
  dbms_output.put_line('orcas_internal_extension_checksum_plsql_db='||pa_orcas_checksum.get_extension_checksum());      
end;
/
