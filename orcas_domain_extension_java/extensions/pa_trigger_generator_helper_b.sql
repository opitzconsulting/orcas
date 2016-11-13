create or replace package body pa_trigger_generator_helper is
  pv_syex_model ot_syex_model;
  pv_stmt varchar2(32000);

  procedure add_stmt( p_stmt in varchar2 )
  is
  begin  
    execute immediate p_stmt;
  end;  

  procedure add_stmt
  is
  begin
    add_stmt( pv_stmt );
    pv_stmt := null;
  end;
  
  procedure stmt_set( p_stmt_part varchar2 )
  is
  begin
    pv_stmt := p_stmt_part;
  end;
  
  procedure stmt_add( p_stmt_part varchar2 )
  is
  begin
    pv_stmt := pv_stmt || ' ' || p_stmt_part;
  end;    
  
  procedure stmt_add_nl
  is
  begin
    stmt_add(chr(10));
  end;  
  
  procedure stmt_done
  is
  begin
    add_stmt();
  end;    
  
  function get_domain( p_domain_name in varchar2 ) return ot_syex_domain
  is
  begin
    for i in 1..pv_syex_model.i_model_elements.count
    loop
      if( pv_syex_model.i_model_elements(i) is of (ot_syex_domain) )
      then
        if( treat( pv_syex_model.i_model_elements(i) as ot_syex_domain ).i_name = p_domain_name )
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
  
  function get_table( p_table_name in varchar2 ) return ot_syex_table
  is
  begin
    for i in 1..pv_syex_model.i_model_elements.count
    loop
      if( pv_syex_model.i_model_elements(i) is of (ot_syex_table) )
      then
        if( treat( pv_syex_model.i_model_elements(i) as ot_syex_table ).i_name = p_table_name )
        then
          return treat( pv_syex_model.i_model_elements(i) as ot_syex_table );
        end if;
      end if;
    end loop;

    raise_application_error( -20000, 'table not found: ' || p_table_name );
  end;   
  
  function is_column_exists( p_syex_table in out nocopy ot_syex_table, p_column_name in varchar2 ) return number
  is
  begin
    for i in 1..p_syex_table.i_columns.count 
    loop
      if( p_syex_table.i_columns(i).i_name = p_column_name )
      then
        return 1;
      end if;
    end loop;
    
    return 0;
  end;

  procedure handle_table( p_syex_table in out nocopy ot_syex_table )
  is
    v_syex_domain ot_syex_domain;
    v_syex_columndomain ot_syex_columndomain;    
    v_sequence_name varchar2(100);
    v_check_rule varchar2(100);    
    v_hist_table    ot_syex_table;
    
    function get_action_for_check_rule( p_action in varchar2, p_check_rule in varchar2 ) return varchar2
    is
      v_values varchar2(100);
      v_op_index number;      
      v_cp_index number;
      v_comma_index number;            
      v_value1 varchar2(100);
      v_value2 varchar2(100);
      v_value3 varchar2(100);     
      v_action_index number := case when ( p_action = 'inserting' ) then 1 when ( p_action = 'updating' ) then 2 else 3 end;
    begin
      v_op_index := instr( p_check_rule, '(' );
      v_cp_index := instr( p_check_rule, ')', -1 );    
      
      v_values := substr( p_check_rule, v_op_index+1, v_cp_index-v_op_index-1 );
      
      v_comma_index := instr( v_values, ',' );
      if( v_comma_index > 0 )
      then
        v_value1 := substr( v_values, 1, v_comma_index - 1 );
        v_values := substr( v_values, v_comma_index + 1 );
      end if;      
      
      v_comma_index := instr( v_values, ',' );
      if( v_comma_index > 0 )
      then
        v_value2 := substr( v_values, 1, v_comma_index - 1 );
        v_value3 := substr( v_values, v_comma_index + 1 );
      else
        v_value2 := v_values;
      end if;    
      
      if( v_value3 is null )  
      then
        v_action_index := v_action_index - 1;
      end if;
      
      if( v_action_index = 1 )
      then
        return v_value1;
      end if;
      if( v_action_index = 2 )
      then
        return v_value2;
      end if;
      if( v_action_index = 3 )
      then
        return v_value3;
      end if;
    end;
    
    procedure generate_inset( p_action in varchar2 )
    is
    begin
      stmt_add( 'insert into ' || v_hist_table.i_name || '(' );
      stmt_add_nl;        
      for i in 1..v_hist_table.i_columns.count 
      loop
        if( i != 1 )          
        then
          stmt_add( ',' );           
        end if;
        
          stmt_add( v_hist_table.i_columns(i).i_name );   
          stmt_add_nl;                    
      end loop;
      stmt_add( ') values (' );        
      stmt_add_nl;        
      for i in 1..v_hist_table.i_columns.count 
      loop
        if( i != 1 )          
        then
          stmt_add( ',' );           
        end if;
          
          if( is_column_exists( p_syex_table, v_hist_table.i_columns(i).i_name ) = 1 )
          then
            if( p_action = 'deleting' )
            then
              stmt_add( ':old.'||v_hist_table.i_columns(i).i_name );           
            else
              stmt_add( ':new.'||v_hist_table.i_columns(i).i_name );           
            end if;            
          elsif( v_hist_table.i_columns(i).i_default_value is not null )
          then
            stmt_add( v_hist_table.i_columns(i).i_default_value );           
          elsif( v_hist_table.i_columns(i).i_domain is not null )
          then
            v_syex_columndomain := get_column_domain( v_hist_table.i_columns(i).i_domain );
            
            if( v_syex_columndomain.i_generatepk is not null and v_syex_columndomain.i_generatepk.i_sequencenamerules is not null )
            then
              v_sequence_name := pa_domain_extension_helper.get_generated_name_column( v_syex_columndomain.i_generatepk.i_sequencenamerules, v_hist_table.i_columns(i).i_name, v_hist_table.i_name, v_hist_table.i_alias );
              stmt_add( v_sequence_name || '.nextval' );                         
            elsif( v_syex_columndomain.i_generatecc is not null )            
            then
              v_check_rule := pa_domain_extension_helper.get_generated_name_column( v_syex_columndomain.i_generatecc.i_checkrulenamerules, v_hist_table.i_columns(i).i_name, v_hist_table.i_name, v_hist_table.i_alias );
              
              stmt_add( get_action_for_check_rule( p_action, v_check_rule ) );                         
            else
              stmt_add( 'null' );                         
            end if;
          else            
            stmt_add( 'null' );         
          end if;
          stmt_add_nl;            
      end loop;        
      stmt_add( ');' );           
      stmt_add_nl;                       
    end;
  begin
    if( p_syex_table.i_domain is not null )
    then
      v_syex_domain := get_domain( p_syex_table.i_domain );
      
      if( v_syex_domain.i_historytable is not null )
      then
        v_hist_table := get_table( pa_domain_extension_helper.get_generated_name_table( v_syex_domain.i_historytable.i_tablenamerules, p_syex_table.i_name, p_syex_table.i_alias ) );
        stmt_add( 'create or replace trigger ' || p_syex_table.i_alias || '_hist_brud before insert or update or delete on ' || p_syex_table.i_name || ' for each row' );
        stmt_add_nl;
        stmt_add( 'begin' ); 
        stmt_add_nl;        

        stmt_add( 'if( inserting )' ); 
        stmt_add_nl;                
        stmt_add( 'then' );         
        stmt_add_nl;        
        generate_inset( 'inserting' );
        stmt_add( 'elsif( updating )' ); 
        stmt_add_nl;                
        stmt_add( 'then' );         
        stmt_add_nl;        
        generate_inset( 'updating' );
        stmt_add( 'else' );         
        stmt_add_nl;        
        generate_inset( 'deleting' );        
        stmt_add( 'end if;' );         
        stmt_add_nl;        
        
        stmt_add( 'end;' );                        
        stmt_add_nl;        
                
        stmt_done();
      end if;
    end if;
  end;

  procedure run( p_model in ot_syex_model )
  is
    v_syex_table ot_syex_table;
  begin   
    pv_syex_model := p_model;
  
    for i in 1..p_model.i_model_elements.count
    loop
      if( p_model.i_model_elements(i) is of (ot_syex_table) )
      then
        v_syex_table := treat( p_model.i_model_elements(i) as ot_syex_table );
        
        handle_table( v_syex_table );
      end if;
    end loop;  
  end; 
end; 
/
