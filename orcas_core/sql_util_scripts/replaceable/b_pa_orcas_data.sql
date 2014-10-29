create or replace package body pa_orcas_data is

pv_data_metadata_list ct_data_metadata_list := new ct_data_metadata_list();
type t_table_name_to_row_list_map is table of ct_data_rdl index by varchar2(100);
pv_table_name_to_row_list_map t_table_name_to_row_list_map;

/**
 * Gibt die PK-Splate zu der Tabelle zurueck. Es muss genau eine geben.
 */
function get_single_pk_column( p_data_fk_metadata in ot_data_fk_metadata ) return varchar2
is
  v_return varchar2(100);
begin
  if( p_data_fk_metadata.i_pk_column_name is not null )
  then
    return p_data_fk_metadata.i_pk_column_name;
  end if;

  select column_name 
    into v_return
    from user_constraints,
         user_cons_columns
   where user_constraints.table_name = p_data_fk_metadata.i_table_name
     and constraint_type = 'P'
     and user_cons_columns.constraint_name = user_constraints.constraint_name;   
     
  return v_return;     
end;

/**
 * Erstellt ein Statement, welches die Rohdaten liefert.
 */
function create_data_select_statement( p_metadata in ot_data_metadata ) return varchar2;
function create_data_select_statement( p_metadata in ot_data_metadata ) return varchar2
is
  v_statement varchar2(32000);
  v_index number;

  procedure add_columns( p_scs_col_metadata_list in ct_data_col_metadata_list )
  is
    v_scs_col_metadata ot_data_col_metadata;
    v_scs_col_metadata_fk ot_data_col_metadata;    
    v_fk_pk_column_name varchar2(100);    
    v_scs_decode_metadata ot_data_decode_metadata;
  begin
    for i in 1..p_scs_col_metadata_list.count
    loop
      v_scs_col_metadata := treat( p_scs_col_metadata_list(i) as ot_data_col_metadata );
    
      if( v_index != 1 )
      then
        v_statement := v_statement || ' ,';
      end if;
      
      v_statement := v_statement || ' nvl( (select max(decode( rownum, ' || v_index || ', ';
      
      if( v_scs_col_metadata.i_data_fk_metadata is not null )
      then
        v_fk_pk_column_name := get_single_pk_column( v_scs_col_metadata.i_data_fk_metadata );
        
        v_statement := v_statement || ' ( select ' || v_fk_pk_column_name || ' from ' || v_scs_col_metadata.i_data_fk_metadata.i_table_name || ' where';
        for j in 1 .. v_scs_col_metadata.i_data_fk_metadata.i_data_col_metadata_list.count
        loop
          v_scs_col_metadata_fk := treat( v_scs_col_metadata.i_data_fk_metadata.i_data_col_metadata_list(j) as ot_data_col_metadata );
          
          if( j != 1 )
          then
            v_statement := v_statement || ' and';
          end if;
          
          v_statement := v_statement || ' ' || v_scs_col_metadata_fk.i_column_name || ' = i_cell_value';
                
        end loop;
        v_statement := v_statement || ' )';
      elsif( v_scs_col_metadata.i_data_decode_metadata_list is not null )
      then
        v_statement := v_statement || ' decode( i_cell_value';    
        for j in 1..v_scs_col_metadata.i_data_decode_metadata_list.count        
        loop
          v_scs_decode_metadata := v_scs_col_metadata.i_data_decode_metadata_list(j);
          v_statement := v_statement || ', ' || v_scs_decode_metadata.i_value || ', ' || v_scs_decode_metadata.i_column_value;  
        end loop;
        v_statement := v_statement || ' )';    
      else
        v_statement := v_statement || v_scs_col_metadata.i_wrapper_function_prefix;    
        v_statement := v_statement || 'i_cell_value';
        v_statement := v_statement || v_scs_col_metadata.i_wrapper_function_postfix;    
      end if;
      v_statement := v_statement || ', null)) from table(cast(value(scs_rowdata).i_data_celldata_list as ct_data_cdl ))), ' || v_scs_col_metadata.i_dummy_null_value || ' ) col' || v_index;
      
      if( v_scs_col_metadata.i_data_fk_metadata is not null )
      then
        v_fk_pk_column_name := get_single_pk_column( v_scs_col_metadata.i_data_fk_metadata );
        
        v_statement := v_statement || ', ''select ' || v_fk_pk_column_name || ' from ' || v_scs_col_metadata.i_data_fk_metadata.i_table_name || ' where ';
        for j in 1 .. v_scs_col_metadata.i_data_fk_metadata.i_data_col_metadata_list.count
        loop
          v_scs_col_metadata_fk := treat( v_scs_col_metadata.i_data_fk_metadata.i_data_col_metadata_list(j) as ot_data_col_metadata );
          
          if( j != 1 )
          then
            v_statement := v_statement || ' and';
          end if;
          
          v_statement := v_statement || ' ' || v_scs_col_metadata_fk.i_column_name || ' = ''''''' || ' || nvl( (select max(decode( rownum, ' || v_index || ', (i_cell_value), null)) from table(cast(value(scs_rowdata).i_data_celldata_list as ct_data_cdl ))), ' || v_scs_col_metadata.i_dummy_null_value || ' ) || ''''''''';
                
        end loop;
        v_statement := v_statement || ' col' || v_index || '_fk_select';
      end if;
      
      v_index := v_index + 1;
    end loop;  
  end;
