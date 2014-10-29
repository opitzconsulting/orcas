create or replace package body pa_orcas_ddl_call is

  procedure update_schema( p_model in ot_orig_model ) is
  begin
    delete table_script_source;
    commit;
  
    if( pa_orcas_run_parameter.is_createmissingfkindexes = 1 )
    then    
      pa_orcas_compare.compare_and_update( pa_orcas_load_ist.get_ist(), pa_orcas_add_fk_idx_extension.run( p_model ) );
    else
      pa_orcas_compare.compare_and_update( pa_orcas_load_ist.get_ist(), p_model );    
    end if;

    commit;
  end;
end;
/
