create or replace package pa_orcas_sqlplus_model_holder is
  procedure reset_model;
  function get_model return ot_syex_model;
  procedure add_model_element( p_modelelement in ot_syex_modelelement );
  function get_table return ot_syex_table;
  procedure save_table( p_table in ot_syex_table );
end;
/