begin
  v_statement := 'select /*+no_merge */';

  v_index := 1;
  add_columns( p_metadata.i_data_key_columns );
  add_columns( p_metadata.i_data_add_columns );
  add_columns( p_metadata.i_data_def_columns );    

  v_statement := v_statement || ' from table( cast ( :1 as ct_data_rdl ) ) scs_rowdata';
  
  return v_statement;
end;
 
/**
 * Erstellt das Merge Statament.
 */
function create_ins_upd_del_stats( p_metadata in ot_data_metadata ) return varchar2;
function create_ins_upd_del_stats( p_metadata in ot_data_metadata ) return varchar2
is
  v_statement varchar2(32000);
  v_index number;
  v_scs_col_metadata ot_data_col_metadata;  
  
  function get_value_expression( p_data_col_metadata in ot_data_col_metadata, p_index in number, p_add_escape in number ) return varchar2
  is
    v_return varchar2(32000);
  begin
    v_return := '';
    if( p_data_col_metadata.i_sql_text_escape = 1 )
    then
      v_return := v_return || ''''''''' || ';
    end if;
    if( p_add_escape = 1 )
    then
      v_return := v_return || ''' || ';
    end if;
    if(p_data_col_metadata.i_data_fk_metadata is not null)
    then
      v_return := v_return || '''('' || col' || p_index || '_fk_select || '')''';      
    elsif( p_data_col_metadata is of (ot_data_col_metadata_number) )     
    then
      v_return := v_return || ' to_char( col' || p_index || ', ''FM99999999990.099999999999'')';           
    else
      v_return := v_return || 'col' || p_index;      
    end if;

    if( p_data_col_metadata.i_sql_text_escape = 1 )
    then      
      v_return := v_return || ' || ''''''''';
    end if;
    
    return v_return;
  end;
