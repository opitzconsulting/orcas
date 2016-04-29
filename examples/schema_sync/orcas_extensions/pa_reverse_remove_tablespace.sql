create or replace package body pa_reverse_22_remove_defaults is
  pv_default_chartype ot_syex_chartype;
  pv_default_tablespace varchar2(100);

  function hanlde_table( p_input in ot_syex_table ) return ot_syex_table
  is
    v_table ot_syex_table;  
    v_index ot_syex_index;
    v_uniquekey ot_syex_uniquekey;
  begin
    v_table := p_input;

    if( v_table.i_primary_key is not null )
    then
      v_table.i_primary_key.i_tablespace := null;
    end if;
    
    if( v_table.i_ind_uks is not null )
    then
      for i in 1..v_table.i_ind_uks.count
      loop
        if( v_table.i_ind_uks(i) is of (ot_syex_index) ) 
        then
          v_index := treat( v_table.i_ind_uks(i) as ot_syex_index );
          
          v_index.i_tablespace := null;
          v_table.i_ind_uks(i) := v_index;
        else
          v_uniquekey := treat( v_table.i_ind_uks(i) as ot_syex_uniquekey );        
          
          v_uniquekey.i_tablespace := null;
          v_table.i_ind_uks(i) := v_uniquekey;
        end if;
      end loop;    
    end if;      

    v_table.i_tablespace := null;
    
    return v_table;
  end;

  function run( p_input in ot_syex_model ) return ot_syex_model
  is
    v_input ot_syex_model;  
    
    v_nls_length_default varchar2(100);    
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
