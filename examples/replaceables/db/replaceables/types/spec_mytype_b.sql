create or replace type mytype_b force
under mytype_c
(
  myc mytype_c_col,

  overriding member function get_xy( p_val in number ) return varchar2
)
not final
/
