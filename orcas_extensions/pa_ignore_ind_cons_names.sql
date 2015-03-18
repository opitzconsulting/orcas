create or replace package body pa_ignore_ind_cons_names is
  type t_varchar_varchar_map is table of varchar2(1000) index by varchar2(1000);
  pv_index_key_to_name_map t_varchar_varchar_map;
  pv_uk_key_to_name_map t_varchar_varchar_map;  
  pv_fk_key_to_name_map t_varchar_varchar_map;  
  pv_ck_key_to_name_map t_varchar_varchar_map;    
  
  function get_ck_key( p_syex_constraint in ot_syex_constraint, p_syex_table in ot_syex_table ) return varchar2 is
    v_return varchar2(1000);
  begin
    v_return := p_syex_table.i_name || ';' || p_syex_constraint.i_rule;
    
    return upper(v_return);
  end;    

  function get_index_key( p_syex_index in ot_syex_index, p_syex_table in ot_syex_table ) return varchar2 is
    v_return varchar2(1000);
  begin
    v_return := p_syex_table.i_name;
   
    for i in 1..p_syex_index.i_index_columns.count
    loop
      v_return := v_return || ';' || p_syex_index.i_index_columns(i).i_column_name;
    end loop;
    
    return upper(v_return);
  end;  

  function get_fk_key( p_syex_foreignkey in ot_syex_foreignkey, p_syex_table in ot_syex_table ) return varchar2 is
    v_return varchar2(1000);
  begin
    v_return := p_syex_table.i_name;
    
    v_return := v_return || ';' || p_syex_foreignkey.i_desttable;
   
    for i in 1..p_syex_foreignkey.i_srccolumns.count
    loop
      v_return := v_return || ';' || p_syex_foreignkey.i_srccolumns(i).i_column_name;
    end loop;
    
    return upper(v_return);
  end;    

  function get_uk_key( p_syex_uniquekey in ot_syex_uniquekey, p_syex_table in ot_syex_table ) return varchar2 is
    v_return varchar2(1000);
  begin
    v_return := p_syex_table.i_name;
    
    for i in 1..p_syex_uniquekey.i_uk_columns.count
    loop
      v_return := v_return || ';' || p_syex_uniquekey.i_uk_columns(i).i_column_name;
    end loop;
    
    return upper(v_return);
  end;  
  
  procedure handle_table( p_syex_table in out nocopy ot_syex_table )
  is
    v_syex_index ot_syex_index;
    v_syex_uniquekey ot_syex_uniquekey;    
    v_key varchar2(1000);
  begin    
    if( p_syex_table.i_constraints is not null )
    then
      for i in 1..p_syex_table.i_constraints.count
      loop
        v_key := get_ck_key( p_syex_table.i_constraints(i), p_syex_table );
        
        if( pv_ck_key_to_name_map.exists( v_key ) )
        then
          p_syex_table.i_constraints(i).i_consname := pv_ck_key_to_name_map( v_key );
        end if;
      end loop;
    end if;        
  
    if( p_syex_table.i_ind_uks is not null )
    then
      for i in 1..p_syex_table.i_ind_uks.count
      loop
        if( p_syex_table.i_ind_uks(i) is of (ot_syex_index) )
        then      
          v_syex_index := treat( p_syex_table.i_ind_uks(i) as ot_syex_index );
        
          v_key := get_index_key( v_syex_index, p_syex_table );
        
          if( pv_index_key_to_name_map.exists( v_key ) )
          then
            v_syex_index.i_consname := pv_index_key_to_name_map( v_key );
            p_syex_table.i_ind_uks(i) := v_syex_index;
          end if;
        else
          v_syex_uniquekey := treat( p_syex_table.i_ind_uks(i) as ot_syex_uniquekey );
        
          v_key := get_uk_key( v_syex_uniquekey, p_syex_table );
        
          if( pv_uk_key_to_name_map.exists( v_key ) )
          then
            v_syex_uniquekey.i_consname := pv_uk_key_to_name_map( v_key );
            p_syex_table.i_ind_uks(i) := v_syex_uniquekey;
          end if;          
        end if;          
      end loop;
    end if;          
    
    if( p_syex_table.i_foreign_keys is not null )
    then
      for i in 1..p_syex_table.i_foreign_keys.count
      loop
        v_key := get_fk_key( p_syex_table.i_foreign_keys(i), p_syex_table );
        
        if( pv_fk_key_to_name_map.exists( v_key ) )
        then
          p_syex_table.i_foreign_keys(i).i_consname := pv_fk_key_to_name_map( v_key );
        end if;
      end loop;
    end if;            
  end;  

  function run( p_input in ot_syex_model ) return ot_syex_model
  is
    v_input ot_syex_model := p_input;
    v_syex_table ot_syex_table;
    v_syex_index ot_syex_index;
    v_syex_uniquekey ot_syex_uniquekey;    
    v_syex_model_ist ot_syex_model;
  begin   
    v_syex_model_ist := pa_orcas_trans_orig_syex.trans_orig_syex( pa_orcas_load_ist.get_ist() );
    
    for i in 1..v_syex_model_ist.i_model_elements.count
    loop
      if( v_syex_model_ist.i_model_elements(i) is of (ot_syex_table) )
      then
        v_syex_table := treat( v_syex_model_ist.i_model_elements(i) as ot_syex_table );        

        if( v_syex_table.i_constraints is not null )
        then
          for j in 1..v_syex_table.i_constraints.count
          loop
            pv_ck_key_to_name_map( get_ck_key( v_syex_table.i_constraints(j), v_syex_table ) ) := v_syex_table.i_constraints(j).i_consname;
          end loop;
        end if;        

        if( v_syex_table.i_ind_uks is not null )
        then
          for j in 1..v_syex_table.i_ind_uks.count
          loop
            if( v_syex_table.i_ind_uks(j) is of (ot_syex_index) )
            then
              v_syex_index := treat( v_syex_table.i_ind_uks(j) as ot_syex_index );
            
              pv_index_key_to_name_map( get_index_key( v_syex_index, v_syex_table ) ) := v_syex_index.i_consname;
            else
              v_syex_uniquekey := treat( v_syex_table.i_ind_uks(j) as ot_syex_uniquekey );
        
              pv_index_key_to_name_map( get_uk_key( v_syex_uniquekey, v_syex_table ) ) := v_syex_index.i_consname;
            end if;
          end loop;
        end if;          

        if( v_syex_table.i_foreign_keys is not null )
        then
          for j in 1..v_syex_table.i_foreign_keys.count
          loop
            pv_fk_key_to_name_map( get_fk_key( v_syex_table.i_foreign_keys(j), v_syex_table ) ) := v_syex_table.i_foreign_keys(j).i_consname;
          end loop;
        end if;         
      end if;
    end loop;    
  
    for i in 1..v_input.i_model_elements.count
    loop
      if( v_input.i_model_elements(i) is of (ot_syex_table) )
      then
        v_syex_table := treat( v_input.i_model_elements(i) as ot_syex_table );
        
        handle_table( v_syex_table );
        
        v_input.i_model_elements(i) := v_syex_table;
      end if;
    end loop;
  
    return v_input;
  end;
end;
/
