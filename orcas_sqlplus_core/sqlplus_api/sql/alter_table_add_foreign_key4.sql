@@default_include

declare

-- API:
------------------------------------------------------------------------
  v_table_name         varchar2(2000) := upper('&1');
  v_ref_table_name     varchar2(2000) := upper('&2');
  v_delete_rule        varchar2(30)   := substr(upper('&3'),1,30);
  v_column_list        varchar2(4000) := upper('&4');              -- AST 2012-08-08: could contain more than one column
  v_fk_name            varchar2(30)   := substr(upper('&5'),1,30);
  v_destcolumns        varchar2(4000) := upper('&6');              -- ANW 2014-05-09: new in API
  v_deferr_rule        varchar2(30)   := upper('&7');              -- ANW 2014-05-09: new in API
------------------------------------------------------------------------

  v_table            ot_syex_table;
  v_foreignkey       ot_syex_foreignkey;
  v_columnref        ot_syex_columnref;
  v_columnref_2      ot_syex_columnref;
  v_fkdeleteruletype ot_syex_fkdeleteruletype;
  v_deferrtype       ot_syex_deferrtype;

  v_weiter           boolean;
  v_comma            number;  
  v_column_name      varchar2(2000);

begin

 -- GET TABLE  
    v_table   := pa_orcas_sqlplus_model_holder.get_table();
    
 -- TABLE NAME    
    if v_table_name <> v_table.i_name then
      raise_application_error(-20000, 'Table-Name (Param_1) = ' || v_table_name || 
                                      ' stimmt nicht mit aktueller Tabelle = ' || v_table.i_name || ' ueberein.');
    end if;                                  

 -- REF TABLE NAME    
    if v_ref_table_name is null then
      raise_application_error(-20000, 'Ref Table-Name (Param_2) nicht angegeben');
    end if;

    if length(v_ref_table_name) > 30 then
      raise_application_error(-20000, 'Ref Table-Name (Param_2) nicht darf nicht laenger als 30 Zeichen sein');  
    end if;

 -- DELETE RULE
  if v_delete_rule is null then
    v_delete_rule := 'NOTHING';
  end if;
  
  if v_delete_rule not in ('CASCADE','NOTHING','NULLIFY') then
    raise_application_error(-20000, 'Delete Rule falsch (Param_3) ' || v_delete_rule );
  end if;
  
  if v_delete_rule = 'CASCADE' then
      v_fkdeleteruletype := ot_syex_fkdeleteruletype.c_cascade;
  elsif v_delete_rule = 'NOTHING' then
    v_fkdeleteruletype := ot_syex_fkdeleteruletype.c_no_action;
  elsif v_delete_rule = 'NULLIFY' then   
    v_fkdeleteruletype := ot_syex_fkdeleteruletype.c_set_null;
  end if;


 -- DEFERR RULE
  if v_deferr_rule is null then
    v_deferr_rule := 'IMMEDIATE';
  end if;
  
  if v_deferr_rule not in ('IMMEDIATE','DEFERRED') then
    raise_application_error(-20000, 'Deferr Rule falsch (Param_7) ' || v_deferr_rule );
  end if;
  
  if v_deferr_rule = 'DEFERRED' then
    v_deferrtype := ot_syex_deferrtype.c_deferred;
  end if;


 -- COLUMNS
    if v_column_list is null then
      raise_application_error(-20000, 'Column-Name (Param_4) nicht angegeben');
    end if;

 -- DEST_COLUMNS
    if v_destcolumns is null then
      raise_application_error(-20000, 'Dest Columns (Param_6) nicht angegeben');
    end if;

 
 -- FK Name 
    if v_fk_name is null then
      raise_application_error(-20000, 'FK-Name (Param_5) nicht angegeben');
    end if;

    if length(v_fk_name) > 30 then
      raise_application_error(-20000, 'FK-Name (Param_5) nicht darf nicht laenger als 30 Zeichen sein');  
    end if;


 -- CREATE FK
    v_foreignkey := new ot_syex_foreignkey();

    v_foreignkey.i_consname  := v_fk_name;
    v_foreignkey.i_desttable := v_ref_table_name;
    v_foreignkey.i_delete_rule := v_fkdeleteruletype;
    v_foreignkey.i_deferrtype := v_deferrtype;


    -- SRC Columns
    v_foreignkey.i_srccolumns := pa_orcas_sqlplus_utils.get_column_list(v_column_list);

    -- DEST Columns
    v_foreignkey.i_destcolumns := pa_orcas_sqlplus_utils.get_column_list(v_destcolumns);
    
    -- ADD FK TO TABLE   
    if v_table.i_foreign_keys is null then
        v_table.i_foreign_keys := new ct_syex_foreignkey_list();
    end if;
    
    v_table.i_foreign_keys.extend;
    v_table.i_foreign_keys(v_table.i_foreign_keys.count) := v_foreignkey;
 
 -- SAVE TABLE  
    pa_orcas_sqlplus_model_holder.save_table(v_table);
  
end;
/

undefine 1
undefine 2
undefine 3
undefine 4
undefine 5
undefine 6
undefine 7



