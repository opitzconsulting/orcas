create or replace type om_orig_indexoruniquekey_impl force under om_orig_indexoruniquekey
(
  i_default_tablespace varchar2(2000),
  
  overriding member function d_tablespace( p_value in varchar2 ) return varchar2
)
/
create or replace type body om_orig_indexoruniquekey_impl is

  overriding member function d_tablespace( p_value in varchar2 ) return varchar2
  is
  begin
    if( upper( p_value ) = i_default_tablespace )
    then
      return null;
    end if;
    return upper( p_value );
  end;  
  
end;
/
