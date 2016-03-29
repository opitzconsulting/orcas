create or replace package pa_identity_col_trg_gen_helper authid current_user is
  procedure run( p_model in ot_syex_model );
end;
/
