create or replace package pa_orcas_trans_syex_orig is
  function trans_syex_orig( p_input in ot_syex_model ) return ot_orig_model;
end;
/

