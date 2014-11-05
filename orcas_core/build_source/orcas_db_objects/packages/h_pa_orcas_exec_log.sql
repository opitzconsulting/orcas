CREATE OR REPLACE package pa_orcas_exec_log authid current_user is

procedure log_exec_stmt(pi_stmt in varchar2);
procedure exec_stmt (pi_stmt in varchar2, p_keep_exception in number default 0);
procedure log_success (pi_message in varchar2);
procedure log_error (pi_message in varchar2);

end;
/
