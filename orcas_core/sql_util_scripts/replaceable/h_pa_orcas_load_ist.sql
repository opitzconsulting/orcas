create or replace package pa_orcas_load_ist authid current_user is

function get_ist return ot_orig_model;

end;
/
