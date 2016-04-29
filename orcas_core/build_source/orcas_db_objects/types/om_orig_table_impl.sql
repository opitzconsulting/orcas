create or replace type om_orig_table_impl force under om_orig_table
(
  i_default_tablespace varchar2(2000),
  
  overriding member function is_child_order_relevant return number,
  overriding member function get_merge_result( p_diff_values in out nocopy cd_orig_table_list, p_old_values in ct_orig_table_list ) return ct_merge_result_list,
  overriding member function cleanup_values( p_value in out nocopy ot_orig_table ) return number,
  overriding member function d_tablespace( p_value in varchar2 ) return varchar2
)
/
create or replace type body om_orig_table_impl is

  overriding member function is_child_order_relevant return number
  is
  begin
    return 0;
  end;

  overriding member function get_merge_result( p_diff_values in out nocopy cd_orig_table_list, p_old_values in ct_orig_table_list ) return ct_merge_result_list is
    v_merge_result ct_merge_result_list := new ct_merge_result_list();
  begin
    v_merge_result.extend(p_old_values.count());
    
    for i in 1..p_old_values.count()
    loop
      v_merge_result(i) := new ot_merge_result(null);          
      
      for j in 1..p_diff_values.count()
      loop
        if( p_old_values(i).i_name = p_diff_values(j).n_name )
        then
          v_merge_result(i).i_merge_index := j;
        end if;
      end loop;
    end loop;
  
    return v_merge_result;
  end;
  
  overriding member function d_tablespace( p_value in varchar2 ) return varchar2
  is
  begin
    if( upper( p_value ) = i_default_tablespace )
    then
      return null;
    end if;
    return upper( p_value );
  end;  
  
  overriding member function cleanup_values( p_value in out nocopy ot_orig_table ) return number
  is
  begin
    if( (self as om_orig_table).cleanup_values( p_value ) = null ) then null; end if;
    
    if(   ot_orig_permanentnesstype.is_equal( p_value.i_permanentness, ot_orig_permanentnesstype.c_global_temporary ) = 1 )
    then
      if( ot_orig_loggingtype.is_equal( p_value.i_logging, ot_orig_loggingtype.c_nologging ) = 1 )    
      then
        p_value.i_logging := null;
      end if;
    else
      if( ot_orig_loggingtype.is_equal( p_value.i_logging, ot_orig_loggingtype.c_logging ) = 1 )    
      then
        p_value.i_logging := null;
      end if;    
    end if;
    
    return null;
  end;      
  
end;
/
