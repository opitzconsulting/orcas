@@create_types.sql

create table tab_object_type
(
  col_type col_test_type
)
/


create table tab_nested_table
(
  col_type col_test_type_list
) 
nested table col_type store as tab_nested_table_list
/


create table tab_varray
(
  col_type col_test_type_array
)
/


create table tab_nested_table_add_column
(
  col1 number,
  col_type col_test_type_list
) 
nested table col_type store as tab_nested_table_add_column_list
/

