CREATE OR REPLACE package body pa_orcas_compare_diff is
  pv_model_diff od_orig_model;

  pv_stmt varchar2(32000);
  type t_varchar_list is table of varchar2(32000);
  pv_statement_list t_varchar_list;
  pv_default_orig_chartype ot_orig_chartype;
  pv_default_tablespace varchar2(30);
  pv_temporary_tablespace varchar2(30); 
  
  function get_column_list( p_columnref_list in cd_orig_columnref_list ) return varchar2
  is
    v_return varchar2(2000);
  begin
    for i in 1..p_columnref_list.count()
    loop
      if( p_columnref_list(i).is_new = 1 )
      then
        if( v_return is not null )
        then
          v_return := v_return || ',';
        end if;
        
        v_return := v_return || p_columnref_list(i).n_column_name;
      end if;
    end loop;
    
    return v_return;
  end;
    
  function is_equal( p_val1 varchar2, p_val2 varchar2 ) return number
  is
  begin
    if( p_val1 is null and p_val2 is null )
    then
      return 1;
    end if;
    if( p_val1 is null or p_val2 is null )
    then
      return 0;
    end if;
    if( p_val1 = p_val2 )
    then
      return 1;
    else
      return 0;
    end if;
  end;
  
  function is_equal_ignore_case( p_val1 varchar2, p_val2 varchar2 ) return number
  is
  begin
    return is_equal( upper(p_val1), upper(p_val2) );
  end;
  
  function replace_linefeed_by_space ( p_script in varchar2) return varchar2 is
  begin
    return replace(replace(replace(p_script, chr(13) || chr(10),' '), chr(10),' '), chr(13),' ');
  end;
  
  function adjust_compression_literal ( p_literal in varchar2 ) return varchar2 is
  begin
    return replace(replace(replace(p_literal, '_low',' low'), '_high',' high'), '_operations',' operations');
  end;  
  
  function adjust_refreshmethod_literal ( p_literal in varchar2 ) return varchar2 is
  begin
    return replace(replace(p_literal, 'refresh_',' refresh '), 'never_','never ');
  end;    
  
  function get_column_datatype( p_column_diff in od_orig_column ) return varchar2
  is
    v_datatype       varchar2(100);    
  begin
    if ( ot_orig_datatype.is_equal( p_column_diff.n_data_type, ot_orig_datatype.c_object ) = 1 ) 
    then
      v_datatype := p_column_diff.n_object_type;
    else
      if( ot_orig_datatype.is_equal( p_column_diff.n_data_type, ot_orig_datatype.c_long_raw ) = 1 )
      then
        v_datatype := 'long raw';
      else
        v_datatype := upper(p_column_diff.n_data_type.i_name);    
      end if;
      
      if( p_column_diff.n_precision != 0 )
      then
        v_datatype := v_datatype || '(' || p_column_diff.n_precision;
  
        if( p_column_diff.n_scale != 0 )
        then
          v_datatype := v_datatype || ',' || p_column_diff.n_scale;            
        end if;
        
        if( p_column_diff.n_byteorchar is not null )
        then
          v_datatype := v_datatype || ' ' || upper(p_column_diff.n_byteorchar.i_name);            
        end if;      
        
        v_datatype := v_datatype || ')';      
      end if;
      
      if ( is_equal_ignore_case( p_column_diff.n_with_time_zone, 'with_time_zone' ) = 1 )
      then
        v_datatype := v_datatype || ' with time zone';
      end if;  
    end if;
    
    return v_datatype;
  end;    
  
  function get_fk_for_ref_partitioning( p_orig_table in ot_orig_table ) return ot_orig_foreignkey
  is
  begin
    for i in 1..p_orig_table.i_foreign_keys.count()
    loop
      if( upper(p_orig_table.i_foreign_keys(i).i_consname) = upper(treat( p_orig_table.i_tablepartitioning as ot_orig_refpartitions ).i_fkname ))
      then
        return p_orig_table.i_foreign_keys(i);
      end if;
    end loop;
      
    raise_application_error( -20000, 'fk for refpartitioning not found' );
  end;   
  

  function get_mview_elements( p_model_elements in ct_orig_modelelement_list ) return ct_orig_mview_list
  is
    v_return ct_orig_mview_list := new ct_orig_mview_list();
  begin  
    for i in 1 .. p_model_elements.count loop
      if( p_model_elements(i) is of (ot_orig_mview) ) 
      then
        v_return.extend;
        v_return(v_return.count) := treat( p_model_elements(i) as ot_orig_mview );
      end if;
    end loop;    
    
    return v_return;
  end;
  
  function get_sequence_elements( p_model_elements in ct_orig_modelelement_list ) return ct_orig_sequence_list
  is
    v_return ct_orig_sequence_list := new ct_orig_sequence_list();
  begin  
    for i in 1 .. p_model_elements.count loop
      if( p_model_elements(i) is of (ot_orig_sequence) ) 
      then
        v_return.extend;
        v_return(v_return.count) := treat( p_model_elements(i) as ot_orig_sequence );
      end if;
    end loop;    
    
    return v_return;
  end;  
  
  function sort_tables_for_ref_part( p_model_elements in ct_orig_modelelement_list ) return ct_orig_table_list
  is
    v_orig_modelelement_list ct_orig_table_list := new ct_orig_table_list();
    v_orig_table_list ct_orig_modelelement_list := new ct_orig_modelelement_list();    
    v_orig_modelelement ot_orig_modelelement;
    type t_varchar_set is table of number index by varchar2(100);
    v_tab_set t_varchar_set;
    
    procedure clean_orig_table_list
    is
      v_new_orig_table_list ct_orig_modelelement_list := new ct_orig_modelelement_list();  
    begin
      for i in 1..v_orig_table_list.count
      loop
        if( v_orig_table_list(i) is not null )
        then
          v_new_orig_table_list.extend(1);
          v_new_orig_table_list(v_new_orig_table_list.count) := v_orig_table_list(i);
        end if;
      end loop;
      
      v_orig_table_list := v_new_orig_table_list;
    end;    
    
    function add_orig_table_list return number
    is
      v_orig_table ot_orig_table;    
      v_required_table_name varchar2(100) := null; 
    begin
      for i in 1..v_orig_table_list.count
      loop
        v_orig_table := treat( v_orig_table_list(i) as ot_orig_table );
        
        if( v_orig_table.i_tablepartitioning is not null and v_orig_table.i_tablepartitioning is of (ot_orig_refpartitions) )
        then
          v_required_table_name := get_fk_for_ref_partitioning( v_orig_table ).i_desttable;
        else
          v_required_table_name := null;
        end if;
        
        if( v_required_table_name is null or v_tab_set.exists(v_required_table_name) )
        then 
          v_orig_modelelement_list.extend(1);
          v_orig_modelelement_list(v_orig_modelelement_list.count) := v_orig_table;    
          v_tab_set(upper(v_orig_table.i_name)) := 1;
          v_orig_table_list(i) := null;
          clean_orig_table_list();
         
          return 1;
        end if;
      end loop;
      
      return 0;      
    end;
    
    procedure add_orig_table_list_multi    
    is
    begin
      loop
        if( add_orig_table_list = 0 )
        then
          return;
        end if;
      end loop;
    end;
  begin
    for i in 1 .. p_model_elements.count()
    loop
      v_orig_modelelement := p_model_elements(i);
  
      if( v_orig_modelelement is of (ot_orig_table) ) 
      then
        v_orig_table_list.extend(1);
        v_orig_table_list(v_orig_table_list.count) := treat( v_orig_modelelement as ot_orig_table );
        
        add_orig_table_list_multi();
      end if;
    end loop;  
    
    add_orig_table_list_multi();
    
    if( v_orig_table_list.count != 0 )
    then
      raise_application_error( -20000, 'possible table order not found ' || v_orig_table_list.count );
    end if;
    
    return v_orig_modelelement_list;
  end;  
  
  procedure add_stmt( p_stmt in varchar2 )
  is
  begin
    pv_statement_list.extend(1);
    pv_statement_list(pv_statement_list.count) := p_stmt;
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
  
  procedure stmt_done
  is
  begin
    add_stmt();
  end;    
  
  procedure drop_table_constraint_by_name( p_tablename varchar2, p_constraint_name varchar2 )
  is
  begin
    add_stmt( 'alter table ' || p_tablename || ' drop constraint ' || p_constraint_name );
  end;
  
  function has_rows( p_test_stmt in varchar2 ) return number
  is
    c_cur sys_refcursor; 
    v_dummy number;   
  begin
    open c_cur  
    for p_test_stmt;
    
    fetch c_cur into v_dummy;
    
    if( c_cur%found )
    then
      close c_cur;    
      return 1;
    else
      close c_cur;    
      return 0;        
    end if;    
    
  exception
    when others
      then raise_application_error( -20000, sqlerrm||' '||p_test_stmt);    
  end;  
  
  function has_rows_ignore_errors( p_test_stmt in varchar2 ) return number
  is
  begin
    return has_rows( p_test_stmt );
  exception
    when others then
      return 0; 
  end;    
  
  procedure drop_with_dropmode_check( p_test_stmt in varchar2, p_stmt_to_execute in varchar2 )
  is
  begin
    if( pa_orcas_run_parameter.is_dropmode() != 1 )
    then     
      if( has_rows( p_test_stmt ) = 1 )
      then
        if( pa_orcas_run_parameter.is_dropmode_ignore() != 1 )
        then
          raise_application_error( -20000, 'drop mode ist nicht aktiv, daher kann folgendes statement nicht ausgefuehrt werden: ' || p_stmt_to_execute );        
        else
          add_stmt( '-- dropmode-ignore: ' || p_stmt_to_execute );          
          return;
        end if;
      end if;
    end if;
    
    add_stmt( p_stmt_to_execute );
  end;  
  
  procedure handle_sequence( p_sequence_diff in od_orig_sequence )
  is
    v_soll_start_value number;
    v_ist_value number;
    
    procedure load_start_value is
      type curtyp is ref          cursor;
      cur_max_used_value          curtyp;  -- declare cursor variable
    begin
      if( p_sequence_diff.n_max_value_select is not null )
      then
        open cur_max_used_value for p_sequence_diff.n_max_value_select;
        fetch cur_max_used_value into v_soll_start_value;
        close cur_max_used_value;
        
        v_soll_start_value := v_soll_start_value + 1;
      end if;
    exception when others then 
      null; -- kann vorkommen, wenn fuer das select benoetigte Tabellen nicht exisitieren. kann erst richtig korrigiert werden, wenn auch der Tabellenabgleich auf dieses Package umgestellt wurde
    end;
  begin
    load_start_value();
    
    if( p_sequence_diff.is_matched = 0 )
    then
      pv_stmt := 'create sequence ' || p_sequence_diff.n_sequence_name;
      if( p_sequence_diff.n_increment_by is not null )
      then      
        pv_stmt := pv_stmt || ' increment by ' || p_sequence_diff.n_increment_by;
      end if;  
        
      if( v_soll_start_value is not null )
      then
        pv_stmt := pv_stmt || ' start with ' || v_soll_start_value;      
      end if;
      
      if( p_sequence_diff.n_maxvalue is not null )
      then
        pv_stmt := pv_stmt || ' maxvalue ' || p_sequence_diff.n_maxvalue;      
      end if;
      
      if( p_sequence_diff.n_minvalue is not null )
      then
        pv_stmt := pv_stmt || ' minvalue ' || p_sequence_diff.n_minvalue;      
      end if;
      
      if( p_sequence_diff.n_cycle is not null )
      then
        pv_stmt := pv_stmt || ' ' || p_sequence_diff.n_cycle.i_literal;      
      end if;
      
      if( p_sequence_diff.n_cache is not null )
      then
        pv_stmt := pv_stmt || ' cache ' || p_sequence_diff.n_cache;      
      end if;
      
      if( p_sequence_diff.n_order is not null )
      then
        pv_stmt := pv_stmt || ' ' || p_sequence_diff.n_order.i_literal;      
      end if;
      
      add_stmt();
    else
      v_ist_value := p_sequence_diff.o_max_value_select;
      if( v_ist_value < v_soll_start_value )
      then
        add_stmt( 'alter sequence '||p_sequence_diff.n_sequence_name||' increment by '||(v_soll_start_value-v_ist_value) );
        add_stmt( 'declare v_dummy number; begin select '||p_sequence_diff.n_sequence_name||'.nextval into v_dummy from dual; end;' );
        add_stmt( 'alter sequence '||p_sequence_diff.n_sequence_name||' increment by ' || nvl(p_sequence_diff.n_increment_by,1) );
      else
        if( p_sequence_diff.e_increment_by = 0 )
        then  
          add_stmt( 'alter sequence '||p_sequence_diff.n_sequence_name||' increment by ' || nvl(p_sequence_diff.n_increment_by,1) );        
        end if;
      end if;   
      
      if( p_sequence_diff.e_maxvalue = 0 )
      then
        add_stmt( 'alter sequence '|| p_sequence_diff.n_sequence_name || ' maxvalue ' || p_sequence_diff.n_maxvalue );        
      end if;
      
      if( p_sequence_diff.e_minvalue = 0 )
      then
        add_stmt( 'alter sequence '|| p_sequence_diff.n_sequence_name || ' minvalue ' || nvl(p_sequence_diff.n_minvalue,1) );        
      end if;
      
      if( p_sequence_diff.e_cycle = 0 )
      then
        add_stmt( 'alter sequence '|| p_sequence_diff.n_sequence_name || ' ' || p_sequence_diff.n_cycle.i_literal );        
      end if;
      
      if( p_sequence_diff.e_cache = 0 )
      then
        add_stmt( 'alter sequence '|| p_sequence_diff.n_sequence_name || ' cache ' || nvl(p_sequence_diff.n_cache,20) );    
      end if;
      
      if( p_sequence_diff.e_order = 0 )
      then
        add_stmt( 'alter sequence '|| p_sequence_diff.n_sequence_name || ' ' || p_sequence_diff.n_order.i_literal );        
      end if;
      
    end if;
  end;
  
  procedure handle_all_sequences
  is
  begin
    for i in 1 .. pv_model_diff.c_model_elements_sequence.count()
    loop
      if( pv_model_diff.c_model_elements_sequence(i).is_equal = 0 )
      then        
        if( pv_model_diff.c_model_elements_sequence(i).is_new = 1 )
        then                
          handle_sequence( pv_model_diff.c_model_elements_sequence(i) );
        else
          add_stmt( 'drop sequence ' || pv_model_diff.c_model_elements_sequence(i).o_sequence_name );          
        end if;
      end if;
    end loop;
  end;
  
  procedure handle_mview( p_mview_diff od_orig_mview )
  is     
    v_orig_refreshmodetype ot_orig_refreshmodetype;
    v_refreshmode varchar2(10);
      
    procedure create_mview
    is
    begin
      stmt_set( 'create materialized view' );
      stmt_add( p_mview_diff.n_mview_name );
      
      if( ot_orig_buildmodetype.is_equal( p_mview_diff.n_buildmode, ot_orig_buildmodetype.c_prebuilt, ot_orig_buildmodetype.c_immediate ) = 1 )
      then
        stmt_add( 'on prebuilt table' );
      else
        -- Physical properties nur, wenn nicht prebuilt
        if( p_mview_diff.n_tablespace is not null )
        then
          stmt_add( 'tablespace' ); 
          stmt_add( p_mview_diff.n_tablespace );            
        end if;   
    
        if( ot_orig_compresstype.is_equal( p_mview_diff.n_compression, ot_orig_compresstype.c_compress, ot_orig_compresstype.c_nocompress ) = 1 )
        then
          stmt_add( 'compress' );
          if( ot_orig_compressfortype.is_equal( p_mview_diff.n_compressionfor, ot_orig_compressfortype.c_all ) = 1 )
          then
            stmt_add( 'for all operations' );
          elsif ( ot_orig_compressfortype.is_equal( p_mview_diff.n_compressionfor, ot_orig_compressfortype.c_direct_load ) = 1 )
            then
            stmt_add( 'for direct_load operations' );
          elsif ( ot_orig_compressfortype.is_equal( p_mview_diff.n_compressionfor, ot_orig_compressfortype.c_query_low ) = 1 )
            then
            stmt_add( 'for query low' );  
          elsif ( ot_orig_compressfortype.is_equal( p_mview_diff.n_compressionfor, ot_orig_compressfortype.c_query_high ) = 1 )
            then
            stmt_add( 'for query high' );   
          elsif ( ot_orig_compressfortype.is_equal( p_mview_diff.n_compressionfor, ot_orig_compressfortype.c_archive_low ) = 1 )
            then
            stmt_add( 'for archive low' );  
          elsif ( ot_orig_compressfortype.is_equal( p_mview_diff.n_compressionfor, ot_orig_compressfortype.c_archive_high ) = 1 )
            then
            stmt_add( 'for archive high' );             
          end if;
        else
          stmt_add( 'nocompress' );
        end if;
        
        if( ot_orig_paralleltype.is_equal( p_mview_diff.n_parallel, ot_orig_paralleltype.c_parallel ) = 1 )
        then
          stmt_add( 'parallel' ); 
          if ( p_mview_diff.n_parallel_degree > 1 )
          then
            stmt_add( p_mview_diff.n_parallel_degree );
          end if;
        else
          stmt_add( 'noparallel' );               
        end if;      
          
        if( p_mview_diff.n_buildmode is not null )
        then   
          stmt_add( 'build' );
          stmt_add( p_mview_diff.n_buildmode.i_literal );   
        end if;  
      end if;
        
      if( p_mview_diff.n_refreshmethod is not null )
      then
        stmt_add( adjust_refreshmethod_literal(p_mview_diff.n_refreshmethod.i_literal) );    
        
        if( p_mview_diff.n_refreshmode is not null )
        then
          stmt_add( 'on' );
          stmt_add( p_mview_diff.n_refreshmode.i_literal );            
        end if; 
      end if;               

      if( ot_orig_enabletype.is_equal( p_mview_diff.n_queryrewrite, ot_orig_enabletype.c_enable ) = 1 )
      then
        stmt_add( 'enable query rewrite' ); 
      end if;  
        
      stmt_add( 'as' ); 
      stmt_add( replace_linefeed_by_space(p_mview_diff.n_viewselectclob) );   
      add_stmt(); 
    end;

  begin
    if( p_mview_diff.is_matched = 0 )
    then
      create_mview();
    else
      if(    
           p_mview_diff.e_tablespace = 0
        or p_mview_diff.e_viewselectclob = 0 
        or p_mview_diff.e_buildmode = 0 
        )                                        
      then
        add_stmt( 'drop materialized view ' || p_mview_diff.o_mview_name );   
            
        create_mview();
      else 
        if( p_mview_diff.e_queryrewrite = 0 )
        then
          add_stmt( 'alter materialized view ' || p_mview_diff.n_mview_name || ' ' || p_mview_diff.n_queryrewrite.i_literal || ' query rewrite');  
        end if;
        
        if( p_mview_diff.e_refreshmode = 0 or p_mview_diff.e_refreshmethod = 0 )
        then
          v_orig_refreshmodetype := p_mview_diff.n_refreshmode;
          if ( v_orig_refreshmodetype is null ) 
          then
            v_refreshmode := '';
          else
            v_refreshmode := ' on ' || v_orig_refreshmodetype.i_literal;
          end if;
          add_stmt( 'alter materialized view ' || p_mview_diff.n_mview_name || ' ' || adjust_refreshmethod_literal(p_mview_diff.n_refreshmethod.i_literal) || v_refreshmode );  
        end if;
        
        -- Physical parameters nur, wenn nicht prebuilt
        if ( ot_orig_buildmodetype.is_equal(  p_mview_diff.n_buildmode,  ot_orig_buildmodetype.c_prebuilt, ot_orig_buildmodetype.c_immediate ) != 1 )
        then        
          if( p_mview_diff.e_parallel = 0 or p_mview_diff.e_parallel_degree = 0 )
          then
            stmt_set( 'alter materialized view' );
            stmt_add( p_mview_diff.n_mview_name );        
            if( ot_orig_paralleltype.is_equal( p_mview_diff.n_parallel, ot_orig_paralleltype.c_parallel ) = 1 )
            then
              stmt_add( 'parallel' ); 
              if ( p_mview_diff.n_parallel_degree > 1 )
              then
                stmt_add( p_mview_diff.n_parallel_degree );
              end if;
            else
              stmt_add( 'noparallel' );         
            end if;         
            
            stmt_done();
          end if;    
          
          if( p_mview_diff.e_compression = 0 or p_mview_diff.e_compressionfor = 0 )
          then
            stmt_set( 'alter materialized view' );
            stmt_add( p_mview_diff.n_mview_name );        
            if( ot_orig_compresstype.is_equal( p_mview_diff.n_compression, ot_orig_compresstype.c_compress, ot_orig_compresstype.c_nocompress ) = 1 )
            then
              stmt_add( 'compress' ); 
              if ( p_mview_diff.n_compressionFor is not null )
              then
                stmt_add( 'for ' || adjust_compression_literal(p_mview_diff.n_compressionFor.i_literal));
              end if;
            else
              stmt_add( 'nocompress' );         
            end if;         
            
            stmt_done();
          end if;  
        end if;  
      end if;     
    end if;  
  end;            
  
  procedure handle_all_mviews
  is
  begin      
    for i in 1 .. pv_model_diff.c_model_elements_mview.count()
    loop
      if( pv_model_diff.c_model_elements_mview(i).is_equal = 0 )
      then        
        if( pv_model_diff.c_model_elements_mview(i).is_new = 1 )
        then                
          handle_mview( pv_model_diff.c_model_elements_mview(i) );
        else
          add_stmt( 'drop materialized view ' || pv_model_diff.c_model_elements_mview(i).o_mview_name );          
        end if;
      end if;
    end loop;  
  end;
  
  function create_column_create_part( p_column_diff od_orig_column ) return varchar2
  is
    v_return varchar2(32000);    
  begin
    v_return := p_column_diff.n_name || ' ' || get_column_datatype( p_column_diff );
    
    if( p_column_diff.n_default_value is not null )
    then
      v_return := v_return || ' default ' || p_column_diff.n_default_value;
    end if;
    
    if( p_column_diff.c_identity.is_new = 1 )
    then
      v_return := v_return || ' generated';
      if( p_column_diff.c_identity.n_always is not null)
      then
        v_return := v_return || ' always';      
      end if;
      if( p_column_diff.c_identity.n_by_default is not null)
      then
        v_return := v_return || ' by default';      
      end if;
      if( p_column_diff.c_identity.n_on_null is not null)
      then
        v_return := v_return || ' on null';      
      end if;
      v_return := v_return || ' as identity';      
      
      v_return := v_return || ' (';      

      if( p_column_diff.c_identity.n_increment_by is not null and p_column_diff.c_identity.n_increment_by > 0 )    
      then
        v_return := v_return || ' increment by ' || p_column_diff.c_identity.n_increment_by;
      else      
        v_return := v_return || ' increment by 1';
      end if;      
      
      if( p_column_diff.c_identity.n_maxvalue is not null and p_column_diff.c_identity.n_maxvalue > 0)
      then
        v_return := v_return || ' maxvalue ' ||p_column_diff.c_identity.n_maxvalue;      
      end if;
      
      if( p_column_diff.c_identity.n_minvalue is not null and p_column_diff.c_identity.n_minvalue > 0)
      then
        v_return := v_return || ' minvalue ' || p_column_diff.c_identity.n_minvalue;      
      end if;
      
      if( p_column_diff.c_identity.n_cycle is not null )
      then
        v_return := v_return || ' ' || p_column_diff.c_identity.n_cycle.i_literal;      
      end if;
      
      if( p_column_diff.c_identity.n_cache is not null and p_column_diff.c_identity.n_cache > 0)
      then
        v_return := v_return || ' cache ' || p_column_diff.c_identity.n_cache;      
      end if;
      
      if( p_column_diff.c_identity.n_order is not null )
      then
        v_return := v_return || ' ' || p_column_diff.c_identity.n_order.i_literal;      
      end if;
      
      v_return := v_return || ' )';            
    end if;    
    
    if( p_column_diff.n_notnull_flg = 1 )      
    then
      v_return := v_return || ' not null';
    end if;      
    
    return v_return;
  end;
  
  function create_foreign_key_clause( p_foreignkey_diff od_orig_foreignkey ) return varchar2
  is
    v_return varchar2(32000);
    v_orig_refpartition ot_orig_refpartition;
  begin
    v_return := 'constraint ' || p_foreignkey_diff.n_consname || ' foreign key (' || get_column_list( p_foreignkey_diff.c_srccolumns ) || ') references ' || p_foreignkey_diff.n_desttable || '(' || get_column_list( p_foreignkey_diff.c_destcolumns ) || ')';      

    if( p_foreignkey_diff.n_delete_rule is not null )
    then      
      if( p_foreignkey_diff.n_delete_rule = ot_orig_fkdeleteruletype.c_cascade )
      then
        v_return := v_return || ' on delete cascade';
      end if;
      if( p_foreignkey_diff.n_delete_rule = ot_orig_fkdeleteruletype.c_set_null )
      then
        v_return := v_return || ' on delete set null';
      end if;
    end if;
    
    if( p_foreignkey_diff.n_deferrtype is not null )
    then
      v_return := v_return || ' deferrable initially  ' || p_foreignkey_diff.n_deferrtype.i_name;
    end if;         

    return v_return; 
  end;     
  
  procedure create_table( p_table_diff od_orig_table )
  is
    function create_column_clause( p_column_list_diff in cd_orig_column_list ) return varchar2
    is
      v_return varchar2(32000);  
    begin
      for i in 1..p_column_list_diff.count()
      loop
        if( v_return is not null )
        then
          v_return := v_return || ',';
        end if;
        
        if( p_column_list_diff(i).is_new = 1 )
        then
          v_return := v_return || ' ' || create_column_create_part( p_column_list_diff(i) );
        end if;
      end loop;
    
      return v_return;
    end;
    
    function create_column_storage_clause( p_lobstorage_diff_list in cd_orig_lobstorage_list ) return varchar2
    is
      v_return varchar2(32000);  
    begin
      for i in 1..p_lobstorage_diff_list.count()
      loop
        if( p_lobstorage_diff_list(i).is_new = 1 )
        then
          v_return := v_return || ' lob(' || p_lobstorage_diff_list(i).n_column_name || ') store as (tablespace ' || p_lobstorage_diff_list(i).n_tablespace || ')';
        end if;
      end loop;
    
      return v_return;
    end;  
        
    function create_range_valuelist( p_orig_rangepartitionval_list ct_orig_rangepartitionval_list ) return varchar2
    is
      v_return varchar2(32000);
      v_orig_rangepartitionval ot_orig_rangepartitionval; 
    begin
      for j in 1..p_orig_rangepartitionval_list.count()
      loop
        v_orig_rangepartitionval := p_orig_rangepartitionval_list(j);
        if(j!=1)
        then
          v_return := v_return || ',';              
        end if;
          
        if( v_orig_rangepartitionval.i_value is not null )
        then
          v_return := v_return || v_orig_rangepartitionval.i_value;                    
        else
          v_return := v_return || 'maxvalue';
        end if;
      end loop;       
      
      return v_return;   
    end;  
    
    function create_list_valuelist( p_orig_listpartitionvalu_list ct_orig_listpartitionvalu_list ) return varchar2
    is
      v_return varchar2(32000);
      v_orig_listpartitionvalu ot_orig_listpartitionvalu; 
    begin
      for j in 1..p_orig_listpartitionvalu_list.count()
      loop
        v_orig_listpartitionvalu := p_orig_listpartitionvalu_list(j);
        if(j!=1)
        then
          v_return := v_return || ',';              
        end if;
          
        if( v_orig_listpartitionvalu.i_value is not null )
        then
          v_return := v_return || v_orig_listpartitionvalu.i_value;                    
        else
          v_return := v_return || 'default';
        end if;
      end loop;       
      
      return v_return;   
    end;    
    
    function create_sub_range_clause( p_orig_rangesubsubpart ot_orig_rangesubsubpart ) return varchar2
    is
      v_return varchar2(32000);
    begin  
      v_return := v_return || 'subpartition ' || p_orig_rangesubsubpart.i_name || ' values less than (';
        
      v_return := v_return || create_range_valuelist(p_orig_rangesubsubpart.i_value );
        
      v_return := v_return || ')';                    
        
      if( p_orig_rangesubsubpart.i_tablespace is not null )
      then
        v_return := v_return || ' tablespace ' || p_orig_rangesubsubpart.i_tablespace;
      end if;      
    
      return v_return;
    end;  
    
    function create_sub_list_clause( p_orig_listsubsubpart ot_orig_listsubsubpart ) return varchar2
    is
      v_return varchar2(32000);
    begin  
      v_return := v_return || 'subpartition ' || p_orig_listsubsubpart.i_name || ' values (';
        
      v_return := v_return || create_list_valuelist(p_orig_listsubsubpart.i_value );
        
      v_return := v_return || ')';                    
        
      if( p_orig_listsubsubpart.i_tablespace is not null )
      then
        v_return := v_return || ' tablespace ' || p_orig_listsubsubpart.i_tablespace;
      end if;      
    
      return v_return;
    end;  
    
    function create_sub_hash_clause( p_orig_hashsubsubpart ot_orig_hashsubsubpart ) return varchar2
    is
      v_return varchar2(32000);
    begin  
      v_return := v_return || 'subpartition ' || p_orig_hashsubsubpart.i_name;
              
      if( p_orig_hashsubsubpart.i_tablespace is not null )
      then
        v_return := v_return || ' tablespace ' || p_orig_hashsubsubpart.i_tablespace;
      end if;      
    
      return v_return;
    end;  
    
    function create_subpartitions( p_orig_subsubpart_list ct_orig_subsubpart_list ) return varchar2
    is
      v_return varchar2(32000);
      v_orig_subsubpart ot_orig_subsubpart;
    begin
      v_return := v_return || '(';              
      for i in 1..p_orig_subsubpart_list.count
      loop    
        v_orig_subsubpart := p_orig_subsubpart_list(i);
        
        if(i!=1)
        then
          v_return := v_return || ',';              
        end if;
        
        if( v_orig_subsubpart is of (ot_orig_rangesubsubpart) )
        then
          v_return := v_return || create_sub_range_clause( treat( v_orig_subsubpart as ot_orig_rangesubsubpart) );
        elsif( v_orig_subsubpart is of (ot_orig_listsubsubpart) )
        then
          v_return := v_return || create_sub_list_clause( treat( v_orig_subsubpart as ot_orig_listsubsubpart) );        
        elsif( v_orig_subsubpart is of (ot_orig_hashsubsubpart) )
        then
          v_return := v_return || create_sub_hash_clause( treat( v_orig_subsubpart as ot_orig_hashsubsubpart) );          
        else    
          raise_application_error(-20000,'subpartitionstyp unbekannt');
        end if;
      end loop;
      
      v_return := v_return || ')';              
    
      return v_return;
    end;
    
    function create_range_sub_parts( p_orig_rangesubparts ot_orig_rangesubparts ) return varchar2
    is
      v_return varchar2(32000);
    begin
      v_return := v_return || ' subpartition by range (';              
      for i in 1..p_orig_rangesubparts.i_columns.count()
      loop
        if(i!=1)
        then
          v_return := v_return || ',';              
        end if;
        v_return := v_return || p_orig_rangesubparts.i_columns(i).i_column_name;
      end loop;
      v_return := v_return || ')';           
    
      return v_return;
    end;  
    
    function create_list_sub_parts( p_orig_listsubparts ot_orig_listsubparts ) return varchar2
    is
      v_return varchar2(32000);
    begin
      v_return := v_return || ' subpartition by list (' || p_orig_listsubparts.i_column.i_column_name || ')';           
    
      return v_return;
    end;    
    
    function create_hash_sub_parts( p_orig_hashsubparts ot_orig_hashsubparts ) return varchar2
    is
      v_return varchar2(32000);
    begin
      v_return := v_return || ' subpartition by hash (' || p_orig_hashsubparts.i_column.i_column_name || ')';           
    
      return v_return;
    end;    
    
    function create_table_sub_parts( p_orig_tablesubpart ot_orig_tablesubpart ) return varchar2
    is
      v_return varchar2(32000);
    begin
      if( p_orig_tablesubpart is of (ot_orig_rangesubparts) )
      then
        v_return := v_return || create_range_sub_parts( treat( p_orig_tablesubpart as ot_orig_rangesubparts) );
      elsif( p_orig_tablesubpart is of (ot_orig_listsubparts) )
      then
        v_return := v_return || create_list_sub_parts( treat( p_orig_tablesubpart as ot_orig_listsubparts) );
      elsif( p_orig_tablesubpart is of (ot_orig_hashsubparts) )
      then
        v_return := v_return || create_hash_sub_parts( treat( p_orig_tablesubpart as ot_orig_hashsubparts) );
      else    
        raise_application_error(-20000,'subpartitionstyp unbekannt');
      end if;
      
      return v_return;
    end;
    
    function create_range_clause( p_orig_rangepartitions ot_orig_rangepartitions ) return varchar2
    is
      v_return varchar2(32000);
      v_orig_rangepartition ot_orig_rangepartition;         
      v_orig_rangesubpart ot_orig_rangesubpart;
    begin
      v_return := v_return || 'partition by range (';      
      for i in 1..p_orig_rangepartitions.i_columns.count()
      loop
        if(i!=1)
        then
          v_return := v_return || ',';              
        end if;
        v_return := v_return || p_orig_rangepartitions.i_columns(i).i_column_name;
      end loop;
      v_return := v_return || ')';      
      
      if( p_orig_rangepartitions.i_intervalexpression is not null )
      then
        v_return := v_return || 'interval (' || p_orig_rangepartitions.i_intervalexpression || ')';      
      end if;
      
      if( p_orig_rangepartitions.i_tablesubpart is not null )
      then
        v_return := v_return || create_table_sub_parts( p_orig_rangepartitions.i_tablesubpart );
      
        v_return := v_return || '(';
              
        for i in 1..p_orig_rangepartitions.i_subpartitionlist.count()
        loop
          v_orig_rangesubpart := p_orig_rangepartitions.i_subpartitionlist(i);
          if(i!=1)
          then
            v_return := v_return || ',';              
          end if;
          v_return := v_return || 'partition ' || v_orig_rangesubpart.i_name || ' values less than (';
          
          v_return := v_return || create_range_valuelist(v_orig_rangesubpart.i_value );        
          
          v_return := v_return || ')';         
          
          v_return := v_return || create_subpartitions( v_orig_rangesubpart.i_subpartlist );        
        end loop;
        v_return := v_return || ')';       
      else
        v_return := v_return || '(';
        for i in 1..p_orig_rangepartitions.i_partitionlist.count()
        loop 
          v_orig_rangepartition := p_orig_rangepartitions.i_partitionlist(i);
          if(i!=1)
          then
            v_return := v_return || ',';              
          end if;
          v_return := v_return || 'partition ' || v_orig_rangepartition.i_name || ' values less than (';
        
          v_return := v_return || create_range_valuelist(v_orig_rangepartition.i_value );
        
          v_return := v_return || ')';                    
        
          if( v_orig_rangepartition.i_tablespace is not null )
          then
            v_return := v_return || ' tablespace ' || v_orig_rangepartition.i_tablespace;
          end if;      
        end loop; 
        v_return := v_return || ')';        
      end if;             
  
      return v_return; 
    end; 
    
    function create_list_clause( p_orig_listpartitions ot_orig_listpartitions ) return varchar2
    is
      v_return varchar2(32000);
      v_orig_listpartition ot_orig_listpartition;
      v_orig_listpartitionvalu ot_orig_listpartitionvalu; 
      v_orig_listsubpart ot_orig_listsubpart;
    begin
      v_return := v_return || 'partition by list (' || p_orig_listpartitions.i_column.i_column_name; 
      v_return := v_return || ')';   
      
      if( p_orig_listpartitions.i_tablesubpart is not null )
      then
        v_return := v_return || create_table_sub_parts( p_orig_listpartitions.i_tablesubpart );
      
        v_return := v_return || '(';
              
        for i in 1..p_orig_listpartitions.i_subpartitionlist.count()
        loop
          v_orig_listsubpart := p_orig_listpartitions.i_subpartitionlist(i);
          if(i!=1)
          then
            v_return := v_return || ',';              
          end if;
          v_return := v_return || 'partition ' || v_orig_listsubpart.i_name || ' values (';
          
          v_return := v_return || create_list_valuelist(v_orig_listsubpart.i_value );        
          
          v_return := v_return || ')';         
          
          v_return := v_return || create_subpartitions( v_orig_listsubpart.i_subpartlist );        
        end loop;
        v_return := v_return || ')';       
      else
        v_return := v_return || '(';    
      
        for i in 1..p_orig_listpartitions.i_partitionlist.count()
        loop
          v_orig_listpartition := p_orig_listpartitions.i_partitionlist(i);
          if(i!=1)
          then
            v_return := v_return || ',';              
          end if;
          v_return := v_return || 'partition ' || v_orig_listpartition.i_name || ' values (';
        
          v_return := v_return || create_list_valuelist( v_orig_listpartition.i_value );        
        
          v_return := v_return || ')';    
          
          if( v_orig_listpartition.i_tablespace is not null )
          then
            v_return := v_return || ' tablespace ' || v_orig_listpartition.i_tablespace;
          end if;                      
        end loop;      
        v_return := v_return || ')';                  
      end if;
  
      return v_return; 
    end;   
    
    function create_hash_clause( p_orig_hashpartitions ot_orig_hashpartitions ) return varchar2
    is
      v_return varchar2(32000);
      v_orig_hashpartition ot_orig_hashpartition;
    begin
      v_return := v_return || 'partition by hash (' || p_orig_hashpartitions.i_column.i_column_name; 
      v_return := v_return || ')(';      
      for i in 1..p_orig_hashpartitions.i_partitionlist.count()
      loop
        v_orig_hashpartition := p_orig_hashpartitions.i_partitionlist(i);
        if(i!=1)
        then
          v_return := v_return || ',';              
        end if;
        v_return := v_return || 'partition ' || v_orig_hashpartition.i_name;
        
        if( v_orig_hashpartition.i_tablespace is not null )
        then
          v_return := v_return || ' tablespace ' || v_orig_hashpartition.i_tablespace;
        end if;
      end loop;      
      v_return := v_return || ')';                  
  
      return v_return; 
    end;     
    
    function create_ref_clause( p_orig_refpartitions ot_orig_refpartitions ) return varchar2
    is
      v_return varchar2(32000);
      v_orig_refpartition ot_orig_refpartition;
    begin
      v_return := v_return || 'partition by reference (' || p_orig_refpartitions.i_fkname; 
      v_return := v_return || ')(';      
      for i in 1..p_orig_refpartitions.i_partitionlist.count()
      loop
        v_orig_refpartition := p_orig_refpartitions.i_partitionlist(i);
        if(i!=1)
        then
          v_return := v_return || ',';              
        end if;
        v_return := v_return || 'partition ' || v_orig_refpartition.i_name;
        
        if( v_orig_refpartition.i_tablespace is not null )
        then
          v_return := v_return || ' tablespace ' || v_orig_refpartition.i_tablespace;
        end if;
      end loop;      
      v_return := v_return || ')';                  
  
      return v_return; 
    end;     
  
    function create_partitioning_clause( p_orig_tablepartitioning ot_orig_tablepartitioning ) return varchar2
    is
      v_return varchar2(32000);
    begin
      if( p_orig_tablepartitioning is null )
      then
        return null;
      end if;
  
      v_return := v_return || ' ';
  
      if( p_orig_tablepartitioning is of (ot_orig_rangepartitions) )
      then
        v_return := v_return || create_range_clause( treat( p_orig_tablepartitioning as ot_orig_rangepartitions) );
      elsif( p_orig_tablepartitioning is of (ot_orig_listpartitions) )
      then
        v_return := v_return || create_list_clause( treat( p_orig_tablepartitioning as ot_orig_listpartitions) );    
      elsif( p_orig_tablepartitioning is of (ot_orig_hashpartitions) )
      then
        v_return := v_return || create_hash_clause( treat( p_orig_tablepartitioning as ot_orig_hashpartitions) );          
      elsif( p_orig_tablepartitioning is of (ot_orig_refpartitions) )
      then
        v_return := v_return || create_ref_clause( treat( p_orig_tablepartitioning as ot_orig_refpartitions) );                
      else    
        raise_application_error(-20000,'partitionstyp unbekannt');
      end if;
  
      return v_return; 
    end;
    
    function create_ref_fk_clause( p_orig_table ot_orig_table ) return varchar2
    is
      v_return varchar2(32000);
      v_orig_refpartition ot_orig_refpartition;
      v_orig_foreignkey ot_orig_foreignkey;
    begin
      if( p_orig_table.i_tablepartitioning is of (ot_orig_refpartitions) )
      then
        v_orig_foreignkey := get_fk_for_ref_partitioning( p_orig_table );
      
