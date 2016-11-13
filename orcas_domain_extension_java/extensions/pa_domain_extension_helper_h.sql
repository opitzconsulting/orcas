create or replace package pa_domain_extension_helper is
  type t_varchar_set is table of number index by varchar2(100);

  function is_equal( p_val1 number, p_val2 number ) return number;    
  
  function is_equal( p_val1 number, p_val2 number, p_default number ) return number;
  
  function is_equal( p_val1 varchar2, p_val2 varchar2 ) return number;
  
  function is_equal_ignore_case( p_val1 varchar2, p_val2 varchar2 ) return number;   
  
  function get_generated_name_table( p_gennamerule_list in ct_syex_gennamerule_list, p_table_name in varchar2, p_alias in varchar2 ) return varchar2;
    
  function get_generated_name_column( p_gennamerule_list in ct_syex_gennamerule_list, p_column_name in varchar2, p_table_name in varchar2, p_alias in varchar2 ) return varchar2;

  function get_generated_name_col_domain( p_gennamerule_list in ct_syex_gennamerule_list, p_column_domain_name in varchar2 ) return varchar2;
end;
/
