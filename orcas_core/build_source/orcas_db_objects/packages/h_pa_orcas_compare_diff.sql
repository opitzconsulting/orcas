CREATE OR REPLACE package pa_orcas_compare_diff authid current_user is

procedure compare_and_update(pi_model_ist in ot_orig_model, pi_model_soll in ot_orig_model);

end;
/
