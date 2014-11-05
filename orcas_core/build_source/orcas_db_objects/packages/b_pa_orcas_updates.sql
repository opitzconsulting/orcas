CREATE OR REPLACE package body pa_orcas_updates is

procedure set_executed( p_script_name in varchar2, p_logname in varchar2 )
is
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
         substr( p_script_name, (instr( p_script_name, '/', 2 )+1) ),
         sysdate,
         user,
         p_logname
         );
  commit;
end;

end;
/
