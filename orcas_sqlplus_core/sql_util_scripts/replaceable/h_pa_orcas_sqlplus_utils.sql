create or replace package pa_orcas_sqlplus_utils is
  function entferne_klammern(v_in_string varchar2) return varchar2;
  function find_tablespace(v_in_string varchar2) return varchar2;
  function find_reverse(v_in_string varchar2) return varchar2;
  function get_column_list(v_in_string varchar2) return ct_syex_columnref_list;
  function get_list_partition_valuelist(v_in_string varchar2)
    return ct_syex_listpartitionvalu_list;
  function get_range_partition_valuelist(v_in_string varchar2)
    return ct_syex_rangepartitionval_list;  
end;
/
