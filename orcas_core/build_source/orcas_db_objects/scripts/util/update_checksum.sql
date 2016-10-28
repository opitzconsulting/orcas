set serveroutput on
set feedback off
set verify off

create or replace package body pa_orcas_checksum
is 
  function get_total_checksum return varchar2
  is
  begin
    return '&1';
  end;

  function get_extension_checksum return varchar2
  is
  begin
    return '&2';
  end;
end;
/

  
