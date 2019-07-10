create or replace type mytype_c force as object
(
  value2 number,

  not instantiable not final member function get_xy( p_val in number ) return varchar2
)
not instantiable
not final
/
