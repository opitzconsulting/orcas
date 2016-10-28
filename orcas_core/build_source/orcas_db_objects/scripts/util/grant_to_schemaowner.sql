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
      execute immediate 'grant select on '||v_owner||'."'||r.object_name||'" to '||v_user;
      execute immediate 'grant insert on '||v_owner||'."'||r.object_name||'" to '||v_user;
      execute immediate 'grant update on '||v_owner||'."'||r.object_name||'" to '||v_user;
      execute immediate 'grant delete on '||v_owner||'."'||r.object_name||'" to '||v_user;
      execute immediate 'create or replace synonym '||v_user||'."'||r.object_name||'" for '||v_owner||'."'||r.object_name||'"';
    end;
  end loop;
  for r in (select object_type,object_name 
              from all_objects
             where object_type in ('PACKAGE','TYPE')
               and owner = v_owner
           ) loop
    begin
      execute immediate 'grant execute on '||v_owner||'."'||r.object_name||'" to '||v_user;
      execute immediate 'create or replace synonym '||v_user||'."'||r.object_name||'" for '||v_owner||'."'||r.object_name||'"';
    end;
  end loop;
    
end;
/
 