begin
  v_statement := 'select';
  
  v_statement := v_statement || ' case when exists ( select 1 from ' || p_metadata.i_table_name || ' where';
  
  for i in 1..p_metadata.i_data_key_columns.count
  loop
    if( i != 1 )
    then
      v_statement := v_statement || ' and';
    end if;
    
    v_statement := v_statement || ' ' || treat( p_metadata.i_data_key_columns(i) as ot_data_col_metadata ).i_column_name || ' = col' || i;
  end loop;    
  
  v_statement := v_statement || ' and rownum = 1 ) then';
  
  if( p_metadata.i_data_add_columns.count = 0 )
  then
    v_statement := v_statement || ' null';    
  else
    v_statement := v_statement || ' ''update ' || p_metadata.i_table_name || ' set ';
  
    for i in 1..p_metadata.i_data_add_columns.count
    loop
      v_scs_col_metadata := treat( p_metadata.i_data_add_columns(i) as ot_data_col_metadata );        
    
      if( i != 1 )
      then
        v_statement := v_statement || ' ,';
      end if;
      
      v_statement := v_statement || ' ' || v_scs_col_metadata.i_column_name || ' = '' || decode( col' || (i + p_metadata.i_data_key_columns.count) || ', ' || v_scs_col_metadata.i_dummy_null_value || ', ''null'', ' || get_value_expression( v_scs_col_metadata, i + p_metadata.i_data_key_columns.count, 0 ) || ' ) || ''';
    end loop;
  
    v_statement := v_statement || ' where';
    
    for i in 1..p_metadata.i_data_key_columns.count
    loop
      v_scs_col_metadata := treat( p_metadata.i_data_key_columns(i) as ot_data_col_metadata );     
    
      if( i != 1 )
      then
        v_statement := v_statement || ' || '' and';
      end if;
      
      v_statement := v_statement || ' ' || v_scs_col_metadata.i_column_name || ' = ' || get_value_expression( v_scs_col_metadata, i, 1 );
    end loop;  
  end if;  
  
  v_statement := v_statement || ' else'; 
  
  v_statement := v_statement || ' ''insert into ' || p_metadata.i_table_name || ' (';    
  
  for i in 1..p_metadata.i_data_def_const_columns.count
  loop
    v_statement := v_statement || ' ' || p_metadata.i_data_def_const_columns(i).i_column_name || ',';          
  end loop;
  
  for i in 1..p_metadata.i_data_key_columns.count
  loop
    if( i != 1 )
    then
      v_statement := v_statement || ' ,';
    end if;
    
    v_statement := v_statement || ' ' || treat( p_metadata.i_data_key_columns(i) as ot_data_col_metadata ).i_column_name;
  end loop;
  for i in 1..p_metadata.i_data_add_columns.count
  loop
    v_statement := v_statement || ' ,';  
    v_statement := v_statement || ' ' || treat( p_metadata.i_data_add_columns(i) as ot_data_col_metadata ).i_column_name;
  end loop;
  for i in 1..p_metadata.i_data_def_columns.count
  loop
    v_statement := v_statement || ' ,';  
    v_statement := v_statement || ' ' || treat( p_metadata.i_data_def_columns(i) as ot_data_col_metadata ).i_column_name;
  end loop;          
  
  v_statement := v_statement || ' ) values (';      
  
  for i in 1..p_metadata.i_data_def_const_columns.count
  loop
    v_statement := v_statement || ' ' || p_metadata.i_data_def_const_columns(i).i_const_expression || ',';          
  end loop;  
  
  v_statement := v_statement || ' ''';      
  
  v_index := 1;
  for i in 1..p_metadata.i_data_key_columns.count
  loop
    v_scs_col_metadata := treat( p_metadata.i_data_key_columns(i) as ot_data_col_metadata );
  
    if( i != 1 )
    then
      v_statement := v_statement || ' || '',''';
    end if;
    
    v_statement := v_statement || ' || decode( col' || v_index || ', ' || v_scs_col_metadata.i_dummy_null_value || ', ''null'', ' || get_value_expression( v_scs_col_metadata, v_index, 0 ) || ' )' ;
    v_index := v_index + 1;    
  end loop;
  for i in 1..p_metadata.i_data_add_columns.count
  loop
    v_scs_col_metadata := treat( p_metadata.i_data_add_columns(i) as ot_data_col_metadata );
      
      v_statement := v_statement || ' || '',''';
    v_statement := v_statement || ' || decode( col' || v_index || ', ' || v_scs_col_metadata.i_dummy_null_value || ', ''null'', ' || get_value_expression( v_scs_col_metadata, v_index, 0 ) || ' )' ;
    v_index := v_index + 1;    
  end loop;
  for i in 1..p_metadata.i_data_def_columns.count
  loop
    v_scs_col_metadata := treat( p_metadata.i_data_def_columns(i) as ot_data_col_metadata );
      
      v_statement := v_statement || ' || '',''';
    v_statement := v_statement || ' || decode( col' || v_index || ', ' || v_scs_col_metadata.i_dummy_null_value || ', ''null'', ' || get_value_expression( v_scs_col_metadata, v_index, 0 ) || ' )' ;
    v_index := v_index + 1;    
  end loop;  
  
  v_statement := v_statement || ' || '' )''';
  
  v_statement := v_statement || ' end as col_statement';   
  
  v_statement := v_statement || ' from ( ' || create_data_select_statement( p_metadata ) || ' )';
  
  v_statement := v_statement || ' where (';  
  for i in 1..(p_metadata.i_data_key_columns.count+p_metadata.i_data_add_columns.count)
  loop
    if( i != 1 )
    then
      v_statement := v_statement || ' ,';
    end if;
    
    v_statement := v_statement || ' col' || i;    
  end loop;
  v_statement := v_statement || ') not in (select';    
  for i in 1..p_metadata.i_data_key_columns.count
  loop
    v_scs_col_metadata := treat( p_metadata.i_data_key_columns(i) as ot_data_col_metadata );  
  
    if( i != 1 )
    then
      v_statement := v_statement || ' ,';
    end if;
    
    v_statement := v_statement || ' nvl( ' || v_scs_col_metadata.i_column_name || ', ' || v_scs_col_metadata.i_dummy_null_value || ' )';
  end loop;  
  for i in 1..p_metadata.i_data_add_columns.count
  loop
    v_scs_col_metadata := treat( p_metadata.i_data_add_columns(i) as ot_data_col_metadata );    
    v_statement := v_statement || ' ,';  
    v_statement := v_statement || ' nvl( ' || v_scs_col_metadata.i_column_name || ', ' || v_scs_col_metadata.i_dummy_null_value || ' )';
  end loop;    
  v_statement := v_statement || ' from ' || p_metadata.i_table_name || ' )';
  
  if( p_metadata.i_delete_knz = 1 )
  then
    v_statement := v_statement || ' union all select ''delete from ' || p_metadata.i_table_name;  
    v_statement := v_statement || ' where';
    
    for i in 1..p_metadata.i_data_key_columns.count
    loop
      v_scs_col_metadata := treat( p_metadata.i_data_key_columns(i) as ot_data_col_metadata );     
    
      if( i != 1 )
      then
        v_statement := v_statement || ' and';
      end if;
      
      v_statement := v_statement || ' ' || v_scs_col_metadata.i_column_name || ' = ' || case when v_scs_col_metadata.i_sql_text_escape = 1 then '''''' end || ''' || ' || case when v_scs_col_metadata.i_sql_text_escape = 1 then ' to_char(' end || v_scs_col_metadata.i_column_name || case when v_scs_col_metadata.i_sql_text_escape = 1 then ') || ''''''' end;
    end loop;  
    
    v_statement := v_statement || ''' as col_statement';     
    
    v_statement := v_statement || ' from ' || p_metadata.i_table_name || ' where ';
    if( p_metadata.i_delete_include_where is not null )
    then
      v_statement := v_statement || p_metadata.i_delete_include_where || ' and ';
    end if;
    v_statement := v_statement || ' not exists ( select 1 from ( ' || create_data_select_statement( p_metadata ) || ' ) where ';
    
    for i in 1..p_metadata.i_data_key_columns.count
    loop
      if( i != 1 )
      then
        v_statement := v_statement || ' and';
      end if;
      
      v_statement := v_statement || ' ' || treat( p_metadata.i_data_key_columns(i) as ot_data_col_metadata ).i_column_name || ' = col' || i;
    end loop;      
    
    v_statement := v_statement || ' )';    
  end if;
  
  v_statement := v_statement || ' order by 1';  
 
  return v_statement;
