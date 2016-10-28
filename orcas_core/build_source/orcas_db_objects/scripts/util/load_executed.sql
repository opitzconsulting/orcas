set serveroutput on
set feedback off
set verify off
begin
  dbms_output.put_line('');
  dbms_output.put_line('orcas_internal_last_executed=');      
  for cur_scripts in 
    (
    select scup_script_name 
      from orcas_updates
     where scup_logname = '&1'
    )
  loop
    dbms_output.put_line( cur_scripts.scup_script_name || ',' );
  end loop;
end;
/
