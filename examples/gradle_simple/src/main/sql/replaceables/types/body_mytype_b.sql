create or replace type body mytype_b as

overriding member function get_xy( p_val in number ) return varchar2
is
begin
  return 'b' || p_val;
end;

end;
/
