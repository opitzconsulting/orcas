create or replace package pa_orcas_model_holder is
  function get_model return ot_syex_model;
  procedure add_model_element( p_modelelement in ot_syex_modelelement );
end;
/
