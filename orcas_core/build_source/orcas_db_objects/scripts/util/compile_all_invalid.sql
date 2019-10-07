set serveroutput on
set verify off

declare
  v_java_src varchar2(200);
  TYPE t_cur IS REF CURSOR;
  v_cur    t_cur;
  v_invalid_count   number;
  v_invalid_count_new   number;
begin
  loop
    select count(1)
      into v_invalid_count
      from user_objects 
     where status = 'INVALID';

    for cur_sql in
    (
      select 'alter package ' || OBJECT_NAME || ' compile' text
        from user_objects 
       where status = 'INVALID' 
         and object_type = 'PACKAGE'
       union all
      select 'alter package ' || OBJECT_NAME || ' compile body' text
        from user_objects 
       where status = 'INVALID' 
         and object_type = 'PACKAGE BODY'
       union all
      select 'alter type ' || OBJECT_NAME || ' compile' text
        from user_objects 
       where status = 'INVALID' 
         and object_type = 'TYPE'
       union all
      select 'alter type ' || OBJECT_NAME || ' compile body' text
        from user_objects 
       where status = 'INVALID' 
         and object_type = 'TYPE BODY'
       union all
      select 'alter '||OBJECT_TYPE || ' ' || OBJECT_NAME || ' compile' text
        from user_objects 
       where status = 'INVALID' 
         and object_type in ('PROCEDURE', 'FUNCTION')
       union all
      select 'alter '||OBJECT_TYPE || ' ' || OBJECT_NAME || ' compile' text
        from user_objects 
       where status = 'INVALID' 
         and object_type in ('VIEW','TRIGGER','SYNONYM')
      
    ) loop
      begin
        execute immediate cur_sql.text;
      exception
        when others then
          null;
      end;
    end loop;

    if('&1' = 'compile_java') then
      open v_cur for
        select 'alter java source "' || long_name || '" compile' text
          from sys.ku$_java_source_view jsv,
               user_objects uo
         where jsv.obj_num = uo.object_id
           and status = 'INVALID'
           and object_type in ('JAVA SOURCE');

      fetch v_cur into v_java_src;

      while v_cur%found
      loop
        execute immediate v_java_src;
        fetch v_cur into v_java_src;
      end loop;
    end if;
    
    select count(1)
      into v_invalid_count_new
      from user_objects 
     where status = 'INVALID';

    if( v_invalid_count = v_invalid_count_new )
    then
      exit;
    end if;

  end loop;
end;
/
