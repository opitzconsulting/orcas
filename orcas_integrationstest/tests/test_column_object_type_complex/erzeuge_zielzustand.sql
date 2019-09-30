@@create_types.sql

create table tab_nested_table_complex
(
  col_type col_test_type_list_list
) 
nested table col_type store as tab_nested_table_complex_list ( nested table column_value store as tab_nested_table_complex_list_list )
/

