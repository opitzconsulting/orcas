create table varray_table_new
(
  id number, 
  col1 object type "t_varchararray"
)
varray col1 store as securefile lob
;

create table varray_table_add
(
  id number, 
  col1 object type "t_varchararray"
)
varray col1 store as securefile lob (deduplicate compress high)
;


