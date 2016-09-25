create or replace package pa_orcas_extension_parameter is
  procedure set_extension_parameter( p_extension_parameter in varchar2 );
  function get_extension_parameter return varchar2;
  function get_extension_parameter_entry( p_extension_parameter_key in varchar2 ) return varchar2;
end;
/
