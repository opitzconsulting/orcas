declare
  drop_count number := 0;
  old_drop_count number;
begin
  -- Types löschen (falls moeglich)
  loop 
    old_drop_count := drop_count;

    for cur_sql in
    (
     select 'drop TYPE "' || OBJECT_NAME || '"' text
        from user_objects 
       where object_type = 'TYPE'
          and &1
        order by object_id desc
    )    
    loop
      begin
        execute immediate cur_sql.text;
  
        drop_count := drop_count + 1;
      exception
        when others then null;
      end;
    end loop;

    exit when drop_count = old_drop_count;
  end loop;
  
end;
/
