create or replace package body pa_generator is
  pv_syex_model ot_syex_model;
  pv_stmt varchar2(32000);

  procedure add_stmt( p_stmt in varchar2 )
  is
  begin  
    pa_orcas_exec_log.exec_stmt( p_stmt );
  end;  

  procedure add_stmt
  is
  begin
    add_stmt( pv_stmt );
    pv_stmt := null;
  end;
  
  procedure clear_stmt
  is
  begin
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
    v_columnidentity ot_syex_columnidentity;    
    v_sequence_name varchar2(100);
    v_generate_trigger boolean := false;
    
    procedure generate_inset( p_action in varchar2 )
    is
    begin
      for i in 1..p_syex_table.i_columns.count 
      loop
        if( p_syex_table.i_columns(i).i_identity is not null )
        then  
          v_columnidentity := p_syex_table.i_columns(i).i_identity;
          v_generate_trigger := true;
        
          if (p_action = 'inserting')
          then
            if( v_columnidentity.i_by_default is not null and v_columnidentity.i_by_default = 'default' ) 
            then
              stmt_add( 'if (:new.' || p_syex_table.i_columns(i).i_name || ' is null) then :new.' || p_syex_table.i_columns(i).i_name || ' := ' 
                      || v_sequence_name || '.nextval; end if;' );         
            end if;            
          
            if( v_columnidentity.i_always is not null and v_columnidentity.i_always = 'always' ) 
            then
              stmt_add( ':new.' || p_syex_table.i_columns(i).i_name || ' := ' 
                      || v_sequence_name || '.nextval;' );  
            end if;    
          elsif (p_action = 'updating')
          then          
            stmt_add( 'if (:new.' || p_syex_table.i_columns(i).i_name || ' is null or :new.' || p_syex_table.i_columns(i).i_name || ' != :old.' 
                      || p_syex_table.i_columns(i).i_name || ') then raise_application_error( -20000, ''value of identity column must not change: '
                      || p_syex_table.i_columns(i).i_name || '''); end if;'  );              
          end if;  
          stmt_add_nl; 
        end if;      
      end loop;                                       
    end;
  begin
    v_sequence_name := p_syex_table.i_alias || '_IDENTITY_SEQ';
      
    stmt_add( 'create or replace trigger ' || p_syex_table.i_alias || '_identity_briu before insert or update on ' || p_syex_table.i_name || ' for each row' );
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
    stmt_add_nl;        
    stmt_add( 'end if;' );         
    stmt_add_nl;      
    stmt_add( 'end;' ); 
    
    if v_generate_trigger = true  
    then
      stmt_done();
    else
      clear_stmt();
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
