create or replace package body pa_reverse_13_tab_domain is
  pv_syex_domain_list ct_syex_domain_list;  
  pv_table_name_set pa_domain_extension_helper.t_varchar_set;    
  
  function get_column_index( p_table in ot_syex_table, p_domaincolumn in ot_syex_domaincolumn ) return number
  is
    v_columnname varchar2(100);
  begin
    v_columnname := pa_domain_extension_helper.get_generated_name_column( p_domaincolumn.i_columnnamerules, p_domaincolumn.i_column.i_name, p_table.i_name, p_table.i_alias );
    
    for i in 1..p_table.i_columns.count
    loop
      if( pa_domain_extension_helper.is_equal_ignore_case( p_table.i_columns(i).i_name, v_columnname ) = 1 )
      then
        return i;
      end if;
    end loop;
  
    return null;
  end;
  
  /**
   * Gibt null zurueck, wenn die domain gar nicht passt, ansonsten eine Zahl um so groesser je besser die domain passt.
   */  
  function rate_domain( p_domain_index in number, p_table in ot_syex_table ) return number
  is
    v_domain ot_syex_domain;
    v_domaincolumn ot_syex_domaincolumn;
    v_return number;
    v_history_table_name varchar2(100);
  begin
    v_domain := pv_syex_domain_list(p_domain_index);
    v_return := 0;
    
    for i in 1..v_domain.i_columns.count
    loop
      v_domaincolumn := v_domain.i_columns(i);
      
      if( get_column_index( p_table, v_domaincolumn ) is null)
      then
        return null;
      end if;
      
      v_return := v_return + 1;
    end loop;
    
    if( v_domain.i_historytable is not null )
    then
      v_history_table_name := pa_domain_extension_helper.get_generated_name_table( v_domain.i_historytable.i_tablenamerules, p_table.i_name, p_table.i_alias ); 
      if( not pv_table_name_set.exists( upper(v_history_table_name) ) )
      then
        return null;
      end if;
      
      v_return := v_return + 10;
    end if;
    
    return v_return;
  end;
  
  function find_best_match_domain( p_table in ot_syex_table ) return number
  is
    v_return number;
    v_best_rating number;
    v_rating number;
  begin
    for i in 1..pv_syex_domain_list.count
    loop
      v_rating := rate_domain( i, p_table );
      
      if( v_rating is not null )
      then
        if( v_best_rating is null or v_rating > v_best_rating )
        then
          v_best_rating := v_rating;
          v_return := i;
        end if;
      end if;
    end loop;
    
    return v_return;
  end;  
  
  function reverse_apply_domain( p_domain_index in number, p_table in ot_syex_table ) return ot_syex_table
  is
    v_table ot_syex_table;
    v_domain ot_syex_domain;
    v_domaincolumn ot_syex_domaincolumn;
    v_new_column_list ct_syex_column_list;
  begin
    v_table := p_table;
    v_domain := pv_syex_domain_list(p_domain_index);
    
    v_table.i_domain := v_domain.i_name;
    
    for i in 1..v_domain.i_columns.count
    loop
      v_domaincolumn := v_domain.i_columns(i);    
      
      v_table.i_columns( get_column_index( v_table, v_domaincolumn ) ) := null;
    end loop;    
    
    v_new_column_list := new ct_syex_column_list();
    for i in 1..v_table.i_columns.count
    loop
      if( v_table.i_columns(i) is not null )
      then
        v_new_column_list.extend;
        v_new_column_list(v_new_column_list.count) := v_table.i_columns(i);
      end if;
    end loop;
    
    v_table.i_columns := v_new_column_list;
    
    if( v_domain.i_historytable is not null )
    then
      pv_table_name_set.delete( upper( pa_domain_extension_helper.get_generated_name_table( v_domain.i_historytable.i_tablenamerules, p_table.i_name, p_table.i_alias ) ) ); 
    end if;
    
    return v_table;
  end;  
  
  function hanlde_table( p_input in ot_syex_table ) return ot_syex_table
  is
    v_table ot_syex_table;  
    v_best_match_domain_idx number;
  begin
    v_table := p_input;
    
    v_best_match_domain_idx := find_best_match_domain( v_table );
      
    if( v_best_match_domain_idx is not null )      
    then
      v_table := reverse_apply_domain( v_best_match_domain_idx, v_table );
    end if;
  
    return v_table;
  end;

  function run( p_input in ot_syex_model ) return ot_syex_model
  is
    v_input ot_syex_model;  
    v_new_modelelement_list ct_syex_modelelement_list;    
    v_append number;    
    
    procedure build_pv
    is
    begin
      pv_syex_domain_list := new ct_syex_domain_list();
      
      for i in 1..v_input.i_model_elements.count
      loop
        if( v_input.i_model_elements(i) is of (ot_syex_domain) )
        then
          pv_syex_domain_list.extend;
          pv_syex_domain_list( pv_syex_domain_list.count ) := treat( v_input.i_model_elements(i) as ot_syex_domain );
        end if;
        
        if( v_input.i_model_elements(i) is of (ot_syex_table) )        
        then
          pv_table_name_set( upper( treat( v_input.i_model_elements(i) as ot_syex_table ).i_name) ) := 1;
        end if;
      end loop;
    end;
  begin
    v_input := p_input;
    
    build_pv();
  
    for i in 1..v_input.i_model_elements.count
    loop
      if( v_input.i_model_elements(i) is of (ot_syex_table) )
      then
        v_input.i_model_elements(i) := hanlde_table( treat( v_input.i_model_elements(i) as ot_syex_table ) );
      end if;
    end loop;
    
    v_new_modelelement_list := new ct_syex_modelelement_list();
    
    for i in 1..v_input.i_model_elements.count
    loop
      v_append := 1;
      if( v_input.i_model_elements(i) is of (ot_syex_table) )
      then        
        if( not pv_table_name_set.exists( upper( treat( v_input.i_model_elements(i) as ot_syex_table ).i_name ) ) )
        then
          v_append := 0;
        end if;
      end if;
      
      if( v_append = 1 )
      then
        v_new_modelelement_list.extend;
        v_new_modelelement_list(v_new_modelelement_list.count) := v_input.i_model_elements(i);
      end if;
    end loop;
    
    v_input.i_model_elements := v_new_modelelement_list;    
    
    return v_input;
  end;
end;
/
