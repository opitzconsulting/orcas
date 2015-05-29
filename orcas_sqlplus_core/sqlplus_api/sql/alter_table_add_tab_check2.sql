@@default_include

declare

-- API:
------------------------------------------------------------------------

  v_table_name        varchar2(30)   := substr(upper('&1'),1,30);
  v_constraint_name   varchar2(30)   := substr(upper('&2'),1,30);
  v_check_condition   varchar2(2000) := upper(substr('&3',1,2000));
  v_deferr_rule       varchar2(30)   := substr(upper('&4'),1,30);        -- OLJ 2014-05-22: new in API
------------------------------------------------------------------------

  v_table             ot_syex_table;
  v_constraint        ot_syex_constraint;  
  v_deferrtype        ot_syex_deferrtype;
------------------------------------------------------------------------   

begin
  
    v_table   := pa_orcas_sqlplus_model_holder.get_table();
    
    -- TABLE NAME    
    if v_table_name <> v_table.i_name then
      raise_application_error(-20000, 'Table-Name (Param_1) = ' || v_table_name || 
                                      ' stimmt nicht mit aktueller Tabelle = ' || v_table.i_name || ' ueberein.');
    end if;     
    
    -- CONSTRAINT NAME    
    if v_constraint_name is null then
      raise_application_error(-20000, 'Constraint-Name (Param_2) nicht angegeben');
    end if;
    
    -- CHECK CONDITION    
    if v_check_condition is null then
      raise_application_error(-20000, 'Check condition (Param_3) nicht angegeben');
    end if;   
    
    -- DEFERR RULE
    if v_deferr_rule is null then
      v_deferr_rule := 'IMMEDIATE';
    end if;
    
    if v_deferr_rule not in ('IMMEDIATE','DEFERRED') then
      raise_application_error(-20000, 'Deferr Rule falsch (Param_4) ' || v_deferr_rule );
    end if;
    
    if v_deferr_rule = 'DEFERRED' then
      v_deferrtype := ot_syex_deferrtype.c_deferred;
    end if;        
    
    -- ADD CONSTRAINT
    if v_table.i_constraints is null then
        v_table.i_constraints := new ct_syex_constraint_list();
    end if;
   
    v_constraint := new ot_syex_constraint();
    v_constraint.i_consname := v_constraint_name;
    v_constraint.i_rule := v_check_condition;
    v_constraint.i_deferrtype := v_deferrtype;
    
    v_table.i_constraints.extend;
    v_table.i_constraints(v_table.i_constraints.count) := v_constraint;
    
    pa_orcas_sqlplus_model_holder.save_table(v_table);
end;
/

undefine 1
undefine 2
undefine 3

