create or replace package body pa_orcas_model_holder is
  pv_model ot_syex_model;
  
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
  
begin
  pv_model := new ot_syex_model();
  pv_model.i_model_elements := new ct_syex_modelelement_list();
end;
/
