begin
  
  for cur_sql in
    (
    select 'drop package ' || OBJECT_NAME  text
      from user_objects 
     where object_type = 'PACKAGE' 
       and not (&1)
    union 	
    select 'drop trigger ' || OBJECT_NAME  text
      from user_objects 
     where object_type = 'TRIGGER'
       and not (&2)
   union
    select 'drop view ' || OBJECT_NAME  text
      from user_objects 
     where object_type = 'VIEW'
       and not (&3)
   union
    select 'drop function ' || OBJECT_NAME text
      from user_objects
    where object_type = 'FUNCTION'
       and not (&4)
    union
    select 'drop procedure ' || OBJECT_NAME text
      from user_objects
    where object_type = 'PROCEDURE'
       and not (&5)
    )
     loop
    execute immediate cur_sql.text;
  end loop;

end;
/
