create or replace package body pa_orcas_add_fk_idx_extension is
  type t_index_map is table of varchar2(1000) index by varchar2(1000);

  function run( p_input in ot_orig_model ) return ot_orig_model
  is
    v_input ot_orig_model := p_input;
    v_orig_table ot_orig_table;
    v_orig_index ot_orig_index;
    v_orig_uniquekey ot_orig_uniquekey;
    v_orig_indexextable ot_orig_indexextable;    
    v_index_map t_index_map;
    v_first_map t_index_map;
    v_index_key varchar2(1000);
    c_index_post_fix varchar2(10) := '_GEN_IX';
    
    procedure add_to_index( p_index_key varchar2 )
    is
    begin
      if( p_index_key is not null )
      then      
        v_index_map( p_index_key ) := p_index_key;
        if instr(p_index_key, ',') > 1 then
            v_first_map( substr(p_index_key, 0, instr(p_index_key, ',') -1 ) ) := p_index_key;
        end if;
      end if;
    end;      
    
    function to_index_key( p_table_name varchar2, p_orig_columnref_list ct_orig_columnref_list ) return varchar2
    is
    begin
      return upper( p_table_name || ' cols ' || pa_orcas_compare.get_column_list( p_orig_columnref_list ) );
    end;     
    
    function to_index_key( p_orig_index ot_orig_index, p_orig_table ot_orig_table ) return varchar2
    is
    begin
      if( p_orig_index.i_function_based_expression is not null )
      then
        return null;
      end if;    
    
      return to_index_key( p_orig_table.i_name, p_orig_index.i_index_columns );
    end;        
    
    function to_index_key( p_orig_indexextable ot_orig_indexextable ) return varchar2
    is
    begin
      if( p_orig_indexextable.i_function_based_expression is not null )
      then
        return null;
      end if;
    
      return to_index_key( p_orig_indexextable.i_table_name, p_orig_indexextable.i_index_columns );
    end;    
    
    function to_index_key( p_orig_uniquekey ot_orig_uniquekey, p_orig_table ot_orig_table ) return varchar2
    is
    begin   
      return to_index_key( p_orig_table.i_name, p_orig_uniquekey.i_uk_columns );
    end;        
    
    function to_index_key( p_orig_foreignkey ot_orig_foreignkey, p_orig_table ot_orig_table ) return varchar2
    is
    begin
      return to_index_key( p_orig_table.i_name, p_orig_foreignkey.i_srccolumns );
    end;    
  begin   
    for i in 1..v_input.i_model_elements.count
    loop
      if( v_input.i_model_elements(i) is of (ot_orig_table) )
      then
        v_orig_table := treat( v_input.i_model_elements(i) as ot_orig_table );
        
        if( v_orig_table.i_primary_key is not null )
        then
          add_to_index( to_index_key( v_orig_table.i_name, v_orig_table.i_primary_key.i_pk_columns ) );
        end if;
        
        if( v_orig_table.i_ind_uks is not null )
        then
          for j in 1..v_orig_table.i_ind_uks.count
          loop
            if( v_orig_table.i_ind_uks(j) is of (ot_orig_index) )
            then
              v_orig_index := treat( v_orig_table.i_ind_uks(j) as ot_orig_index );
              
              add_to_index( to_index_key( v_orig_index, v_orig_table ) );
            end if;
            if( v_orig_table.i_ind_uks(j) is of (ot_orig_uniquekey) )
            then
              v_orig_uniquekey := treat( v_orig_table.i_ind_uks(j) as ot_orig_uniquekey );
              
              add_to_index( to_index_key( v_orig_uniquekey, v_orig_table ) );
            end if;
          end loop;
        end if;
      end if;
      if( v_input.i_model_elements(i) is of (ot_orig_indexextable) )
      then
        v_orig_indexextable := treat( v_input.i_model_elements(i) as ot_orig_indexextable );
        
        add_to_index( to_index_key( v_orig_indexextable ) );
      end if;      
    end loop;
    
    for i in 1..v_input.i_model_elements.count
    loop
      if( v_input.i_model_elements(i) is of (ot_orig_table) )
      then
        v_orig_table := treat( v_input.i_model_elements(i) as ot_orig_table );
        
        if( v_orig_table.i_foreign_keys is not null )
        then
          for j in 1..v_orig_table.i_foreign_keys.count
          loop
            v_index_key := to_index_key( v_orig_table.i_foreign_keys(j), v_orig_table );
            
            if( not v_index_map.exists( v_index_key ) and not v_first_map.exists( v_index_key ) )
            then
              v_orig_indexextable := new ot_orig_indexextable();
              
              v_orig_indexextable.i_table_name := v_orig_table.i_name;
              v_orig_indexextable.i_index_name := substr( v_orig_table.i_foreign_keys(j).i_consname, 1, 30-length(c_index_post_fix) ) || c_index_post_fix;
              v_orig_indexextable.i_index_columns := v_orig_table.i_foreign_keys(j).i_srccolumns;
                            
              v_input.i_model_elements.extend;
              v_input.i_model_elements( v_input.i_model_elements.count ) := v_orig_indexextable;
              
              add_to_index( to_index_key( v_orig_indexextable ) );
            end if;
          end loop;
        end if;
      end if;
    end loop;    
  
    return v_input;
  end;
end;
/
