create or replace package body pa_reverse_02_find_alias is

  function run( p_input in ot_syex_model ) return ot_syex_model
  is
    v_input ot_syex_model;
    v_table ot_syex_table;
  begin
    v_input := p_input;
  
    for i in 1..v_input.i_model_elements.count
    loop
      if( v_input.i_model_elements(i) is of (ot_syex_table) )
      then
        v_table := treat( v_input.i_model_elements(i) as ot_syex_table );
        
        if( v_table.i_primary_key is not null )
        then
          v_table.i_alias := substr(v_table.i_primary_key.i_consname,1,length(v_table.i_primary_key.i_consname)-3);
        end if;
      
        v_input.i_model_elements(i) := v_table;
      end if;
    end loop;
    
    return v_input;
  end;
end;
/
