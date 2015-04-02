create or replace package pa_generator authid current_user is
  procedure run( p_model in ot_syex_model );
end;
/
