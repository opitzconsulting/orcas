create or replace type om_orig_inlinecomment_impl force under om_orig_inlinecomment
(
  overriding member function is_child_order_relevant return number,
  overriding member function get_merge_result( p_diff_values in out nocopy cd_orig_inlinecomment_list, p_old_values in ct_orig_inlinecomment_list ) return ct_merge_result_list  
)
/
create or replace type body om_orig_inlinecomment_impl is
  
  overriding member function is_child_order_relevant return number
  is
  begin
    return 0;
  end;

  overriding member function get_merge_result( p_diff_values in out nocopy cd_orig_inlinecomment_list, p_old_values in ct_orig_inlinecomment_list ) return ct_merge_result_list is
    v_merge_result ct_merge_result_list := new ct_merge_result_list();
  begin
    v_merge_result.extend(p_old_values.count());
    
    for i in 1..p_old_values.count()
    loop
      v_merge_result(i) := new ot_merge_result(null);          
      
      for j in 1..p_diff_values.count()
      loop
        if( ( p_old_values(i).i_column_name is null and p_diff_values(j).n_column_name is null ) or p_old_values(i).i_column_name = p_diff_values(j).n_column_name )
        then
          v_merge_result(i).i_merge_index := j;
        end if;
      end loop;
    end loop;
  
    return v_merge_result;
  end;
end;
/
