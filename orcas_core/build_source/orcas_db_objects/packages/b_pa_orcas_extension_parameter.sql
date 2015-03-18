create or replace package body pa_orcas_extension_parameter is
  type t_parameter_map is table of varchar2(2000) index by varchar2(2000);
  pv_parameter_map t_parameter_map;
  pv_is_map_initialized number := 0;

  function get_extension_parameter return varchar2 is
  begin
    return '@extensionparameter@';
  end;  
  
  procedure handle_part( p_text_part in varchar2 )
  is
    v_colon_index number;    
  begin
    if( p_text_part is null )
    then
      return;
    end if;

    v_colon_index := instr( p_text_part, ':' );
    
    pv_parameter_map( substr( p_text_part, 1, v_colon_index - 1 ) ) := substr( p_text_part, v_colon_index + 1, length(p_text_part) - v_colon_index );
  end;  
  
  procedure initialize_map is
    v_text_rest varchar2(2000);
    v_separator_index number;
  begin
    v_text_rest := get_extension_parameter();
    
    v_text_rest := replace( v_text_rest, '[', '' );
    v_text_rest := replace( v_text_rest, ']', '' );    
    v_text_rest := trim(v_text_rest);
    
    loop    
      v_separator_index := instr( v_text_rest, ',' );
      if( v_separator_index != 0 )
      then
        handle_part( substr( v_text_rest, 1, v_separator_index - 1 ) );
        v_text_rest := trim(substr( v_text_rest, v_separator_index + 1, length(v_text_rest) - v_separator_index ));         
      else
        handle_part( v_text_rest );
        exit;
      end if;
    end loop;
  end;
  

  function get_extension_parameter_entry( p_extension_parameter_key in varchar2 ) return varchar2 is
  begin
    if( pv_is_map_initialized = 0 )
    then
      initialize_map();
      pv_is_map_initialized := 1;
    end if;
  
    if( not pv_parameter_map.exists( p_extension_parameter_key ) )
    then
      return null;
    end if;
  
    return pv_parameter_map( p_extension_parameter_key );
  end;  

end;
/
