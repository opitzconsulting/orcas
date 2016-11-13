create or replace package body pa_02_apply_tab_domain is
  pv_syex_model ot_syex_model;
  pv_history_table_list ct_syex_table_list;
  
  function get_domain( p_domain_name in varchar2 ) return ot_syex_domain
  is
  begin
    for i in 1..pv_syex_model.i_model_elements.count
    loop
      if( pv_syex_model.i_model_elements(i) is of (ot_syex_domain) )
      then
        if( upper( treat( pv_syex_model.i_model_elements(i) as ot_syex_domain ).i_name ) = upper( p_domain_name ) )
        then
          return treat( pv_syex_model.i_model_elements(i) as ot_syex_domain );
        end if;
      end if;
    end loop;

    raise_application_error( -20000, 'domain not found: ' || p_domain_name );
  end;
  
  function get_column_domain( p_domain_name in varchar2 ) return ot_syex_columndomain
  is
  begin
    for i in 1..pv_syex_model.i_model_elements.count
    loop
      if( pv_syex_model.i_model_elements(i) is of (ot_syex_columndomain) )
      then
        if( treat( pv_syex_model.i_model_elements(i) as ot_syex_columndomain ).i_name = p_domain_name )
        then
          return treat( pv_syex_model.i_model_elements(i) as ot_syex_columndomain );
        end if;
      end if;
    end loop;

    raise_application_error( -20000, 'domain not found: ' || p_domain_name );
  end;    
  
  procedure apply_domain( p_syex_table in out nocopy ot_syex_table, p_syex_domain in ot_syex_domain )
  is
    v_syex_domaincolumn ot_syex_domaincolumn;
    v_original_table_column_count number;    
    v_move_column_count number;
    v_append_first_index number;
    v_append_last_index number;
  begin
    if( p_syex_domain.i_columns is not null )
    then
      v_original_table_column_count := p_syex_table.i_columns.count;
    
      p_syex_table.i_columns.extend( p_syex_domain.i_columns.count );

      v_move_column_count := 0;
      
      for i in 1..p_syex_domain.i_columns.count
      loop
        v_syex_domaincolumn := p_syex_domain.i_columns(i);
        
        if( v_syex_domaincolumn.i_append_last_flg = 0 )
        then
          v_move_column_count := v_move_column_count + 1;
        end if;
      end loop;
      
      for i in 1..v_original_table_column_count
      loop
        p_syex_table.i_columns( v_original_table_column_count + v_move_column_count + 1 - i ) := p_syex_table.i_columns( v_original_table_column_count + 1 - i );
      end loop;
      
      v_append_first_index := 1;
      v_append_last_index := v_original_table_column_count + 1 + v_move_column_count;          
      for i in 1..p_syex_domain.i_columns.count
      loop
        v_syex_domaincolumn := p_syex_domain.i_columns(i);
        
        v_syex_domaincolumn.i_column.i_name := pa_domain_extension_helper.get_generated_name_column( v_syex_domaincolumn.i_columnnamerules, v_syex_domaincolumn.i_column.i_name, p_syex_table.i_name, p_syex_table.i_alias );        
        
        if( v_syex_domaincolumn.i_append_last_flg = 0 )
        then
          p_syex_table.i_columns(v_append_first_index) := v_syex_domaincolumn.i_column;
          v_append_first_index := v_append_first_index + 1;
        else
          p_syex_table.i_columns(v_append_last_index) := v_syex_domaincolumn.i_column;        
          v_append_last_index := v_append_last_index + 1;
        end if;
      end loop;
    end if;
  end;

  procedure handle_table( p_syex_table in out nocopy ot_syex_table )
  is
    v_syex_domain ot_syex_domain;
    v_syex_columndomain ot_syex_columndomain;
    v_history_table ot_syex_table;
    
    function is_pk_column( p_colum_name in varchar2  ) return number
    is
    begin
      if( p_syex_table.i_primary_key is not null )
      then
        for i in 1..p_syex_table.i_primary_key.i_pk_columns.count
        loop
          if( pa_domain_extension_helper.is_equal_ignore_case( p_syex_table.i_primary_key.i_pk_columns(i).i_column_name, p_colum_name ) = 1 )
          then
            return 1;
          end if;
        end loop;
      end if;
    
      return 0;
    end;
  begin
    if( p_syex_table.i_domain is not null )
    then
      v_syex_domain := get_domain( p_syex_table.i_domain );
      
      apply_domain( p_syex_table, v_syex_domain );
    
      if( v_syex_domain.i_historytable is not null )
      then
        v_history_table := new ot_syex_table();
        
        v_history_table.i_name := pa_domain_extension_helper.get_generated_name_table( v_syex_domain.i_historytable.i_tablenamerules, p_syex_table.i_name, p_syex_table.i_alias );
        v_history_table.i_alias := pa_domain_extension_helper.get_generated_name_table( v_syex_domain.i_historytable.i_aliasnamerules, p_syex_table.i_name, p_syex_table.i_alias );        
        v_history_table.i_columns := p_syex_table.i_columns;
        
        for i in 1..v_history_table.i_columns.count
        loop
          if( is_pk_column( v_history_table.i_columns(i).i_name ) = 0 )
          then        
            v_history_table.i_columns(i).i_notnull_flg := 0;
          end if;
          v_history_table.i_columns(i).i_default_value := null;          
          
          if( v_history_table.i_columns(i).i_domain is not null )
          then
            v_syex_columndomain := get_column_domain( v_history_table.i_columns(i).i_domain );
            
            v_history_table.i_columns(i).i_data_type := v_syex_columndomain.i_data_type;
            v_history_table.i_columns(i).i_precision := v_syex_columndomain.i_precision;
            v_history_table.i_columns(i).i_scale := v_syex_columndomain.i_scale;        
            v_history_table.i_columns(i).i_byteorchar := v_syex_columndomain.i_byteorchar;        
              
            if( v_syex_columndomain.i_generatepk is null )
            then                            
              v_history_table.i_columns(i).i_domain := null;            
            else
              if( v_syex_domain.i_historytable.i_appendtopkdomain is null )
              then
                v_history_table.i_columns(i).i_domain := null;                          
              else
                v_history_table.i_columns(i).i_domain := v_syex_domain.i_historytable.i_appendtopkdomain;                            
              end if;
            end if;
          end if;
        end loop;
        
        if( v_syex_domain.i_historytable.i_domain is not null )
        then
          apply_domain( v_history_table, get_domain( v_syex_domain.i_historytable.i_domain ) );
        end if;
        
        pv_history_table_list.extend;
        pv_history_table_list(pv_history_table_list.count) := v_history_table;
      end if;
    end if;      
  end;  

  function run( p_input in ot_syex_model ) return ot_syex_model
  is
    v_input ot_syex_model := p_input;
    v_syex_table ot_syex_table;
  begin
    pv_syex_model := p_input;
    pv_history_table_list := new ct_syex_table_list();
  
    for i in 1..v_input.i_model_elements.count
    loop
      if( v_input.i_model_elements(i) is of (ot_syex_table) )
      then
        v_syex_table := treat( v_input.i_model_elements(i) as ot_syex_table );

        handle_table( v_syex_table );

        v_input.i_model_elements(i) := v_syex_table;
      end if;
    end loop;
    
    for i in 1..pv_history_table_list.count
    loop
      v_input.i_model_elements.extend;
      v_input.i_model_elements(v_input.i_model_elements.count) := pv_history_table_list(i);
    end loop;
    
    return v_input;
  end;
end;
/
