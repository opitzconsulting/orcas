create or replace package pa_orcas_add_fk_idx_extension is
  function run( p_input in ot_orig_model ) return ot_orig_model;
end;
/
