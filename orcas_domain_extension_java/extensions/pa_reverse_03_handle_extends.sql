create or replace package body pa_reverse_03_handle_extends is
  function run( p_input in ot_syex_model ) return ot_syex_model
  is
  begin
    --return pa_01_handle_extends.run( p_input );
    return p_input;
  end;
end;
/
