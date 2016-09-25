create or replace package pa_orcas_extensions authid current_user is
  function is_extensions_exists return number;
  function call_extensions( p_input in ot_syex_model, p_extension_parameter in varchar2 ) return ot_syex_model;
  function call_reverse_extensions( p_input in ot_syex_model, p_extension_parameter in varchar2 ) return ot_syex_model;
end;
/

