
declare
  v_syex_model ot_syex_model;
  v_orig_model ot_orig_model;
begin
  pa_orcas_xtext_model.build();  
  v_syex_model := pa_orcas_extensions.call_extensions( pa_orcas_model_holder.get_model() );
          
  &1..run( v_syex_model ); 
end;
/
          
