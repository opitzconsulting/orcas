CREATE OR REPLACE package body pa_orcas_exec_log is

procedure log_exec_stmt(pi_stmt in varchar2)
is
  v_stmt varchar2(32000);
  c_max_line_length number := 2000;  
  v_last_space_index number(10);
  
  procedure write_to_table
  is
  begin
    insert into orcas_table_script_source(SCRIPT,id) values(v_stmt, nvl((select count(1) from orcas_table_script_source),0));    
  end;
begin
  if(length(pi_stmt)>c_max_line_length)
  then
    v_stmt := substr(pi_stmt,1,c_max_line_length);
    
    v_last_space_index := instr(v_stmt,' ',-1);
    
    v_stmt := substr(pi_stmt,1,v_last_space_index);
    
    write_to_table();
    
    log_exec_stmt(substr(pi_stmt,v_last_space_index));
    
    return;
  end if;

  --Logging der "echten" Tabellenscripte in die Datenbank
  if (substr(pi_stmt, -1, 1) = ';')
  then
    if(instr(upper(pi_stmt),'END;',length(pi_stmt)-4,1)!=0) then    
      v_stmt := pi_stmt|| '
      /';
    else
      v_stmt := pi_stmt;
    end if;
  else
    if(instr(upper(pi_stmt),'END',length(pi_stmt)-4,1)!=0) then
      v_stmt := pi_stmt||';
      /';
    else
      v_stmt := pi_stmt||';';
    end if;
  end if;
  
  write_to_table();
end;

procedure exec_stmt (pi_stmt in varchar2, p_keep_exception in number default 0)
is
begin
  if( pa_orcas_run_parameter.is_logonly() = 0 )
  then
    if( p_keep_exception = 0 )
    then
      begin
        execute immediate pi_stmt;
      exception
        when others
          then raise_application_error( -20000, sqlerrm||' '||pi_stmt);
      end;
    else
      execute immediate pi_stmt;
    end if;    
  end if;    
        
  log_exec_stmt( pi_stmt );
end;

procedure log_success (pi_message in varchar2)
is
begin
    dbms_output.put_line('SUCCEED: '||pi_message);
end;

procedure log_error (pi_message in varchar2)
is
begin
  raise_application_error( -20000, pi_message);
end;

end;
/
