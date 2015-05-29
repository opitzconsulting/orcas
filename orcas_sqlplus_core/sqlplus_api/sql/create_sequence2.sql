@@default_include

declare

-- API:
------------------------------------------------------------------------
  v_sequence_name         varchar2(30)   := substr(upper('&1'),1,30);
  v_max_value_select      varchar2(2000) := upper('&2');
  v_minvalue              varchar2(2000) := upper('&3');                        -- OLJ 2014-05-21: new in API
  v_maxvalue              varchar2(2000) := upper('&4');                        -- OLJ 2014-05-21: new in API
  v_increment_by          varchar2(2000) := upper('&5');                        -- OLJ 2014-05-21: new in API
  v_order                 varchar2(30)   := trim(substr(upper('&6'),1,30));     -- OLJ 2014-05-21: new in API
  v_cycle                 varchar2(30)   := trim(substr(upper('&7'),1,30));     -- OLJ 2014-05-21: new in API
  v_cache                 varchar2(2000) := upper('&8');                        -- OLJ 2014-05-21: new in API 
------------------------------------------------------------------------  
  
  v_sequence            ot_syex_sequence;
  v_ordertype           ot_syex_ordertype;
  v_cycletype           ot_syex_cycletype;
  v_tablespace          varchar2(2000);
  
----------------------------------------------------------------  
  
begin
     -- SEQUENCE NAME    
    if v_sequence_name is null then
      raise_application_error(-20000, 'Sequence-Name (Param_1) nicht angegeben');
    end if;  
     
    v_sequence := new ot_syex_sequence();    
    v_sequence.i_sequence_name := v_sequence_name;
    
    -- MAX USED VALUE SELECT
    v_sequence.i_max_value_select := v_max_value_select;
    
    -- MINVALUE
    if (v_minvalue is not null) then
      if ( length(trim(translate(v_minvalue, ' 0123456789', ' '))) is not null ) then
        raise_application_error(-20000, 'Minvalue (Param_3) muss numerisch sein.');
      else
        v_sequence.i_minvalue := to_number(v_minvalue);
      end if;
    else
      v_sequence.i_minvalue := 1;
    end if;  
    
    -- MAXVALUE
    if (v_maxvalue is not null) then
      if ( length(trim(translate(v_maxvalue, ' 0123456789', ' '))) is not null ) then
        raise_application_error(-20000, 'Maxvalue (Param_4) muss numerisch sein.');
      else
        v_sequence.i_maxvalue := to_number(v_maxvalue);
      end if;
    end if;      
    
    -- INCREMENT BY
    if (v_increment_by is not null) then
      if ( length(trim(translate(v_maxvalue, ' 0123456789', ' '))) is not null ) then
        raise_application_error(-20000, 'Increment by (Param_5) muss numerisch sein.');
      else
        v_sequence.i_increment_by := to_number(v_increment_by);
      end if;
    else
        v_sequence.i_increment_by := 1;
    end if;
    
    -- CACHE
    if (v_cache is not null) then
      if ( length(trim(translate(v_cache, ' 0123456789', ' '))) is not null ) then
        raise_application_error(-20000, 'Cache (Param_8) muss numerisch sein.');
      else
        v_sequence.i_cache := to_number(v_cache);
      end if;
    end if;       
    
    -- ORDERTYPE
    if nvl(v_order, 'NULL') = 'NOORDER' then
      v_ordertype := ot_syex_ordertype.c_noorder;
    elsif nvl(v_order, 'NULL') = 'ORDER' then
      v_ordertype := ot_syex_ordertype.c_order;
    end if;
    
    v_sequence.i_order := v_ordertype;
 
    -- CYCLETYPE
    if nvl(v_cycle, 'NULL') = 'NOCYCLE' then
      v_cycletype := ot_syex_cycletype.c_nocycle;
    elsif nvl(v_cycle, 'NULL') = 'CYCLE' then
      v_cycletype := ot_syex_cycletype.c_cycle;
    end if;    
   
   v_sequence.i_cycle := v_cycletype;
    
    -- ADD SEQUENCE TO MODEL   
    pa_orcas_sqlplus_model_holder.add_model_element(v_sequence);
end;
/

undefine 1
undefine 2
undefine 3
undefine 4
undefine 5
undefine 6
undefine 7
undefine 8