end;

/**
 * Erstellt das Statament um die Defaultdaten zu vergleichen.
 */
function create_check_def_data_stmt( p_metadata in ot_data_metadata ) return varchar2;
function create_check_def_data_stmt( p_metadata in ot_data_metadata ) return varchar2
is
  v_statement varchar2(32000);
  v_table_select varchar2(2000);
begin
  v_table_select := 'select to_char(';
  
  for i in 1..p_metadata.i_data_key_columns.count
  loop
    if( i != 1 )
    then
      v_table_select := v_table_select || ' ||';
    end if;
    
    v_table_select := v_table_select || ' ' || treat( p_metadata.i_data_key_columns(i) as ot_data_col_metadata ).i_column_name;
  end loop;
  v_table_select := v_table_select || ' ) as curr_key, to_char(';      
  for i in 1..p_metadata.i_data_def_columns.count
  loop
    if( i != 1 )
    then
      v_table_select := v_table_select || ' ||';
    end if;

    v_table_select := v_table_select || ' ' || treat( p_metadata.i_data_def_columns(i) as ot_data_col_metadata ).i_column_name;
  end loop;       
  v_table_select := v_table_select || ' ) as curr_value';        
  
  v_table_select := v_table_select || ' from ' || p_metadata.i_table_name;


  v_statement := 'select key, def_value, (select curr_value from ( ' || v_table_select || ' ) where curr_key = key ) as curr_def_value from ( select';

  for i in 1..p_metadata.i_data_key_columns.count
  loop
    if( i != 1 )
    then
      v_statement := v_statement || ' ||';
    end if;
    
    v_statement := v_statement || ' decode( col' || i || ', ' || treat( p_metadata.i_data_key_columns(i) as ot_data_col_metadata ).i_dummy_null_value || ', null, col' || i || ' )' ;    
  end loop;
  v_statement := v_statement || ' as key, ';    
  for i in 1..p_metadata.i_data_def_columns.count
  loop
    if( i != 1 )
    then
      v_statement := v_statement || ' ||';
    end if;

    v_statement := v_statement || ' decode( col' || (i+p_metadata.i_data_key_columns.count+p_metadata.i_data_add_columns.count) || ', ' || treat( p_metadata.i_data_def_columns(i) as ot_data_col_metadata ).i_dummy_null_value || ', null, col' || (i+p_metadata.i_data_key_columns.count+p_metadata.i_data_add_columns.count) || ' )' ;    
  end loop;   
  v_statement := v_statement || ' as def_value';      
  
  v_statement := v_statement || ' from ( ' || create_data_select_statement( p_metadata ) || ' )';    
  v_statement := v_statement || ' minus ' || v_table_select || ' )';
  
  return v_statement;
