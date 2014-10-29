create or replace package body pa_03_apply_col_domain is
  pv_syex_model ot_syex_model;
  pv_syex_sequence_list ct_syex_sequence_list;
  pv_syex_table_fk_lookup_list ct_syex_table_list;
  
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
  
  procedure handle_table( p_syex_table in out nocopy ot_syex_table )
  is
    v_syex_column ot_syex_column;
    v_syex_columndomain ot_syex_columndomain;
    v_syex_sequence ot_syex_sequence;
    v_syex_primarykey ot_syex_primarykey;
    v_syex_uniquekey ot_syex_uniquekey;  
    v_syex_constraint ot_syex_constraint;
  begin
    for i in 1..p_syex_table.i_columns.count
    loop
      if( p_syex_table.i_columns(i).i_domain is not null )
      then
        v_syex_column := p_syex_table.i_columns(i);
        
        v_syex_columndomain := get_column_domain( v_syex_column.i_domain );
        
        v_syex_column.i_data_type := v_syex_columndomain.i_data_type;
        v_syex_column.i_precision := v_syex_columndomain.i_precision;
        v_syex_column.i_scale := v_syex_columndomain.i_scale;        
        v_syex_column.i_byteorchar := v_syex_columndomain.i_byteorchar;
        
        if( v_syex_columndomain.i_default_value is not null )
        then
          v_syex_column.i_default_value := v_syex_columndomain.i_default_value;        
        end if;
        
        if( v_syex_columndomain.i_notnull is not null )
        then
          v_syex_column.i_notnull := v_syex_columndomain.i_notnull;        
        end if;        
        
        p_syex_table.i_columns(i) := v_syex_column;
        
        if( v_syex_columndomain.i_generatepk is not null )
        then
          v_syex_primarykey := new ot_syex_primarykey();
        
          v_syex_primarykey.i_pk_columns := new ct_syex_columnref_list( ot_syex_columnref() );
          v_syex_primarykey.i_pk_columns(1).i_column_name := v_syex_column.i_name;
          v_syex_primarykey.i_consname := pa_domain_extension_helper.get_generated_name_column( v_syex_columndomain.i_generatepk.i_constraintnamerules, v_syex_column.i_name, p_syex_table.i_name, p_syex_table.i_alias);        
        
          if( p_syex_table.i_primary_key is null )
          then            
            p_syex_table.i_primary_key := v_syex_primarykey;
          else          
            if( p_syex_table.i_primary_key.i_consname != v_syex_primarykey.i_consname )
            then
              raise_application_error( -20000, 'pk setup invalid: ' || p_syex_table.i_primary_key.i_consname || ' ' || v_syex_primarykey.i_consname );
            end if;
            
            p_syex_table.i_primary_key.i_pk_columns.extend;
            p_syex_table.i_primary_key.i_pk_columns(p_syex_table.i_primary_key.i_pk_columns.count) := v_syex_primarykey.i_pk_columns(1);
          end if;
          
          if( v_syex_columndomain.i_generatepk.i_sequencenamerules is not null )
          then
            v_syex_sequence := new ot_syex_sequence();
          
            v_syex_sequence.i_sequence_name := pa_domain_extension_helper.get_generated_name_column( v_syex_columndomain.i_generatepk.i_sequencenamerules, v_syex_column.i_name, p_syex_table.i_name, p_syex_table.i_alias);
            v_syex_sequence.i_max_value_select := 'select max(' || v_syex_column.i_name || ') from ' || p_syex_table.i_name;
          
            pv_syex_sequence_list.extend;
            pv_syex_sequence_list(pv_syex_sequence_list.count) := v_syex_sequence;          
          end if;
        end if;
        
        if( v_syex_columndomain.i_generateuk is not null )
        then
          v_syex_uniquekey := new ot_syex_uniquekey();
        
          v_syex_uniquekey.i_uk_columns := new ct_syex_columnref_list( ot_syex_columnref() );
          v_syex_uniquekey.i_uk_columns(1).i_column_name := v_syex_column.i_name;
          v_syex_uniquekey.i_consname := pa_domain_extension_helper.get_generated_name_column( v_syex_columndomain.i_generateuk.i_constraintnamerules, v_syex_column.i_name, p_syex_table.i_name, p_syex_table.i_alias);                    

          if( p_syex_table.i_ind_uks is null )
          then
            p_syex_table.i_ind_uks := new ct_syex_indexoruniquekey_list();
          end if;
          p_syex_table.i_ind_uks.extend(1);
          p_syex_table.i_ind_uks(p_syex_table.i_ind_uks.count) := v_syex_uniquekey;
        end if;     
        
        if( v_syex_columndomain.i_generatecc is not null )
        then
          v_syex_constraint := new ot_syex_constraint();
        
          v_syex_constraint.i_consname := pa_domain_extension_helper.get_generated_name_column( v_syex_columndomain.i_generatecc.i_constraintnamerules, v_syex_column.i_name, p_syex_table.i_name, p_syex_table.i_alias);                    
          v_syex_constraint.i_rule := pa_domain_extension_helper.get_generated_name_column( v_syex_columndomain.i_generatecc.i_checkrulenamerules, v_syex_column.i_name, p_syex_table.i_name, p_syex_table.i_alias);                    

          if( p_syex_table.i_constraints is null )
          then
            p_syex_table.i_constraints := new ct_syex_constraint_list();
          end if;
          p_syex_table.i_constraints.extend(1);
          p_syex_table.i_constraints(p_syex_table.i_constraints.count) := v_syex_constraint;
        end if;          
        
      end if;
    end loop;
  end;
  
  procedure handle_table_fk( p_syex_table in out nocopy ot_syex_table )
  is
    v_syex_column ot_syex_column;
    v_syex_columndomain ot_syex_columndomain;
    v_syex_foreignkey ot_syex_foreignkey;  
    
    procedure lookup_table
    is
      v_match_pk_column_name varchar2(100);      
    begin
      for i in 1..pv_syex_table_fk_lookup_list.count
      loop
        if(pv_syex_table_fk_lookup_list(i).i_primary_key is not null)
        then
          v_match_pk_column_name := pa_domain_extension_helper.get_generated_name_column( v_syex_columndomain.i_generatefk.i_pkcolumnnamerules, v_syex_column.i_name, pv_syex_table_fk_lookup_list(i).i_name, pv_syex_table_fk_lookup_list(i).i_alias );
        
          for j in 1..pv_syex_table_fk_lookup_list(i).i_primary_key.i_pk_columns.count()
          loop
            if( v_match_pk_column_name = pv_syex_table_fk_lookup_list(i).i_primary_key.i_pk_columns(j).i_column_name )
            then
              v_syex_foreignkey.i_destcolumns := new ct_syex_columnref_list( pv_syex_table_fk_lookup_list(i).i_primary_key.i_pk_columns(j) );
              v_syex_foreignkey.i_desttable := pv_syex_table_fk_lookup_list(i).i_name;

              return;
            end if;
          end loop;
        end if;
      end loop;
      
      raise_application_error(-20000,'fk not found: '||p_syex_table.i_name||'.'||v_syex_column.i_name);
    end;
  begin
    for i in 1..p_syex_table.i_columns.count
    loop
      if( p_syex_table.i_columns(i).i_domain is not null )
      then
        v_syex_column := p_syex_table.i_columns(i);
        
        v_syex_columndomain := get_column_domain( v_syex_column.i_domain );
        
        if( v_syex_columndomain.i_generatefk is not null )
        then
          v_syex_foreignkey := new ot_syex_foreignkey();
        
          v_syex_foreignkey.i_srccolumns := new ct_syex_columnref_list( ot_syex_columnref() );
          v_syex_foreignkey.i_srccolumns(1).i_column_name := v_syex_column.i_name;
          v_syex_foreignkey.i_consname := pa_domain_extension_helper.get_generated_name_column( v_syex_columndomain.i_generatefk.i_constraintnamerules, v_syex_column.i_name, p_syex_table.i_name, p_syex_table.i_alias );                    
          v_syex_foreignkey.i_delete_rule := v_syex_columndomain.i_generatefk.i_delete_rule;
          lookup_table();

          if( p_syex_table.i_foreign_keys is null )
          then
            p_syex_table.i_foreign_keys := new ct_syex_foreignkey_list();
          end if;
          p_syex_table.i_foreign_keys.extend(1);
          p_syex_table.i_foreign_keys(p_syex_table.i_foreign_keys.count) := v_syex_foreignkey;
        end if;                  
      end if;
    end loop;
  end;  

  function run( p_input in ot_syex_model ) return ot_syex_model
  is
    v_input ot_syex_model := p_input;
    v_syex_table ot_syex_table;
  begin
    pv_syex_model := p_input;
    pv_syex_sequence_list := new ct_syex_sequence_list();
    pv_syex_table_fk_lookup_list := new ct_syex_table_list();
  
    for i in 1..v_input.i_model_elements.count
    loop
      if( v_input.i_model_elements(i) is of (ot_syex_table) )
      then
        v_syex_table := treat( v_input.i_model_elements(i) as ot_syex_table );

        handle_table( v_syex_table );

        pv_syex_table_fk_lookup_list.extend;
        pv_syex_table_fk_lookup_list(pv_syex_table_fk_lookup_list.count) := v_syex_table;
        v_input.i_model_elements(i) := v_syex_table;
      end if;
    end loop;
    
    for i in 1..v_input.i_model_elements.count
    loop
      if( v_input.i_model_elements(i) is of (ot_syex_table) )
      then
        v_syex_table := treat( v_input.i_model_elements(i) as ot_syex_table );

        handle_table_fk( v_syex_table );

        v_input.i_model_elements(i) := v_syex_table;
      end if;
    end loop;    
    
    for i in 1..pv_syex_sequence_list.count
    loop
      v_input.i_model_elements.extend();
      v_input.i_model_elements( v_input.i_model_elements.count ) := pv_syex_sequence_list(i);
    end loop;    

    return v_input;
  end;
end;
/
