create or replace package pa_orcas_checksum
is 
  function get_total_checksum return varchar2;

  function get_extension_checksum return varchar2;
end;
/