end;  

procedure register_table( p_metadata in ot_data_metadata )
is
begin
  pv_data_metadata_list.extend;
  pv_data_metadata_list( pv_data_metadata_list.count ) := p_metadata;
end;

procedure insert_into( p_table_name in varchar2, p_rows in ct_data_rdl )
is
begin
  pv_table_name_to_row_list_map( p_table_name ) := p_rows;
end;  

procedure do_merge( p_metadata in ot_data_metadata );
procedure do_merge( p_metadata in ot_data_metadata )
is
  type t_refcursor     is ref cursor;
  cur_statements_data  t_refcursor;  
  v_statement          varchar2(2000);      
  v_data_statement     varchar2(32000);        
  v_rows               ct_data_rdl;
begin
  v_rows := pv_table_name_to_row_list_map( p_metadata.i_table_name );

  begin
    v_data_statement := create_ins_upd_del_stats( p_metadata );      
  
    if( p_metadata.i_delete_knz = 1 )
    then
      open cur_statements_data for v_data_statement using v_rows, v_rows;    
    else
      open cur_statements_data for v_data_statement using v_rows;          
    end if;    
  exception
    when others
      then 
        -- statement fuer fehlersuche protokollieren
        pa_orcas_exec_log.log_exec_stmt( v_data_statement );
        commit;
        raise;
  end; 
  
  loop
    fetch cur_statements_data 
     into v_statement;
          
    exit when cur_statements_data%notfound;

    pa_orcas_exec_log.exec_stmt( v_statement );
  end loop;       
exception
  when others
    then 
      -- statement fuer fehlersuche protokollieren
      pa_orcas_exec_log.log_exec_stmt( v_data_statement );
      commit;
      raise;  
end;