--        v_return := v_return || ', ' || create_foreign_key_clause( v_orig_foreignkey );
      end if;
      return v_return; 
    end;   
  begin
    pv_stmt := 'create';
    if( ot_orig_permanentnesstype.is_equal( p_table_diff.n_permanentness, ot_orig_permanentnesstype.c_global_temporary ) = 1 ) 
    then pv_stmt := pv_stmt || ' global ' || p_table_diff.n_permanentness.i_literal; 
    end if;    
    pv_stmt := pv_stmt || ' ' || 'table';
    pv_stmt := pv_stmt || ' ' || p_table_diff.n_name;
    pv_stmt := pv_stmt || ' ' || '(';       
    pv_stmt := pv_stmt || ' ' || create_column_clause(p_table_diff.c_columns);       
--    pv_stmt := pv_stmt || ' ' || create_ref_fk_clause( p_orig_table ); 
    pv_stmt := pv_stmt || ' ' || ')';             
    if( p_table_diff.n_transactioncontrol is not null ) 
    then
      pv_stmt := pv_stmt || ' ' || 'on commit ';           
      pv_stmt := pv_stmt || ' ' || p_table_diff.n_transactioncontrol.i_literal;       
      pv_stmt := pv_stmt || ' ' || 'rows nocache';       
    else 
      pv_stmt := pv_stmt || ' ' || create_column_storage_clause(p_table_diff.c_lobstorages);
      if( p_table_diff.n_tablespace is not null )
      then
        pv_stmt := pv_stmt || ' ' || 'tablespace';              
        pv_stmt := pv_stmt || ' ' || p_table_diff.n_tablespace; 
      end if;
      if( ot_orig_permanentnesstype.is_equal( p_table_diff.n_permanentness, ot_orig_permanentnesstype.c_global_temporary ) = 0 ) 
      then
          if( ot_orig_loggingtype.is_equal( p_table_diff.n_logging, ot_orig_loggingtype.c_nologging ) = 1 )
            then
              pv_stmt := pv_stmt || ' ' || 'nologging';
            else
              pv_stmt := pv_stmt || ' ' || 'logging';               
          end if;
      end if;
      if( ot_orig_compresstype.is_equal( p_table_diff.n_compression, ot_orig_compresstype.c_compress, ot_orig_compresstype.c_nocompress ) = 1 )
      then
        pv_stmt := pv_stmt || ' ' || 'compress';
        if( ot_orig_compressfortype.is_equal( p_table_diff.n_compressionfor, ot_orig_compressfortype.c_all ) = 1 )
        then
          pv_stmt := pv_stmt || ' ' || 'for all operations';
        elsif ( ot_orig_compressfortype.is_equal( p_table_diff.n_compressionfor, ot_orig_compressfortype.c_direct_load ) = 1 )
          then
          pv_stmt := pv_stmt || ' ' || 'for direct_load operations';
        elsif ( ot_orig_compressfortype.is_equal( p_table_diff.n_compressionfor, ot_orig_compressfortype.c_query_low ) = 1 )
          then
          pv_stmt := pv_stmt || ' ' || 'for query low';  
        elsif ( ot_orig_compressfortype.is_equal( p_table_diff.n_compressionfor, ot_orig_compressfortype.c_query_high ) = 1 )
          then
          pv_stmt := pv_stmt || ' ' || 'for query high';   
        elsif ( ot_orig_compressfortype.is_equal( p_table_diff.n_compressionfor, ot_orig_compressfortype.c_archive_low ) = 1 )
          then
          pv_stmt := pv_stmt || ' ' || 'for archive low';  
        elsif ( ot_orig_compressfortype.is_equal( p_table_diff.n_compressionfor, ot_orig_compressfortype.c_archive_high ) = 1 )
          then
          pv_stmt := pv_stmt || ' ' || 'for archive high';             
        end if;
      else
        if( ot_orig_permanentnesstype.is_equal( p_table_diff.n_permanentness, ot_orig_permanentnesstype.c_global_temporary ) = 0 ) 
        then
          pv_stmt := pv_stmt || ' ' || 'nocompress';
        end if;
      end if;
      if( ot_orig_paralleltype.is_equal( p_table_diff.n_parallel, ot_orig_paralleltype.c_parallel ) = 1 )
      then
        stmt_add( 'parallel' ); 
        if ( p_table_diff.n_parallel_degree > 1 )
        then
          stmt_add( p_table_diff.n_parallel_degree );
        end if;
      else
        stmt_add( 'noparallel' );               
      end if;      
