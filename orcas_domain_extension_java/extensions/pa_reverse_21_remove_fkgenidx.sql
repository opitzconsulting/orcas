create or replace package body pa_reverse_21_remove_fkgenidx is

  function hanlde_table( p_input in ot_syex_table ) return ot_syex_table
  is
    v_table ot_syex_table;  
    v_indexoruniquekey_list ct_syex_indexoruniquekey_list;
  begin
    v_table := p_input;

    if( v_table.i_ind_uks is not null )
    then
      v_indexoruniquekey_list := new ct_syex_indexoruniquekey_list();
      
      for i in 1..v_table.i_ind_uks.count
      loop
        if( not instr( v_table.i_ind_uks(i).i_consname, '_GEN_IX' ) > 1 )
        then
          v_indexoruniquekey_list.extend();
          v_indexoruniquekey_list(v_indexoruniquekey_list.count) := v_table.i_ind_uks(i);
        end if;
      end loop;
      
      v_table.i_ind_uks := v_indexoruniquekey_list;
    end if;
  
    return v_table;
  end;

  function run( p_input in ot_syex_model ) return ot_syex_model
  is
    v_input ot_syex_model;  
  begin
    v_input := p_input;
    
    for i in 1..v_input.i_model_elements.count
    loop
      if( v_input.i_model_elements(i) is of (ot_syex_table) )
      then
        v_input.i_model_elements(i) := hanlde_table( treat( v_input.i_model_elements(i) as ot_syex_table ) );
      end if;
    end loop;
    
    return v_input;
  end;
end;
/