procedure do_merge
is
  v_fk_ordered_metadata_list ct_data_metadata_list;
  
  procedure fill_fk_ordered_metadata_list 
  is
    type t_varchar2_set is table of number index by varchar2(100);
    v_added_table_name_set t_varchar2_set;
    v_all_table_name_set t_varchar2_set;
    v_metadata ot_data_metadata;
    
    function is_fk_ok( p_data_col_metadata_list in ct_data_col_metadata_list ) return number
    is
      v_data_col_metadata ot_data_col_metadata;
    begin
      for i in 1..p_data_col_metadata_list.count
      loop
        v_data_col_metadata := treat( p_data_col_metadata_list(i) as ot_data_col_metadata );    
        
        if( v_data_col_metadata.i_data_fk_metadata is not null )
        then
          if( v_all_table_name_set.exists( v_data_col_metadata.i_data_fk_metadata.i_table_name ) )        
          then
            if( not v_added_table_name_set.exists( v_data_col_metadata.i_data_fk_metadata.i_table_name ) )
            then
              return 0;
            end if;              
          end if;
        end if;
      end loop;
    
      return 1;
    end;
    
    function single_run return number
    is
      v_return number := 0;
    begin
      for i in 1..pv_data_metadata_list.count
      loop
        v_metadata := pv_data_metadata_list(i);
        
        if( not v_added_table_name_set.exists( v_metadata.i_table_name ) )
        then
          if(    is_fk_ok( v_metadata.i_data_key_columns ) = 1
             and is_fk_ok( v_metadata.i_data_add_columns ) = 1
             and is_fk_ok( v_metadata.i_data_def_columns ) = 1
            )          
          then
            v_fk_ordered_metadata_list.extend;
            v_fk_ordered_metadata_list(v_fk_ordered_metadata_list.count) := pv_data_metadata_list(i);
            v_added_table_name_set( v_metadata.i_table_name ) := 1;
            v_return := 1;
          end if;              
        end if;    
      end loop;
      
      return v_return;
    end;
  begin
    v_fk_ordered_metadata_list := new ct_data_metadata_list();
    
    for i in 1..pv_data_metadata_list.count
    loop
      v_all_table_name_set( pv_data_metadata_list(i).i_table_name ) := 1;
    end loop;
  
    loop
      if( single_run() = 0 )
      then
        for i in 1..pv_data_metadata_list.count
        loop
          if( not v_added_table_name_set.exists( pv_data_metadata_list(i).i_table_name ) )        
          then
            raise_application_error( -20000, 'fk-Reihenfolge konnte nicht ermittelt werden' || pv_data_metadata_list(i).i_table_name );          
          end if;
        end loop;      
        
        raise_application_error( -20000, 'sollte nicht hier sein... ' || v_fk_ordered_metadata_list.count || ' ' || pv_data_metadata_list.count );                  
      end if;
      
      exit when v_fk_ordered_metadata_list.count = pv_data_metadata_list.count;
    end loop;
  end;
begin
  delete table_script_source;
  commit;
  
  fill_fk_ordered_metadata_list();

  for i in 1..v_fk_ordered_metadata_list.count
  loop 
    do_merge( v_fk_ordered_metadata_list(i) );
  end loop;
  
  commit;    
  
  for cur_dummy in
  (
    select 1
      from table_script_source
  )
  loop
    pa_orcas_exec_log.exec_stmt( 'commit' );                        
    
    commit;
    
    return;
  end loop;  
end;

procedure do_check_only
is
  type t_refcursor     is ref cursor;
  cur_def_diff_data    t_refcursor;
  v_def_diff_key       varchar2(2000);
  v_def_diff_def_data  varchar2(2000);  
  v_def_diff_cur_data  varchar2(2000);    
  v_first              number := 1;
  v_metadata           ot_data_metadata;
  v_rows               ct_data_rdl;
begin
  for i in 1..pv_data_metadata_list.count
  loop   
    v_metadata := pv_data_metadata_list(i);
    v_rows := pv_table_name_to_row_list_map( v_metadata.i_table_name );
  
    if( v_metadata.i_data_def_columns.count > 0 )
    then    
      open cur_def_diff_data for create_check_def_data_stmt( v_metadata ) using v_rows;
      
      loop
        fetch cur_def_diff_data 
         into v_def_diff_key,
              v_def_diff_def_data,
              v_def_diff_cur_data;
              
        exit when cur_def_diff_data%notfound;
        
        if( v_first = 1 )
        then
          v_first := 0;
          
          dbms_output.put_line( '############# Defaultdatenabweichungen in Tabelle ' || v_metadata.i_table_name || ' #############' );          
        end if;

        dbms_output.put_line( 'key: ' || v_def_diff_key || ' default: "' || v_def_diff_def_data || '" current: "' || v_def_diff_cur_data || '"' );
      end loop;      
    end if;
  end loop;
end;

