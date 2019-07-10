create or replace type mytype_a force
under mytype_c
(
  constructor function mytype_a return self as result,
  overriding member function get_xy( p_val in number ) return varchar2
)
not final
/
