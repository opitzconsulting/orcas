
declare
  v_syex_model ot_syex_model;
  v_orig_model ot_orig_model;
begin
  pa_orcas_xtext_model.build();
  v_syex_model := pa_orcas_extensions.call_extensions( pa_orcas_model_holder.get_model() );
          
  v_orig_model := pa_orcas_trans_syex_orig.trans_syex_orig( v_syex_model ); 
  pa_orcas_ddl_call.update_schema( v_orig_model );
end;
/
          