procedure genertae_reverse_script
is
  v_metadata                ot_data_metadata;
  v_data_col_metadata_list  ct_data_col_metadata_list;
  v_data_col_metadata       ot_data_col_metadata;
  v_stmt                    varchar2(32000);
  
  procedure add_col_metadata( p_data_col_metadata_list  ct_data_col_metadata_list )
  is
  begin
    for i in 1..p_data_col_metadata_list.count
    loop
      v_data_col_metadata_list.extend();
      v_data_col_metadata_list( v_data_col_metadata_list.count ) := p_data_col_metadata_list(i);
    end loop;
  end;
begin
  delete table_script_source;
  commit;
  
  pa_orcas_exec_log.log_exec_stmt( 'SET SERVEROUTPUT     ON' );            
  pa_orcas_exec_log.log_exec_stmt( 'SET VERIFY OFF' );            
  pa_orcas_exec_log.log_exec_stmt( 'SET ECHO            OFF' );            
  pa_orcas_exec_log.log_exec_stmt( 'SET FEEDBACK        OFF' );            
  pa_orcas_exec_log.log_exec_stmt( 'SET HEADING            OFF' );            
  pa_orcas_exec_log.log_exec_stmt( 'SET ESCAPE            ON' );            
  pa_orcas_exec_log.log_exec_stmt( 'SET LINESIZE         2000;' );            
  pa_orcas_exec_log.log_exec_stmt( 'SET PAGESIZE        9999;' );            
  pa_orcas_exec_log.log_exec_stmt( 'SET trimspool on' );            
  pa_orcas_exec_log.log_exec_stmt( 'set long 1000000000 longc 60000' );              

  for i in 1..pv_data_metadata_list.count
  loop   
    v_metadata := pv_data_metadata_list(i);
    
    v_data_col_metadata_list := new ct_data_col_metadata_list();
    
    add_col_metadata( v_metadata.i_data_key_columns );
    add_col_metadata( v_metadata.i_data_add_columns );
    add_col_metadata( v_metadata.i_data_def_columns );        
    
    pa_orcas_exec_log.log_exec_stmt( 'spool &' || '1.' || lower(v_metadata.i_table_name) || '.sql' );    
    pa_orcas_exec_log.log_exec_stmt( 'select ''begin pa_orcas_data.insert_into( ''''' || v_metadata.i_table_name || ''''',  ct_data_rdl('' from dual' );        
    v_stmt := '';
    v_stmt := v_stmt || 'select decode(rn,1,'' '','','') || rowdata from (select rownum rn, ''ot_data_rd( ct_data_vc2( '''''' || ';    
    
    for j in 1..v_data_col_metadata_list.count
    loop
      v_data_col_metadata := treat( v_data_col_metadata_list(j) as ot_data_col_metadata );         
    
      if( j != 1 )
      then
        v_stmt := v_stmt || ' || '''''', '''''' || ';          
      end if;
      
      if( v_data_col_metadata.i_data_fk_metadata is not null )
      then
        v_stmt := v_stmt || '( select ' || treat( v_data_col_metadata.i_data_fk_metadata.i_data_col_metadata_list(1) as ot_data_col_metadata ).i_column_name || ' from ' || v_data_col_metadata.i_data_fk_metadata.i_table_name || ' where ' || get_single_pk_column( v_data_col_metadata.i_data_fk_metadata ) || ' = ' || v_metadata.i_table_name || '.' || v_data_col_metadata.i_column_name || ')';
      else
        v_stmt := v_stmt || v_data_col_metadata.i_column_name;                      
      end if;
    end loop;    
    
    v_stmt := v_stmt || ' || '''''' ) )'' rowdata from ' || v_metadata.i_table_name || ' ) tabdata order by 1';           
    
    pa_orcas_exec_log.log_exec_stmt( v_stmt );            
    
    pa_orcas_exec_log.log_exec_stmt( 'select ''  ) );'' from dual' );            
    pa_orcas_exec_log.log_exec_stmt( 'select ''end;'' from dual' );            
    pa_orcas_exec_log.log_exec_stmt( 'select ''/'' from dual' );                    
    
    pa_orcas_exec_log.log_exec_stmt( 'spool off' );        
  end loop;
  
  commit;  
end;

end;
/
