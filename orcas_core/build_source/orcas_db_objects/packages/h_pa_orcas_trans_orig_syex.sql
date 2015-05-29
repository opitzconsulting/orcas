create or replace package pa_orcas_trans_orig_syex is
  function trans_orig_syex( p_input in ot_orig_model ) return ot_syex_model;
end;
/

