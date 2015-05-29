-- Rechte und Synonyme auf SCS Objekte
declare
  v_owner varchar2(30) := USER;
  v_user varchar2(30) := upper('&1');
begin
  for r in (select object_type,object_name 
              from all_objects
             where object_type in ('TABLE')
               and owner = v_owner
           ) loop
    begin
      execute immediate 'grant references on '||v_owner||'."'||r.object_name||'" to '||v_user;
    end; 
  end loop;
end;
/
 