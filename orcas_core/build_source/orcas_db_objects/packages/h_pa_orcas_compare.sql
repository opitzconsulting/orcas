CREATE OR REPLACE package pa_orcas_compare authid current_user is

function get_column_list( p_columnref_list in ct_orig_columnref_list ) return varchar2;

procedure compare_and_update(pi_model_ist in ot_orig_model, pi_model_soll in ot_orig_model);

end;
/
