set feedback off
set verify off
set term off

begin
  insert into orcas_updates
         (
         scup_id,
         scup_script_name,
         scup_date,
         scup_schema,
         scup_logname
         )
  values (
         nvl
         (
           (
           select max( scup_id ) +1
             from orcas_updates
           ),
           1
         ),
         substr( '&1', (instr( '&1', '/', 2 )+1) ),
         sysdate,
         user,
         '&2'
         );
  commit;
end;
/

