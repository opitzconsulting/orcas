create or replace type body mytype_a as

constructor function mytype_a return self as result
is
begin
  return;
end;

overriding member function get_xy( p_val in number ) return varchar2
is
begin
  return 'xy';
end;

end;
/
