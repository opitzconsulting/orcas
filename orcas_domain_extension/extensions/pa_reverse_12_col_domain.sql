create or replace package body pa_reverse_12_col_domain is
  pv_syex_columndomain_list ct_syex_columndomain_list;  
  type t_varchar_set is table of number index by varchar2(100);
  pv_remove_sequence_name_set t_varchar_set;  
  
  function find_uk( p_table in ot_syex_table, p_uk_name in varchar2 ) return ot_syex_uniquekey
  is
  begin
    if( p_table.i_ind_uks is null )
    then
      return null;
    end if;
  
    for i in 1..p_table.i_ind_uks.count
    loop
      if( pa_domain_extension_helper.is_equal_ignore_case( p_table.i_ind_uks(i).i_consname, p_uk_name ) = 1 )
      then
        return treat( p_table.i_ind_uks(i) as ot_syex_uniquekey);
      end if;
    end loop;
    
    return null;
  end;
  
  function find_cc( p_table in ot_syex_table, p_cc_name in varchar2 ) return ot_syex_constraint
  is
  begin
    if( p_table.i_constraints is null )
    then
      return null;
    end if;
  
    for i in 1..p_table.i_constraints.count
    loop
      if( pa_domain_extension_helper.is_equal_ignore_case( p_table.i_constraints(i).i_consname, p_cc_name ) = 1 )
      then
        return p_table.i_constraints(i);
      end if;
    end loop;
    
    return null;
  end;  
  
  function find_fk( p_table in ot_syex_table, p_fk_name in varchar2 ) return ot_syex_foreignkey
  is
  begin
    if( p_table.i_foreign_keys is null )
    then
      return null;
    end if;
  
    for i in 1..p_table.i_foreign_keys.count
    loop
      if( pa_domain_extension_helper.is_equal_ignore_case( p_table.i_foreign_keys(i).i_consname, p_fk_name ) = 1 )
      then
        return p_table.i_foreign_keys(i);
      end if;
    end loop;
    
    return null;
  end;  
  
  function remove_uk( p_syex_indexoruniquekey_list in ct_syex_indexoruniquekey_list, p_syex_uniquekey in ot_syex_uniquekey ) return ct_syex_indexoruniquekey_list
  is
    v_return ct_syex_indexoruniquekey_list;
  begin
    v_return := new ct_syex_indexoruniquekey_list();
    
    for i in 1..p_syex_indexoruniquekey_list.count
    loop
      if( pa_domain_extension_helper.is_equal_ignore_case( p_syex_indexoruniquekey_list(i).i_consname, p_syex_uniquekey.i_consname ) != 1 )
      then
        v_return.extend;
        v_return(v_return.count) := p_syex_indexoruniquekey_list(i);
      end if;
    end loop;
    
    return v_return;
  end;  
  
  function remove_cc( p_syex_constraint_list in ct_syex_constraint_list, p_syex_constraint in ot_syex_constraint ) return ct_syex_constraint_list
  is
    v_return ct_syex_constraint_list;
  begin
    v_return := new ct_syex_constraint_list();
    
    for i in 1..p_syex_constraint_list.count
    loop
      if( pa_domain_extension_helper.is_equal_ignore_case( p_syex_constraint_list(i).i_consname, p_syex_constraint.i_consname ) != 1 )
      then
        v_return.extend;
        v_return(v_return.count) := p_syex_constraint_list(i);
      end if;
    end loop;
    
    return v_return;
  end;  
  
  function remove_fk( p_syex_foreignkey_list in ct_syex_foreignkey_list, p_syex_foreignkey in ot_syex_foreignkey ) return ct_syex_foreignkey_list
  is
    v_return ct_syex_foreignkey_list;
  begin
    v_return := new ct_syex_foreignkey_list();
    
    for i in 1..p_syex_foreignkey_list.count
    loop
      if( pa_domain_extension_helper.is_equal_ignore_case( p_syex_foreignkey_list(i).i_consname, p_syex_foreignkey.i_consname ) != 1 )
      then
        v_return.extend;
        v_return(v_return.count) := p_syex_foreignkey_list(i);
      end if;
    end loop;
    
    return v_return;
  end;  
  
  /**
   * Gibt null zurueck, wenn die domain gar nicht passt, ansonsten eine Zahl um so groesser je besser die domain passt.
   */
  function rate_column_domain( p_column_domain_index in number, p_column in ot_syex_column, p_table in ot_syex_table ) return number
  is
    v_columndomain ot_syex_columndomain;
    v_return number;
    v_syex_uniquekey ot_syex_uniquekey;
    v_syex_constraint ot_syex_constraint;
    v_syex_foreignkey ot_syex_foreignkey;
  begin
    v_columndomain := pv_syex_columndomain_list(p_column_domain_index);
    
    if
    ( 
        ot_syex_datatype.is_equal(           v_columndomain.i_data_type,  p_column.i_data_type )   = 1 
    and pa_domain_extension_helper.is_equal( v_columndomain.i_precision,  p_column.i_precision )   = 1 
    and pa_domain_extension_helper.is_equal( v_columndomain.i_scale,      p_column.i_scale,       0                       ) = 1 
    and ot_syex_chartype.is_equal(           v_columndomain.i_byteorchar, p_column.i_byteorchar,  ot_syex_chartype.c_byte ) = 1   
    )
    then
      v_return := 0;
    
      if( v_columndomain.i_notnull is not null )
      then
        if( p_column.i_notnull is not null )
        then
          v_return := v_return + 1;
        else
          return null;        
        end if;
      end if;
      
      if( v_columndomain.i_default_value is not null )
      then
        if( p_column.i_default_value = v_columndomain.i_default_value )
        then
          v_return := v_return + 3;
        else
          return null;        
        end if;
      end if;      
      
      if( v_columndomain.i_generatepk is not null )
      then
        if( p_table.i_primary_key is null )
        then
          return null;
        end if;
        
        if( p_table.i_primary_key.i_consname != pa_domain_extension_helper.get_generated_name_column( v_columndomain.i_generatepk.i_constraintnamerules, p_column.i_name, p_table.i_name, p_table.i_alias) )
        then
          return null;
        end if;
        
        if( p_table.i_primary_key.i_pk_columns.count != 1 )
        then
          return null;
        end if;
        
        if( p_table.i_primary_key.i_pk_columns(1).i_column_name != p_column.i_name )
        then
          return null;
        end if;        
               
       /* if( v_syex_columndomain.i_generatepk.i_sequencenamerules is not null )
          then
            v_syex_sequence := new ot_syex_sequence();
          
            v_syex_sequence.i_sequence_name := pa_domain_extension_helper.get_generated_name( v_syex_columndomain.i_generatepk.i_sequencenamerules, v_syex_column.i_name, p_syex_table.i_alias);
            v_syex_sequence.i_max_value_select := 'select max(' || v_syex_column.i_name || ') from ' || p_syex_table.i_name;
          
            pv_syex_sequence_list.extend;
            pv_syex_sequence_list(pv_syex_sequence_list.count) := v_syex_sequence;          
            
                    v_return := v_return + 50;
          end if;*/
          
        v_return := v_return + 100;
      end if;      
      
      if( v_columndomain.i_generateuk is not null )
      then
        v_syex_uniquekey := find_uk( p_table, pa_domain_extension_helper.get_generated_name_column( v_columndomain.i_generateuk.i_constraintnamerules, p_column.i_name, p_table.i_name, p_table.i_alias) );
      
        if( v_syex_uniquekey is null )
        then
          return null;
        end if;
        
        if( v_syex_uniquekey.i_uk_columns.count != 1 )
        then
          return null;
        end if;
        
        if( v_syex_uniquekey.i_uk_columns(1).i_column_name != p_column.i_name )
        then
          return null;
        end if;
               
        v_return := v_return + 50;
      end if;
            
      if( v_columndomain.i_generatecc is not null )
      then
        v_syex_constraint := find_cc( p_table, pa_domain_extension_helper.get_generated_name_column( v_columndomain.i_generatecc.i_constraintnamerules, p_column.i_name, p_table.i_name, p_table.i_alias) );
      
        if( v_syex_constraint is null )
        then
          return null;
        end if;
        
        if( pa_domain_extension_helper.is_equal_ignore_case( v_syex_constraint.i_rule, pa_domain_extension_helper.get_generated_name_column( v_columndomain.i_generatecc.i_checkrulenamerules, p_column.i_name, p_table.i_name, p_table.i_alias) ) != 1 )
        then
          return null;
        end if;
        
        v_return := v_return + 40;
      end if;     
      
      if( v_columndomain.i_generatefk is not null )
      then
        v_syex_foreignkey := find_fk( p_table, pa_domain_extension_helper.get_generated_name_column( v_columndomain.i_generatefk.i_constraintnamerules, p_column.i_name, p_table.i_name, p_table.i_alias) );
      
        if( v_syex_foreignkey is null )
        then
          return null;
        end if;
        
        if( v_syex_foreignkey.i_srccolumns.count != 1 )
        then
          return null;
        end if;
        
        if( v_syex_foreignkey.i_srccolumns(1).i_column_name != p_column.i_name )
        then
          return null;
        end if;
        
        -- pruefung auf dest-columns und table erst mal weggelassen
               
        v_return := v_return + 80;
      end if;      
      
      return v_return;
    else
      return null;
    end if;
  end;
  
  function find_best_match_column_domain( p_column in ot_syex_column, p_table in ot_syex_table ) return number
  is
    v_return number;
    v_best_rating number;
    v_rating number;
  begin
    for i in 1..pv_syex_columndomain_list.count
    loop
      v_rating := rate_column_domain( i, p_column, p_table );
      
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
  
  function reverse_apply_column_domain( p_column_domain_index in number, p_column in ot_syex_column, p_table in out nocopy ot_syex_table ) return ot_syex_column
  is
    v_column ot_syex_column;
    v_columndomain ot_syex_columndomain;
  begin
    v_column := p_column;
    v_columndomain := pv_syex_columndomain_list(p_column_domain_index);
    
    v_column.i_domain := v_columndomain.i_name;
    v_column.i_data_type := null;
    v_column.i_precision := null;
    v_column.i_scale := null;    
    v_column.i_byteorchar := null;        
    
    if( v_columndomain.i_notnull is not null )
    then
      v_column.i_notnull := null;            
    end if;
    
    if( v_columndomain.i_default_value is not null )
    then
      v_column.i_default_value := null;            
    end if;    
    
    if( v_columndomain.i_generatepk is not null )
    then
      p_table.i_primary_key := null;
      
      if( v_columndomain.i_generatepk.i_sequencenamerules is not null )
      then
        pv_remove_sequence_name_set( upper( pa_domain_extension_helper.get_generated_name_column( v_columndomain.i_generatepk.i_sequencenamerules, p_column.i_name, p_table.i_name, p_table.i_alias) ) ) := 1;
      end if;
    end if;        
    
    if( v_columndomain.i_generateuk is not null )
    then
      p_table.i_ind_uks := remove_uk( p_table.i_ind_uks, find_uk( p_table, pa_domain_extension_helper.get_generated_name_column( v_columndomain.i_generateuk.i_constraintnamerules, p_column.i_name, p_table.i_name, p_table.i_alias) ) );
    end if;          
    
    if( v_columndomain.i_generatecc is not null )
    then
      p_table.i_constraints := remove_cc( p_table.i_constraints, find_cc( p_table, pa_domain_extension_helper.get_generated_name_column( v_columndomain.i_generatecc.i_constraintnamerules, p_column.i_name, p_table.i_name, p_table.i_alias) ) );
    end if;          
    
    if( v_columndomain.i_generatefk is not null )
    then
      p_table.i_foreign_keys := remove_fk( p_table.i_foreign_keys, find_fk( p_table, pa_domain_extension_helper.get_generated_name_column( v_columndomain.i_generatefk.i_constraintnamerules, p_column.i_name, p_table.i_name, p_table.i_alias) ) );
    end if;           
    
    return v_column;
  end;  
  
  function hanlde_table( p_input in ot_syex_table ) return ot_syex_table
  is
    v_table ot_syex_table;  
    v_column ot_syex_column;
    v_best_match_column_domain_idx number;
  begin
    v_table := p_input;
    
    for i in 1..v_table.i_columns.count
    loop
      v_column := v_table.i_columns(i);
      
      v_best_match_column_domain_idx := find_best_match_column_domain( v_column, v_table );
      
      if( v_best_match_column_domain_idx is not null )      
      then
        v_table.i_columns(i) := reverse_apply_column_domain( v_best_match_column_domain_idx, v_column, v_table );
      end if;
    end loop;
  
    return v_table;
  end;

  function run( p_input in ot_syex_model ) return ot_syex_model
  is
    v_input ot_syex_model;  
    
    v_new_modelelement_list ct_syex_modelelement_list;
    v_append number;
    v_sequence ot_syex_sequence;    
    
    procedure build_columndomain_list
    is
    begin
      pv_syex_columndomain_list := new ct_syex_columndomain_list();
      
      for i in 1..v_input.i_model_elements.count
      loop
        if( v_input.i_model_elements(i) is of (ot_syex_columndomain) )
        then
          pv_syex_columndomain_list.extend;
          pv_syex_columndomain_list( pv_syex_columndomain_list.count ) := treat( v_input.i_model_elements(i) as ot_syex_columndomain );
        end if;
      end loop;
    end;
  begin
    v_input := p_input;
    pv_remove_sequence_name_set.delete;
    
    build_columndomain_list();
  
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
      if( v_input.i_model_elements(i) is of (ot_syex_sequence) )
      then        
        v_sequence := treat( v_input.i_model_elements(i) as ot_syex_sequence );
        
        if( pv_remove_sequence_name_set.exists( upper( v_sequence.i_sequence_name ) ) )
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
