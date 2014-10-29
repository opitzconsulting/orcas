create or replace package pa_orcas_ddl_call authid current_user is

procedure update_schema( p_model in ot_orig_model );

end;
/
