begin
  for cur_sql in
    (
    select 'drop java source "' || long_name || '"' text 
      from sys.ku$_java_source_view,
           user_objects
     where obj_num = object_id
       and object_type in ('JAVA SOURCE')
    )
  loop       
    execute immediate cur_sql.text;
  end loop;
  for cur_sql in
    (
    select 'drop java class "' || name || '"' text
    from user_java_classes
    where &1
    )       
  loop       
    execute immediate cur_sql.text;
  end loop;

end;
/
