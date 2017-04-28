create or replace type t_varchararray as varray(10) of varchar2(128)
/

create table varray_table 
(
  id number, 
  col1 t_varchararray
)
varray col1 store as securefile lob (compress)
/



