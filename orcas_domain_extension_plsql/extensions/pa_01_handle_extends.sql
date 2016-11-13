create or replace package body pa_01_handle_extends is
  pv_syex_model ot_syex_model;
  
  function merge_domains( p_syex_domain in ot_syex_domain, p_super_domain in ot_syex_domain ) return ot_syex_domain
  is
    v_syex_domain ot_syex_domain;
    
    v_syex_domaincolumn_list ct_syex_domaincolumn_list := new ct_syex_domaincolumn_list();
    
    procedure add_columns( p_syex_domaincolumn_list ct_syex_domaincolumn_list )
    is
    begin
      if( p_syex_domaincolumn_list is not null )
      then
        for i in 1 .. p_syex_domaincolumn_list.count
        loop
          v_syex_domaincolumn_list.extend;
          v_syex_domaincolumn_list( v_syex_domaincolumn_list.count ) := p_syex_domaincolumn_list(i);
        end loop;   
      end if;
    end;
  begin
    v_syex_domain := p_syex_domain;
    
    add_columns( p_super_domain.i_columns );
    add_columns( p_syex_domain.i_columns );    
    
    v_syex_domain.i_columns := v_syex_domaincolumn_list;    
    
    if( v_syex_domain.i_historytable is null )
    then
      v_syex_domain.i_historytable := p_super_domain.i_historytable;
    end if;
    
    return v_syex_domain;
  end;
  
  function get_domain( p_domain_name in varchar2 ) return ot_syex_domain;
  
  function build_merged_domain( p_syex_domain in ot_syex_domain ) return ot_syex_domain
  is
  begin
    if( p_syex_domain.i_extends is null )
    then
      return p_syex_domain;
    else
      return merge_domains( p_syex_domain, get_domain( p_syex_domain.i_extends ) );
    end if;
  end;

  function get_domain( p_domain_name in varchar2 ) return ot_syex_domain
  is
  begin
    for i in 1..pv_syex_model.i_model_elements.count
    loop
      if( pv_syex_model.i_model_elements(i) is of (ot_syex_domain) )
      then
        if( treat( pv_syex_model.i_model_elements(i) as ot_syex_domain ).i_name = p_domain_name )
        then
          return build_merged_domain( treat( pv_syex_model.i_model_elements(i) as ot_syex_domain ) );
        end if;
      end if;
    end loop;

    raise_application_error( -20000, 'domain not found: ' || p_domain_name );
  end;
  
  function run( p_input in ot_syex_model ) return ot_syex_model
  is
  begin
    pv_syex_model := p_input;
      
    for i in 1..pv_syex_model.i_model_elements.count
    loop
      if( pv_syex_model.i_model_elements(i) is of (ot_syex_domain) )
      then
        pv_syex_model.i_model_elements(i) := get_domain( treat( pv_syex_model.i_model_elements(i) as ot_syex_domain ).i_name );
      end if;
    end loop;
    
    return pv_syex_model;
  end;
end;
/
