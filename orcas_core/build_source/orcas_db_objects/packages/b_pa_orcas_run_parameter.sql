create or replace package body pa_orcas_run_parameter is
  function get_excludewheresequence return varchar2 is
  begin
    return '@excludewheresequence@';
  end;  

  function get_excludewheretable return varchar2 is
  begin
    return '@excludewheretable@';
  end;  
  
  function get_excludewheremview return varchar2 is
  begin
    return '@excludewheremview@';
  end;    
  
  function get_dateformat return varchar2 is
  begin
    return '@dateformat@';
  end;

  function is_logonly return number is
  begin
    if( '@logonly@' = 'false' )
    then
      return 0;
    else
      return 1;
    end if;  
  end;  
  
  function is_dropmode return number is
  begin
    if( '@dropmode@' = 'true' )
    then
      return 1;
    else
      return 0;
    end if;  
  end;      
  
  function is_indexparallelcreate return number is
  begin
    if( '@indexparallelcreate@' = 'true' )
    then
      return 1;
    else
      return 0;
    end if;  
  end;   
  
  function is_indexmovetablespace return number is
  begin
    if( '@indexmovetablespace@' = 'true' )
    then
      return 1;
    else
      return 0;
    end if;  
  end;      
  
  function is_tablemovetablespace return number is
  begin
    if( '@tablemovetablespace@' = 'true' )
    then
      return 1;
    else
      return 0;
    end if;  
  end;      
  
  function is_createmissingfkindexes return number is
  begin
    if( '@createmissingfkindexes@' = 'true' )
    then
      return 1;
    else
      return 0;
    end if;  
  end;  
  
end;
/
