create or replace package body pa_reverse_01_add_domain_def is

  function run( p_input in ot_syex_model ) return ot_syex_model
  is
    v_last_model ot_syex_model;
    v_input ot_syex_model;
    v_syex_domain ot_syex_domain;
    v_syex_columndomain ot_syex_columndomain;
  begin
    v_input := p_input;
  
    pa_orcas_xtext_model.build();
    v_last_model := pa_orcas_model_holder.get_model();
  
    for i in 1..v_last_model.i_model_elements.count
    loop
      if( v_last_model.i_model_elements(i) is of (ot_syex_domain) )
      then
        v_syex_domain := treat( v_last_model.i_model_elements(i) as ot_syex_domain );
        
        for i in 1..v_syex_domain.i_columns.count
        loop
          if( v_syex_domain.i_columns(i).i_column.i_precision = 0 )
          then
            v_syex_domain.i_columns(i).i_column.i_precision := null;
          end if;
        end loop;
      
        v_input.i_model_elements.extend;        
        v_input.i_model_elements(v_input.i_model_elements.count) := v_syex_domain;
      end if;
      if( v_last_model.i_model_elements(i) is of (ot_syex_columndomain) )
      then
        v_syex_columndomain := treat( v_last_model.i_model_elements(i) as ot_syex_columndomain );
        
        if( v_syex_columndomain.i_precision = 0 )
        then
          v_syex_columndomain.i_precision := null;
        end if;        
      
        v_input.i_model_elements.extend;
        v_input.i_model_elements(v_input.i_model_elements.count) := v_syex_columndomain;
      end if;
    end loop;
    
    return v_input;
  end;
end;
/
