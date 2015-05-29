create or replace package body pa_orcas_sqlplus_model_holder is
  pv_model ot_syex_model;

  procedure reset_model
  is
  begin
    pv_model := new ot_syex_model();
    pv_model.i_model_elements := new ct_syex_modelelement_list();
  end;

  function get_model return ot_syex_model
  is
  begin
    return pv_model;
  end;

  procedure add_model_element( p_modelelement in ot_syex_modelelement )
  is
  begin
    pv_model.i_model_elements.extend;
    pv_model.i_model_elements(pv_model.i_model_elements.count) := p_modelelement;
  end;

  function get_table return ot_syex_table
  is
  begin
    return treat(pv_model.i_model_elements(pv_model.i_model_elements.count) as ot_syex_table);
  end;

  procedure save_table( p_table in ot_syex_table )
  is
  begin
    pv_model.i_model_elements(pv_model.i_model_elements.count) := p_table;
  end;

begin
  reset_model();
end;
/
