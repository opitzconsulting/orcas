create or replace package pa_orcas_extensions is
  function call_extensions( p_input in ot_syex_model ) return ot_syex_model;
  function call_reverse_extensions( p_input in ot_syex_model ) return ot_syex_model;
end;
/

