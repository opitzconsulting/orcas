create or replace package pa_orcas_run_parameter is
  function get_excludewheresequence return varchar2;
  function get_excludewheretable return varchar2;
  function get_excludewheremview return varchar2;
  function is_logonly return number;
  function is_dropmode return number;  
  function is_indexparallelcreate return number;  
  function is_createmissingfkindexes return number;   
  function is_indexmovetablespace return number;
  function is_tablemovetablespace return number;
end;
/
