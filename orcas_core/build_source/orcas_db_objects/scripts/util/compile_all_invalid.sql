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
        if('&1' != 'false') 
        then
          execute immediate 'begin &1..pa_orcas_exec_log.log_exec_stmt(''' || cur_sql.text || '''); end;';
        end if;
      exception
        when others then
          dbms_output.put_line( 'Error : ' || cur_sql.text || ' Message: ' || sqlerrm );
      end;
    end loop;
  
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
      if('&1' != 'false') 
      then
        execute immediate 'begin &1..pa_orcas_exec_log.log_exec_stmt(''' || v_java_src || '''); end;';
      end if;
      fetch v_cur into v_java_src;
    end loop;
    
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

set pagesize 0
set linesize 2000

select name,
       type,
       line,
       position,
       text,
       (
       select trim(text)
         from user_source
        where user_source.name = user_errors.name
          and user_source.type = user_errors.type
          and user_source.line = user_errors.line
       ) as line_text
  from user_errors
 order by 1, 3, 4;


select substr(object_name,1,30) object_name, 
       object_type 
  from user_objects 
 where status = 'INVALID';

begin
  for cur_dummy in
  (
    select 1
      from user_objects 
     where status = 'INVALID'    
  )
  loop
    raise_application_error( -20000, 'compile errors' );
  end loop;
end;
/

