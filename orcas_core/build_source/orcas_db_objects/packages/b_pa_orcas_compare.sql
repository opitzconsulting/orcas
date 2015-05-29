CREATE OR REPLACE package body pa_orcas_compare is
  pv_model_ist ot_orig_model;  
  pv_model_soll ot_orig_model;
  pv_orig_table_list_ist ct_orig_table_list;
  pv_orig_table_list_soll ct_orig_table_list;
  pv_orig_mview_list_ist ct_orig_mview_list;
  pv_orig_mview_list_soll ct_orig_mview_list;
  pv_orig_sequence_list_ist ct_orig_sequence_list;    
  pv_orig_sequence_list_soll ct_orig_sequence_list;      
  pv_stmt varchar2(32000);
  type t_varchar_list is table of varchar2(32000);
  pv_statement_list t_varchar_list;
  pv_default_orig_chartype ot_orig_chartype;
  pv_default_tablespace varchar2(30);
  pv_temporary_tablespace varchar2(30); 
  
  function get_column_list( p_columnref_list in ct_orig_columnref_list ) return varchar2
  is
    v_return varchar2(2000);
  begin
    v_return := p_columnref_list(1).i_column_name;
    
    for i in 2..p_columnref_list.count()
    loop
      v_return := v_return || ',' || p_columnref_list(i).i_column_name;
    end loop;
    
    return v_return;
  end;
  
  function is_equal( p_val1 number, p_val2 number ) return number
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
  
  function is_equal( p_val1 number, p_val2 number, p_default number ) return number
  is
  begin
    if( p_val1 is null and p_val2 is null )
    then
      return 1;
    end if;
    if( nvl(p_val1,p_default) = nvl(p_val2,p_default) )
    then
      return 1;
    else
      return 0;
    end if;
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
  
  function is_equal( p_val1 ct_orig_columnref_list, p_val2 ct_orig_columnref_list ) return number
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
    
    return is_equal_ignore_case( get_column_list( p_val1 ), get_column_list( p_val2 ) );
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
  
  function is_equal_foreignkey( p_val1 ot_orig_foreignkey, p_val2 ot_orig_foreignkey ) return number  
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
    if(  is_equal_ignore_case              ( p_val1.i_consname,     p_val2.i_consname     ) != 1
      or is_equal                          ( p_val1.i_srccolumns,   p_val2.i_srccolumns   ) != 1        
      or is_equal_ignore_case              ( p_val1.i_desttable,    p_val2.i_desttable    ) != 1                  
      or is_equal                          ( p_val1.i_destcolumns,  p_val2.i_destcolumns  ) != 1                          
      or ot_orig_deferrtype.is_equal       ( p_val1.i_deferrtype,   p_val2.i_deferrtype   ) != 1                                       
      or ot_orig_fkdeleteruletype.is_equal ( p_val1.i_delete_rule,  p_val2.i_delete_rule, ot_orig_fkdeleteruletype.c_no_action ) != 1                                                    
            )
    then
      return 0;
    else
      return 1;
    end if;
  end;  
  
  function get_column_datatype( p_column in ot_orig_column ) return varchar2
  is
    v_datatype       varchar2(100);    
  begin
    if ( ot_orig_datatype.is_equal( p_column.i_data_type, ot_orig_datatype.c_object ) = 1 ) 
    then
      v_datatype := p_column.i_object_type;
    else
      if( ot_orig_datatype.is_equal( p_column.i_data_type, ot_orig_datatype.c_long_raw ) = 1 )
      then
        v_datatype := 'long raw';
      else
        v_datatype := upper(p_column.i_data_type.i_name);    
      end if;
      
      if( p_column.i_precision != 0 )
      then
        v_datatype := v_datatype || '(' || p_column.i_precision;
  
        if( p_column.i_scale != 0 )
        then
          v_datatype := v_datatype || ',' || p_column.i_scale;            
        end if;
        
        if( p_column.i_byteorchar is not null )
        then
          v_datatype := v_datatype || ' ' || upper(p_column.i_byteorchar.i_name);            
        end if;      
        
        v_datatype := v_datatype || ')';      
      end if;
      
      if ( is_equal_ignore_case( p_column.i_with_time_zone, 'with_time_zone' ) = 1 )
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
  
  procedure drop_table_constraint_by_name( p_orig_table ot_orig_table, p_constraint_name varchar2 )
  is
  begin
    add_stmt( 'alter table ' || p_orig_table.i_name || ' drop constraint ' || p_constraint_name );
  end;
  
  procedure drop_table_pk_if_exists( p_orig_table ot_orig_table )
  is
  begin
    if( p_orig_table is not null and p_orig_table.i_primary_key is not null )
    then
      drop_table_constraint_by_name( p_orig_table, p_orig_table.i_primary_key.i_consname );
    end if;
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
        raise_application_error( -20000, 'drop mode ist nicht aktiv, daher kann folgendes statement nicht ausgefuehrt werden: ' || p_stmt_to_execute );
      end if;
    end if;
    
    add_stmt( p_stmt_to_execute );
  end;  
  
  function find_sequence( p_orig_sequence in ot_orig_sequence, p_orig_sequence_list in ct_orig_sequence_list ) return ot_orig_sequence
  is
  begin
    for i in 1 .. p_orig_sequence_list.count loop
      if( upper(p_orig_sequence_list(i).i_sequence_name) = upper(p_orig_sequence.i_sequence_name) )
      then
        return p_orig_sequence_list(i);
      end if;
    end loop;
    
    return null;
  end;   

  procedure handle_sequence( p_orig_sequence_soll in ot_orig_sequence )
  is
    v_orig_sequence_ist ot_orig_sequence;
    v_soll_start_value number;
    v_ist_value number;
    v_increment_by number;
    v_cache_size number;
    v_min_value number;
    v_max_value number;
    v_cycle_flag ot_orig_cycletype;
    v_order_flag ot_orig_ordertype;
    
    procedure load_start_value is
      type curtyp is ref          cursor;
      cur_max_used_value          curtyp;  -- declare cursor variable
    begin
      if( p_orig_sequence_soll.i_max_value_select is not null )
      then
        open cur_max_used_value for p_orig_sequence_soll.i_max_value_select;
        fetch cur_max_used_value into v_soll_start_value;
        close cur_max_used_value;
        
        v_soll_start_value := v_soll_start_value + 1;
      end if;
    exception when others then 
      null; -- kann vorkommen, wenn fuer das select benoetigte Tabellen nicht exisitieren. kann erst richtig korrigiert werden, wenn auch der Tabellenabgleich auf dieses Package umgestellt wurde
    end;
  begin
    load_start_value();
    v_orig_sequence_ist := find_sequence( p_orig_sequence_soll, pv_orig_sequence_list_ist );
    v_increment_by := p_orig_sequence_soll.i_increment_by;
    v_cache_size := p_orig_sequence_soll.i_cache;
    v_min_value := p_orig_sequence_soll.i_minvalue;
    v_max_value := p_orig_sequence_soll.i_maxvalue;
    v_cycle_flag := p_orig_sequence_soll.i_cycle;
    v_order_flag := p_orig_sequence_soll.i_order;
    
    if( p_orig_sequence_soll.i_increment_by is null or p_orig_sequence_soll.i_increment_by = 0 )    
    then
      v_increment_by := 1;
    end if;
    
    if( v_orig_sequence_ist is null )
    then
      pv_stmt := 'create sequence ' || p_orig_sequence_soll.i_sequence_name;
      pv_stmt := pv_stmt || ' increment by ' || v_increment_by;
      if( v_soll_start_value is not null )
      then
        pv_stmt := pv_stmt || ' start with ' || v_soll_start_value;      
      end if;
      
      if( v_max_value is not null and v_max_value > 0)
      then
        pv_stmt := pv_stmt || ' maxvalue ' || v_max_value;      
      end if;
      
      if( v_min_value is not null and v_min_value > 0)
      then
        pv_stmt := pv_stmt || ' minvalue ' || v_min_value;      
      end if;
      
      if( v_cycle_flag is not null )
      then
        pv_stmt := pv_stmt || ' ' || v_cycle_flag.i_literal;      
      end if;
      
      if( v_cache_size is not null and v_cache_size > 0)
      then
        pv_stmt := pv_stmt || ' cache ' || v_cache_size;      
      end if;
      
      if( v_order_flag is not null )
      then
        pv_stmt := pv_stmt || ' ' || v_order_flag.i_literal;      
      end if;
      
      add_stmt();
    else
      v_ist_value := v_orig_sequence_ist.i_max_value_select;
      if( v_ist_value < v_soll_start_value )
      then
        add_stmt( 'alter sequence '||p_orig_sequence_soll.i_sequence_name||' increment by '||(v_soll_start_value-v_ist_value) );
        add_stmt( 'declare v_dummy number; begin select '||p_orig_sequence_soll.i_sequence_name||'.nextval into v_dummy from dual; end;' );
        add_stmt( 'alter sequence '||p_orig_sequence_soll.i_sequence_name||' increment by ' || v_increment_by );
      else
        if( v_increment_by != v_orig_sequence_ist.i_increment_by )
        then  
          add_stmt( 'alter sequence '||p_orig_sequence_soll.i_sequence_name||' increment by ' || v_increment_by );        
        end if;
      end if;   
      
      if( v_max_value is not null and v_max_value > 0 and v_max_value <> v_orig_sequence_ist.i_maxvalue)
      then
        add_stmt( 'alter sequence '|| p_orig_sequence_soll.i_sequence_name || ' maxvalue ' || v_max_value );        
      end if;
      
      if( v_min_value is not null and v_min_value > 0 and v_min_value <> v_orig_sequence_ist.i_minvalue)
      then
        add_stmt( 'alter sequence '|| p_orig_sequence_soll.i_sequence_name || ' minvalue ' || v_min_value );        
      end if;
      
      if( v_cycle_flag is not null and v_cycle_flag.i_literal <> v_orig_sequence_ist.i_cycle.i_literal)
      then
        add_stmt( 'alter sequence '|| p_orig_sequence_soll.i_sequence_name || ' ' || v_cycle_flag.i_literal );        
      end if;
      
      if( v_cache_size is not null and v_cache_size > 0 and v_cache_size <> v_orig_sequence_ist.i_cache)
      then
        add_stmt( 'alter sequence '|| p_orig_sequence_soll.i_sequence_name || ' cache ' || v_cache_size );    
      end if;
      
      if( v_order_flag is not null and v_order_flag.i_literal <> v_orig_sequence_ist.i_order.i_literal)
      then
        add_stmt( 'alter sequence '|| p_orig_sequence_soll.i_sequence_name || ' ' || v_order_flag.i_literal );        
      end if;
      
    end if;
  end;
  
  procedure handle_all_sequences
  is
    v_modelelement ot_orig_modelelement;
    v_orig_sequence ot_orig_sequence;
  begin
    for i in 1 .. pv_model_soll.i_model_elements.count loop
      v_modelelement := pv_model_soll.i_model_elements(i);
  
      if( v_modelelement is of (ot_orig_sequence) ) 
      then
        v_orig_sequence := treat( v_modelelement as ot_orig_sequence );
        handle_sequence( v_orig_sequence );
      end if;
    end loop;
    
    for i in 1 .. pv_model_ist.i_model_elements.count loop
      v_modelelement := pv_model_ist.i_model_elements(i);
  
      if( v_modelelement is of (ot_orig_sequence) ) 
      then
        v_orig_sequence := treat( v_modelelement as ot_orig_sequence );      
        if( find_sequence( v_orig_sequence, pv_orig_sequence_list_soll ) is null )
        then
          add_stmt( 'drop sequence ' || v_orig_sequence.i_sequence_name );
        end if;
      end if;
    end loop;    
  end;
  
  function find_mview( p_orig_mview in ot_orig_mview, p_orig_mview_list in ct_orig_mview_list ) return ot_orig_mview
  is
  begin
    for i in 1 .. p_orig_mview_list.count loop
      if( upper(p_orig_mview_list(i).i_mview_name) = upper(p_orig_mview.i_mview_name) )
      then
        return p_orig_mview_list(i);
      end if;
    end loop;
    
    return null;
  end;   
  
  function find_mview_by_table( p_orig_table in ot_orig_table, p_orig_mview_list in ct_orig_mview_list ) return ot_orig_mview
  is
    v_orig_mview ot_orig_mview;
  begin
    for i in 1 .. p_orig_mview_list.count loop
      if( upper(p_orig_mview_list(i).i_mview_name) = upper(p_orig_table.i_name) )
      then
        return p_orig_mview_list(i);
      end if;
    end loop;
    
    return null;
  end;     
  
  procedure handle_mview( p_orig_mview_soll ot_orig_mview )
  is     
    v_orig_mview_ist ot_orig_mview;    
    v_orig_refreshmodetype ot_orig_refreshmodetype;
    v_refreshmode varchar2(10);

      
    procedure create_mview
    is
    begin
      stmt_set( 'create materialized view' );
      stmt_add( p_orig_mview_soll.i_mview_name );
      
      if( ot_orig_buildmodetype.is_equal( p_orig_mview_soll.i_buildmode, ot_orig_buildmodetype.c_prebuilt, ot_orig_buildmodetype.c_immediate ) = 1 )
      then
        stmt_add( 'on prebuilt table' );
      else
        -- Physical properties nur, wenn nicht prebuilt
        if( p_orig_mview_soll.i_tablespace is not null )
        then
          stmt_add( 'tablespace' ); 
          stmt_add( p_orig_mview_soll.i_tablespace );            
        end if;   
    
        if( ot_orig_compresstype.is_equal( p_orig_mview_soll.i_compression, ot_orig_compresstype.c_compress, ot_orig_compresstype.c_nocompress ) = 1 )
        then
          stmt_add( 'compress' );
          if( ot_orig_compressfortype.is_equal( p_orig_mview_soll.i_compressionfor, ot_orig_compressfortype.c_all ) = 1 )
          then
            stmt_add( 'for all operations' );
          elsif ( ot_orig_compressfortype.is_equal( p_orig_mview_soll.i_compressionfor, ot_orig_compressfortype.c_direct_load ) = 1 )
            then
            stmt_add( 'for direct_load operations' );
          elsif ( ot_orig_compressfortype.is_equal( p_orig_mview_soll.i_compressionfor, ot_orig_compressfortype.c_query_low ) = 1 )
            then
            stmt_add( 'for query low' );  
          elsif ( ot_orig_compressfortype.is_equal( p_orig_mview_soll.i_compressionfor, ot_orig_compressfortype.c_query_high ) = 1 )
            then
            stmt_add( 'for query high' );   
          elsif ( ot_orig_compressfortype.is_equal( p_orig_mview_soll.i_compressionfor, ot_orig_compressfortype.c_archive_low ) = 1 )
            then
            stmt_add( 'for archive low' );  
          elsif ( ot_orig_compressfortype.is_equal( p_orig_mview_soll.i_compressionfor, ot_orig_compressfortype.c_archive_high ) = 1 )
            then
            stmt_add( 'for archive high' );             
          end if;
        else
          stmt_add( 'nocompress' );
        end if;
        
        if( ot_orig_paralleltype.is_equal( p_orig_mview_soll.i_parallel, ot_orig_paralleltype.c_parallel ) = 1 )
        then
          stmt_add( 'parallel' ); 
          if ( p_orig_mview_soll.i_parallel_degree > 1 )
          then
            stmt_add( p_orig_mview_soll.i_parallel_degree );
          end if;
        else
          stmt_add( 'noparallel' );               
        end if;      
          
        if( p_orig_mview_soll.i_buildmode is not null )
        then   
          stmt_add( 'build' );
          stmt_add( p_orig_mview_soll.i_buildmode.i_literal );   
        end if;  
      end if;
        
      if( p_orig_mview_soll.i_refreshmethod is not null )
      then
        stmt_add( adjust_refreshmethod_literal(p_orig_mview_soll.i_refreshmethod.i_literal) );    
        
        if( p_orig_mview_soll.i_refreshmode is not null )
        then
          stmt_add( 'on' );
          stmt_add( p_orig_mview_soll.i_refreshmode.i_literal );            
        end if; 
      end if;               

      if( ot_orig_enabletype.is_equal( p_orig_mview_soll.i_queryrewrite, ot_orig_enabletype.c_enable ) = 1 )
      then
        stmt_add( 'enable query rewrite' ); 
      end if;  
        
      stmt_add( 'as' ); 
      stmt_add( replace_linefeed_by_space(p_orig_mview_soll.i_viewselectclob) );   
      add_stmt(); 
    end;

  begin
    v_orig_mview_ist := find_mview( p_orig_mview_soll, pv_orig_mview_list_ist );
      
    if( v_orig_mview_ist is null )
    then
      create_mview();
    else
      if(    
        ( is_equal_ignore_case(v_orig_mview_ist.i_tablespace, nvl(p_orig_mview_soll.i_tablespace, pv_default_tablespace)) != 1
          and not ( v_orig_mview_ist.i_tablespace is null and p_orig_mview_soll.i_tablespace is null ) )
        or is_equal_ignore_case(replace(v_orig_mview_ist.i_viewselectclob, '"', ''), replace_linefeed_by_space(p_orig_mview_soll.i_viewselectclob)) != 1                             
        or ot_orig_buildmodetype.is_equal(  v_orig_mview_ist.i_buildmode,  p_orig_mview_soll.i_buildmode, ot_orig_buildmodetype.c_immediate ) != 1
        )                                        
      then
        add_stmt( 'drop materialized view ' || p_orig_mview_soll.i_mview_name );   
            
        create_mview();
      else 
        if( ot_orig_enabletype.is_equal(  v_orig_mview_ist.i_queryrewrite,  p_orig_mview_soll.i_queryrewrite, ot_orig_enabletype.c_disable ) != 1 )
        then
          add_stmt( 'alter materialized view ' || p_orig_mview_soll.i_mview_name || ' ' || p_orig_mview_soll.i_queryrewrite.i_literal || ' query rewrite');  
        end if;
        
        if( ot_orig_refreshmethodtype.is_equal(  v_orig_mview_ist.i_refreshmethod,  p_orig_mview_soll.i_refreshmethod, ot_orig_refreshmethodtype.c_force ) != 1 
          or ot_orig_refreshmodetype.is_equal(  v_orig_mview_ist.i_refreshmode,  p_orig_mview_soll.i_refreshmode, ot_orig_refreshmodetype.c_demand ) != 1
        )
        then
          v_orig_refreshmodetype := p_orig_mview_soll.i_refreshmode;
          if ( v_orig_refreshmodetype is null ) 
          then
            v_refreshmode := '';
          else
            v_refreshmode := ' on ' || v_orig_refreshmodetype.i_literal;
          end if;
          add_stmt( 'alter materialized view ' || p_orig_mview_soll.i_mview_name || ' ' || adjust_refreshmethod_literal(p_orig_mview_soll.i_refreshmethod.i_literal) || v_refreshmode );  
        end if;
        
        -- Physical parameters nur, wenn nicht prebuilt
        if ( ot_orig_buildmodetype.is_equal(  p_orig_mview_soll.i_buildmode,  ot_orig_buildmodetype.c_prebuilt, ot_orig_buildmodetype.c_immediate ) != 1 )
        then        
         if(    ot_orig_paralleltype.is_equal(  v_orig_mview_ist.i_parallel,        p_orig_mview_soll.i_parallel,         ot_orig_paralleltype.c_noparallel ) != 1 
              or is_equal(                       v_orig_mview_ist.i_parallel_degree, p_orig_mview_soll.i_parallel_degree,  0 )                                 != 1
            )
          then
            stmt_set( 'alter materialized view' );
            stmt_add( p_orig_mview_soll.i_mview_name );        
            if( ot_orig_paralleltype.is_equal( p_orig_mview_soll.i_parallel, ot_orig_paralleltype.c_parallel ) = 1 )
            then
              stmt_add( 'parallel' ); 
              if ( p_orig_mview_soll.i_parallel_degree > 1 )
              then
                stmt_add( p_orig_mview_soll.i_parallel_degree );
              end if;
            else
              stmt_add( 'noparallel' );         
            end if;         
            
            stmt_done();
          end if;    
          
          if(  ot_orig_compresstype.is_equal(  v_orig_mview_ist.i_compression,   p_orig_mview_soll.i_compression,    ot_orig_compresstype.c_nocompress ) != 1 
              or ot_orig_compressfortype.is_equal(  v_orig_mview_ist.i_compressionFor,   p_orig_mview_soll.i_compressionFor,    ot_orig_compressfortype.c_direct_load ) != 1
            )
          then
            stmt_set( 'alter materialized view' );
            stmt_add( p_orig_mview_soll.i_mview_name );        
            if( ot_orig_compresstype.is_equal( p_orig_mview_soll.i_compression, ot_orig_compresstype.c_compress, ot_orig_compresstype.c_nocompress ) = 1 )
            then
              stmt_add( 'compress' ); 
              if ( p_orig_mview_soll.i_compressionFor is not null )
              then
                stmt_add( 'for ' || adjust_compression_literal(p_orig_mview_soll.i_compressionFor.i_literal));
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
    v_modelelement ot_orig_modelelement;
    v_orig_mview ot_orig_mview;
  begin
    
    for i in 1 .. pv_model_soll.i_model_elements.count loop
      v_modelelement := pv_model_soll.i_model_elements(i);
  
      if( v_modelelement is of (ot_orig_mview) ) 
      then
        v_orig_mview := treat( v_modelelement as ot_orig_mview );
        handle_mview( v_orig_mview );
      end if;
    end loop;
    
    for i in 1 .. pv_model_ist.i_model_elements.count loop
      v_modelelement := pv_model_ist.i_model_elements(i);
  
      if( v_modelelement is of (ot_orig_mview) ) 
      then
        v_orig_mview := treat( v_modelelement as ot_orig_mview );      
        if( find_mview( v_orig_mview, pv_orig_mview_list_soll ) is null )
        then
          add_stmt( 'drop materialized view ' || v_orig_mview.i_mview_name );
        end if;
      end if;
    end loop;        
  end;
  
  function find_table_position_by_name( p_tablename in varchar2, p_orig_table_list in ct_orig_table_list ) return number
  is
  begin
    for i in 1 .. p_orig_table_list.count loop
      if( upper(p_orig_table_list(i).i_name) = upper(p_tablename) )
      then
        return i;
      end if;
    end loop;
    
    return null;
  end; 
  
  function find_table( p_orig_table in ot_orig_table, p_orig_table_list in ct_orig_table_list ) return ot_orig_table
  is
    v_table_index number;
  begin
    v_table_index := find_table_position_by_name( p_orig_table.i_name, p_orig_table_list );
    
    if( v_table_index is null )
    then
      return null;
    else    
      return p_orig_table_list( v_table_index );
    end if;
  end;    
  
  function create_column_create_part( p_orig_column ot_orig_column ) return varchar2
  is
    v_return varchar2(32000);    
  begin
    v_return := p_orig_column.i_name || ' ' || get_column_datatype( p_orig_column );
    
    if( p_orig_column.i_default_value is not null )
    then
      v_return := v_return || ' default ' || p_orig_column.i_default_value;
    end if;
    
    $IF DBMS_DB_VERSION.VERSION >= 12 $THEN
      if( p_orig_column.i_identity is not null )
      then
        v_return := v_return || ' generated';
        if( p_orig_column.i_identity.i_always is not null)
        then
          v_return := v_return || ' always';      
        end if;
        if( p_orig_column.i_identity.i_by_default is not null)
        then
          v_return := v_return || ' by default';      
        end if;
        if( p_orig_column.i_identity.i_on_null is not null)
        then
          v_return := v_return || ' on null';      
        end if;
        v_return := v_return || ' as identity';      
        
        v_return := v_return || ' (';      
  
        if( p_orig_column.i_identity.i_increment_by is not null and p_orig_column.i_identity.i_increment_by > 0 )    
        then
          v_return := v_return || ' increment by ' || p_orig_column.i_identity.i_increment_by;
        else      
          v_return := v_return || ' increment by 1';
        end if;      
        
        if( p_orig_column.i_identity.i_maxvalue is not null and p_orig_column.i_identity.i_maxvalue > 0)
        then
          v_return := v_return || ' maxvalue ' || p_orig_column.i_identity.i_maxvalue;      
        end if;
        
        if( p_orig_column.i_identity.i_minvalue is not null and p_orig_column.i_identity.i_minvalue > 0)
        then
          v_return := v_return || ' minvalue ' || p_orig_column.i_identity.i_minvalue;      
        end if;
        
        if( p_orig_column.i_identity.i_cycle is not null )
        then
          v_return := v_return || ' ' || p_orig_column.i_identity.i_cycle.i_literal;      
        end if;
        
        if( p_orig_column.i_identity.i_cache is not null and p_orig_column.i_identity.i_cache > 0)
        then
          v_return := v_return || ' cache ' || p_orig_column.i_identity.i_cache;      
        end if;
        
        if( p_orig_column.i_identity.i_order is not null )
        then
          v_return := v_return || ' ' || p_orig_column.i_identity.i_order.i_literal;      
        end if;
        
        v_return := v_return || ' )';            
      end if;    
    $END
    
    if( p_orig_column.i_notnull_flg = 1 )      
    then
      v_return := v_return || ' not null';
    end if;      
    
    return v_return;
  end;
  
  function create_foreign_key_clause( p_orig_foreignkey ot_orig_foreignkey ) return varchar2
  is
    v_return varchar2(32000);
    v_orig_refpartition ot_orig_refpartition;
  begin
    v_return := 'constraint ' || p_orig_foreignkey.i_consname || ' foreign key (' || get_column_list( p_orig_foreignkey.i_srccolumns ) || ') references ' || p_orig_foreignkey.i_desttable || '(' || get_column_list( p_orig_foreignkey.i_destcolumns ) || ')';      

    if( p_orig_foreignkey.i_delete_rule is not null )
    then      
      if( p_orig_foreignkey.i_delete_rule = ot_orig_fkdeleteruletype.c_cascade )
      then
        v_return := v_return || ' on delete cascade';
      end if;
      if( p_orig_foreignkey.i_delete_rule = ot_orig_fkdeleteruletype.c_set_null )
      then
        v_return := v_return || ' on delete set null';
      end if;
    end if;
    
    if( p_orig_foreignkey.i_deferrtype is not null )
    then
      v_return := v_return || ' deferrable initially  ' || p_orig_foreignkey.i_deferrtype.i_name;
    end if;         

    return v_return; 
  end;     
  
  procedure create_table( p_orig_table ot_orig_table )
  is
    function create_column_clause( p_orig_column_list in ct_orig_column_list ) return varchar2
    is
      v_return varchar2(32000);  
      v_orig_column ot_orig_column;
    begin
      for i in 1..p_orig_column_list.count()
      loop
        if( i != 1 )
        then
          v_return := v_return || ',';
        end if;
        
        v_orig_column := p_orig_column_list(i);
        
        v_return := v_return || ' ' || create_column_create_part( v_orig_column );
      end loop;
    
      return v_return;
    end;
    
    function create_column_storage_clause( p_orig_lobstorage_list in ct_orig_lobstorage_list ) return varchar2
    is
      v_return varchar2(32000);  
      v_orig_lobstorage ot_orig_lobstorage;
    begin
      if( p_orig_lobstorage_list is null )
      then
        return null;
      end if;  
    
      for i in 1..p_orig_lobstorage_list.count()
      loop
        v_orig_lobstorage := p_orig_lobstorage_list(i);
        
        v_return := v_return || ' lob(' || v_orig_lobstorage.i_column_name || ') store as (tablespace ' || v_orig_lobstorage.i_tablespace || ')';
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
      
        v_return := v_return || ', ' || create_foreign_key_clause( v_orig_foreignkey );
      end if;
      return v_return; 
    end;   
  begin
    pv_stmt := 'create';
    if( ot_orig_permanentnesstype.is_equal( p_orig_table.i_permanentness, ot_orig_permanentnesstype.c_global_temporary ) = 1 ) 
    then pv_stmt := pv_stmt || ' global ' || p_orig_table.i_permanentness.i_literal; 
    end if;    
    pv_stmt := pv_stmt || ' ' || 'table';
    pv_stmt := pv_stmt || ' ' || p_orig_table.i_name;
    pv_stmt := pv_stmt || ' ' || '(';       
    pv_stmt := pv_stmt || ' ' || create_column_clause(p_orig_table.i_columns);       
    pv_stmt := pv_stmt || ' ' || create_ref_fk_clause( p_orig_table ); 
    pv_stmt := pv_stmt || ' ' || ')';             
    if( p_orig_table.i_transactioncontrol is not null ) 
    then
      pv_stmt := pv_stmt || ' ' || 'on commit ';           
      pv_stmt := pv_stmt || ' ' || p_orig_table.i_transactioncontrol.i_literal;       
      pv_stmt := pv_stmt || ' ' || 'rows nocache';       
    else 
      pv_stmt := pv_stmt || ' ' || create_column_storage_clause(p_orig_table.i_lobstorages);
      if( p_orig_table.i_tablespace is not null )
      then
        pv_stmt := pv_stmt || ' ' || 'tablespace';              
        pv_stmt := pv_stmt || ' ' || p_orig_table.i_tablespace; 
      end if;
      if( ot_orig_permanentnesstype.is_equal( p_orig_table.i_permanentness, ot_orig_permanentnesstype.c_global_temporary ) = 0 ) 
      then
          if( ot_orig_loggingtype.is_equal( p_orig_table.i_logging, ot_orig_loggingtype.c_nologging ) = 1 )
            then
              pv_stmt := pv_stmt || ' ' || 'nologging';
            else
              pv_stmt := pv_stmt || ' ' || 'logging';               
          end if;
      end if;
      if( ot_orig_compresstype.is_equal( p_orig_table.i_compression, ot_orig_compresstype.c_compress, ot_orig_compresstype.c_nocompress ) = 1 )
      then
        pv_stmt := pv_stmt || ' ' || 'compress';
        if( ot_orig_compressfortype.is_equal( p_orig_table.i_compressionfor, ot_orig_compressfortype.c_all ) = 1 )
        then
          pv_stmt := pv_stmt || ' ' || 'for all operations';
        elsif ( ot_orig_compressfortype.is_equal( p_orig_table.i_compressionfor, ot_orig_compressfortype.c_direct_load ) = 1 )
          then
          pv_stmt := pv_stmt || ' ' || 'for direct_load operations';
        elsif ( ot_orig_compressfortype.is_equal( p_orig_table.i_compressionfor, ot_orig_compressfortype.c_query_low ) = 1 )
          then
          pv_stmt := pv_stmt || ' ' || 'for query low';  
        elsif ( ot_orig_compressfortype.is_equal( p_orig_table.i_compressionfor, ot_orig_compressfortype.c_query_high ) = 1 )
          then
          pv_stmt := pv_stmt || ' ' || 'for query high';   
        elsif ( ot_orig_compressfortype.is_equal( p_orig_table.i_compressionfor, ot_orig_compressfortype.c_archive_low ) = 1 )
          then
          pv_stmt := pv_stmt || ' ' || 'for archive low';  
        elsif ( ot_orig_compressfortype.is_equal( p_orig_table.i_compressionfor, ot_orig_compressfortype.c_archive_high ) = 1 )
          then
          pv_stmt := pv_stmt || ' ' || 'for archive high';             
        end if;
      else
        if( ot_orig_permanentnesstype.is_equal( p_orig_table.i_permanentness, ot_orig_permanentnesstype.c_global_temporary ) = 0 ) 
        then
          pv_stmt := pv_stmt || ' ' || 'nocompress';
        end if;
      end if;
      if( ot_orig_paralleltype.is_equal( p_orig_table.i_parallel, ot_orig_paralleltype.c_parallel ) = 1 )
      then
        stmt_add( 'parallel' ); 
        if ( p_orig_table.i_parallel_degree > 1 )
        then
          stmt_add( p_orig_table.i_parallel_degree );
        end if;
      else
        stmt_add( 'noparallel' );               
      end if;      
      pv_stmt := pv_stmt || ' ' || create_partitioning_clause( p_orig_table.i_tablepartitioning );
      pv_stmt := pv_stmt || ' ' || 'nocache nomonitoring';       
    end if;
    
    add_stmt;
  end;  
  
  function find_table_column( p_orig_column ot_orig_column, p_orig_table ot_orig_table ) return ot_orig_column
  is
  begin
    for i in 1 .. p_orig_table.i_columns.count
    loop
      if( is_equal_ignore_case( p_orig_table.i_columns(i).i_name, p_orig_column.i_name ) = 1 )
      then
        return p_orig_table.i_columns(i);
      end if;
    end loop;

    return null;
  end;
  
  function find_table_comment( p_orig_inlinecomment ot_orig_inlinecomment, p_orig_table ot_orig_table ) return ot_orig_inlinecomment
  is
  begin
    if( p_orig_table is null or p_orig_table.i_comments is null )
    then
      return null;
    end if;
    
    for i in 1 .. p_orig_table.i_comments.count
    loop
      if( is_equal_ignore_case( p_orig_table.i_comments(i).i_column_name, p_orig_inlinecomment.i_column_name ) = 1 )
      then
        return p_orig_table.i_comments(i);
      end if;
    end loop;

    return null;
  end;  
  
  function find_table_constraint( p_orig_constraint ot_orig_constraint, p_orig_table ot_orig_table ) return ot_orig_constraint
  is
  begin
    if( p_orig_table is null or p_orig_table.i_constraints is null )
    then
      return null;
    end if;
  
    for i in 1 .. p_orig_table.i_constraints.count
    loop
      if( is_equal_ignore_case( p_orig_table.i_constraints(i).i_consname, p_orig_constraint.i_consname) = 1 )
      then
        return p_orig_table.i_constraints(i);
      end if;
    end loop;

    return null;
  end;    
  
  function find_table_uniquekey( p_orig_uniquekey ot_orig_uniquekey, p_orig_table ot_orig_table ) return ot_orig_uniquekey
  is
  begin
    if( p_orig_table is null or p_orig_table.i_ind_uks is null )
    then
      return null;
    end if;
  
    for i in 1 .. p_orig_table.i_ind_uks.count
    loop
      if( is_equal_ignore_case( p_orig_table.i_ind_uks(i).i_consname, p_orig_uniquekey.i_consname) = 1 )
      then
        if( p_orig_table.i_ind_uks(i) is of (ot_orig_uniquekey) )
        then
          return treat( p_orig_table.i_ind_uks(i) as ot_orig_uniquekey );
        end if;
      end if;
    end loop;

    return null;
  end;     
  
  function find_table_uniquekey_by_index( p_indexname varchar2, p_orig_table ot_orig_table ) return ot_orig_uniquekey
  is
    v_uniquekey ot_orig_uniquekey;
  begin
    if( p_orig_table is null or p_orig_table.i_ind_uks is null )
    then
      return null;
    end if;
  
    for i in 1 .. p_orig_table.i_ind_uks.count
    loop
      if( p_orig_table.i_ind_uks(i) is of (ot_orig_uniquekey) )
      then
        v_uniquekey := treat( p_orig_table.i_ind_uks(i) as ot_orig_uniquekey );
        if( is_equal_ignore_case( v_uniquekey.i_indexname, p_indexname ) = 1 )
        then        
          return v_uniquekey;
        end if;
      end if;
    end loop;

    return null;
  end;  
  
  function find_table_index( p_orig_index ot_orig_index, p_orig_table ot_orig_table ) return ot_orig_index
  is
  begin
    if( p_orig_table is null or p_orig_table.i_ind_uks is null )
    then
      return null;
    end if;
  
    for i in 1 .. p_orig_table.i_ind_uks.count
    loop
      if( is_equal_ignore_case( p_orig_table.i_ind_uks(i).i_consname, p_orig_index.i_consname) = 1 )
      then
        if( p_orig_table.i_ind_uks(i) is of (ot_orig_index) )
        then
          return treat( p_orig_table.i_ind_uks(i) as ot_orig_index );
        end if;
      end if;
    end loop;

    return null;
  end;     
  
  function find_table_foreignkey( p_orig_foreignkey ot_orig_foreignkey, p_orig_table ot_orig_table ) return ot_orig_foreignkey
  is
  begin
    if( p_orig_table is null or p_orig_table.i_foreign_keys is null )
    then
      return null;
    end if;
  
    for i in 1 .. p_orig_table.i_foreign_keys.count
    loop
      if( is_equal_ignore_case( p_orig_table.i_foreign_keys(i).i_consname, p_orig_foreignkey.i_consname ) = 1 )
      then
        return p_orig_table.i_foreign_keys(i);
      end if;
    end loop;

    return null;
  end;  
  
  procedure handle_table( p_orig_table_soll ot_orig_table ) is
    v_orig_table_ist ot_orig_table;
    v_orig_index_soll ot_orig_index;
    v_orig_uniquekey_soll ot_orig_uniquekey;        
    v_tablespace varchar2(100);

    
    function find_lobstorage( p_column_name in varchar2 ) return ot_orig_lobstorage
    is
    begin
      if( p_orig_table_soll.i_lobstorages is null )
      then  
        return null;
      end if;
    
      for i in 1..p_orig_table_soll.i_lobstorages.count
      loop
        if( upper(p_orig_table_soll.i_lobstorages(i).i_column_name) = upper(p_column_name) )
        then
          return p_orig_table_soll.i_lobstorages(i);
        end if;
      end loop;
  
      return null;
    end;      
    
    procedure handle_column( p_orig_column_soll ot_orig_column )
    is     
      v_orig_column_ist ot_orig_column;    
      
      function get_soll_precision return number
      is
      begin
        if( p_orig_column_soll.i_precision != 0 )
        then      
          return p_orig_column_soll.i_precision;
        end if;
        
        if( ot_orig_datatype.is_equal( p_orig_column_soll.i_data_type, ot_orig_datatype.c_char ) = 1 )
        then
          return 1;
        end if;
        
        if( ot_orig_datatype.is_equal( p_orig_column_soll.i_data_type, ot_orig_datatype.c_timestamp ) = 1 )
        then
          return 6;
        end if;
        
        if( ot_orig_datatype.is_equal( p_orig_column_soll.i_data_type, ot_orig_datatype.c_float ) = 1 )
        then
          return 126;
        end if;
        
        return 0;
      end;

      function is_recreate_column return number
      is
      begin
        if( p_orig_column_soll.i_data_type is not null and  v_orig_column_ist.i_data_type is not null )
        then
          if( p_orig_column_soll.i_data_type != v_orig_column_ist.i_data_type )
          then
            return 1;
          end if;
          
          if( get_soll_precision() < v_orig_column_ist.i_precision )
          then
            return 1;          
          end if;
          
          if( p_orig_column_soll.i_scale < v_orig_column_ist.i_scale )
          then
            return 1;
          end if;
        end if;  
        
        if( is_equal_ignore_case( p_orig_column_soll.i_object_type, v_orig_column_ist.i_object_type ) != 1 )
        then
          return 1;
        end if;        

        return 0;        
      end;
      
      procedure recreate_column
      is
        v_tmp_old_column_name varchar2(100) := 'DTO_' || p_orig_column_soll.i_name;
        v_tmp_new_column_name varchar2(100) := 'DTN_' || p_orig_column_soll.i_name;
      begin
        add_stmt( 'alter table ' || v_orig_table_ist.i_name || ' add ' || v_tmp_new_column_name || ' ' || get_column_datatype( p_orig_column_soll ) );
      
        for cur_trigger in
          (
          select trigger_name
            from user_triggers
           where table_name = upper( v_orig_table_ist.i_name )
          )
        loop
          add_stmt( 'alter trigger ' || cur_trigger.trigger_name || ' disable' );
        end loop;
      
        add_stmt( 'update ' || v_orig_table_ist.i_name || ' set ' || v_tmp_new_column_name || ' = ' || v_orig_column_ist.i_name );
        add_stmt( 'commit' );
      
        for cur_trigger in
          (
          select trigger_name
            from user_triggers
           where table_name = upper( v_orig_table_ist.i_name )
          )
        loop
          add_stmt( 'alter trigger ' || cur_trigger.trigger_name || ' enable' );
        end loop;
      
        add_stmt( 'alter table ' || v_orig_table_ist.i_name || ' rename column ' || v_orig_column_ist.i_name || ' to ' || v_tmp_old_column_name );
        add_stmt( 'alter table ' || v_orig_table_ist.i_name || ' rename column ' || v_tmp_new_column_name || ' to ' || p_orig_column_soll.i_name );
        add_stmt( 'alter table ' || v_orig_table_ist.i_name || ' drop column ' || v_tmp_old_column_name );
        
        if( p_orig_column_soll.i_default_value is not null )
        then
          pv_stmt := 'alter table ' || v_orig_table_ist.i_name || ' modify ( ' || p_orig_column_soll.i_name || ' default';
          pv_stmt := pv_stmt || ' ' || p_orig_column_soll.i_default_value;
          pv_stmt := pv_stmt || ' ' || ')';
          add_stmt();
        end if;
        
        if( p_orig_column_soll.i_notnull_flg = 1 )
        then
          pv_stmt := 'alter table ' || v_orig_table_ist.i_name || ' modify ( ' || p_orig_column_soll.i_name;
          pv_stmt := pv_stmt || ' ' || 'not null';
          pv_stmt := pv_stmt || ' ' || ')';
          add_stmt();
        end if;          
      end;
    begin
      v_orig_column_ist := find_table_column( p_orig_column_soll, v_orig_table_ist );
      
      if( v_orig_column_ist is null )
      then
        pv_stmt := 'alter table ' || v_orig_table_ist.i_name || ' add ' || create_column_create_part( p_orig_column_soll );
        
        if( find_lobstorage( p_orig_column_soll.i_name ) is not null )
        then
          pv_stmt := pv_stmt || ' ' || 'lob (' || p_orig_column_soll.i_name || ') store as ( tablespace ' || find_lobstorage( p_orig_column_soll.i_name ).i_tablespace || ' )';
        end if;
        
        add_stmt;
      else
        if( is_recreate_column() = 1 )
        then
          recreate_column();
        else
          if(   ot_orig_chartype.is_equal( v_orig_column_ist.i_byteorchar, p_orig_column_soll.i_byteorchar, pv_default_orig_chartype )  != 1
             or is_equal(                  v_orig_column_ist.i_precision,  get_soll_precision(), 0 )                                    != 1
             or is_equal(                  v_orig_column_ist.i_scale,      p_orig_column_soll.i_scale, 0 )                              != 1 
            )                       
          then
            add_stmt( 'alter table ' || v_orig_table_ist.i_name || ' modify ( ' || v_orig_column_ist.i_name || ' ' || get_column_datatype( p_orig_column_soll ) || ')' );          
          end if;
        
          if( is_equal( v_orig_column_ist.i_default_value, p_orig_column_soll.i_default_value ) != 1 )
          then
            pv_stmt := 'alter table ' || v_orig_table_ist.i_name || ' modify ( ' || v_orig_column_ist.i_name || ' default';
            if( p_orig_column_soll.i_default_value is null )
            then
              pv_stmt := pv_stmt || ' ' || 'null';
            else
              pv_stmt := pv_stmt || ' ' || p_orig_column_soll.i_default_value;
            end if;            
            pv_stmt := pv_stmt || ' ' || ')';
            add_stmt();
          end if;
          
          if( is_equal( v_orig_column_ist.i_notnull_flg, p_orig_column_soll.i_notnull_flg ) != 1 )
          then
            pv_stmt := 'alter table ' || v_orig_table_ist.i_name || ' modify ( ' || v_orig_column_ist.i_name;
            if( p_orig_column_soll.i_notnull_flg = 0 )
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
    
    procedure handle_constraint( p_orig_constraint_soll ot_orig_constraint )
    is     
      v_orig_constraint_ist ot_orig_constraint;     
      
      procedure create_constraint
      is
      begin
        stmt_set( 'alter table ' || p_orig_table_soll.i_name || ' add constraint ' || p_orig_constraint_soll.i_consname || ' check (' || p_orig_constraint_soll.i_rule || ')' );
        if( p_orig_constraint_soll.i_deferrtype is not null )
        then
          stmt_add( 'deferrable initially ' || p_orig_constraint_soll.i_deferrtype.i_name );
        end if;
        if( p_orig_constraint_soll.i_status is not null )
        then
          stmt_add( p_orig_constraint_soll.i_status.i_name );
        end if;
        
        add_stmt();
      end;
    begin
      v_orig_constraint_ist := find_table_constraint( p_orig_constraint_soll, v_orig_table_ist );
      
      if( v_orig_constraint_ist is null )
      then
        create_constraint();
      else
        if(   v_orig_constraint_ist.i_rule != p_orig_constraint_soll.i_rule 
          or  ot_orig_deferrtype.is_equal( v_orig_constraint_ist.i_deferrtype, p_orig_constraint_soll.i_deferrtype ) != 1  
          )
        then
          drop_table_constraint_by_name( v_orig_table_ist, v_orig_constraint_ist.i_consname );
          
          create_constraint();
        end if;
      end if;        
    end;            
    
    procedure handle_uniquekey( p_orig_uniquekey_soll ot_orig_uniquekey )
    is     
      v_orig_uniquekey_ist ot_orig_uniquekey;     
      
      procedure create_uniquekey
      is
      begin
        stmt_set( 'alter table ' || p_orig_table_soll.i_name || ' add constraint ' || p_orig_uniquekey_soll.i_consname || ' unique (' || get_column_list( p_orig_uniquekey_soll.i_uk_columns ) || ')' );
        if( p_orig_uniquekey_soll.i_tablespace is not null )
        then
            stmt_add( 'using index tablespace ' || p_orig_uniquekey_soll.i_tablespace );
        else 
          if( p_orig_uniquekey_soll.i_indexname is not null and is_equal_ignore_case(p_orig_uniquekey_soll.i_indexname, p_orig_uniquekey_soll.i_consname) != 1 )
          then
            stmt_add( 'using index ' || p_orig_uniquekey_soll.i_indexname );
          end if;  
        end if;
        if( p_orig_uniquekey_soll.i_status is not null )
        then
          stmt_add( p_orig_uniquekey_soll.i_status.i_name );
        end if;
        
        add_stmt();
      end;
    begin
      v_orig_uniquekey_ist := find_table_uniquekey( p_orig_uniquekey_soll, v_orig_table_ist );
      
      if( v_orig_uniquekey_ist is null )
      then
        create_uniquekey();
      else
        if( is_equal( v_orig_uniquekey_ist.i_uk_columns, p_orig_uniquekey_soll.i_uk_columns ) != 1 
            or ( is_equal_ignore_case(v_orig_uniquekey_ist.i_indexname, nvl(p_orig_uniquekey_soll.i_indexname, v_orig_uniquekey_ist.i_indexname)) != 1 
              and is_equal_ignore_case(v_orig_uniquekey_ist.i_indexname, v_orig_uniquekey_ist.i_consname) != 1 )
            or ( is_equal_ignore_case(v_orig_uniquekey_ist.i_tablespace, nvl(p_orig_uniquekey_soll.i_tablespace, pv_default_tablespace)) != 1 
             and not (v_orig_uniquekey_ist.i_tablespace is null and p_orig_uniquekey_soll.i_tablespace is null) 
             and pa_orcas_run_parameter.is_indexmovetablespace = 1 ) )
        then
          drop_table_constraint_by_name( v_orig_table_ist, v_orig_uniquekey_ist.i_consname );
          
          create_uniquekey();
        end if;
      end if;        
    end;         
    
    procedure handle_index( p_orig_index_soll ot_orig_index )
    is     
      v_orig_index_ist ot_orig_index;   
      
      procedure create_index
      is
      begin
        stmt_set( 'create' );
        stmt_add( p_orig_index_soll.i_unique );
        if( p_orig_index_soll.i_bitmap is not null )
        then
          stmt_add( 'bitmap' );          
        end if;
        stmt_add( 'index' );
        stmt_add( p_orig_index_soll.i_consname );                  
        stmt_add( 'on' );         
        stmt_add( p_orig_table_soll.i_name ); 
        stmt_add( '(' );                  
        if( p_orig_index_soll.i_function_based_expression is not null )
        then
          stmt_add( p_orig_index_soll.i_function_based_expression ); 
        else
          stmt_add( get_column_list( p_orig_index_soll.i_index_columns ) );                   
        end if;
        stmt_add( ')' );              
        if( p_orig_index_soll.i_domain_index_expression is not null )
        then
          stmt_add( p_orig_index_soll.i_domain_index_expression ); 
        else
          if( p_orig_index_soll.i_logging is not null )
          then
            stmt_add( p_orig_index_soll.i_logging.i_literal );          
          end if;
        end if;
        if( p_orig_index_soll.i_tablespace is not null )
        then
          stmt_add( 'tablespace' ); 
          stmt_add( p_orig_index_soll.i_tablespace );            
        end if;         
        if( p_orig_index_soll.i_global is not null )
        then
          stmt_add( p_orig_index_soll.i_global.i_literal );          
        end if;      
        if( p_orig_index_soll.i_bitmap is null and  
            ot_orig_compresstype.is_equal(  p_orig_index_soll.i_compression,     ot_orig_compresstype.c_compress ) = 1 
          )
        then
          stmt_add( 'compress' ); 
        elsif ( ot_orig_compresstype.is_equal( p_orig_index_soll.i_compression,     ot_orig_compresstype.c_nocompress ) = 1 )
        then
          stmt_add( 'nocompress' );                
        end if;
        if(   ot_orig_paralleltype.is_equal( p_orig_index_soll.i_parallel, ot_orig_paralleltype.c_parallel ) = 1
           or pa_orcas_run_parameter.is_indexparallelcreate = 1 )
        then
          stmt_add( 'parallel' ); 
          if ( p_orig_index_soll.i_parallel_degree > 1 )
          then
            stmt_add( p_orig_index_soll.i_parallel_degree );
          end if;
        end if;

        add_stmt();
        
        if(   ot_orig_paralleltype.is_equal( p_orig_index_soll.i_parallel, ot_orig_paralleltype.c_noparallel, ot_orig_paralleltype.c_noparallel ) = 1
          and pa_orcas_run_parameter.is_indexparallelcreate = 1 )
        then
          add_stmt( 'alter index ' || p_orig_index_soll.i_consname || ' noparallel' ); 
        end if;        
      end;

    begin
      v_orig_index_ist := find_table_index( p_orig_index_soll, v_orig_table_ist );
      
      if( v_orig_index_ist is null )
      then
        create_index();
      else
        if(   
-- domain index kann nicht abgeglichen werden           
              (
                (
                  is_equal(                          v_orig_index_ist.i_index_columns,              p_orig_index_soll.i_index_columns                                 ) != 1
               or is_equal(                          v_orig_index_ist.i_function_based_expression,  p_orig_index_soll.i_function_based_expression                     ) != 1                           
               or is_equal(                          v_orig_index_ist.i_domain_index_expression,    p_orig_index_soll.i_domain_index_expression                       ) != 1                       
                )              
            and p_orig_index_soll.i_domain_index_expression is null
              )
           or is_equal_ignore_case(              v_orig_index_ist.i_unique,            p_orig_index_soll.i_unique                                                 ) != 1           
           or is_equal_ignore_case(              v_orig_index_ist.i_bitmap,            p_orig_index_soll.i_bitmap                                                 ) != 1                      
           or ot_orig_indexglobaltype.is_equal(  v_orig_index_ist.i_global,            p_orig_index_soll.i_global,              ot_orig_indexglobaltype.c_global  ) != 1 
           or ot_orig_compresstype.is_equal(     v_orig_index_ist.i_compression,       p_orig_index_soll.i_compression,         ot_orig_compresstype.c_nocompress ) != 1
          )                                        
        then
          add_stmt( 'drop index ' || v_orig_index_ist.i_consname );   
          
          create_index();
        else
          if(    ot_orig_paralleltype.is_equal(  v_orig_index_ist.i_parallel,        p_orig_index_soll.i_parallel,         ot_orig_paralleltype.c_noparallel ) != 1 
              or is_equal(                       v_orig_index_ist.i_parallel_degree, p_orig_index_soll.i_parallel_degree,  0 )                                 != 1
            )
          then
            stmt_set( 'alter index' );
            stmt_add( p_orig_index_soll.i_consname );        
            if( ot_orig_paralleltype.is_equal( p_orig_index_soll.i_parallel, ot_orig_paralleltype.c_parallel ) = 1 )
            then
              stmt_add( 'parallel' ); 
              if ( p_orig_index_soll.i_parallel_degree > 1 )
              then
                stmt_add( p_orig_index_soll.i_parallel_degree );
              end if;
            else
              stmt_add( 'noparallel' );         
            end if;         
            
            stmt_done();
          end if;
          
          if(  ot_orig_loggingtype.is_equal(      v_orig_index_ist.i_logging,                    p_orig_index_soll.i_logging,    ot_orig_loggingtype.c_logging     ) != 1 
            )
          then
            stmt_set( 'alter index' );
            stmt_add( v_orig_index_ist.i_consname );        
            if( ot_orig_loggingtype.is_equal( p_orig_index_soll.i_logging, ot_orig_loggingtype.c_logging, ot_orig_loggingtype.c_nologging ) = 1 )
            then
              stmt_add( 'logging' ); 
            else
              stmt_add( 'nologging' );         
            end if;         
            
            stmt_done();
          end if;
          
          if ( is_equal_ignore_case(v_orig_index_ist.i_tablespace, nvl(p_orig_index_soll.i_tablespace, pv_default_tablespace)) != 1 
             and not (v_orig_index_ist.i_tablespace is null and p_orig_index_soll.i_tablespace is null) and pa_orcas_run_parameter.is_indexmovetablespace = 1 )
          then
            stmt_set( 'alter index' );
            stmt_add( p_orig_index_soll.i_consname );     
            stmt_add( 'rebuild tablespace' ); 
            stmt_add( nvl(p_orig_index_soll.i_tablespace, pv_default_tablespace) );  
            stmt_done();
          end if;          
        end if;
      end if;        
    end;   
    
    procedure handle_mviewlog( p_orig_table_soll ot_orig_table )
    is     
      v_orig_mviewlog_ist ot_orig_mviewlog;
      v_orig_mviewlog_soll ot_orig_mviewlog;
      v_default_tablespace varchar2(30);
      c_date_format constant varchar2(30) := pa_orcas_run_parameter.get_dateformat();
      
      procedure create_mviewlog
      is
      begin
        stmt_set( 'create materialized view log on' );       
        stmt_add( p_orig_table_soll.i_name ); 
        
        if( v_orig_mviewlog_soll.i_tablespace is not null )
        then
          stmt_add( 'tablespace' ); 
          stmt_add( v_orig_mviewlog_soll.i_tablespace );            
        end if;   
        
        if( ot_orig_paralleltype.is_equal( v_orig_mviewlog_soll.i_parallel, ot_orig_paralleltype.c_parallel ) = 1 )
        then
          stmt_add( 'parallel' ); 
          if ( v_orig_mviewlog_soll.i_parallel_degree > 1 )
          then
            stmt_add( v_orig_mviewlog_soll.i_parallel_degree );
          end if;
        end if;
        
        stmt_add( 'with' );
        
        if( nvl(v_orig_mviewlog_soll.i_primarykey,'null') = 'primary' 
            or nvl(v_orig_mviewlog_soll.i_rowid,'null') != 'rowid'
        )
        then
          stmt_add( 'primary key' ); 
          if( nvl(v_orig_mviewlog_soll.i_rowid,'null') = 'rowid' )
          then
            stmt_add( ',' );
          end if;     
        end if;  
        
        if( nvl(v_orig_mviewlog_soll.i_rowid,'null') = 'rowid' )
           then 
           stmt_add( 'rowid' );
        end if;  
        
        if( nvl(v_orig_mviewlog_soll.i_withsequence,'null') = 'sequence' )
           then
           stmt_add( ',' );
           stmt_add( 'sequence' );
        end if;           
                          
        if( v_orig_mviewlog_soll.i_columns is not null )
        then
          stmt_add( '(' );
          stmt_add( get_column_list( v_orig_mviewlog_soll.i_columns ) ); 
          stmt_add( ')' ); 
        end if;     
                 
        if( nvl(v_orig_mviewlog_soll.i_commitscn,'null') = 'commit_scn' )
           then
           stmt_add( ',' );
           stmt_add( 'commit scn' );
        end if;      

        if( v_orig_mviewlog_soll.i_newvalues is not null )
           then
           stmt_add( v_orig_mviewlog_soll.i_newvalues.i_literal );
           stmt_add( 'new values' );
        end if;    
        
        if (   v_orig_mviewlog_soll.i_startwith is not null 
            or v_orig_mviewlog_soll.i_next is not null 
            or (v_orig_mviewlog_soll.i_repeatInterval is not null and v_orig_mviewlog_soll.i_repeatInterval != 0))
          then
            stmt_add( 'purge' );
            if (v_orig_mviewlog_soll.i_startwith is not null)
              then
                stmt_add( 'start with' );
                stmt_add( 'to_date(''' || v_orig_mviewlog_soll.i_startwith || ''',''' || c_date_format || ''')' );
            end if;
            if (v_orig_mviewlog_soll.i_next is not null)
              then
                stmt_add( 'next' );
                stmt_add( 'to_date(''' || v_orig_mviewlog_soll.i_next || ''',''' || c_date_format || ''')' );
              else 
                if (v_orig_mviewlog_soll.i_repeatInterval is not null and v_orig_mviewlog_soll.i_repeatInterval != 0)
                then
                    stmt_add( 'repeat interval' );
                    stmt_add( v_orig_mviewlog_soll.i_repeatInterval );
                end if;
            end if;
          else
            if( ot_orig_synchronoustype.is_equal( v_orig_mviewlog_soll.i_synchronous, ot_orig_synchronoustype.c_asynchronous ) = 1 )
              then 
               stmt_add( 'purge immediate asynchronous' );
            end if;
        end if;
        
        add_stmt();
        
      end;

    begin
      v_orig_mviewlog_ist := v_orig_table_ist.i_mviewlog;
      v_orig_mviewlog_soll := p_orig_table_soll.i_mviewlog;
      
      if( v_orig_mviewlog_ist is null )
      then
        create_mviewlog();
      else
        select distinct(default_tablespace) into v_default_tablespace from user_users;
        if(   
           is_equal(                             v_orig_mviewlog_ist.i_columns,        v_orig_mviewlog_soll.i_columns                                 ) != 1   
           or 
           (  is_equal_ignore_case(              'rowid',                              v_orig_mviewlog_soll.i_rowid                                   ) = 1 and
              is_equal_ignore_case(              v_orig_mviewlog_ist.i_primarykey,     v_orig_mviewlog_soll.i_primarykey                              ) != 1 ) 
           or 
           (  v_orig_mviewlog_soll.i_rowid is null and
              is_equal_ignore_case(              v_orig_mviewlog_ist.i_primarykey,     'primary'                                                  ) != 1 )    
           or is_equal_ignore_case(              v_orig_mviewlog_ist.i_rowid,          v_orig_mviewlog_soll.i_rowid                                   ) != 1
           or is_equal_ignore_case(              v_orig_mviewlog_ist.i_withsequence,   v_orig_mviewlog_soll.i_withsequence                            ) != 1
           or is_equal_ignore_case(              v_orig_mviewlog_ist.i_commitscn,      v_orig_mviewlog_soll.i_commitscn                               ) != 1
           or ( is_equal_ignore_case(v_orig_mviewlog_ist.i_tablespace, nvl(v_orig_mviewlog_soll.i_tablespace, v_default_tablespace)) != 1 
              and not (v_orig_mviewlog_ist.i_tablespace is null and v_orig_mviewlog_soll.i_tablespace is null) )
          )                                        
        then
          add_stmt( 'drop materialized view log on ' || v_orig_table_ist.i_name );   
          
          create_mviewlog();
        else
          if(    ot_orig_paralleltype.is_equal(  v_orig_mviewlog_ist.i_parallel,        v_orig_mviewlog_soll.i_parallel,         ot_orig_paralleltype.c_noparallel ) != 1 
              or is_equal(                       v_orig_mviewlog_ist.i_parallel_degree, v_orig_mviewlog_soll.i_parallel_degree,  0 )                                 != 1
            )
          then
            stmt_set( 'alter materialized view log on' );
            stmt_add( p_orig_table_soll.i_name );        
            if( ot_orig_paralleltype.is_equal( v_orig_mviewlog_soll.i_parallel, ot_orig_paralleltype.c_parallel ) = 1 )
            then
              stmt_add( 'parallel' ); 
              if ( v_orig_mviewlog_soll.i_parallel_degree > 1 )
              then
                stmt_add( v_orig_mviewlog_soll.i_parallel_degree );
              end if;
            else
              stmt_add( 'noparallel' );         
            end if;         
            
            stmt_done();
          end if;
          
          if(  ot_orig_newvaluestype.is_equal( v_orig_mviewlog_ist.i_newvalues,  v_orig_mviewlog_soll.i_newvalues,  ot_orig_newvaluestype.c_excluding  ) != 1 
            )
          then
            stmt_set( 'alter materialized view log on' );
            stmt_add( p_orig_table_soll.i_name );       
            
            if( ot_orig_newvaluestype.is_equal( v_orig_mviewlog_soll.i_newvalues, ot_orig_newvaluestype.c_including, ot_orig_newvaluestype.c_excluding ) = 1 )
            then
              stmt_add( 'including' ); 
            else
              stmt_add( 'excluding' );         
            end if;                    
            stmt_add( 'new values' );
            
            stmt_done();
          end if;
          
          if (   is_equal(v_orig_mviewlog_soll.i_startwith, v_orig_mviewlog_ist.i_startwith) != 1 
              or is_equal(v_orig_mviewlog_soll.i_next, v_orig_mviewlog_ist.i_next) != 1 
              or (is_equal(nvl(v_orig_mviewlog_soll.i_repeatInterval,0), nvl(v_orig_mviewlog_ist.i_repeatInterval,0)) != 1)
              )
            then
                stmt_add( 'alter materialized view log on' ); 
                stmt_add( p_orig_table_soll.i_name );    
                stmt_add( 'purge' );   
                if (    is_equal(v_orig_mviewlog_soll.i_startwith, v_orig_mviewlog_ist.i_startwith) != 1 )
                then
                    stmt_add( 'start with' );
                    stmt_add( 'to_date(''' || v_orig_mviewlog_soll.i_startwith || ''',''' || c_date_format || ''')' );
                end if;
                if (is_equal(v_orig_mviewlog_soll.i_next, v_orig_mviewlog_ist.i_next) != 1)
                then
                    stmt_add( 'next' );
                    stmt_add( 'to_date(''' || v_orig_mviewlog_soll.i_next || ''',''' || c_date_format ||''')' );
                else 
                    if (is_equal(v_orig_mviewlog_soll.i_repeatInterval, v_orig_mviewlog_ist.i_repeatInterval) != 1
                        and v_orig_mviewlog_soll.i_repeatInterval != 0)
                    then
                        stmt_add( 'repeat interval' );
                        stmt_add( v_orig_mviewlog_soll.i_repeatInterval );
                    end if;
                end if;
               
            stmt_done();
          else
          
              if(  ot_orig_synchronoustype.is_equal( v_orig_mviewlog_ist.i_synchronous,  v_orig_mviewlog_soll.i_synchronous,  ot_orig_synchronoustype.c_synchronous  ) != 1 )
              then
                stmt_add( 'alter materialized view log on' ); 
                stmt_add( p_orig_table_soll.i_name );       
                if( ot_orig_synchronoustype.is_equal( v_orig_mviewlog_soll.i_synchronous, ot_orig_synchronoustype.c_asynchronous, ot_orig_synchronoustype.c_synchronous ) = 1 )
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
      
    procedure handle_comment( p_orig_inlinecomment_soll ot_orig_inlinecomment )
    is     
      v_orig_inlinecomment_ist ot_orig_inlinecomment;
    begin
      v_orig_inlinecomment_ist := find_table_comment( p_orig_inlinecomment_soll, v_orig_table_ist );
      
      if(   v_orig_inlinecomment_ist is null
        or  is_equal( v_orig_inlinecomment_ist.i_comment, p_orig_inlinecomment_soll.i_comment ) != 1 )
      then      
        stmt_set( 'comment on' );
        stmt_add( p_orig_inlinecomment_soll.i_comment_object.i_name );                
        stmt_add( ' ' );                        
        stmt_add( p_orig_table_soll.i_name );
        if( p_orig_inlinecomment_soll.i_column_name is not null )
        then
          stmt_add( '.' );
          stmt_add( p_orig_inlinecomment_soll.i_column_name );                
        end if;
        stmt_add( 'is' );        
        stmt_add( '''' || replace(p_orig_inlinecomment_soll.i_comment,'''','''''') || '''' );                
        add_stmt();
      end if;
    end;    
  begin
    v_orig_table_ist := find_table( p_orig_table_soll, pv_orig_table_list_ist );
    
    if( v_orig_table_ist is not null )
    then
      if( ot_orig_permanentnesstype.is_equal(p_orig_table_soll.i_permanentness, ot_orig_permanentnesstype.c_global_temporary, ot_orig_permanentnesstype.c_permanent ) = 1 )
      then
        v_tablespace := pv_temporary_tablespace;
      else
        v_tablespace := pv_default_tablespace;
      end if;
      
      if(   ot_orig_permanentnesstype.is_equal( v_orig_table_ist.i_permanentness, p_orig_table_soll.i_permanentness, ot_orig_permanentnesstype.c_permanent ) = 0 
         or ot_orig_permanentnesstran.is_equal( v_orig_table_ist.i_transactioncontrol, p_orig_table_soll.i_transactioncontrol ) = 0 
        )      
      then
        drop_with_dropmode_check( 'select 1 from ' || v_orig_table_ist.i_name,
                                  'drop table ' || v_orig_table_ist.i_name );
        v_orig_table_ist := null;
      else
        if ( is_equal_ignore_case(v_orig_table_ist.i_tablespace, nvl(p_orig_table_soll.i_tablespace, v_tablespace)) != 1 
              and pa_orcas_run_parameter.is_tablemovetablespace = 1
              and not (v_orig_table_ist.i_tablespace is null and p_orig_table_soll.i_tablespace is null) ) 
        then
          stmt_set( 'alter table' );                      
          stmt_add( p_orig_table_soll.i_name ); 
          stmt_add( 'move tablespace' );
          stmt_add( nvl(p_orig_table_soll.i_tablespace, v_tablespace) );
          stmt_done;
        end if;
      end if;
    end if;
    
    if( v_orig_table_ist is null )
    then
      create_table( p_orig_table_soll );
    else    
      for i in 1 .. p_orig_table_soll.i_columns.count
      loop
        handle_column( p_orig_table_soll.i_columns(i) );
      end loop;
      
      for i in 1 .. v_orig_table_ist.i_columns.count
      loop
        if( find_table_column( v_orig_table_ist.i_columns(i), p_orig_table_soll ) is null )
        then
          drop_with_dropmode_check( 'select 1 from ' || v_orig_table_ist.i_name || ' where ' || v_orig_table_ist.i_columns(i).i_name || ' is not null',
                                    'alter table ' || v_orig_table_ist.i_name || ' drop column ' || v_orig_table_ist.i_columns(i).i_name );
        end if;
      end loop;
      
      if(ot_orig_loggingtype.is_equal(  v_orig_table_ist.i_logging,        p_orig_table_soll.i_logging,         ot_orig_loggingtype.c_logging ) != 1 )
      then
        if( p_orig_table_soll.i_transactioncontrol is null and p_orig_table_soll.i_tablepartitioning is null ) 
        then
            stmt_set( 'alter table' );
            stmt_add( p_orig_table_soll.i_name );        
            if( ot_orig_loggingtype.is_equal( p_orig_table_soll.i_logging, ot_orig_loggingtype.c_nologging ) = 1 )
            then
              stmt_add( 'nologging' ); 
            else
              stmt_add( 'logging' );         
            end if;  
            stmt_done();
        end if;       
        
      end if;
      
      if(    ot_orig_paralleltype.is_equal(  v_orig_table_ist.i_parallel,        p_orig_table_soll.i_parallel,         ot_orig_paralleltype.c_noparallel ) != 1 
          or is_equal(                       v_orig_table_ist.i_parallel_degree, p_orig_table_soll.i_parallel_degree,  0 )                                 != 1
        )
      then
        stmt_set( 'alter table' );
        stmt_add( p_orig_table_soll.i_name );        
        if( ot_orig_paralleltype.is_equal( p_orig_table_soll.i_parallel, ot_orig_paralleltype.c_parallel ) = 1 )
        then
          stmt_add( 'parallel' ); 
          if ( p_orig_table_soll.i_parallel_degree > 1 )
          then
            stmt_add( p_orig_table_soll.i_parallel_degree );
          end if;
        else
          stmt_add( 'noparallel' );         
        end if;         
        
        stmt_done();
      end if;
      
      if(   ot_orig_permanentnesstype.is_equal( v_orig_table_ist.i_permanentness, ot_orig_permanentnesstype.c_permanent ) = 1 and  
        ( ot_orig_compresstype.is_equal(  v_orig_table_ist.i_compression,       p_orig_table_soll.i_compression,         ot_orig_compresstype.c_nocompress ) != 1 
          or ot_orig_compressfortype.is_equal(  v_orig_table_ist.i_compressionFor,  p_orig_table_soll.i_compressionFor, ot_orig_compressfortype.c_direct_load ) != 1 )
        )
      then
        stmt_set( 'alter table' );
        stmt_add( v_orig_table_ist.i_name );        
        if( ot_orig_compresstype.is_equal( p_orig_table_soll.i_compression, ot_orig_compresstype.c_compress, ot_orig_compresstype.c_nocompress ) = 1 )
        then
          stmt_add( 'compress' ); 
          if ( p_orig_table_soll.i_compressionFor is not null )
          then
            stmt_add( 'for ' || adjust_compression_literal(p_orig_table_soll.i_compressionFor.i_literal));
          end if;
        else
          stmt_add( 'nocompress' );         
        end if;         
        
        stmt_done();
      end if;    
    end if;
    
    if( p_orig_table_soll.i_constraints is not null )
    then
      for i in 1 .. p_orig_table_soll.i_constraints.count
      loop
        handle_constraint( p_orig_table_soll.i_constraints(i) );
      end loop;
    end if;
    
    if( p_orig_table_soll.i_ind_uks is not null )
    then
      for i in 1 .. p_orig_table_soll.i_ind_uks.count
      loop
         if( p_orig_table_soll.i_ind_uks(i) is of (ot_orig_index) ) 
         then
           v_orig_index_soll := treat( p_orig_table_soll.i_ind_uks(i) as ot_orig_index );
           
           handle_index( v_orig_index_soll );           
         else
           v_orig_uniquekey_soll := treat( p_orig_table_soll.i_ind_uks(i) as ot_orig_uniquekey );       
           
           handle_uniquekey( v_orig_uniquekey_soll );
         end if;
      end loop;
    end if;    
    
    if( p_orig_table_soll.i_primary_key is not null )
    then
      if(   v_orig_table_ist is null
         or is_equal_ignore_case( p_orig_table_soll.i_primary_key.i_consname,   v_orig_table_ist.i_primary_key.i_consname   ) != 1
         or is_equal(             p_orig_table_soll.i_primary_key.i_pk_columns, v_orig_table_ist.i_primary_key.i_pk_columns ) != 1
         or is_equal(             p_orig_table_soll.i_primary_key.i_reverse,    v_orig_table_ist.i_primary_key.i_reverse    ) != 1  
         or ( is_equal_ignore_case(v_orig_table_ist.i_primary_key.i_tablespace, nvl(p_orig_table_soll.i_primary_key.i_tablespace, pv_default_tablespace)) != 1 
             and not (v_orig_table_ist.i_primary_key.i_tablespace is null and p_orig_table_soll.i_primary_key.i_tablespace is null) 
             and pa_orcas_run_parameter.is_indexmovetablespace = 1 )
        )
      then
        drop_table_pk_if_exists( v_orig_table_ist );
        
        pv_stmt := 'alter table ' || p_orig_table_soll.i_name || ' add';
        if( p_orig_table_soll.i_primary_key.i_consname is not null )
        then
          pv_stmt := pv_stmt || ' ' || 'constraint ' || p_orig_table_soll.i_primary_key.i_consname;        
        end if;
        pv_stmt := pv_stmt || ' ' || 'primary key (' || get_column_list( p_orig_table_soll.i_primary_key.i_pk_columns ) || ')';
        
        if(    p_orig_table_soll.i_primary_key.i_tablespace is not null
            or p_orig_table_soll.i_primary_key.i_reverse is not null )
        then
          pv_stmt := pv_stmt || ' ' || 'using index';
            
          if( p_orig_table_soll.i_primary_key.i_reverse is not null )
          then        
            pv_stmt := pv_stmt || ' ' || 'reverse';
          end if;
            
          if( p_orig_table_soll.i_primary_key.i_tablespace is not null )
          then        
            pv_stmt := pv_stmt || ' ' || 'tablespace ' || p_orig_table_soll.i_primary_key.i_tablespace;
          end if;
        end if;

        add_stmt();
      end if; 
    else
      drop_table_pk_if_exists( v_orig_table_ist );
    end if;
    
    if( p_orig_table_soll.i_comments is not null )
    then
      for i in 1..p_orig_table_soll.i_comments.count
      loop
        handle_comment( p_orig_table_soll.i_comments(i) );
      end loop;
    end if;
    
    if( p_orig_table_soll.i_mviewlog is not null )
    then
      handle_mviewlog( p_orig_table_soll );
    end if;
    
  end;
  
  procedure handle_all_tables is     
    v_orig_table_ist ot_orig_table;
    v_orig_table_soll ot_orig_table;
    v_orig_mview_soll ot_orig_mview;
    v_orig_foreignkey_soll ot_orig_foreignkey;
    v_orig_uniquekey ot_orig_uniquekey;
    v_orig_index ot_orig_index;
    v_fk_false_data_select varchar2(32000);
    v_fk_false_data_where_part varchar2(32000);
  begin
    for i in 1 .. pv_model_ist.i_model_elements.count loop
      if( pv_model_ist.i_model_elements(i) is of (ot_orig_table) ) 
      then
        v_orig_table_ist := treat( pv_model_ist.i_model_elements(i) as ot_orig_table );      

        v_orig_table_soll := find_table( v_orig_table_ist, pv_orig_table_list_soll );
        if( v_orig_table_ist.i_foreign_keys is not null )
        then
          for i in 1 .. v_orig_table_ist.i_foreign_keys.count
          loop
            v_orig_foreignkey_soll := find_table_foreignkey( v_orig_table_ist.i_foreign_keys(i), v_orig_table_soll );
  
            if( is_equal_foreignkey( v_orig_table_ist.i_foreign_keys(i), v_orig_foreignkey_soll ) != 1 )
            then
              drop_table_constraint_by_name( v_orig_table_ist, v_orig_table_ist.i_foreign_keys(i).i_consname );
            end if;
          end loop;
        end if;    
      end if;            
    end loop;     
  
    for i in 1 .. pv_model_ist.i_model_elements.count loop
      if( pv_model_ist.i_model_elements(i) is of (ot_orig_table) ) 
      then
        v_orig_table_ist := treat( pv_model_ist.i_model_elements(i) as ot_orig_table );      
        v_orig_table_soll := find_table( v_orig_table_ist, pv_orig_table_list_soll );
                
        if( v_orig_table_soll is null )
        then         
          v_orig_mview_soll := find_mview_by_table( v_orig_table_ist, pv_orig_mview_list_soll );
          
          if( v_orig_mview_soll is null and v_orig_table_ist.i_name not like 'ORCAS_%')
          then  
            drop_with_dropmode_check( 'select 1 from ' || v_orig_table_ist.i_name,
                                      'drop table ' || v_orig_table_ist.i_name );
          end if;                            
        else
          if( v_orig_table_ist.i_constraints is not null )
          then
            for i in 1 .. v_orig_table_ist.i_constraints.count
            loop
              if( find_table_constraint( v_orig_table_ist.i_constraints(i), v_orig_table_soll ) is null )
              then
                drop_table_constraint_by_name( v_orig_table_ist, v_orig_table_ist.i_constraints(i).i_consname );
              end if;
            end loop;
          end if;  
          
          if( v_orig_table_ist.i_mviewlog is not null and v_orig_table_soll.i_mviewlog is null )
          then
            add_stmt( 'drop materialized view log on ' || v_orig_table_ist.i_name );  
          end if;
          
          if( v_orig_table_ist.i_ind_uks is not null )
          then
            for i in 1 .. v_orig_table_ist.i_ind_uks.count
            loop            
              if( v_orig_table_ist.i_ind_uks(i) is of (ot_orig_index) ) 
              then
                v_orig_index := treat( v_orig_table_ist.i_ind_uks(i) as ot_orig_index );
                
                if( find_table_index( v_orig_index, v_orig_table_soll ) is null and
                    find_table_uniquekey_by_index( v_orig_index.i_consname, v_orig_table_ist ) is null )
                then
                  add_stmt( 'drop index ' || v_orig_index.i_consname );                                  
                end if; 
              else
                v_orig_uniquekey := treat( v_orig_table_ist.i_ind_uks(i) as ot_orig_uniquekey );           
                
                if( find_table_uniquekey( v_orig_uniquekey, v_orig_table_soll ) is null )
                then
                  drop_table_constraint_by_name( v_orig_table_ist, v_orig_uniquekey.i_consname );                  
                end if;   
              end if;
            end loop;
          end if;   
          
          if( v_orig_table_ist.i_comments is not null )
          then
            for i in 1 .. v_orig_table_ist.i_comments.count
            loop
              if( find_table_comment( v_orig_table_ist.i_comments(i), v_orig_table_soll ) is null )
              then
                stmt_set( 'comment on' );
                stmt_add( v_orig_table_ist.i_comments(i).i_comment_object.i_name );                
                stmt_add( ' ' );                
                stmt_add( v_orig_table_ist.i_name );
                if( v_orig_table_ist.i_comments(i).i_column_name is not null )
                then
                  stmt_add( '.' );
                  stmt_add( v_orig_table_ist.i_comments(i).i_column_name );                
                end if;
                stmt_add( 'is ''''' );        
                
                add_stmt();           
              end if;
            end loop;
          end if;        
        end if;
      end if;        
    end loop;    
    
    for i in 1 .. pv_orig_table_list_soll.count loop
      
      -- Sonderbehandlung für Tabellen zu Mviews, die nicht prebuilt sind, sie dürfen nicht behandelt werden
      v_orig_mview_soll := find_mview_by_table( pv_orig_table_list_soll(i), pv_orig_mview_list_soll );          
      if( v_orig_mview_soll is null or
          ( v_orig_mview_soll is not null and 
            ot_orig_buildmodetype.is_equal( v_orig_mview_soll.i_buildmode, ot_orig_buildmodetype.c_prebuilt, ot_orig_buildmodetype.c_immediate ) = 1
          )) 
      then  
        handle_table( pv_orig_table_list_soll(i) );
      end if;  
    end loop;
    
    for i in 1 .. pv_orig_table_list_soll.count loop
      v_orig_table_soll := pv_orig_table_list_soll(i);
      v_orig_table_ist := find_table( v_orig_table_soll, pv_orig_table_list_ist );
      if( v_orig_table_soll.i_foreign_keys is not null )
      then
        for i in 1 .. v_orig_table_soll.i_foreign_keys.count
        loop
          v_orig_foreignkey_soll := v_orig_table_soll.i_foreign_keys(i);          
        
          if( is_equal_foreignkey( v_orig_foreignkey_soll, find_table_foreignkey( v_orig_foreignkey_soll, v_orig_table_ist ) ) != 1 )
          then
            if(   v_orig_table_ist is null 
              and v_orig_table_soll.i_tablepartitioning is not null 
              and v_orig_table_soll.i_tablepartitioning is of (ot_orig_refpartitions) 
              and get_fk_for_ref_partitioning( v_orig_table_soll ).i_consname = v_orig_foreignkey_soll.i_consname
              )
            then
              -- in diesem Fall haben wir eine ref-partitionierte Tabelle die in diesem Lauf angelegt wurde, und damit ist der get_fk_for_ref_partitioning schon angelegt worden.
              null;
            else
              if(   pa_orcas_run_parameter.is_dropmode = 1 
                and v_orig_table_ist is not null 
                )
              then
                v_fk_false_data_where_part := null;
                for i in 1 .. v_orig_foreignkey_soll.i_srccolumns.count
                loop
                  if( v_fk_false_data_where_part is not null )
                  then
                    v_fk_false_data_where_part := v_fk_false_data_where_part || ' or ';
                  end if;
                  v_fk_false_data_where_part := v_fk_false_data_where_part || v_orig_foreignkey_soll.i_srccolumns(i).i_column_name || ' is not null ';
                end loop;
                v_fk_false_data_where_part := 'where (' || v_fk_false_data_where_part || ') and (' || get_column_list( v_orig_foreignkey_soll.i_srccolumns ) || ') not in (select ' || get_column_list( v_orig_foreignkey_soll.i_destcolumns ) || '  from ' || v_orig_foreignkey_soll.i_desttable || ')';
                v_fk_false_data_select := 'select 1 from ' || v_orig_table_ist.i_name || ' ' || v_fk_false_data_where_part;
                
                if( has_rows_ignore_errors( v_fk_false_data_select ) = 1 )
                then
                  if( ot_orig_fkdeleteruletype.is_equal( v_orig_foreignkey_soll.i_delete_rule, ot_orig_fkdeleteruletype.c_cascade ) = 1 )
                  then
                    add_stmt( 'delete ' || v_orig_table_ist.i_name || ' ' || v_fk_false_data_where_part );
                    add_stmt( 'commit' );
                  elsif( ot_orig_fkdeleteruletype.is_equal( v_orig_foreignkey_soll.i_delete_rule, ot_orig_fkdeleteruletype.c_set_null ) = 1 )
                  then
                    add_stmt( 'update ' || v_orig_table_ist.i_name || ' set ' || get_column_list( v_orig_foreignkey_soll.i_srccolumns ) || ' = null ' || v_fk_false_data_where_part );
                    add_stmt( 'commit' );                  
                  else
                    raise_application_error( -20000, 'Fehler beim FK Aufbau ' || v_orig_foreignkey_soll.i_consname || ' auf tabelle ' || v_orig_table_ist.i_name || ' Datenbereinigung nicht möglich, da keine delete rule. ' || v_fk_false_data_select );    
                  end if;
                end if;
              end if;
                          
              stmt_set( 'alter table ' || v_orig_table_soll.i_name );
              stmt_add( 'add' );
              stmt_add( create_foreign_key_clause( v_orig_foreignkey_soll ) );
              stmt_done();      
            end if;
          end if;
        end loop;
      end if;    
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

  procedure compare_and_update(pi_model_ist in ot_orig_model, pi_model_soll in ot_orig_model)
  is
    v_nls_length_default varchar2(100);
    
    procedure inline_soll_ext_comments
    is
      v_orig_comment ot_orig_comment;
      v_table_index number;
      v_orig_table ot_orig_table;
      v_new_orig_inlinecomment ot_orig_inlinecomment;
    begin
      for i in 1 .. pv_model_soll.i_model_elements.count 
      loop
        if( pv_model_soll.i_model_elements(i) is of (ot_orig_comment) ) 
        then
          v_orig_comment := treat( pv_model_soll.i_model_elements(i) as ot_orig_comment );
        
          v_table_index := find_table_position_by_name( v_orig_comment.i_table_name, pv_orig_table_list_soll );
          
          v_orig_table := pv_orig_table_list_soll( v_table_index );
          
          v_new_orig_inlinecomment := new ot_orig_inlinecomment();
          v_new_orig_inlinecomment.i_column_name     := v_orig_comment.i_column_name;
          v_new_orig_inlinecomment.i_comment         := v_orig_comment.i_comment;          
          v_new_orig_inlinecomment.i_comment_object  := v_orig_comment.i_comment_object;                 
          
          if( v_orig_table.i_comments is null )
          then
            v_orig_table.i_comments := new ct_orig_inlinecomment_list();
          end if;
          
          v_orig_table.i_comments.extend;
          v_orig_table.i_comments( v_orig_table.i_comments.count ) := v_new_orig_inlinecomment;
          
          pv_orig_table_list_soll( v_table_index ) := v_orig_table;
        end if;
      end loop;
    end;
    
    procedure inline_soll_ext_indexes
    is
      v_orig_indexextable ot_orig_indexextable;
      v_table_index number;
      v_orig_table ot_orig_table;
      v_new_orig_index ot_orig_index;
    begin
      for i in 1 .. pv_model_soll.i_model_elements.count 
      loop
        if( pv_model_soll.i_model_elements(i) is of (ot_orig_indexextable) ) 
        then
          v_orig_indexextable := treat( pv_model_soll.i_model_elements(i) as ot_orig_indexextable );
        
          v_table_index := find_table_position_by_name( v_orig_indexextable.i_table_name, pv_orig_table_list_soll );
          
          v_orig_table := pv_orig_table_list_soll( v_table_index );
          
          v_new_orig_index := new ot_orig_index();
          v_new_orig_index.i_consname                   := v_orig_indexextable.i_index_name;
          v_new_orig_index.i_index_columns              := v_orig_indexextable.i_index_columns; 
          v_new_orig_index.i_global                     := v_orig_indexextable.i_global;           
          v_new_orig_index.i_logging                    := v_orig_indexextable.i_logging;           
          v_new_orig_index.i_parallel                   := v_orig_indexextable.i_parallel;   
          v_new_orig_index.i_parallel_degree            := v_orig_indexextable.i_parallel_degree;
          v_new_orig_index.i_bitmap                     := v_orig_indexextable.i_bitmap;           
          v_new_orig_index.i_unique                     := v_orig_indexextable.i_uniqueness;                     
          v_new_orig_index.i_function_based_expression  := v_orig_indexextable.i_function_based_expression;           
          v_new_orig_index.i_domain_index_expression    := v_orig_indexextable.i_domain_index_expression;           
          v_new_orig_index.i_tablespace                 := v_orig_indexextable.i_tablespace;
          v_new_orig_index.i_compression                := v_orig_indexextable.i_compression;
          
          if( v_orig_table.i_ind_uks is null )
          then
            v_orig_table.i_ind_uks := new ct_orig_indexoruniquekey_list();
          end if;
          
          v_orig_table.i_ind_uks.extend;
          v_orig_table.i_ind_uks( v_orig_table.i_ind_uks.count ) := v_new_orig_index;
          
          pv_orig_table_list_soll( v_table_index ) := v_orig_table;
        end if;
      end loop;
    end;
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
  
    pv_model_ist := pi_model_ist;
    pv_model_soll := pi_model_soll;
    -- dass sortieren der istdaten ist unnoetig, aber im moment die einfachste moeglichkeit die liste der tabellen zu ermitteln
    pv_orig_table_list_ist      := sort_tables_for_ref_part( pv_model_ist.i_model_elements );      
    pv_orig_table_list_soll     := sort_tables_for_ref_part( pv_model_soll.i_model_elements );      
    pv_orig_sequence_list_ist   := get_sequence_elements( pv_model_ist.i_model_elements );          
    pv_orig_sequence_list_soll  := get_sequence_elements( pv_model_soll.i_model_elements );      
    pv_orig_mview_list_ist      := get_mview_elements( pv_model_ist.i_model_elements );          
    pv_orig_mview_list_soll     := get_mview_elements( pv_model_soll.i_model_elements );      
    
    inline_soll_ext_indexes();
    inline_soll_ext_comments();
    
    pv_statement_list := new t_varchar_list();
    pv_stmt := null;
    
    handle_all_tables();   

    handle_all_sequences();   
    
    handle_all_mviews();
    
    execute_all_statements();    
  end;   

end;
/
