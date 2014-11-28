begin
  
  for cur_sql in
    (
    select 'drop package ' || OBJECT_NAME  text
      from user_objects 
     where object_type = 'PACKAGE' 
       and &1
    union 	
    select 'drop trigger ' || OBJECT_NAME  text
      from user_objects 
     where object_type = 'TRIGGER'
       and &2
   union
    select 'drop view ' || OBJECT_NAME  text
      from user_objects 
     where object_type = 'VIEW'
       and &3
   union
    select 'drop function ' || OBJECT_NAME text
      from user_objects
    where object_type = 'FUNCTION'
       and &4
    union
    select 'drop procedure ' || OBJECT_NAME text
      from user_objects
    where object_type = 'PROCEDURE'
       and &5
    )
     loop
    execute immediate cur_sql.text;
  end loop;

end;
/
