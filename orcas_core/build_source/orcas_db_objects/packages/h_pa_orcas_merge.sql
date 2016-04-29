CREATE OR REPLACE package pa_orcas_merge authid current_user is

function get_merge_result( p_new_model in ot_orig_model, p_old_model in ot_orig_model ) return od_orig_model;

end;
/
