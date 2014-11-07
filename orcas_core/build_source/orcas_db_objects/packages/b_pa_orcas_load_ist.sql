create or replace package body pa_orcas_load_ist is

  type t_ignore_cache is table of number index by varchar2(100);
  v_ignore_cache t_ignore_cache;
  
  function get_exclude_where( p_exclude_where in varchar2 ) return varchar2
  is
  begin
    if( instr( p_exclude_where, '@' ) = 1 )
    then
      return 'object_name like ''%$%''';
    else
      return p_exclude_where;
    end if;
  end;
  
  procedure load_ignore_cache( p_exclude_where in varchar2, p_type in varchar2 )
  is
    c_cur sys_refcursor; 
    v_ignore_cache_key varchar2(100);
    v_dummy number;   
    v_name varchar2(100);
  begin
    open c_cur  
    for 'select object_name, case when (' || get_exclude_where(p_exclude_where) || ') then 1 else 0 end from user_objects where object_type=''' || p_type || '''';
    
    loop
      fetch c_cur into v_name, v_dummy;
      exit when c_cur%notfound;
      
      v_ignore_cache_key := v_name || '_' || p_type;      
      
      v_ignore_cache(v_ignore_cache_key) := v_dummy;
    end loop; 
  end;

  function is_ignored( p_name in varchar2, p_exclude_where in varchar2, p_type in varchar2 ) return number
  is
    c_cur sys_refcursor; 
    v_dummy number;    
    v_ignore_cache_key varchar2(100);
  begin
    v_ignore_cache_key := p_name || '_' || p_type;
  
    if( v_ignore_cache.exists(v_ignore_cache_key) )
    then
      return v_ignore_cache(v_ignore_cache_key);
    end if;
  
    return 1;  
  end;
  
  function is_ignored_sequence( p_name in varchar2 ) return number
  is
  begin
    return is_ignored( p_name, pa_orcas_run_parameter.get_excludewheresequence(), 'SEQUENCE' );
  end;
  
  function is_ignored_table( p_name in varchar2 ) return number
  is
  begin
    return is_ignored( p_name, pa_orcas_run_parameter.get_excludewheretable(), 'TABLE' );
  end;  
  
  function is_ignored_mview( p_name in varchar2 ) return number
  is
  begin
    return is_ignored( p_name, pa_orcas_run_parameter.get_excludewheremview(), 'MATERIALIZED VIEW' );
  end;    

  function load_all_sequences return ct_orig_modelelement_list is
    v_return ct_orig_modelelement_list :=  ct_orig_modelelement_list();
    v_orig_sequence ot_orig_sequence;
  begin
    for cur_seq in
      (
      select sequence_name,
             increment_by,
             last_number,
             cache_size,
             min_value,
             max_value,
             cycle_flag,
             order_flag
        from user_sequences        
       order by sequence_name
      )
    loop
      if( is_ignored_sequence( cur_seq.sequence_name ) = 0 )
      then
        v_orig_sequence := ot_orig_sequence();
        
        v_orig_sequence.i_sequence_name := cur_seq.sequence_name;
        v_orig_sequence.i_increment_by := cur_seq.increment_by;              
        v_orig_sequence.i_max_value_select := cur_seq.last_number;   
        v_orig_sequence.i_cache := cur_seq.cache_size;      
        v_orig_sequence.i_minvalue := cur_seq.min_value;      
        v_orig_sequence.i_maxvalue := cur_seq.max_value;     
        
        if (cur_seq.cycle_flag = 'Y')
        then
            v_orig_sequence.i_cycle := ot_orig_cycletype.c_cycle(); 
        else
            v_orig_sequence.i_cycle := ot_orig_cycletype.c_nocycle(); 
        end if;  
        
        if (cur_seq.order_flag = 'Y')
        then
            v_orig_sequence.i_order := ot_orig_ordertype.c_order(); 
        else
            v_orig_sequence.i_order := ot_orig_ordertype.c_noorder(); 
        end if;  
        
        v_return.extend(1);
        v_return(v_return.count) := v_orig_sequence;
      end if;
    end loop;
  
    return v_return;
  end;
  
  function load_all_mviews return ct_orig_mview_list is
    v_return ct_orig_mview_list :=  ct_orig_mview_list();
    v_orig_mview ot_orig_mview;
  begin
    for cur_mview in
      (
      select mview_name,
             query,
             updatable,
             rewrite_enabled,
             refresh_mode,
             refresh_method,
             build_mode,
             staleness,
             unknown_prebuilt,
             compile_state,
             trim(degree) degree,
             trim(compression) compression,  
             trim(compress_for) compress_for,
             tablespace_name
        from user_mviews mviews
        left outer join user_tables tables
        on mviews.mview_name = tables.table_name
       order by mview_name
      )
    loop
      if( is_ignored_mview( cur_mview.mview_name ) = 0 )
      then
        v_orig_mview := ot_orig_mview();
        
        v_orig_mview.i_mview_name := cur_mview.mview_name;
        v_orig_mview.i_viewselect := '"' || cur_mview.query || '"';     
        
        if (cur_mview.build_mode is not null)
        then
          if (cur_mview.build_mode = 'IMMEDIATE')
          then
            v_orig_mview.i_buildmode := ot_orig_buildmodetype.c_immediate();
          elsif (cur_mview.build_mode = 'DEFERRED')
          then
            v_orig_mview.i_buildmode := ot_orig_buildmodetype.c_deferred();  
          elsif (cur_mview.build_mode = 'PREBUILT')
          then
            v_orig_mview.i_buildmode := ot_orig_buildmodetype.c_prebuilt();  
          end if;       
        end if;
        
        if (cur_mview.refresh_mode is not null)
        then
          if (cur_mview.refresh_mode = 'COMMIT')
          then
            v_orig_mview.i_refreshmode := ot_orig_refreshmodetype.c_commit();
          elsif (cur_mview.refresh_mode = 'DEMAND')
          then
            v_orig_mview.i_refreshmode := ot_orig_refreshmodetype.c_demand();    
          end if;       
        end if;        
        
        if (cur_mview.refresh_method is not null)
        then
          if (cur_mview.refresh_method = 'COMPLETE')
          then
            v_orig_mview.i_refreshmethod := ot_orig_refreshmethodtype.c_complete();
          elsif (cur_mview.refresh_method = 'FORCE')
          then
            v_orig_mview.i_refreshmethod := ot_orig_refreshmethodtype.c_force();  
          elsif (cur_mview.refresh_method = 'FAST')
          then
            v_orig_mview.i_refreshmethod := ot_orig_refreshmethodtype.c_fast();    
          elsif (cur_mview.refresh_method = 'NEVER')
          then
            v_orig_mview.i_refreshmethod := ot_orig_refreshmethodtype.c_never();  
          end if;       
        end if;              
        
        if (cur_mview.rewrite_enabled = 'Y')
        then
            v_orig_mview.i_queryrewrite := ot_orig_enabletype.c_enable(); 
        else
            v_orig_mview.i_queryrewrite := ot_orig_enabletype.c_disable(); 
        end if;  
        
        -- Physical parameters nur, wenn nicht prebuilt
        if( nvl(cur_mview.build_mode, 'NULL') != 'PREBUILT' )     
        then
          v_orig_mview.i_tablespace   := cur_mview.tablespace_name;
          
          if ( cur_mview.compression is not null )
          then
            if ( upper(cur_mview.compression) = 'ENABLED' )  
            then
              v_orig_mview.i_compression := ot_orig_compresstype.c_compress;  
              if ( upper(NVL(cur_mview.compress_for,'NULL')) like '%OLTP%' ) 
              then
                v_orig_mview.i_compressionfor := ot_orig_compressfortype.c_all();
              elsif ( upper(NVL(cur_mview.compress_for,'NULL')) = 'BASIC' ) 
              then
                v_orig_mview.i_compressionfor := ot_orig_compressfortype.c_direct_load();  
              elsif ( upper(NVL(cur_mview.compress_for,'NULL')) = 'QUERY LOW' ) 
              then
                v_orig_mview.i_compressionfor := ot_orig_compressfortype.c_query_low();  
              elsif ( upper(NVL(cur_mview.compress_for,'NULL')) = 'QUERY HIGH' ) 
              then
                v_orig_mview.i_compressionfor := ot_orig_compressfortype.c_query_high();   
              elsif ( upper(NVL(cur_mview.compress_for,'NULL')) = 'ARCHIVE LOW' ) 
              then
                v_orig_mview.i_compressionfor := ot_orig_compressfortype.c_archive_low();  
              elsif ( upper(NVL(cur_mview.compress_for,'NULL')) = 'ARCHIVE HIGH' ) 
              then
                v_orig_mview.i_compressionfor := ot_orig_compressfortype.c_archive_high();  
              end if;
            elsif ( upper(cur_mview.compression) = 'DISABLED' )
            then
              v_orig_mview.i_compression := ot_orig_compresstype.c_nocompress;
            end if;  
          end if;  
          
          if ( cur_mview.degree is not null )
          then
            if( nvl(cur_mview.degree, '0') = '1' )        
            then
              v_orig_mview.i_parallel := ot_orig_paralleltype.c_noparallel;        
            else
              v_orig_mview.i_parallel := ot_orig_paralleltype.c_parallel();
              if ( cur_mview.degree != 'DEFAULT' ) 
              then
                v_orig_mview.i_parallel_degree := to_number(cur_mview.degree);
              end if;
            end if;   
          end if;  
        end if;               
        
        v_return.extend(1);
        v_return(v_return.count) := v_orig_mview;
      end if;
    end loop;
  
    return v_return;
  end;  
  
  function load_all_tables return ct_orig_table_list is
    v_return ct_orig_table_list := ct_orig_table_list();
    v_orig_table ot_orig_table;
    v_last_found_table_index_name varchar2(100);
    v_last_found_table_index number;
    type t_varchar2_to_varchar2_map is table of varchar2(100) index by varchar2(100);
    v_generated_name_map t_varchar2_to_varchar2_map;
    v_unique_key_to_table_name_map t_varchar2_to_varchar2_map;    
    v_primarykey_to_table_name_map t_varchar2_to_varchar2_map;   
    v_tablespace varchar2(30);
    
    function find_table_index( p_table_name in varchar2 ) return number
    is
    begin
      if( p_table_name = v_last_found_table_index_name )
      then
        return v_last_found_table_index;
      end if;
    
      for i in 1..v_return.count
      loop
        if( v_return(i) .i_name = p_table_name )
        then
          v_last_found_table_index := i;
          v_last_found_table_index_name := p_table_name;
        
          return i;
        end if;
      end loop;
      
      raise_application_error( -20000, 'table not found: ' || p_table_name );
    end;    
    
    function find_index_index( p_table_index in number, p_index_name in varchar2 ) return number
    is
    begin
      for i in 1..v_return(p_table_index).i_ind_uks.count
      loop
        if( v_return(p_table_index).i_ind_uks(i).i_consname = p_index_name )
        then
          return i;
        end if;
      end loop;
      
      raise_application_error( -20000, 'index not found: ' || p_table_index || ' ' || p_index_name );
    end;       
    
    procedure add_column( p_table_name in varchar2, p_orig_column in ot_orig_column )
    is
      v_table_index number;
    begin
      v_table_index := find_table_index( p_table_name );
      v_return( v_table_index ).i_columns.extend(1);
      v_return( v_table_index ).i_columns( v_return( v_table_index ).i_columns.count ) := p_orig_column;
    end;
    
    procedure load_columns
    is
      v_orig_column ot_orig_column;    
      v_type_count PLS_INTEGER;
    begin
      for cur_tab_cols in
        (
        select user_tab_cols.table_name,
               user_tab_cols.column_name,
               data_type,
               data_type_owner,
               data_length, 
               data_precision, 
               data_scale,
               char_length,
               nullable,  
               char_used,
               data_default,
               column_id,
$IF DBMS_DB_VERSION.VERSION >= 12 $THEN                         
               default_on_null,
               generation_type
$ELSE
               null default_on_null,
               null generation_type
$END
          from user_tab_cols
$IF DBMS_DB_VERSION.VERSION >= 12 $THEN          
          left outer join user_tab_identity_cols 
               on ( user_tab_cols.column_name = user_tab_identity_cols.column_name
                and user_tab_cols.table_name = user_tab_identity_cols.table_name)
$END                
         where hidden_column = 'NO'
         order by table_name, column_id, column_name
        )
        
      loop
        if( is_ignored_table( cur_tab_cols.table_name ) = 0 )
        then
          v_orig_column := new ot_orig_column();
          
          v_orig_column.i_name := cur_tab_cols.column_name;
          v_orig_column.i_default_value := substr(cur_tab_cols.data_default,1,4000);
          v_orig_column.i_default_value := trim( v_orig_column.i_default_value );
          if(   v_orig_column.i_default_value = '''''' 
             or upper(v_orig_column.i_default_value) = 'NULL' )
          then
            v_orig_column.i_default_value := null;
          end if;
          
          if( cur_tab_cols.nullable = 'N' )
          then
            v_orig_column.i_notnull_flg := 1;
          else
            v_orig_column.i_notnull_flg := 0;          
          end if;
          
          if (cur_tab_cols.char_used = 'B')
          then
              v_orig_column.i_byteorchar := ot_orig_chartype.c_byte(); 
          elsif  (cur_tab_cols.char_used = 'C') 
          then
              v_orig_column.i_byteorchar := ot_orig_chartype.c_char(); 
          end if;  
          
          if (cur_tab_cols.data_type = 'NUMBER')
          then
              v_orig_column.i_data_type := ot_orig_datatype.c_number(); 
              v_orig_column.i_precision := cur_tab_cols.data_precision;
              v_orig_column.i_scale     := cur_tab_cols.data_scale;
              
          elsif  (cur_tab_cols.data_type = 'BLOB') 
          then
              v_orig_column.i_data_type := ot_orig_datatype.c_blob(); 
          elsif  (cur_tab_cols.data_type = 'CLOB') 
          then
              v_orig_column.i_data_type := ot_orig_datatype.c_clob(); 
          elsif  (cur_tab_cols.data_type = 'NCLOB') 
          then
              v_orig_column.i_data_type := ot_orig_datatype.c_nclob(); 
          elsif  (cur_tab_cols.data_type = 'VARCHAR2') 
          then
              v_orig_column.i_data_type := ot_orig_datatype.c_varchar2(); 
              v_orig_column.i_precision := cur_tab_cols.char_length;     
          elsif  (cur_tab_cols.data_type = 'NVARCHAR2') 
          then
              v_orig_column.i_data_type := ot_orig_datatype.c_nvarchar2(); 
              v_orig_column.i_precision := cur_tab_cols.char_length;      
              v_orig_column.i_byteorchar := null;
          elsif  (cur_tab_cols.data_type = 'CHAR') 
          then
              v_orig_column.i_data_type := ot_orig_datatype.c_char(); 
              v_orig_column.i_precision := cur_tab_cols.char_length;                           
          elsif  (cur_tab_cols.data_type = 'DATE') 
          then
              v_orig_column.i_data_type := ot_orig_datatype.c_date();                
          elsif  (cur_tab_cols.data_type = 'XMLTYPE') 
          then
              v_orig_column.i_data_type := ot_orig_datatype.c_xmltype();   
          elsif  ( instr(cur_tab_cols.data_type,'TIMESTAMP') = 1 ) 
          then
              v_orig_column.i_data_type := ot_orig_datatype.c_timestamp();   
              v_orig_column.i_precision := cur_tab_cols.data_scale;                             
          elsif  (cur_tab_cols.data_type = 'ROWID') 
          then
              v_orig_column.i_data_type := ot_orig_datatype.c_rowid();       
          elsif  (cur_tab_cols.data_type = 'LONG RAW') 
          then
              v_orig_column.i_data_type := ot_orig_datatype.c_long_raw();
          elsif  (cur_tab_cols.data_type = 'LONG') 
          then
              v_orig_column.i_data_type := ot_orig_datatype.c_long();
          elsif  (cur_tab_cols.data_type = 'RAW') 
          then
              v_orig_column.i_data_type := ot_orig_datatype.c_raw(); 
              v_orig_column.i_precision := cur_tab_cols.data_length; 
          elsif  (cur_tab_cols.data_type = 'FLOAT') 
          then
              v_orig_column.i_data_type := ot_orig_datatype.c_float(); 
              v_orig_column.i_precision := cur_tab_cols.data_precision;               
          else
              if( cur_tab_cols.data_type_owner is null )
              then
                raise_application_error( -20000, cur_tab_cols.data_type || ' ' || v_type_count );              
              end if;
              
              v_orig_column.i_object_type := cur_tab_cols.data_type;     
              v_orig_column.i_data_type := ot_orig_datatype.c_object();           
              
              if( cur_tab_cols.data_type_owner not in (user,'PUBLIC') )
              then
                v_orig_column.i_object_type := cur_tab_cols.data_type_owner || '.' || v_orig_column.i_object_type;
              end if;
          end if;          
          
          if( instr(v_orig_column.i_default_value,'ISEQ$$') > 0 )
          then
            v_orig_column.i_default_value := null;            
          end if;
          
          if( cur_tab_cols.generation_type is not null )
          then
            v_orig_column.i_default_value := null;
          
            v_orig_column.i_identity := new ot_orig_columnidentity();
            
            if( cur_tab_cols.generation_type = 'ALWAYS' )            
            then
              v_orig_column.i_identity.i_always := 'always';
            end if;
            if( cur_tab_cols.generation_type = 'BY DEFAULT' )            
            then
              v_orig_column.i_identity.i_by_default := 'default';            
            end if;
            
            if( cur_tab_cols.default_on_null = 'YES' )            
            then
              v_orig_column.i_identity.i_on_null := 'null';            
            end if;            
          end if;
        
          add_column( cur_tab_cols.table_name, v_orig_column );
        end if;
      end loop;        
    end;
    
    procedure add_indexoruniquekey( p_table_name in varchar2, p_orig_indexoruniquekey in ot_orig_indexoruniquekey )
    is
      v_table_index number;
    begin
      v_table_index := find_table_index( p_table_name );
      
      v_return( v_table_index ).i_ind_uks.extend(1);
      v_return( v_table_index ).i_ind_uks( v_return( v_table_index ).i_ind_uks.count ) := p_orig_indexoruniquekey;
    end;    
    
    procedure load_indexes
    is
      v_orig_index ot_orig_index;
    begin            
      for cur_indexes in
        (
        select index_name,
               table_name,
               uniqueness,
               tablespace_name,
               logging,
               degree,
               partitioned,
               index_type,
               compression
          from user_indexes
         where generated = 'N'
           and (index_name,table_name) not in 
               (
               select constraint_name,
                      table_name
                 from user_constraints
                where constraint_type in ( 'U', 'P' )
                  and constraint_name = user_constraints.index_name
               )
         order by table_name,
                  index_name
        )
      loop
        if( is_ignored_table( cur_indexes.table_name ) = 0 )
        then      
          v_orig_index := new ot_orig_index();
          
          v_orig_index.i_consname := cur_indexes.index_name;        
          v_orig_index.i_index_columns := ct_orig_columnref_list();
          v_orig_index.i_tablespace := cur_indexes.tablespace_name;
          
          if( cur_indexes.uniqueness = 'UNIQUE' )
          then
            v_orig_index.i_unique := 'unique';
          end if;
          
          if( cur_indexes.index_type = 'BITMAP' )
          then
            v_orig_index.i_bitmap := 'bitmap';
          end if;
          
          if( cur_indexes.degree = '1' )        
          then
            v_orig_index.i_parallel := ot_orig_paralleltype.c_noparallel;        
          else
            v_orig_index.i_parallel := ot_orig_paralleltype.c_parallel();
            if ( cur_indexes.degree != 'DEFAULT' ) 
            then
              v_orig_index.i_parallel_degree := to_number(cur_indexes.degree);
            end if;
          end if;        
          if( cur_indexes.logging = 'YES' )        
          then
            v_orig_index.i_logging := ot_orig_loggingtype.c_logging;        
          else
            v_orig_index.i_logging := ot_orig_loggingtype.c_nologging;        
          end if;   
          if( cur_indexes.partitioned = 'NO' )        
          then
            if( cur_indexes.index_type = 'BITMAP' )
            then
              v_orig_index.i_global := NULL;
            else  
              v_orig_index.i_global := ot_orig_indexglobaltype.c_global;   
            end if;  
          else
            v_orig_index.i_global := ot_orig_indexglobaltype.c_local;
            -- cur_indexes.logging ist hier null, da das logging pro partition eingestellt ist
            v_orig_index.i_logging := ot_orig_loggingtype.c_logging;                
          end if;             
          if( cur_indexes.compression = 'ENABLED' )        
          then
            v_orig_index.i_compression := ot_orig_compresstype.c_compress;        
          elsif( cur_indexes.compression = 'DISABLED' )  
          then
            v_orig_index.i_compression := ot_orig_compresstype.c_nocompress;       
          end if; 
          
          add_indexoruniquekey( cur_indexes.table_name, v_orig_index );
        end if;
      end loop;    
    end;
    
    procedure add_index_column( p_table_name in varchar2, p_index_name in varchar2, p_orig_columnref in ot_orig_columnref )
    is
      v_table_index number;
      v_index_index number;      
      v_orig_index ot_orig_index;
    begin
      v_table_index := find_table_index( p_table_name );      
      v_index_index := find_index_index( v_table_index, p_index_name );
      
      v_orig_index := treat( v_return( v_table_index ).i_ind_uks( v_index_index ) as ot_orig_index);
      
      v_orig_index.i_index_columns.extend(1);
      v_orig_index.i_index_columns( v_orig_index.i_index_columns.count ) := p_orig_columnref;
      
      v_return( v_table_index ).i_ind_uks( v_index_index ) := v_orig_index;
    end;        
    
    procedure load_index_columns
    is
      v_orig_columnref ot_orig_columnref;
    begin            
      for cur_ind_columns in
        (
        select user_ind_columns.table_name, 
               user_ind_columns.index_name,
               column_name
          from user_ind_columns,
               user_indexes
         where generated = 'N'
           and user_ind_columns.index_name = user_indexes.index_name
           and (user_indexes.index_name,user_indexes.table_name) not in 
               (
               select constraint_name,
                      table_name
                 from user_constraints
                where constraint_type in ( 'U', 'P' )
                  and constraint_name = user_constraints.index_name
               )        
         order by table_name,
                  index_name,
                  column_position
        )
      loop
        if( is_ignored_table( cur_ind_columns.table_name ) = 0 )
        then      
          v_orig_columnref := new ot_orig_columnref();
          
          v_orig_columnref.i_column_name := cur_ind_columns.column_name;                
          
          add_index_column( cur_ind_columns.table_name, cur_ind_columns.index_name, v_orig_columnref );
        end if;
      end loop;    
    end;
    
    procedure set_index_column_expression( p_table_name in varchar2, p_index_name in varchar2, p_column_position in number, p_expression in varchar2, p_max_column_position_for_ind in number )
    is
      v_table_index number;
      v_index_index number;      
      v_orig_index ot_orig_index;
    begin
      v_table_index := find_table_index( p_table_name );      
      v_index_index := find_index_index( v_table_index, p_index_name );
      
      v_orig_index := treat( v_return( v_table_index ).i_ind_uks( v_index_index ) as ot_orig_index);
      
      v_orig_index.i_index_columns( p_column_position ).i_column_name := replace(replace( ltrim(p_expression,',') ,'"',NULL),' ',NULL);
      
      if( p_column_position = p_max_column_position_for_ind )
      then
        v_orig_index.i_function_based_expression := pa_orcas_compare.get_column_list( v_orig_index.i_index_columns );
        v_orig_index.i_index_columns := null;
      end if;
      
      v_return( v_table_index ).i_ind_uks( v_index_index ) := v_orig_index;
    end;     
    
    procedure load_index_expressions
    is
      v_orig_columnref ot_orig_columnref;
    begin            
      for cur_ind_columns in
        (
        select user_ind_expressions.table_name, 
               user_ind_expressions.index_name,
               column_expression,
               column_position,
               max (column_position) 
                 over 
                 (
                   partition by 
                     user_ind_expressions.table_name, 
                     user_ind_expressions.index_name
                 ) as max_column_position_for_index
          from user_ind_expressions,
               user_indexes
         where generated = 'N'
           and user_ind_expressions.index_name = user_indexes.index_name
           and (user_indexes.index_name,user_indexes.table_name) not in 
               (
               select constraint_name,
                      table_name
                 from user_constraints
                where constraint_type in ( 'U', 'P' )
                  and constraint_name = user_constraints.index_name
               )        
         order by table_name,
                  index_name,
                  column_position
        )
      loop
        if( is_ignored_table( cur_ind_columns.table_name ) = 0 )
        then      
          set_index_column_expression( cur_ind_columns.table_name, cur_ind_columns.index_name, cur_ind_columns.column_position, cur_ind_columns.column_expression, cur_ind_columns.max_column_position_for_index );
        end if;
      end loop;    
    end;    
    
    procedure add_constraint( p_table_name in varchar2, p_orig_constraint in ot_orig_constraint )
    is
      v_table_index number;
    begin
      v_table_index := find_table_index( p_table_name );
      
      v_return( v_table_index ).i_constraints.extend(1);
      v_return( v_table_index ).i_constraints( v_return( v_table_index ).i_constraints.count ) := p_orig_constraint;
    end;         
    
    procedure add_foreignkey( p_table_name in varchar2, p_orig_foreignkey in ot_orig_foreignkey )
    is
      v_table_index number;
    begin
      v_table_index := find_table_index( p_table_name );
      
      v_return( v_table_index ).i_foreign_keys.extend(1);
      v_return( v_table_index ).i_foreign_keys( v_return( v_table_index ).i_foreign_keys.count ) := p_orig_foreignkey;
    end;       
    
    procedure load_table_constraints
    is
      v_orig_table ot_orig_table;
      v_orig_primarykey ot_orig_primarykey;
      v_orig_uniquekey ot_orig_uniquekey;
      v_orig_constraint ot_orig_constraint;
      v_orig_foreignkey ot_orig_foreignkey;
      v_orig_enabletype ot_orig_enabletype;
      v_orig_deferrtype ot_orig_deferrtype;
      v_generated_name number;
    begin            
      for cur_constraints in
        (
        select user_constraints.table_name, 
               constraint_name, 
               constraint_type, 
               search_condition, 
               r_constraint_name, 
               delete_rule, 
               deferrable, 
               deferred, 
               user_constraints.status,
               user_constraints.generated,
               user_indexes.tablespace_name,
               user_indexes.index_type
          from user_constraints
          left outer join user_indexes on (user_constraints.index_name = user_indexes.index_name)
         order by table_name,
                  constraint_name
        )
      loop
        if( is_ignored_table( cur_constraints.table_name ) = 0 )
        then      
          if( cur_constraints.status = 'ENABLED' )        
          then
            v_orig_enabletype := ot_orig_enabletype.c_enable;
          else
            v_orig_enabletype := ot_orig_enabletype.c_disable;
          end if;         
          
          if( cur_constraints.deferrable = 'DEFERRABLE' )
          then
            if( cur_constraints.deferred = 'DEFERRED' )        
            then
              v_orig_deferrtype := ot_orig_deferrtype.c_deferred;
            else
              v_orig_deferrtype := ot_orig_deferrtype.c_immediate;
            end if;       
          else
            v_orig_deferrtype := null;                
          end if;
          
          if( cur_constraints.generated = 'GENERATED NAME' )        
          then
            v_generated_name := 1;
          else
            v_generated_name := 0;
          end if;           
        
          if(    cur_constraints.constraint_type = 'P' )
          then
            v_orig_primarykey := new ot_orig_primarykey();
            v_orig_primarykey.i_pk_columns := new ct_orig_columnref_list();
          
            if( v_generated_name = 0 )
            then
              v_orig_primarykey.i_consname := cur_constraints.constraint_name;
            end if;
            v_primarykey_to_table_name_map( cur_constraints.constraint_name ) := cur_constraints.table_name;            
            v_orig_primarykey.i_status := v_orig_enabletype;
            v_orig_primarykey.i_tablespace := cur_constraints.tablespace_name;
            
            if( cur_constraints.index_type = 'NORMAL/REV' )
            then
              v_orig_primarykey.i_reverse := 'reverse';
            end if;
            
            v_return( find_table_index( cur_constraints.table_name ) ).i_primary_key := v_orig_primarykey;
          elsif( cur_constraints.constraint_type = 'U' )          
          then
            v_orig_uniquekey := new ot_orig_uniquekey();
            
            v_orig_uniquekey.i_consname := cur_constraints.constraint_name;
            v_orig_uniquekey.i_status := v_orig_enabletype;
            v_orig_uniquekey.i_uk_columns := new ct_orig_columnref_list();                  
            v_orig_uniquekey.i_tablespace := cur_constraints.tablespace_name;            

            v_unique_key_to_table_name_map( cur_constraints.constraint_name ) := cur_constraints.table_name;
            
            if( v_generated_name = 1 )
            then
              v_generated_name_map( cur_constraints.constraint_name ) := 'GENERATED_NAME';
            end if;                  
            
            add_indexoruniquekey( cur_constraints.table_name, v_orig_uniquekey );
          elsif( cur_constraints.constraint_type = 'R' )
          then
            v_orig_foreignkey := new ot_orig_foreignkey();
            
            if( v_generated_name = 0 )
            then
              v_orig_foreignkey.i_consname := cur_constraints.constraint_name;
            end if;
            v_orig_foreignkey.i_status := v_orig_enabletype;
            v_orig_foreignkey.i_deferrtype := v_orig_deferrtype;
            -- in i_desttable wird der ref-constraintname zwischengespeichert, wird durch update_foreignkey_srcdata dann korrekt aktualisiert
            v_orig_foreignkey.i_desttable := cur_constraints.r_constraint_name;
            v_orig_foreignkey.i_srccolumns := new ct_orig_columnref_list();
            
            if( cur_constraints.delete_rule = 'NO ACTION' )
            then
              v_orig_foreignkey.i_delete_rule := ot_orig_fkdeleteruletype.c_no_action;
            elsif( cur_constraints.delete_rule = 'SET NULL' )
            then
              v_orig_foreignkey.i_delete_rule := ot_orig_fkdeleteruletype.c_set_null;            
            else
              v_orig_foreignkey.i_delete_rule := ot_orig_fkdeleteruletype.c_cascade;
            end if;
            
            add_foreignkey( cur_constraints.table_name, v_orig_foreignkey );          
          null;
          elsif( cur_constraints.constraint_type = 'C' )
          then
            v_orig_constraint := new ot_orig_constraint();
            
            -- check-constraints muessen immer einen namen haben, sonst kommen da auch "xy is not null" generierte constraints mit
            if( v_generated_name = 0 )
            then            
              v_orig_constraint.i_consname := cur_constraints.constraint_name;
              v_orig_constraint.i_status := v_orig_enabletype;
              v_orig_constraint.i_deferrtype := v_orig_deferrtype;
              v_orig_constraint.i_rule := cur_constraints.search_condition;                        
            
              add_constraint( cur_constraints.table_name, v_orig_constraint );                    
            end if;
          else 
            raise_application_error( -20000, 'constraint type unknown ' || cur_constraints.constraint_type );
          end if;        
        end if;
      end loop;    
    end;            
    
    procedure add_primarykey_column( p_table_name in varchar2, p_orig_columnref in ot_orig_columnref )
    is
      v_table_index number;
    begin
      v_table_index := find_table_index( p_table_name );      
      
      v_return( v_table_index ).i_primary_key.i_pk_columns.extend(1);
      v_return( v_table_index ).i_primary_key.i_pk_columns( v_return( v_table_index ).i_primary_key.i_pk_columns.count ) := p_orig_columnref;
    end;        
    
    procedure add_uniquekey_column( p_table_name in varchar2, p_constraint_name in varchar2, p_orig_columnref in ot_orig_columnref )
    is
      v_table_index number;
      v_index_index number;      
      v_orig_uniquekey ot_orig_uniquekey;
    begin
      v_table_index := find_table_index( p_table_name );      
      v_index_index := find_index_index( v_table_index, p_constraint_name );
      
      v_orig_uniquekey := treat( v_return( v_table_index ).i_ind_uks( v_index_index ) as ot_orig_uniquekey);
      
      v_orig_uniquekey.i_uk_columns.extend(1);
      v_orig_uniquekey.i_uk_columns( v_orig_uniquekey.i_uk_columns.count ) := p_orig_columnref;
      
      v_return( v_table_index ).i_ind_uks( v_index_index ) := v_orig_uniquekey;
    end; 
    
    function find_foreignkey_index( p_table_index in number, p_constraint_name in varchar2 ) return number
    is
    begin
      for i in 1..v_return(p_table_index).i_foreign_keys.count
      loop
        if( v_return(p_table_index).i_foreign_keys(i).i_consname = p_constraint_name )
        then
          return i;
        end if;
      end loop;
      
      raise_application_error( -20000, 'foreign key not found: ' || p_table_index || ' ' || p_constraint_name );
    end;     
    
    procedure add_foreignkey_column( p_table_name in varchar2, p_constraint_name in varchar2, p_orig_columnref in ot_orig_columnref )
    is
      v_table_index number;
      v_foreignkey_index number;      
      v_orig_index ot_orig_index;
    begin
      v_table_index := find_table_index( p_table_name );      
      v_foreignkey_index := find_foreignkey_index( v_table_index, p_constraint_name );            
      
      v_return( v_table_index ).i_foreign_keys( v_foreignkey_index ).i_srccolumns.extend(1);
      v_return( v_table_index ).i_foreign_keys( v_foreignkey_index ).i_srccolumns( v_return( v_table_index ).i_foreign_keys( v_foreignkey_index ).i_srccolumns.count ) := p_orig_columnref;
    end;         
    
    procedure load_constraint_columns
    is
      v_orig_columnref ot_orig_columnref;
    begin            
      for cur_cons_columns in
        (
        select user_cons_columns.table_name, 
               column_name, 
               position, 
               user_cons_columns.constraint_name,
               constraint_type
          from user_cons_columns,
               user_constraints
         where user_cons_columns.constraint_name = user_constraints.constraint_name
           and user_cons_columns.table_name = user_constraints.table_name
         order by table_name,
                  constraint_name,
                  position
        )
      loop
        if( is_ignored_table( cur_cons_columns.table_name ) = 0 )
        then      
          v_orig_columnref := new ot_orig_columnref();
          
          v_orig_columnref.i_column_name := cur_cons_columns.column_name;     
          
          if(    cur_cons_columns.constraint_type = 'P' )
          then
            add_primarykey_column( cur_cons_columns.table_name, v_orig_columnref );                              
          elsif( cur_cons_columns.constraint_type = 'U' )                    
          then
            add_uniquekey_column( cur_cons_columns.table_name, cur_cons_columns.constraint_name, v_orig_columnref );          
          elsif( cur_cons_columns.constraint_type = 'R' )        
          then
            add_foreignkey_column( cur_cons_columns.table_name, cur_cons_columns.constraint_name, v_orig_columnref );                    
          end if;
        end if;
      end loop;    
    end;    
    
    procedure add_inlinecomment( p_table_name in varchar2, p_orig_inlinecomment in ot_orig_inlinecomment )
    is
      v_table_index number;
    begin
      v_table_index := find_table_index( p_table_name );
      
      v_return( v_table_index ).i_comments.extend(1);
      v_return( v_table_index ).i_comments( v_return( v_table_index ).i_comments.count ) := p_orig_inlinecomment;
    end;    
    
    procedure load_table_comments
    is
      v_orig_inlinecomment ot_orig_inlinecomment;
    begin            
      for cur_tab_comments in
        (
        select table_name,
               comments
          from user_tab_comments
         where comments is not null
         order by table_name
        )
      loop
        if( is_ignored_table( cur_tab_comments.table_name ) = 0 )
        then      
          v_orig_inlinecomment := new ot_orig_inlinecomment();
          
          v_orig_inlinecomment.i_comment := cur_tab_comments.comments;
          v_orig_inlinecomment.i_comment_object := ot_orig_commentobjecttype.c_table();
          
          add_inlinecomment( cur_tab_comments.table_name, v_orig_inlinecomment );
        end if;
      end loop;    
    end;        
    
    procedure load_column_comments
    is
      v_orig_inlinecomment ot_orig_inlinecomment;
    begin            
      for cur_col_comments in
        (
        select table_name,
               column_name,
               comments
          from user_col_comments
         where comments is not null          
         order by table_name,
                  column_name
        )
      loop
        if( is_ignored_table( cur_col_comments.table_name ) = 0 )
        then      
          v_orig_inlinecomment := new ot_orig_inlinecomment();
          
          v_orig_inlinecomment.i_comment := cur_col_comments.comments;
          v_orig_inlinecomment.i_column_name := cur_col_comments.column_name;
          v_orig_inlinecomment.i_comment_object := ot_orig_commentobjecttype.c_column();          
          
          add_inlinecomment( cur_col_comments.table_name, v_orig_inlinecomment );
        end if;
      end loop;    
    end;        
    
    procedure add_lobstorage( p_table_name in varchar2, p_orig_lobstorage in ot_orig_lobstorage )
    is
      v_table_index number;
    begin
      v_table_index := find_table_index( p_table_name );
      
      v_return( v_table_index ).i_lobstorages.extend(1);
      v_return( v_table_index ).i_lobstorages( v_return( v_table_index ).i_lobstorages.count ) := p_orig_lobstorage;
    end;            
    
    procedure load_lobstorage
    is
      v_orig_lobstorage ot_orig_lobstorage;
    begin            
      for cur_lobs in
        (
        select table_name,
               column_name,
               tablespace_name
          from user_lobs 
         order by table_name,
                  column_name
        )
      loop
        if( is_ignored_table( cur_lobs.table_name ) = 0 )
        then      
          v_orig_lobstorage  := new ot_orig_lobstorage();
          
          v_orig_lobstorage.i_column_name := cur_lobs.column_name;
          v_orig_lobstorage.i_tablespace  := cur_lobs.tablespace_name;
          
          add_lobstorage( cur_lobs.table_name, v_orig_lobstorage );
        end if;
      end loop;    
    end;   
    
    procedure set_mviewlog( p_table_name in varchar2, p_orig_mviewlog in ot_orig_mviewlog )
    is
      v_table_index number;
    begin
      v_table_index := find_table_index( p_table_name );
      
      v_return( v_table_index ).i_mviewlog := p_orig_mviewlog;
    end;    
    
    procedure add_mviewlog_column( p_table_name in varchar2, p_orig_columnref in ot_orig_columnref )
    is
      v_table_index number;     
      v_orig_mviewlog ot_orig_mviewlog;
    begin
      v_table_index := find_table_index( p_table_name );  
    
      v_orig_mviewlog := v_return( v_table_index ).i_mviewlog;
      
      if ( v_orig_mviewlog.i_columns is null) 
      then
        v_orig_mviewlog.i_columns := new ct_orig_columnref_list();
      end if;
      
      v_orig_mviewlog.i_columns.extend(1);
      v_orig_mviewlog.i_columns( v_orig_mviewlog.i_columns.count ) := p_orig_columnref;
      
      v_return( v_table_index ).i_mviewlog := v_orig_mviewlog;
    end;        
    
    procedure load_mviewlog_columns
    is
      v_orig_columnref ot_orig_columnref;
    begin            
      for cur_mviewlog_columns in
        (
        select master, column_name, column_id from(

          select logs.master,
                         tab_columns.column_name,
                         tab_columns.column_id
                    from user_mview_logs logs join
                         user_tab_columns tab_columns            
                      on logs.log_table = tab_columns.table_name 
                     and tab_columns.column_name not like '%$$'           
          minus            
          select logs.master,
                         tab_columns.column_name,
                         tab_columns.column_id
                    from user_mview_logs logs 
                    join user_tab_columns tab_columns 
                      on logs.log_table = tab_columns.table_name 
                     and tab_columns.column_name not like '%$$' 
                    join user_constraints cons 
                      on logs.master = cons.table_name
                     and cons.constraint_type = 'P'
                    join user_cons_columns cons_columns
                      on cons.constraint_name = cons_columns.constraint_name
                     and cons_columns.column_name = tab_columns.column_name
          )           
        order by master, column_id            
        )
      loop
        if( is_ignored_table(  cur_mviewlog_columns.master ) = 0 )
        then      
          v_orig_columnref := new ot_orig_columnref();         
          v_orig_columnref.i_column_name := cur_mviewlog_columns.column_name;                
          
          add_mviewlog_column( cur_mviewlog_columns.master, v_orig_columnref );
        end if;
      end loop;    
    end;
    
    procedure load_mviewlogs
    is
      v_orig_mviewlog ot_orig_mviewlog;
    begin            
      for cur_mviewlogs in
        (
        select master,
               log_table,
               rowids,
               primary_key,
               sequence,
               include_new_values,
               purge_asynchronous,
               purge_deferred,
               to_date(purge_start,'dd.mm.yy') purge_start,
               case when instr(purge_interval, 'sysdate') > 0 then substr(purge_interval, 11) end purge_interval,
               case when purge_interval is not null and instr(purge_interval, 'to_date') > 0 
               then to_date(substr(purge_interval, instr(purge_interval, '''',1,1)+1, instr(purge_interval, '''',1,2)-instr(purge_interval, '''',1,1)-1),substr(purge_interval, instr(purge_interval, '''',1,3)+1, instr(purge_interval, '''',1,4)-instr(purge_interval, '''',1,3)-1))
               end purge_next,
               commit_scn_based,
               tablespace_name, 
               trim(degree) degree
          from user_mview_logs logs
          join user_tables tabs
            on logs.log_table = tabs.table_name
         order by master
        )
      loop
        if( is_ignored_table( cur_mviewlogs.master ) = 0 )
        then      
          v_orig_mviewlog  := new ot_orig_mviewlog();
          
          if (cur_mviewlogs.primary_key = 'YES')
          then
            v_orig_mviewlog.i_primarykey := 'primary';
          end if;  
          
          if (cur_mviewlogs.rowids = 'YES')
          then
            v_orig_mviewlog.i_rowid := 'rowid';
          end if;  
          
          if (cur_mviewlogs.sequence = 'YES')
          then
            v_orig_mviewlog.i_withsequence := 'sequence';
          end if;  
          
          if (cur_mviewlogs.commit_scn_based = 'YES')
          then
            v_orig_mviewlog.i_commitscn := 'commit_scn';
          end if;
          
          v_orig_mviewlog.i_purge := 'purge';
          if (cur_mviewlogs.purge_deferred = 'YES')
          then
            v_orig_mviewlog.i_startwith := cur_mviewlogs.purge_start;
            v_orig_mviewlog.i_repeatInterval := cur_mviewlogs.purge_interval;
            v_orig_mviewlog.i_next := cur_mviewlogs.purge_next;
          else 
            if ( cur_mviewlogs.purge_asynchronous = 'YES')
              then
                v_orig_mviewlog.i_synchronous := ot_orig_synchronoustype.c_asynchronous;
              else 
                v_orig_mviewlog.i_synchronous := ot_orig_synchronoustype.c_synchronous;
            end if; 
          end if; 
          
          if (cur_mviewlogs.include_new_values = 'YES')
          then
            v_orig_mviewlog.i_newvalues := ot_orig_newvaluestype.c_including;
          else
            v_orig_mviewlog.i_newvalues := ot_orig_newvaluestype.c_excluding;
          end if;  
          
          v_orig_mviewlog.i_tablespace := cur_mviewlogs.tablespace_name;
        
          if( cur_mviewlogs.degree = '1' )        
          then
            v_orig_mviewlog.i_parallel := ot_orig_paralleltype.c_noparallel;        
          else
            v_orig_mviewlog.i_parallel := ot_orig_paralleltype.c_parallel();
            if ( cur_mviewlogs.degree != 'DEFAULT' ) 
            then
              v_orig_mviewlog.i_parallel_degree := to_number(cur_mviewlogs.degree);
            end if;
          end if;   
          
          set_mviewlog( cur_mviewlogs.master, v_orig_mviewlog );
        end if;
      end loop;    
    end;        
    
    procedure update_foreignkey_destdata
    is
      v_dest_table_index number;      
      v_dest_uniquekey_index number;
      v_ref_constraint_name varchar2(100);
    begin
      for v_table_index in 1..v_return.count
      loop
        for v_foreignkey_index in 1..v_return(v_table_index).i_foreign_keys.count
        loop
          v_ref_constraint_name := v_return(v_table_index).i_foreign_keys(v_foreignkey_index).i_desttable;
          
          if( v_primarykey_to_table_name_map.exists(v_ref_constraint_name) )
          then
            v_dest_table_index := find_table_index( v_primarykey_to_table_name_map( v_ref_constraint_name ) );            

            v_return(v_table_index).i_foreign_keys(v_foreignkey_index).i_destcolumns := v_return(v_dest_table_index).i_primary_key.i_pk_columns;
          else
            if( not v_unique_key_to_table_name_map.exists(v_ref_constraint_name) )
            then
              raise_application_error( -20000, 'fk referenzierter uk nicht gefunden: ' || v_return(v_table_index).i_name || ' ' || v_return(v_table_index).i_foreign_keys(v_foreignkey_index).i_consname || ' ' || v_ref_constraint_name );
            end if;
            v_dest_table_index := find_table_index( v_unique_key_to_table_name_map( v_ref_constraint_name ) );
            v_dest_uniquekey_index := find_index_index( v_dest_table_index, v_ref_constraint_name );
            
            v_return(v_table_index).i_foreign_keys(v_foreignkey_index).i_destcolumns := treat( v_return(v_dest_table_index).i_ind_uks(v_dest_uniquekey_index) as ot_orig_uniquekey ).i_uk_columns;
          end if;
          
          v_return(v_table_index).i_foreign_keys(v_foreignkey_index).i_desttable := v_return(v_dest_table_index).i_name;          
        end loop;      
      end loop;
    end; 
    
    procedure remove_generated_uk_names
    is
    begin
      for v_table_index in 1..v_return.count
      loop
        for v_ind_uks_index in 1..v_return(v_table_index).i_ind_uks.count
        loop
          if( v_generated_name_map.exists(v_return(v_table_index).i_ind_uks(v_ind_uks_index).i_consname) )
          then
            v_return(v_table_index).i_ind_uks(v_ind_uks_index).i_consname := null;
          end if;
        end loop;      
      end loop;
    end;  
    
    procedure set_compression( p_table_name in varchar2, p_orig_compression ot_orig_compresstype, p_orig_compressionfor ot_orig_compressfortype )
    is
      v_table_index number;
    begin
      v_table_index := find_table_index( p_table_name );
      
      v_return( v_table_index ).i_compression := p_orig_compression;
      v_return( v_table_index ).i_compressionfor := p_orig_compressionfor;
    end;       
    
    procedure set_partitioning( p_table_name in varchar2, p_orig_tablepartitioning ot_orig_tablepartitioning )
    is
      v_table_index number;
    begin
      v_table_index := find_table_index( p_table_name );
      
      v_return( v_table_index ).i_tablepartitioning := p_orig_tablepartitioning;
    end;   
    
    /**
     * Partitionierung wird mit einzelselects geladen, da die Struktur sehr uneinheitlich ist und es erwatungsgemaess nur wenige Daten zu lesen gibt.
     */
    procedure load_partitioning
    is
      v_orig_hashpartitions ot_orig_hashpartitions;
      v_orig_hashpartition ot_orig_hashpartition;
      
      v_orig_listpartitions ot_orig_listpartitions;
      v_orig_listpartition ot_orig_listpartition;
      v_orig_listpartitionvalu ot_orig_listpartitionvalu;      
      v_orig_listsubpart ot_orig_listsubpart;
      
      v_orig_rangepartitions ot_orig_rangepartitions;
      v_orig_rangepartition ot_orig_rangepartition;
      v_orig_rangepartitionval ot_orig_rangepartitionval;
      v_orig_rangesubpart ot_orig_rangesubpart;
      
      v_orig_compression ot_orig_compresstype;
      v_orig_compressionfor ot_orig_compressfortype;
      
      v_high_value varchar2(32000);
      v_exit_loop number;
      
      function load_tablesubpart( p_table_name in varchar2, p_subpartitioning_type in varchar2 ) return ot_orig_tablesubpart
      is
        v_orig_hashsubparts ot_orig_hashsubparts;
        v_orig_listsubparts ot_orig_listsubparts;
        v_orig_rangesubparts ot_orig_rangesubparts;
        
        v_orig_columnref ot_orig_columnref;
      begin
        if    ( p_subpartitioning_type = 'NONE' )
        then
          return null;
        elsif ( p_subpartitioning_type = 'HASH' )
        then
          v_orig_hashsubparts := new ot_orig_hashsubparts();
          
          v_orig_hashsubparts.i_column := new ot_orig_columnref();
          
          for cur_part_col in
            (
            select column_name
              from user_subpart_key_columns
             where name = p_table_name
               and object_type = 'TABLE'
            )
          loop
            v_orig_hashsubparts.i_column.i_column_name := cur_part_col.column_name;
          end loop;
          
          return v_orig_hashsubparts;
        elsif ( p_subpartitioning_type = 'LIST' )
        then
          v_orig_listsubparts := new ot_orig_listsubparts();
          
          v_orig_listsubparts.i_column := new ot_orig_columnref();
          
          for cur_part_col in
            (
            select column_name
              from user_subpart_key_columns
             where name = p_table_name
               and object_type = 'TABLE'
            )
          loop
            v_orig_listsubparts.i_column.i_column_name := cur_part_col.column_name;
          end loop;
          
          return v_orig_listsubparts;
        elsif ( p_subpartitioning_type = 'RANGE' )
        then
          v_orig_rangesubparts := new ot_orig_rangesubparts();
          
          v_orig_rangesubparts.i_columns := new ct_orig_columnref_list();
          
          for cur_part_col in
            (
            select column_name
              from user_subpart_key_columns
             where name = p_table_name
               and object_type = 'TABLE'
               order by column_position
            )
          loop
            v_orig_columnref := new ot_orig_columnref(cur_part_col.column_name);
            v_orig_rangesubparts.i_columns.extend;
            v_orig_rangesubparts.i_columns(v_orig_rangesubparts.i_columns.count) := v_orig_columnref;
          end loop;
          
          return v_orig_rangesubparts;  
        else
          --            raise_application_error( -20000, 'partitionstyp unbekannt: ' || cur_part_tables.partitioning_type || ' ' || cur_part_tables.subpartitioning_type );            
          return null;                
        end if;        
      end;
      
      function get_orig_listpart_valuelist(v_in_string varchar2)
        return ct_orig_listpartitionvalu_list is
        v_listpartitionvalu_list ct_orig_listpartitionvalu_list := new ct_orig_listpartitionvalu_list();
        v_listpartitionvalu ot_orig_listpartitionvalu := new ot_orig_listpartitionvalu();
        v_value_list varchar2(2000) := v_in_string;
        v_comma number;
        v_weiter boolean := true;
    
      begin
    
        while v_weiter loop
    
          if instr(v_value_list, ',') > 0 then
            v_comma := instr(v_value_list, ',');
            v_listpartitionvalu.i_value := replace(substr(v_value_list, 1, v_comma - 1),'DEFAULT','default');
            v_value_list := substr(v_value_list, v_comma + 1);
          else
            v_listpartitionvalu.i_value := v_value_list;
            v_weiter := false;
          end if;
    
          v_listpartitionvalu_list.extend;
          v_listpartitionvalu_list(v_listpartitionvalu_list.count) := v_listpartitionvalu;
    
        end loop;
    
        return v_listpartitionvalu_list;
      end;  
      
      function get_orig_rangepart_valuelist(v_in_string varchar2)
        return ct_orig_rangepartitionval_list is
        v_rangepartitionval_list ct_orig_rangepartitionval_list := new ct_orig_rangepartitionval_list();
        v_rangepartitionval ot_orig_rangepartitionval := new ot_orig_rangepartitionval();
        v_value_list varchar2(2000) := v_in_string;
        v_comma number;
        v_weiter boolean := true;
    
      begin
    
        while v_weiter loop
    
          if instr(v_value_list, ',') > 0 then
            v_comma := instr(v_value_list, ',');
            v_rangepartitionval.i_value := replace(substr(v_value_list, 1, v_comma - 1),'MAXVALUE','maxvalue');
            v_value_list := substr(v_value_list, v_comma + 1);
          else
            v_rangepartitionval.i_value := v_value_list;
            v_weiter := false;
          end if;
    
          v_rangepartitionval_list.extend;
          v_rangepartitionval_list(v_rangepartitionval_list.count) := v_rangepartitionval;
    
        end loop;
    
        return v_rangepartitionval_list;
      end;        
      
      function load_subpartlist( p_table_name in varchar2, p_partition_name in varchar2, p_subpartitioning_type in varchar2 ) return ct_orig_subsubpart_list
      is
        v_return ct_orig_subsubpart_list;
        
        v_orig_hashsubsubpart ot_orig_hashsubsubpart;
        v_orig_listsubsubpart ot_orig_listsubsubpart;
        v_orig_rangesubsubpart ot_orig_rangesubsubpart;
        
        v_orig_listpartitionvalu_list ct_orig_listpartitionvalu_list;
        v_orig_listpartitionvalu ot_orig_listpartitionvalu;
        v_orig_rangepartitionval_list ct_orig_rangepartitionval_list;
        v_orig_rangepartitionval ot_orig_rangepartitionval;
      begin
        v_return := new ct_orig_subsubpart_list();
        
        for cur_tab_subpartitions in        
          (
          select subpartition_name, 
                 high_value, 
                 tablespace_name
            from user_tab_subpartitions
           where table_name = p_table_name
             and partition_name = p_partition_name
           order by subpartition_position
          )
        loop
          v_return.extend;
      
          if    ( p_subpartitioning_type = 'HASH' )
          then
            v_orig_hashsubsubpart := new ot_orig_hashsubsubpart();
            
            v_orig_hashsubsubpart.i_name := cur_tab_subpartitions.subpartition_name;
            v_orig_hashsubsubpart.i_tablespace := cur_tab_subpartitions.tablespace_name;
            
            v_return(v_return.count) := v_orig_hashsubsubpart;
          elsif    ( p_subpartitioning_type = 'LIST' )
          then
            v_orig_listsubsubpart := new ot_orig_listsubsubpart();
            v_orig_listpartitionvalu_list := get_orig_listpart_valuelist(cur_tab_subpartitions.high_value);
              
            v_orig_listsubsubpart.i_value := v_orig_listpartitionvalu_list;
            v_orig_listsubsubpart.i_name := cur_tab_subpartitions.subpartition_name;
            v_orig_listsubsubpart.i_tablespace := cur_tab_subpartitions.tablespace_name;
              
            v_return(v_return.count) := v_orig_listsubsubpart;  
          elsif    ( p_subpartitioning_type = 'RANGE' )
          then
            v_orig_rangesubsubpart := new ot_orig_rangesubsubpart();
            v_orig_rangepartitionval_list := get_orig_rangepart_valuelist(cur_tab_subpartitions.high_value);
              
            v_orig_rangesubsubpart.i_value := v_orig_rangepartitionval_list;
            v_orig_rangesubsubpart.i_name := cur_tab_subpartitions.subpartition_name;
            v_orig_rangesubsubpart.i_tablespace := cur_tab_subpartitions.tablespace_name;
              
            v_return(v_return.count) := v_orig_rangesubsubpart;    
          else
            --            raise_application_error( -20000, 'subpartitionstyp unbekannt: ' || cur_part_tables.subpartitioning_type );            
            return null;                
          end if;        
        end loop;
        
        return v_return;
      end;      
    begin            
      for cur_part_tables in
        (
        select table_name, 
               partitioning_type, 
               subpartitioning_type, 
               interval,
               def_tablespace_name,
               def_compression,
               def_compress_for
          from user_part_tables
        )
      loop
        if( is_ignored_table( cur_part_tables.table_name ) = 0 )
        then
          -- Read compression type, works only for one compression type for all partitions
          v_orig_compression := null;
          v_orig_compressionfor := null;
          if ( upper(NVL(cur_part_tables.def_compression,'NULL')) = 'ENABLED' )  
          then
            v_orig_compression := ot_orig_compresstype.c_compress;  
            if ( upper(NVL(cur_part_tables.def_compress_for,'NULL')) like '%OLTP%' ) 
            then
              v_orig_compressionfor := ot_orig_compressfortype.c_all;
            elsif ( upper(NVL(cur_part_tables.def_compress_for,'NULL')) = 'BASIC' ) 
            then
              v_orig_compressionfor := ot_orig_compressfortype.c_direct_load();  
            elsif ( upper(NVL(cur_part_tables.def_compress_for,'NULL')) = 'QUERY LOW' ) 
            then
              v_orig_compressionfor := ot_orig_compressfortype.c_query_low();  
            elsif ( upper(NVL(cur_part_tables.def_compress_for,'NULL')) = 'QUERY HIGH' ) 
            then
              v_orig_compressionfor := ot_orig_compressfortype.c_query_high();    
            elsif ( upper(NVL(cur_part_tables.def_compress_for,'NULL')) = 'ARCHIVE LOW' ) 
            then
              v_orig_compressionfor := ot_orig_compressfortype.c_archive_low();  
            elsif ( upper(NVL(cur_part_tables.def_compress_for,'NULL')) = 'ARCHIVE HIGH' ) 
            then
              v_orig_compressionfor := ot_orig_compressfortype.c_archive_high();      
            end if;
          end if;  
          if (v_orig_compression is not null) 
          then
            set_compression( cur_part_tables.table_name, v_orig_compression, v_orig_compressionfor );
          end if;  
             
          if    ( cur_part_tables.partitioning_type = 'HASH' and cur_part_tables.subpartitioning_type = 'NONE' )
          then
            v_orig_hashpartitions := ot_orig_hashpartitions();
            
            v_orig_hashpartitions.i_column := new ot_orig_columnref();
            for cur_part_col in
              (
              select column_name
                from user_part_key_columns
               where name = cur_part_tables.table_name
                 and object_type = 'TABLE'
              )
            loop
              v_orig_hashpartitions.i_column.i_column_name := cur_part_col.column_name;
            end loop;
            
            v_orig_hashpartitions.i_partitionlist := new ct_orig_hashpartition_list();
            for cur_tab_partitions in
              (
              select partition_name,
                     tablespace_name
                from user_tab_partitions
               where table_name = cur_part_tables.table_name
               order by partition_position
              )
            loop
              v_orig_hashpartition := new ot_orig_hashpartition();
              
              v_orig_hashpartition.i_name := cur_tab_partitions.partition_name;
              v_orig_hashpartition.i_tablespace := cur_tab_partitions.tablespace_name;
              
              v_orig_hashpartitions.i_partitionlist.extend;
              v_orig_hashpartitions.i_partitionlist(v_orig_hashpartitions.i_partitionlist.count) := v_orig_hashpartition;
            end loop;            
          
            set_partitioning( cur_part_tables.table_name, v_orig_hashpartitions );
          elsif ( cur_part_tables.partitioning_type = 'LIST' )
          then
            v_orig_listpartitions := ot_orig_listpartitions();
            
            v_orig_listpartitions.i_tablesubpart := load_tablesubpart( cur_part_tables.table_name, cur_part_tables.subpartitioning_type );
            v_orig_listpartitions.i_column := new ot_orig_columnref();
            for cur_part_col in
              (
              select column_name
                from user_part_key_columns
               where name = cur_part_tables.table_name
                 and object_type = 'TABLE'
              )
            loop
              v_orig_listpartitions.i_column.i_column_name := cur_part_col.column_name;
            end loop;
            
            v_orig_listpartitions.i_partitionlist := new ct_orig_listpartition_list();
            for cur_tab_partitions in
              (
              select partition_name,
                     tablespace_name,
                     high_value
                from user_tab_partitions
               where table_name = cur_part_tables.table_name
               order by partition_position               
              )
            loop
              v_high_value := cur_tab_partitions.high_value;
              v_orig_listpartition := new ot_orig_listpartition();
              
              v_orig_listpartition.i_name := cur_tab_partitions.partition_name;
              v_orig_listpartition.i_tablespace := cur_tab_partitions.tablespace_name;
              
              v_orig_listpartition.i_value := new ct_orig_listpartitionvalu_list();
              
              if( upper(v_high_value )= 'DEFAULT' )
              then
                v_orig_listpartitionvalu := new ot_orig_listpartitionvalu();
                v_orig_listpartitionvalu.i_default := 'default';
                
                v_orig_listpartition.i_value.extend();
                v_orig_listpartition.i_value( v_orig_listpartition.i_value.count ) := v_orig_listpartitionvalu;
              else
                v_exit_loop := 0;
                loop
                  v_orig_listpartitionvalu := new ot_orig_listpartitionvalu();                
                
                  if( instr( v_high_value, ',' ) = 0 )
                  then
                    v_orig_listpartitionvalu.i_value := trim( v_high_value );
                    v_exit_loop := 1;
                  else                    
                    v_orig_listpartitionvalu.i_value := trim( substr( v_high_value, 1, instr( v_high_value, ',' ) - 1 ) );                  
                    v_high_value := substr( v_high_value, instr( v_high_value, ',' ) + 1 );
                  end if;
                  
                  v_orig_listpartition.i_value.extend();
                  v_orig_listpartition.i_value( v_orig_listpartition.i_value.count ) := v_orig_listpartitionvalu;                                  
                  
                  if( v_exit_loop = 1 )
                  then
                    exit;
                  end if;
                end loop;
              end if;
              
              v_orig_listpartitions.i_partitionlist.extend;
              v_orig_listpartitions.i_partitionlist(v_orig_listpartitions.i_partitionlist.count) := v_orig_listpartition;
            end loop;            
            
            if( v_orig_listpartitions.i_tablesubpart is not null )
            then
              v_orig_listpartitions.i_subpartitionlist := new ct_orig_listsubpart_list();
              
              for i in 1..v_orig_listpartitions.i_partitionlist.count
              loop
                v_orig_listpartition := v_orig_listpartitions.i_partitionlist(i);
                
                v_orig_listsubpart := new ot_orig_listsubpart();
                
                v_orig_listsubpart.i_name := v_orig_listpartition.i_name;
                v_orig_listsubpart.i_value := v_orig_listpartition.i_value;
                v_orig_listsubpart.i_subpartlist := load_subpartlist( cur_part_tables.table_name, v_orig_listsubpart.i_name, cur_part_tables.subpartitioning_type );
                
                v_orig_listpartitions.i_subpartitionlist.extend;
                v_orig_listpartitions.i_subpartitionlist(v_orig_listpartitions.i_subpartitionlist.count) := v_orig_listsubpart;
              end loop;
              
              v_orig_listpartitions.i_partitionlist := null;
            end if;
          
            set_partitioning( cur_part_tables.table_name, v_orig_listpartitions );
          elsif ( cur_part_tables.partitioning_type = 'RANGE' )
          then
            v_orig_rangepartitions := ot_orig_rangepartitions();
            
            v_orig_rangepartitions.i_intervalexpression := cur_part_tables.interval;
            
            v_orig_rangepartitions.i_tablesubpart := load_tablesubpart( cur_part_tables.table_name, cur_part_tables.subpartitioning_type );
            v_orig_rangepartitions.i_columns := new ct_orig_columnref_list();
            for cur_part_col in
              (
              select column_name
                from user_part_key_columns
               where name = cur_part_tables.table_name
                 and object_type = 'TABLE'
               order by column_position
              )
            loop
              v_orig_rangepartitions.i_columns.extend;
              v_orig_rangepartitions.i_columns( v_orig_rangepartitions.i_columns.count ) := new ot_orig_columnref();
              v_orig_rangepartitions.i_columns( v_orig_rangepartitions.i_columns.count ).i_column_name := cur_part_col.column_name;              
            end loop;
            
            v_orig_rangepartitions.i_partitionlist := new ct_orig_rangepartition_list();
            for cur_tab_partitions in
              (
              select partition_name,
                     tablespace_name,
                     high_value
                from user_tab_partitions
               where table_name = cur_part_tables.table_name
               order by partition_position               
              )
            loop
              v_high_value := cur_tab_partitions.high_value;
              v_orig_rangepartition := new ot_orig_rangepartition();
              
              v_orig_rangepartition.i_name := cur_tab_partitions.partition_name;
              v_orig_rangepartition.i_tablespace := cur_tab_partitions.tablespace_name;
              
              v_orig_rangepartition.i_value := new ct_orig_rangepartitionval_list();
              
              v_exit_loop := 0;
              loop
                v_orig_rangepartitionval := new ot_orig_rangepartitionval();                
              
                if( instr( v_high_value, ',' ) = 0 )
                then
                  v_orig_rangepartitionval.i_value := trim( v_high_value );
                  v_exit_loop := 1;
                else                    
                  v_orig_rangepartitionval.i_value := trim( substr( v_high_value, 1, instr( v_high_value, ',' ) - 1 ) );                 
                  v_high_value := substr( v_high_value, instr( v_high_value, ',' ) + 1 );
                end if;
                
                if( upper(v_orig_rangepartitionval.i_value) = 'MAXVALUE' )
                then
                  v_orig_rangepartitionval.i_maxvalue := 'maxvalue';
                  v_orig_rangepartitionval.i_value := null;
                end if;                
                
                v_orig_rangepartition.i_value.extend();
                v_orig_rangepartition.i_value( v_orig_rangepartition.i_value.count ) := v_orig_rangepartitionval;                                  
                
                if( v_exit_loop = 1 )
                then
                  exit;
                end if;
              end loop;
              
              v_orig_rangepartitions.i_partitionlist.extend;
              v_orig_rangepartitions.i_partitionlist(v_orig_rangepartitions.i_partitionlist.count) := v_orig_rangepartition;
            end loop;      
            
            -- SUBPARTIIONING
            if( v_orig_rangepartitions.i_tablesubpart is not null )
            then
              v_orig_rangepartitions.i_subpartitionlist := new ct_orig_rangesubpart_list();
              
              for i in 1..v_orig_rangepartitions.i_partitionlist.count
              loop
                v_orig_rangepartition := v_orig_rangepartitions.i_partitionlist(i);
                
                v_orig_rangesubpart := new ot_orig_rangesubpart();
                
                v_orig_rangesubpart.i_name := v_orig_rangepartition.i_name;
                v_orig_rangesubpart.i_value := v_orig_rangepartition.i_value;
                v_orig_rangesubpart.i_subpartlist := load_subpartlist( cur_part_tables.table_name, v_orig_rangesubpart.i_name, cur_part_tables.subpartitioning_type );
                
                v_orig_rangepartitions.i_subpartitionlist.extend;
                v_orig_rangepartitions.i_subpartitionlist(v_orig_rangepartitions.i_subpartitionlist.count) := v_orig_rangesubpart;
              end loop;
              
              v_orig_rangepartitions.i_partitionlist := null;
            end if;
          
            set_partitioning( cur_part_tables.table_name, v_orig_rangepartitions );            
          else
--            raise_application_error( -20000, 'partitionstyp unbekannt: ' || cur_part_tables.partitioning_type || ' ' || cur_part_tables.subpartitioning_type );            
            null;
          end if;
        end if;
      end loop;    
    end;    
    
  begin
    for cur_tables in
      (
      select tables.table_name,
             tables.tablespace_name, 
             tables.temporary,
             tables.duration,
             tables.logging,
             trim(degree) degree,
             trim(compression) compression,  
             trim(compress_for) compress_for,
             parts.def_tablespace_name as part_tabspace
        from user_tables tables left outer join user_part_tables parts
        on tables.table_name = parts.table_name 
       order by table_name
      )
    loop
      if( is_ignored_table( cur_tables.table_name ) = 0 )
      then
        v_orig_table := ot_orig_table();
        
        v_orig_table.i_name         := cur_tables.table_name;
        v_orig_table.i_tablespace   := nvl(cur_tables.tablespace_name, cur_tables.part_tabspace);
        v_orig_table.i_columns      := ct_orig_column_list();
        v_orig_table.i_ind_uks      := ct_orig_indexoruniquekey_list();
        v_orig_table.i_comments     := ct_orig_inlinecomment_list();
        v_orig_table.i_lobstorages  := ct_orig_lobstorage_list();
        v_orig_table.i_constraints  := ct_orig_constraint_list();
        v_orig_table.i_foreign_keys := ct_orig_foreignkey_list();
        
        if cur_tables.logging = 'YES'
        then v_orig_table.i_logging := ot_orig_loggingtype.c_logging;     
        else v_orig_table.i_logging := ot_orig_loggingtype.c_nologging;   
        end if;
        
        if( cur_tables.degree = '1' )        
        then
          v_orig_table.i_parallel := ot_orig_paralleltype.c_noparallel;        
        else
          v_orig_table.i_parallel := ot_orig_paralleltype.c_parallel();
          if ( cur_tables.degree != 'DEFAULT' ) 
          then
            v_orig_table.i_parallel_degree := to_number(cur_tables.degree);
          end if;
        end if;   
        
        if( cur_tables.compression is not null )     
        then
          if ( upper(cur_tables.compression) = 'ENABLED' )  
          then
            v_orig_table.i_compression := ot_orig_compresstype.c_compress;  
            if ( upper(NVL(cur_tables.compress_for,'NULL')) like '%OLTP%' ) 
            then
              v_orig_table.i_compressionfor := ot_orig_compressfortype.c_all();
            elsif ( upper(NVL(cur_tables.compress_for,'NULL')) = 'BASIC' ) 
            then
              v_orig_table.i_compressionfor := ot_orig_compressfortype.c_direct_load();  
            elsif ( upper(NVL(cur_tables.compress_for,'NULL')) = 'QUERY LOW' ) 
            then
              v_orig_table.i_compressionfor := ot_orig_compressfortype.c_query_low();  
            elsif ( upper(NVL(cur_tables.compress_for,'NULL')) = 'QUERY HIGH' ) 
            then
              v_orig_table.i_compressionfor := ot_orig_compressfortype.c_query_high();   
            elsif ( upper(NVL(cur_tables.compress_for,'NULL')) = 'ARCHIVE LOW' ) 
            then
              v_orig_table.i_compressionfor := ot_orig_compressfortype.c_archive_low();  
            elsif ( upper(NVL(cur_tables.compress_for,'NULL')) = 'ARCHIVE HIGH' ) 
            then
              v_orig_table.i_compressionfor := ot_orig_compressfortype.c_archive_high();  
            end if;
          elsif ( upper(cur_tables.compression) = 'DISABLED' )
          then
            v_orig_table.i_compression := ot_orig_compresstype.c_nocompress;
          end if;  
        end if;               
        
        if( cur_tables.temporary = 'Y' )
        then
          v_orig_table.i_permanentness := ot_orig_permanentnesstype.c_global_temporary();
          
          if( instr( cur_tables.duration, 'SESSION' ) = 0 )
          then
            v_orig_table.i_transactioncontrol := ot_orig_permanentnesstran.c_on_commit_delete();          
          else
            v_orig_table.i_transactioncontrol := ot_orig_permanentnesstran.c_on_commit_preserve();
          end if;          
        else
          v_orig_table.i_permanentness := ot_orig_permanentnesstype.c_permanent();
        end if;
        
        v_return.extend(1);
        v_return(v_return.count) := v_orig_table;
      end if;
    end loop;
    
    load_columns();    
    load_indexes();    
    load_index_columns();    
    load_index_expressions();
    load_table_constraints();    
    load_constraint_columns();    
    load_table_comments();    
    load_column_comments();
    update_foreignkey_destdata();
    remove_generated_uk_names();
    load_partitioning();
    load_mviewlogs();
    load_mviewlog_columns();
    
    return v_return;
  end;  

  function get_ist return ot_orig_model is
    v_orig_model ot_orig_model := new ot_orig_model();
    v_orig_modelelement_list ct_orig_modelelement_list; 
    v_orig_table_list ct_orig_table_list;
    v_orig_mview_list ct_orig_mview_list;
    
    procedure add_to_return is
    begin
      for i in 1..v_orig_modelelement_list.count
      loop
        v_orig_model.i_model_elements.extend(1);
        v_orig_model.i_model_elements(v_orig_model.i_model_elements.count) := v_orig_modelelement_list(i);
      end loop;
    end;    
  begin
    load_ignore_cache( pa_orcas_run_parameter.get_excludewheresequence(), 'SEQUENCE'  );
    load_ignore_cache( pa_orcas_run_parameter.get_excludewheretable(), 'TABLE'  );
    load_ignore_cache( pa_orcas_run_parameter.get_excludewheremview(), 'MATERIALIZED VIEW'  );
  
    v_orig_model.i_model_elements := new ct_orig_modelelement_list();
  
    v_orig_modelelement_list := load_all_sequences();
    add_to_return();
    
    v_orig_table_list := load_all_tables();
    for i in 1..v_orig_table_list.count
    loop
      v_orig_model.i_model_elements.extend(1);
      v_orig_model.i_model_elements(v_orig_model.i_model_elements.count) := v_orig_table_list(i);
    end loop;
    
    v_orig_mview_list := load_all_mviews();
    for i in 1..v_orig_mview_list.count
    loop
      v_orig_model.i_model_elements.extend(1);
      v_orig_model.i_model_elements(v_orig_model.i_model_elements.count) := v_orig_mview_list(i);
    end loop;

    return v_orig_model;
  end;
end;
/