--      pv_stmt := pv_stmt || ' ' || create_partitioning_clause( p_orig_table.i_tablepartitioning );
      pv_stmt := pv_stmt || ' ' || 'nocache nomonitoring';       
    end if;
    
    add_stmt;
  end;   
  
  procedure handle_constraint( p_tablename in varchar2, p_constraint_diff in out nocopy od_orig_constraint )
  is     
    procedure create_constraint
    is
    begin
      stmt_set( 'alter table ' || p_tablename || ' add constraint ' || p_constraint_diff.n_consname || ' check (' || p_constraint_diff.n_rule || ')' );
      if( p_constraint_diff.n_deferrtype is not null )
      then
        stmt_add( 'deferrable initially ' || p_constraint_diff.n_deferrtype.i_name );
      end if;
      if( p_constraint_diff.n_status is not null )
      then
        stmt_add( p_constraint_diff.n_status.i_name );
      end if;
      
      add_stmt();
    end;
  begin
    if( p_constraint_diff.is_matched = 0 
     or p_constraint_diff.is_recreate_needed = 1 )
    then
      create_constraint();
    end if;        
  end;     
  
  procedure handle_uniquekey( p_tablename in varchar2, p_uniquekey_diff in out nocopy od_orig_uniquekey )
  is     
    procedure create_uniquekey
    is
    begin
      stmt_set( 'alter table ' || p_tablename || ' add constraint ' || p_uniquekey_diff.n_consname || ' unique (' || get_column_list( p_uniquekey_diff.c_uk_columns ) || ')' );
      if( p_uniquekey_diff.n_tablespace is not null )
      then
          stmt_add( 'using index tablespace ' || p_uniquekey_diff.n_tablespace );
      else 
        if( p_uniquekey_diff.n_indexname is not null and is_equal_ignore_case( p_uniquekey_diff.n_indexname, p_uniquekey_diff.n_consname) != 1 )
        then
          stmt_add( 'using index ' || p_uniquekey_diff.n_indexname );
        end if;  
      end if;
      if( p_uniquekey_diff.n_status is not null )
      then
        stmt_add( p_uniquekey_diff.n_status.i_name );
      end if;
      
      add_stmt();
    end;
  begin
    if( p_uniquekey_diff.is_matched = 0 
     or p_uniquekey_diff.is_recreate_needed = 1 )
    then
      create_uniquekey();
    end if;        
  end;     
  
 procedure handle_index( p_tablename in varchar2, p_index_diff in out nocopy od_orig_index )
  is     
    procedure create_index
    is
    begin
      stmt_set( 'create' );
      stmt_add( p_index_diff.n_unique );
      if( p_index_diff.n_bitmap is not null )
      then
        stmt_add( 'bitmap' );          
      end if;
      stmt_add( 'index' );
      stmt_add( p_index_diff.n_consname );                  
      stmt_add( 'on' );         
      stmt_add( p_tablename ); 
      stmt_add( '(' );                  
      if( p_index_diff.n_function_based_expression is not null )
      then
        stmt_add( p_index_diff.n_function_based_expression ); 
      else
        stmt_add( get_column_list( p_index_diff.c_index_columns ) );                   
      end if;
      stmt_add( ')' );              
      if( p_index_diff.n_domain_index_expression is not null )
      then
        stmt_add( p_index_diff.n_domain_index_expression ); 
      else
        if( p_index_diff.n_logging is not null )
        then
          stmt_add( p_index_diff.n_logging.i_literal );          
        end if;
      end if;
      if( p_index_diff.n_tablespace is not null )
      then
        stmt_add( 'tablespace' ); 
        stmt_add( p_index_diff.n_tablespace );            
      end if;         
      if( p_index_diff.n_global is not null )
      then
        stmt_add( p_index_diff.n_global.i_literal );          
      end if;      
      if( p_index_diff.n_bitmap is null and  
          ot_orig_compresstype.is_equal(  p_index_diff.n_compression,     ot_orig_compresstype.c_compress ) = 1 
        )
      then
        stmt_add( 'compress' ); 
      elsif ( ot_orig_compresstype.is_equal( p_index_diff.n_compression,     ot_orig_compresstype.c_nocompress ) = 1 )
      then
        stmt_add( 'nocompress' );                
      end if;
      if(   ot_orig_paralleltype.is_equal( p_index_diff.n_parallel, ot_orig_paralleltype.c_parallel ) = 1
         or pa_orcas_run_parameter.is_indexparallelcreate = 1 )
      then
        stmt_add( 'parallel' ); 
        if ( p_index_diff.n_parallel_degree > 1 )
        then
          stmt_add( p_index_diff.n_parallel_degree );
        end if;
      end if;

      add_stmt();
      
      if(   ot_orig_paralleltype.is_equal( p_index_diff.n_parallel, ot_orig_paralleltype.c_noparallel, ot_orig_paralleltype.c_noparallel ) = 1
        and pa_orcas_run_parameter.is_indexparallelcreate = 1 )
      then
        add_stmt( 'alter index ' || p_index_diff.n_consname || ' noparallel' ); 
      end if;        
    end;

  begin      
    if( p_index_diff.is_matched = 0 
     or p_index_diff.is_recreate_needed = 1 )
    then
      create_index();
    else
      if(    p_index_diff.e_parallel = 0
          or p_index_diff.e_parallel_degree = 0
        )
      then
        stmt_set( 'alter index' );
        stmt_add( p_index_diff.n_consname );        
        if( ot_orig_paralleltype.is_equal( p_index_diff.n_parallel, ot_orig_paralleltype.c_parallel ) = 1 )
        then
          stmt_add( 'parallel' ); 
          if ( p_index_diff.n_parallel_degree > 1 )
          then
            stmt_add( p_index_diff.n_parallel_degree );
          end if;
        else
          stmt_add( 'noparallel' );         
        end if;         
        
        stmt_done();
      end if;
      
      if(  p_index_diff.e_logging = 0
        )
      then
        stmt_set( 'alter index' );
        stmt_add( p_index_diff.n_consname );        
        if( ot_orig_loggingtype.is_equal( p_index_diff.n_logging, ot_orig_loggingtype.c_nologging ) = 1 )
        then
          stmt_add( 'nologging' ); 
        else
          stmt_add( 'logging' );         
        end if;         
        
        stmt_done();
      end if;
      
      if ( p_index_diff.e_tablespace = 0
         and not (p_index_diff.o_tablespace is null and p_index_diff.n_tablespace is null) and pa_orcas_run_parameter.is_indexmovetablespace = 1 )
      then
        stmt_set( 'alter index' );
        stmt_add( p_index_diff.n_consname );     
        stmt_add( 'rebuild tablespace' ); 
        stmt_add( nvl(p_index_diff.n_tablespace, pv_default_tablespace) );  
        stmt_done();
      end if;          
    end if;        
  end;   
      
  procedure handle_comment( p_tablename in varchar2, p_inlinecomment_diff in out nocopy od_orig_inlinecomment )
  is     
  begin
    if( p_inlinecomment_diff.is_equal = 0 )
    then      
      if( p_inlinecomment_diff.is_new = 1 )
      then           
        stmt_set( 'comment on' );
        stmt_add( p_inlinecomment_diff.n_comment_object.i_name );                
        stmt_add( ' ' );                        
        stmt_add( p_tablename );
        if( p_inlinecomment_diff.n_column_name is not null )
        then
          stmt_add( '.' );
          stmt_add( p_inlinecomment_diff.n_column_name );                
        end if;
        stmt_add( 'is' );        
        stmt_add( '''' || replace(p_inlinecomment_diff.n_comment,'''','''''') || '''' );                
        add_stmt();
      else
        stmt_set( 'comment on' );
        stmt_add( p_inlinecomment_diff.o_comment_object.i_name );                
        stmt_add( ' ' );                        
        stmt_add( p_tablename );
        if( p_inlinecomment_diff.o_column_name is not null )
        then
          stmt_add( '.' );
          stmt_add( p_inlinecomment_diff.o_column_name );                
        end if;
        stmt_add( 'is' );        
        stmt_add( '''''' );                
        add_stmt();        
      end if;          
    end if;
  end;    
  
  procedure handle_mviewlog( p_table_diff in out nocopy od_orig_table )
  is     
    v_default_tablespace varchar2(30);
    c_date_format constant varchar2(30) := pa_orcas_run_parameter.get_dateformat();
    
    procedure create_mviewlog
    is
    begin
      stmt_set( 'create materialized view log on' );       
      stmt_add( p_table_diff.n_name ); 
      
      if( p_table_diff.c_mviewlog.n_tablespace is not null )
      then
        stmt_add( 'tablespace' ); 
        stmt_add( p_table_diff.c_mviewlog.n_tablespace );            
      end if;   
      
      if( ot_orig_paralleltype.is_equal( p_table_diff.c_mviewlog.n_parallel, ot_orig_paralleltype.c_parallel ) = 1 )
      then
        stmt_add( 'parallel' ); 
        if ( p_table_diff.c_mviewlog.n_parallel_degree > 1 )
        then
          stmt_add( p_table_diff.c_mviewlog.n_parallel_degree );
        end if;
      end if;
      
      stmt_add( 'with' );
      
      if( nvl(p_table_diff.c_mviewlog.n_primarykey,'null') = 'primary' 
          or nvl(p_table_diff.c_mviewlog.n_rowid,'null') != 'rowid'
      )
      then
        stmt_add( 'primary key' ); 
        if( nvl(p_table_diff.c_mviewlog.n_rowid,'null') = 'rowid' )
        then
          stmt_add( ',' );
        end if;     
      end if;  
      
      if( nvl(p_table_diff.c_mviewlog.n_rowid,'null') = 'rowid' )
         then 
         stmt_add( 'rowid' );
      end if;  
      
      if( nvl(p_table_diff.c_mviewlog.n_withsequence,'null') = 'sequence' )
         then
         stmt_add( ',' );
         stmt_add( 'sequence' );
      end if;           
                        
      if( p_table_diff.c_mviewlog.c_columns.count() > 0 )
      then
        stmt_add( '(' );
        stmt_add( get_column_list( p_table_diff.c_mviewlog.c_columns ) ); 
        stmt_add( ')' ); 
      end if;     
               
      if( nvl(p_table_diff.c_mviewlog.n_commitscn,'null') = 'commit_scn' )
         then
         stmt_add( ',' );
         stmt_add( 'commit scn' );
      end if;      

      if( p_table_diff.c_mviewlog.n_newvalues is not null )
         then
         stmt_add( p_table_diff.c_mviewlog.n_newvalues.i_literal );
         stmt_add( 'new values' );
      end if;    
      
      if (   p_table_diff.c_mviewlog.n_startwith is not null 
          or p_table_diff.c_mviewlog.n_next is not null 
          or (p_table_diff.c_mviewlog.n_repeatInterval is not null and p_table_diff.c_mviewlog.n_repeatInterval != 0))
        then
          stmt_add( 'purge' );
          if (p_table_diff.c_mviewlog.n_startwith is not null)
            then
              stmt_add( 'start with' );
              stmt_add( 'to_date(''' || p_table_diff.c_mviewlog.n_startwith || ''',''' || c_date_format || ''')' );
          end if;
          if (p_table_diff.c_mviewlog.n_next is not null)
            then
              stmt_add( 'next' );
              stmt_add( 'to_date(''' || p_table_diff.c_mviewlog.n_next || ''',''' || c_date_format || ''')' );
            else 
              if (p_table_diff.c_mviewlog.n_repeatInterval is not null and p_table_diff.c_mviewlog.n_repeatInterval != 0)
              then
                  stmt_add( 'repeat interval' );
                  stmt_add( p_table_diff.c_mviewlog.n_repeatInterval );
              end if;
          end if;
        else
          if( ot_orig_synchronoustype.is_equal( p_table_diff.c_mviewlog.n_synchronous, ot_orig_synchronoustype.c_asynchronous ) = 1 )
            then 
             stmt_add( 'purge immediate asynchronous' );
          end if;
      end if;
      
      add_stmt();
      
    end;

  begin
    if( p_table_diff.c_mviewlog.is_matched = 0 or p_table_diff.c_mviewlog.is_old = 0 )
    then
      if( p_table_diff.c_mviewlog.is_new = 1 )
      then
        create_mviewlog();
      end if;
    else
      select distinct(default_tablespace) into v_default_tablespace from user_users;
      if(   
         p_table_diff.c_mviewlog.e_columns = 0
         or 
         (  is_equal_ignore_case(              'rowid',                              p_table_diff.c_mviewlog.n_rowid                                   ) = 1 and
            p_table_diff.c_mviewlog.e_primarykey = 0
         ) 
         or 
         (  p_table_diff.c_mviewlog.n_rowid is null and
            is_equal_ignore_case(              p_table_diff.c_mviewlog.o_primarykey,     'primary'                                                  ) != 1 
         )    
         or p_table_diff.c_mviewlog.e_rowid = 0
         or p_table_diff.c_mviewlog.e_withsequence = 0
         or p_table_diff.c_mviewlog.e_commitscn = 0
         or p_table_diff.c_mviewlog.e_tablespace = 0              
        )                                        
      then
        add_stmt( 'drop materialized view log on ' || p_table_diff.o_name );   
        
        create_mviewlog();
      else
        if(    p_table_diff.c_mviewlog.e_parallel = 0
            or p_table_diff.c_mviewlog.e_parallel_degree = 0
          )
        then
          stmt_set( 'alter materialized view log on' );
          stmt_add( p_table_diff.n_name );        
          if( ot_orig_paralleltype.is_equal( p_table_diff.c_mviewlog.n_parallel, ot_orig_paralleltype.c_parallel ) = 1 )
          then
            stmt_add( 'parallel' ); 
            if ( p_table_diff.c_mviewlog.n_parallel_degree > 1 )
            then
              stmt_add( p_table_diff.c_mviewlog.n_parallel_degree );
            end if;
          else
            stmt_add( 'noparallel' );         
          end if;         
          
          stmt_done();
        end if;
        
        if(  p_table_diff.c_mviewlog.e_newvalues = 0
          )
        then
          stmt_set( 'alter materialized view log on' );
          stmt_add( p_table_diff.n_name );       
          
          if( ot_orig_newvaluestype.is_equal( p_table_diff.c_mviewlog.n_newvalues, ot_orig_newvaluestype.c_including, ot_orig_newvaluestype.c_excluding ) = 1 )
          then
            stmt_add( 'including' ); 
          else
            stmt_add( 'excluding' );         
          end if;                    
          stmt_add( 'new values' );
          
          stmt_done();
        end if;
        
        if (   p_table_diff.c_mviewlog.e_startwith = 0
            or p_table_diff.c_mviewlog.e_next = 0
            or p_table_diff.c_mviewlog.e_repeatInterval = 0
            )
          then
              stmt_add( 'alter materialized view log on' ); 
              stmt_add( p_table_diff.n_name );    
              stmt_add( 'purge' );   
              if( p_table_diff.c_mviewlog.e_startwith = 0 )
              then
                  stmt_add( 'start with' );
                  stmt_add( 'to_date(''' || p_table_diff.c_mviewlog.n_startwith || ''',''' || c_date_format || ''')' );
              end if;
              if( p_table_diff.c_mviewlog.e_next = 0 )
              then
                  stmt_add( 'next' );
                  stmt_add( 'to_date(''' || p_table_diff.c_mviewlog.n_next || ''',''' || c_date_format ||''')' );
              else 
                  if (p_table_diff.c_mviewlog.e_repeatInterval = 0)
                  then
                      stmt_add( 'repeat interval' );
                      stmt_add( p_table_diff.c_mviewlog.n_repeatInterval );
                  end if;
              end if;
             
          stmt_done();
        else
        
            if( p_table_diff.c_mviewlog.e_synchronous = 0 )
            then
              stmt_add( 'alter materialized view log on' ); 
              stmt_add( p_table_diff.n_name );       
              if( ot_orig_synchronoustype.is_equal( p_table_diff.c_mviewlog.n_synchronous, ot_orig_synchronoustype.c_asynchronous, ot_orig_synchronoustype.c_synchronous ) = 1 )
              then
                  stmt_add( 'purge immediate asynchronous' ); 
              else
                  stmt_add( 'purge immediate synchronous' ); 
              end if;
              
              stmt_done();
            end if;
            
        end if;
        
      end if;
    end if;        
  end;         
  
  procedure handle_primarykey( p_tablename in varchar2, p_primarykey_diff in out nocopy od_orig_primarykey )   
  is
  begin
    if( p_primarykey_diff.is_matched = 0 
     or p_primarykey_diff.is_recreate_needed = 1 
      )
    then
      pv_stmt := 'alter table ' || p_tablename || ' add';
      if( p_primarykey_diff.n_consname is not null )
      then
        pv_stmt := pv_stmt || ' ' || 'constraint ' || p_primarykey_diff.n_consname;        
      end if;
      pv_stmt := pv_stmt || ' ' || 'primary key (' || get_column_list( p_primarykey_diff.c_pk_columns ) || ')';
      
      if(    p_primarykey_diff.n_tablespace is not null
          or p_primarykey_diff.n_reverse is not null )
      then
        pv_stmt := pv_stmt || ' ' || 'using index';
          
        if( p_primarykey_diff.n_reverse is not null )
        then        
          pv_stmt := pv_stmt || ' ' || 'reverse';
        end if;
          
        if( p_primarykey_diff.n_tablespace is not null )
        then        
          pv_stmt := pv_stmt || ' ' || 'tablespace ' || p_primarykey_diff.n_tablespace;
        end if;
      end if;

      add_stmt();
    end if; 
  end;  
  
  procedure handle_table( p_table_diff in out nocopy od_orig_table ) 
  is
    function find_lobstorage( p_column_name in varchar2 ) return od_orig_lobstorage
    is
    begin
      if( p_table_diff.c_lobstorages is null )
      then  
        return null;
      end if;
    
      for i in 1..p_table_diff.c_lobstorages.count
      loop
        if( p_table_diff.c_lobstorages(i).n_column_name = p_column_name )
        then
          return p_table_diff.c_lobstorages(i);
        end if;
      end loop;
  
      return null;
    end;      
    
    procedure handle_column( p_column_diff in out nocopy od_orig_column )
    is     
      procedure recreate_column
      is
        v_tmp_old_column_name varchar2(100) := 'DTO_' || p_column_diff.n_name;
        v_tmp_new_column_name varchar2(100) := 'DTN_' || p_column_diff.n_name;
      begin
        add_stmt( 'alter table ' || p_table_diff.n_name || ' add ' || v_tmp_new_column_name || ' ' || get_column_datatype( p_column_diff ) );
      
        for cur_trigger in
          (
          select trigger_name
            from user_triggers
           where table_name = p_table_diff.n_name
          )
        loop
          add_stmt( 'alter trigger ' || cur_trigger.trigger_name || ' disable' );
        end loop;
      
        add_stmt( 'update ' || p_table_diff.n_name || ' set ' || v_tmp_new_column_name || ' = ' || p_column_diff.o_name );
        add_stmt( 'commit' );
      
        for cur_trigger in
          (
          select trigger_name
            from user_triggers
           where table_name = p_table_diff.n_name
          )
        loop
          add_stmt( 'alter trigger ' || cur_trigger.trigger_name || ' enable' );
        end loop;
      
        add_stmt( 'alter table ' || p_table_diff.n_name || ' rename column ' ||  p_column_diff.o_name || ' to ' || v_tmp_old_column_name );
        add_stmt( 'alter table ' || p_table_diff.n_name || ' rename column ' || v_tmp_new_column_name || ' to ' || p_column_diff.n_name );
        add_stmt( 'alter table ' || p_table_diff.n_name || ' drop column ' || v_tmp_old_column_name );
        
        if( p_column_diff.n_default_value is not null )
        then
          pv_stmt := 'alter table ' || p_table_diff.n_name || ' modify ( ' || p_column_diff.n_name || ' default';
          pv_stmt := pv_stmt || ' ' || p_column_diff.n_default_value;
          pv_stmt := pv_stmt || ' ' || ')';
          add_stmt();
        end if;
        
        if( p_column_diff.n_notnull_flg = 1 )
        then
          pv_stmt := 'alter table ' || p_table_diff.n_name || ' modify ( ' || p_column_diff.n_name;
          pv_stmt := pv_stmt || ' ' || 'not null';
          pv_stmt := pv_stmt || ' ' || ')';
          add_stmt();
        end if;          
      end;
    begin
      if(  p_column_diff.is_matched = 0 )
      then
        pv_stmt := 'alter table ' || p_table_diff.n_name || ' add ' || create_column_create_part( p_column_diff );
        
        if( find_lobstorage( p_column_diff.n_name ) is not null )
        then
          pv_stmt := pv_stmt || ' ' || 'lob (' || p_column_diff.n_name || ') store as ( tablespace ' || find_lobstorage( p_column_diff.n_name ).n_tablespace || ' )';
        end if;
        
        add_stmt;
      else
        if( p_column_diff.is_recreate_needed = 1 )
        then
          recreate_column();
        else
          if(   p_column_diff.e_byteorchar = 0
             or p_column_diff.e_precision = 0
             or p_column_diff.e_scale = 0
            )                       
          then
            add_stmt( 'alter table ' || p_table_diff.n_name || ' modify ( ' || p_column_diff.n_name || ' ' || get_column_datatype( p_column_diff ) || ')' );          
          end if;
        
          if( p_column_diff.e_default_value = 0 )
          then
            pv_stmt := 'alter table ' || p_table_diff.n_name || ' modify ( ' || p_column_diff.n_name || ' default';
            if( p_column_diff.n_default_value is null )
            then
              pv_stmt := pv_stmt || ' ' || 'null';
            else
              pv_stmt := pv_stmt || ' ' || p_column_diff.n_default_value;
            end if;            
            pv_stmt := pv_stmt || ' ' || ')';
            add_stmt();
          end if;
          
          if( p_column_diff.e_notnull_flg = 0 )
          then
            pv_stmt := 'alter table ' || p_table_diff.n_name || ' modify ( ' || p_column_diff.n_name;
            if( p_column_diff.n_notnull_flg = 0 )
            then
              pv_stmt := pv_stmt || ' ' || 'null';
            else
              pv_stmt := pv_stmt || ' ' || 'not null';
            end if;            
            pv_stmt := pv_stmt || ' ' || ')';
            add_stmt();
          end if;          
        end if;                    
      end if;        
    end;    
  
  begin
    if( p_table_diff.is_matched = 1 )
    then
      if(   p_table_diff.e_tablespace = 0
        and pa_orcas_run_parameter.is_tablemovetablespace = 1
        ) 
      then
        stmt_set( 'alter table' );                      
        stmt_add( p_table_diff.n_name); 
        stmt_add( 'move tablespace' );
        stmt_add( p_table_diff.n_tablespace );
        stmt_done;
      end if;
    end if;
    
    if( p_table_diff.is_matched = 0 or p_table_diff.is_recreate_needed = 1 )
    then
      create_table( p_table_diff );
    else    
      for i in 1 .. p_table_diff.c_columns.count
      loop
        if( p_table_diff.c_columns(i).is_new = 1 )
        then
          handle_column( p_table_diff.c_columns(i) );
        else
          drop_with_dropmode_check( 'select 1 from ' || p_table_diff.o_name || ' where ' || p_table_diff.c_columns(i).o_name || ' is not null',
                                    'alter table ' || p_table_diff.o_name || ' drop column ' || p_table_diff.c_columns(i).o_name );        
        end if;
      end loop;
      
      if( p_table_diff.e_logging = 0 )
      then
        if( p_table_diff.n_transactioncontrol is null ) 
        then
            stmt_set( 'alter table' );
            stmt_add( p_table_diff.n_name );        
            if( ot_orig_loggingtype.is_equal( p_table_diff.n_logging, ot_orig_loggingtype.c_nologging ) = 1 )
            then
              stmt_add( 'nologging' ); 
            else
              stmt_add( 'logging' );         
            end if;  
            stmt_done();
        end if;       
        
      end if;
      
      if(    p_table_diff.e_parallel = 0
          or p_table_diff.e_parallel_degree = 0
        )
      then
        stmt_set( 'alter table' );
        stmt_add( p_table_diff.n_name );        
        if( ot_orig_paralleltype.is_equal( p_table_diff.n_parallel, ot_orig_paralleltype.c_parallel ) = 1 )
        then
          stmt_add( 'parallel' ); 
          if ( p_table_diff.n_parallel_degree > 1 )
          then
            stmt_add( p_table_diff.n_parallel_degree );
          end if;
        else
          stmt_add( 'noparallel' );         
        end if;         
        
        stmt_done();
      end if;
      
      if(    ot_orig_permanentnesstype.is_equal( p_table_diff.o_permanentness, ot_orig_permanentnesstype.c_permanent ) = 1 
         and (  p_table_diff.e_compression = 0 
             or p_table_diff.e_compressionFor = 0
             )
        )
      then
        stmt_set( 'alter table' );
        stmt_add( p_table_diff.n_name );        
        if( ot_orig_compresstype.is_equal( p_table_diff.n_compression, ot_orig_compresstype.c_compress, ot_orig_compresstype.c_nocompress ) = 1 )
        then
          stmt_add( 'compress' ); 
          if ( p_table_diff.n_compressionFor is not null )
          then
            stmt_add( 'for ' || adjust_compression_literal(p_table_diff.n_compressionFor.i_literal));
          end if;
        else
          stmt_add( 'nocompress' );         
        end if;         
        
        stmt_done();
      end if;    
    end if;
    
    if( p_table_diff.c_primary_key.is_new = 1 )
    then
      handle_primarykey( p_table_diff.n_name, p_table_diff.c_primary_key );
    end if;    
    
    for i in 1 .. p_table_diff.c_constraints.count
    loop
      if( p_table_diff.c_constraints(i).is_new = 1 )
      then
        handle_constraint( p_table_diff.n_name, p_table_diff.c_constraints(i) );
      end if;
    end loop;
    
    for i in 1 .. p_table_diff.c_ind_uks_index.count
    loop
      if( p_table_diff.c_ind_uks_index(i).is_new = 1 )
      then
        handle_index( p_table_diff.n_name, p_table_diff.c_ind_uks_index(i) );           
      end if;        
    end loop;    
    
    for i in 1 .. p_table_diff.c_ind_uks_uniquekey.count
    loop
      if( p_table_diff.c_ind_uks_uniquekey(i).is_new = 1 )
      then
        handle_uniquekey( p_table_diff.n_name, p_table_diff.c_ind_uks_uniquekey(i) );
      end if;        
    end loop;
    
    for i in 1..p_table_diff.c_comments.count
    loop
      handle_comment( nvl( p_table_diff.n_name, p_table_diff.o_name ), p_table_diff.c_comments(i) );
    end loop;
    
    if( p_table_diff.c_mviewlog.is_equal = 0 )
    then
      handle_mviewlog( p_table_diff );
    end if;
    
  end;
  
  procedure create_foreign_key( p_foreignkey_diff in out nocopy od_orig_foreignkey, p_table_diff in out nocopy od_orig_table )
  is
    v_fk_false_data_select varchar2(32000);
    v_fk_false_data_where_part varchar2(32000);  
  begin
    if(   pa_orcas_run_parameter.is_dropmode = 1 
      and p_table_diff.is_old = 1
      )
    then
      v_fk_false_data_where_part := null;
      for i in 1 .. p_foreignkey_diff.c_srccolumns.count
      loop
        if( v_fk_false_data_where_part is not null )
        then
          v_fk_false_data_where_part := v_fk_false_data_where_part || ' or ';
        end if;
        v_fk_false_data_where_part := v_fk_false_data_where_part || p_foreignkey_diff.c_srccolumns(i).n_column_name || ' is not null ';
      end loop;
                
      v_fk_false_data_where_part := 'where (' || v_fk_false_data_where_part || ') and (' || get_column_list( p_foreignkey_diff.c_srccolumns ) || ') not in (select ' || get_column_list( p_foreignkey_diff.c_destcolumns ) || '  from ' || p_foreignkey_diff.n_desttable || ')';
      v_fk_false_data_select := 'select 1 from ' || p_table_diff.n_name || ' ' || v_fk_false_data_where_part;
                
      if( has_rows_ignore_errors( v_fk_false_data_select ) = 1 )
      then
        if( ot_orig_fkdeleteruletype.is_equal( p_foreignkey_diff.n_delete_rule, ot_orig_fkdeleteruletype.c_cascade ) = 1 )
        then
          add_stmt( 'delete ' || p_table_diff.n_name || ' ' || v_fk_false_data_where_part );
          add_stmt( 'commit' );
        elsif( ot_orig_fkdeleteruletype.is_equal( p_foreignkey_diff.n_delete_rule, ot_orig_fkdeleteruletype.c_set_null ) = 1 )
        then
          add_stmt( 'update ' || p_table_diff.n_name || ' set ' || get_column_list( p_foreignkey_diff.c_srccolumns ) || ' = null ' || v_fk_false_data_where_part );
          add_stmt( 'commit' );                  
        else
          raise_application_error( -20000, 'Fehler beim FK Aufbau ' || p_foreignkey_diff.n_consname || ' auf tabelle ' || p_table_diff.n_name || ' Datenbereinigung nicht möglich, da keine delete rule. ' || v_fk_false_data_select );    
        end if;
      end if;
    end if;
                          
    stmt_set( 'alter table ' || p_table_diff.n_name );
    stmt_add( 'add' );
    stmt_add( create_foreign_key_clause( p_foreignkey_diff ) );
    stmt_done();  
  end;
  
  procedure handle_all_tables is     
  begin
    for i in 1 .. pv_model_diff.c_model_elements_table.count loop
      for j in 1 .. pv_model_diff.c_model_elements_table(i).c_foreign_keys.count
      loop
        if(    pv_model_diff.c_model_elements_table(i).c_foreign_keys(j).is_old = 1
           and (
                  pv_model_diff.c_model_elements_table(i).c_foreign_keys(j).is_matched = 0 
               or pv_model_diff.c_model_elements_table(i).c_foreign_keys(j).is_recreate_needed = 1
               )
          )
        then
          drop_table_constraint_by_name( pv_model_diff.c_model_elements_table(i).o_name, pv_model_diff.c_model_elements_table(i).c_foreign_keys(j).o_consname );
        end if;
      end loop;
    end loop;     
    
    for i in 1 .. pv_model_diff.c_model_elements_table.count loop
      if(      pv_model_diff.c_model_elements_table(i).is_old = 1
           and (
               pv_model_diff.c_model_elements_table(i).is_matched = 0 
            or pv_model_diff.c_model_elements_table(i).is_recreate_needed = 1                
               )
        )
      then
        drop_with_dropmode_check( 'select 1 from ' || pv_model_diff.c_model_elements_table(i).o_name,
                                    'drop table ' || pv_model_diff.c_model_elements_table(i).o_name );
      else
        for j in 1 .. pv_model_diff.c_model_elements_table(i).c_constraints.count
        loop
          if(    pv_model_diff.c_model_elements_table(i).c_constraints(j).is_old = 1
             and (
                   pv_model_diff.c_model_elements_table(i).c_constraints(j).is_matched = 0 
                or pv_model_diff.c_model_elements_table(i).c_constraints(j).is_recreate_needed = 1                    
                 )
            )
          then
            drop_table_constraint_by_name( pv_model_diff.c_model_elements_table(i).o_name, pv_model_diff.c_model_elements_table(i).c_constraints(j).o_consname );
          end if;
        end loop;
        
        if(    pv_model_diff.c_model_elements_table(i).c_mviewlog.is_old = 1
           and (
                 pv_model_diff.c_model_elements_table(i).c_mviewlog.is_matched = 0
              or pv_model_diff.c_model_elements_table(i).c_mviewlog.is_recreate_needed = 1                    
               )
          )           
        then
          add_stmt( 'drop materialized view log on ' || pv_model_diff.c_model_elements_table(i).o_name );         
        end if;        
        
        for j in 1 .. pv_model_diff.c_model_elements_table(i).c_ind_uks_uniquekey.count
        loop
          if(    pv_model_diff.c_model_elements_table(i).c_ind_uks_uniquekey(j).is_old = 1
             and (
                   pv_model_diff.c_model_elements_table(i).c_ind_uks_uniquekey(j).is_matched = 0
                or pv_model_diff.c_model_elements_table(i).c_ind_uks_uniquekey(j).is_recreate_needed = 1                    
                 )
            )             
          then
            drop_table_constraint_by_name( pv_model_diff.c_model_elements_table(i).o_name, pv_model_diff.c_model_elements_table(i).c_ind_uks_uniquekey(j).o_consname );            
          end if;
        end loop;        
        
        for j in 1 .. pv_model_diff.c_model_elements_table(i).c_ind_uks_index.count
        loop
          if(    pv_model_diff.c_model_elements_table(i).c_ind_uks_index(j).is_old = 1
             and (
                   pv_model_diff.c_model_elements_table(i).c_ind_uks_index(j).is_matched = 0 
                or pv_model_diff.c_model_elements_table(i).c_ind_uks_index(j).is_recreate_needed = 1                    
                 )
            )
          then
            add_stmt( 'drop index ' || pv_model_diff.c_model_elements_table(i).c_ind_uks_index(j).o_consname );     
          end if;
        end loop;        
        
        
        for j in 1 .. pv_model_diff.c_model_elements_table(i).c_comments.count
        loop
          if(    pv_model_diff.c_model_elements_table(i).c_comments(j).is_old = 1
             and pv_model_diff.c_model_elements_table(i).c_comments(j).is_matched = 0 )
          then
            stmt_set( 'comment on' );
            stmt_add( pv_model_diff.c_model_elements_table(i).c_comments(j).o_comment_object.i_name );                
            stmt_add( ' ' );                
            stmt_add( pv_model_diff.c_model_elements_table(i).o_name );
            if( pv_model_diff.c_model_elements_table(i).c_comments(j).o_column_name is not null )
            then
              stmt_add( '.' );
              stmt_add( pv_model_diff.c_model_elements_table(i).c_comments(j).o_column_name );                
            end if;
            stmt_add( 'is ''''' );        
                
            add_stmt();     
          end if;
        end loop;          
        
        if(    pv_model_diff.c_model_elements_table(i).c_primary_key.is_old = 1
           and (
                 pv_model_diff.c_model_elements_table(i).c_primary_key.is_matched = 0
              or pv_model_diff.c_model_elements_table(i).c_primary_key.is_recreate_needed = 1                    
               )
          )           
        then
          drop_table_constraint_by_name( pv_model_diff.c_model_elements_table(i).o_name, pv_model_diff.c_model_elements_table(i).c_primary_key.o_consname );            
        end if;          
      end if;        
    end loop;    
    

    for i in 1 .. pv_model_diff.c_model_elements_table.count loop
      if( pv_model_diff.c_model_elements_table(i).is_new = 1 ) 
      then              
        -- Sonderbehandlung für Tabellen zu Mviews, die nicht prebuilt sind, sie dürfen nicht behandelt werden        
        handle_table( pv_model_diff.c_model_elements_table(i) );
      end if;        
    end loop;    
  
    for i in 1 .. pv_model_diff.c_model_elements_table.count loop
      for j in 1 .. pv_model_diff.c_model_elements_table(i).c_foreign_keys.count
      loop
        if(    pv_model_diff.c_model_elements_table(i).c_foreign_keys(j).is_new = 1
           and (
                  pv_model_diff.c_model_elements_table(i).c_foreign_keys(j).is_matched = 0 
               or pv_model_diff.c_model_elements_table(i).c_foreign_keys(j).is_recreate_needed = 1
               )
          )        
        then
          if(   pv_model_diff.c_model_elements_table(i).is_old = 0
            and pv_model_diff.c_model_elements_table(i).c_tablepartitioning_refpartiti.is_new = 1
            and pv_model_diff.c_model_elements_table(i).c_tablepartitioning_refpartiti.n_fkname = pv_model_diff.c_model_elements_table(i).c_foreign_keys(j).n_consname
            )
          then
            -- in diesem Fall haben wir eine ref-partitionierte Tabelle die in diesem Lauf angelegt wurde, und damit ist der get_fk_for_ref_partitioning schon angelegt worden.
            null;
          else
            create_foreign_key( pv_model_diff.c_model_elements_table(i).c_foreign_keys(j), pv_model_diff.c_model_elements_table(i) );    
          end if;
        end if;
      end loop;
    end loop;     
  end;
  
  procedure execute_all_statements is    
  begin
    commit;
  
    for i in 1..pv_statement_list.count
    loop
--      dbms_output.put_line( pv_statement_list(i) );
      pa_orcas_exec_log.exec_stmt( pv_statement_list(i) );   
    end loop;
  end;
  
  procedure update_is_recreate_needed
  is
    function is_index_recreate( p_table_diff in out nocopy od_orig_table, p_indexname varchar2 ) return number
    is
    begin
      for i in 1..p_table_diff.c_ind_uks_index.count
      loop
        if( p_table_diff.c_ind_uks_index(i).n_consname = p_indexname )
        then
          return p_table_diff.c_ind_uks_index(i).is_recreate_needed;
        end if;
      end loop;
      
      raise_application_error( -20000, 'index not found: ' || p_indexname || ' ' || p_table_diff.n_name );
    end;
    
    function is_recreate_column( p_column_diff in out nocopy od_orig_column ) return number
    is
    begin
      if( p_column_diff.n_data_type is not null and  p_column_diff.o_data_type is not null )
      then
        if( p_column_diff.e_data_type = 0 )
        then
          return 1;
        end if;
        
        if( p_column_diff.n_precision < p_column_diff.o_precision )
        then
          return 1;          
        end if;
        
        if( p_column_diff.n_scale < p_column_diff.o_scale )
        then
          return 1;
        end if;
      end if;  
      
      if( p_column_diff.e_object_type = 0 )
      then
        return 1;
      end if;        

      return 0;        
    end;
          
  begin
    for i in 1..pv_model_diff.c_model_elements_table.count
    loop
      if( pv_model_diff.c_model_elements_table(i).is_matched = 1 )
      then 
        if( pv_model_diff.c_model_elements_table(i).e_permanentness = 0 
         or pv_model_diff.c_model_elements_table(i).e_transactioncontrol = 0 )
        then 
          pv_model_diff.c_model_elements_table(i).is_recreate_needed := 1;
        end if;
        
        for j in 1..pv_model_diff.c_model_elements_table(i).c_columns.count
        loop
          if( pv_model_diff.c_model_elements_table(i).c_columns(j).is_matched = 1 )
          then
            if( is_recreate_column( pv_model_diff.c_model_elements_table(i).c_columns(j) ) = 1 )
            then
              pv_model_diff.c_model_elements_table(i).c_columns(j).is_recreate_needed := 1;
            end if;
          end if;
        end loop;        
        
        if( pv_model_diff.c_model_elements_table(i).c_primary_key.is_matched = 1 )        
        then
          if( pv_model_diff.c_model_elements_table(i).c_primary_key.e_consname = 0
           or pv_model_diff.c_model_elements_table(i).c_primary_key.e_pk_columns = 0
           or pv_model_diff.c_model_elements_table(i).c_primary_key.e_reverse = 0
           or ( 
                pv_model_diff.c_model_elements_table(i).c_primary_key.e_tablespace = 0 
            and pa_orcas_run_parameter.is_indexmovetablespace = 1 
              )        
            )
          then
            pv_model_diff.c_model_elements_table(i).c_primary_key.is_recreate_needed := 1;
          end if;
        end if;
        
        for j in 1..pv_model_diff.c_model_elements_table(i).c_ind_uks_index.count
        loop
          if( pv_model_diff.c_model_elements_table(i).c_ind_uks_index(j).is_matched = 1 )
          then
            if
              (   
                (
                  (
                    pv_model_diff.c_model_elements_table(i).c_ind_uks_index(j).e_index_columns = 0
                 or pv_model_diff.c_model_elements_table(i).c_ind_uks_index(j).e_function_based_expression = 0
                 or pv_model_diff.c_model_elements_table(i).c_ind_uks_index(j).e_domain_index_expression = 0
                  )              
  -- domain index kann nicht abgeglichen werden                           
              and pv_model_diff.c_model_elements_table(i).c_ind_uks_index(j).n_domain_index_expression is null
                )
             or pv_model_diff.c_model_elements_table(i).c_ind_uks_index(j).e_unique = 0
             or pv_model_diff.c_model_elements_table(i).c_ind_uks_index(j).e_bitmap = 0
             or pv_model_diff.c_model_elements_table(i).c_ind_uks_index(j).e_global = 0
             or pv_model_diff.c_model_elements_table(i).c_ind_uks_index(j).e_compression = 0
              )
            then
              pv_model_diff.c_model_elements_table(i).c_ind_uks_index(j).is_recreate_needed := 1;
            end if;
          end if;
        end loop;
        
        for j in 1..pv_model_diff.c_model_elements_table(i).c_ind_uks_uniquekey.count
        loop
          if( pv_model_diff.c_model_elements_table(i).c_ind_uks_uniquekey(j).is_matched = 1 )
          then
            if(
                pv_model_diff.c_model_elements_table(i).c_ind_uks_uniquekey(j).e_uk_columns = 0
             or pv_model_diff.c_model_elements_table(i).c_ind_uks_uniquekey(j).e_indexname = 0
              )
            then
              pv_model_diff.c_model_elements_table(i).c_ind_uks_uniquekey(j).is_recreate_needed := 1;
            else
              if( pv_model_diff.c_model_elements_table(i).c_ind_uks_uniquekey(j).n_indexname is not null )
              then
                if( is_index_recreate( pv_model_diff.c_model_elements_table(i), pv_model_diff.c_model_elements_table(i).c_ind_uks_uniquekey(j).n_indexname ) = 1 )
                then
                  pv_model_diff.c_model_elements_table(i).c_ind_uks_uniquekey(j).is_recreate_needed := 1;                  
                end if;
              end if;
            end if;
          end if;
        end loop;
                
        for j in 1..pv_model_diff.c_model_elements_table(i).c_constraints.count
        loop
          if( pv_model_diff.c_model_elements_table(i).c_constraints(j).is_matched = 1 )
          then        
            if(  pv_model_diff.c_model_elements_table(i).c_constraints(j).e_rule = 0 
              or pv_model_diff.c_model_elements_table(i).c_constraints(j).e_deferrtype = 0
              )
            then
              pv_model_diff.c_model_elements_table(i).c_constraints(j).is_recreate_needed := 1;
            end if;
          end if;
        end loop;        
        
        for j in 1..pv_model_diff.c_model_elements_table(i).c_foreign_keys.count
        loop
          if( pv_model_diff.c_model_elements_table(i).c_foreign_keys(j).is_matched = 1 )
          then        
            if( pv_model_diff.c_model_elements_table(i).c_foreign_keys(j).is_equal = 0 )
            then
              pv_model_diff.c_model_elements_table(i).c_foreign_keys(j).is_recreate_needed := 1;
            end if;
          end if;
        end loop;                
        
      end if;        
    end loop;
  end;

  procedure compare_and_update( pi_model_ist in ot_orig_model, pi_model_soll in ot_orig_model )
  is
    v_nls_length_default varchar2(100);
  begin
    select value 
      into v_nls_length_default 
      from nls_instance_parameters 
     where parameter = 'NLS_LENGTH_SEMANTICS';
     
    if( v_nls_length_default = 'BYTE' )
    then
      pv_default_orig_chartype := ot_orig_chartype.c_byte;
    else
      pv_default_orig_chartype := ot_orig_chartype.c_char;
    end if;    
    
    select default_tablespace,
           temporary_tablespace
      into pv_default_tablespace,
           pv_temporary_tablespace 
      from user_users;    
  
    pv_model_diff := pa_orcas_merge.get_merge_result( pi_model_soll, pi_model_ist );  
    
    update_is_recreate_needed();

    -- dass sortieren der istdaten ist unnoetig, aber im moment die einfachste moeglichkeit die liste der tabellen zu ermitteln
    
    pv_statement_list := new t_varchar_list();
    pv_stmt := null;
    
    handle_all_tables();   

    handle_all_sequences();   
    
    handle_all_mviews();
    
    execute_all_statements();    
  end;   

end;
/
