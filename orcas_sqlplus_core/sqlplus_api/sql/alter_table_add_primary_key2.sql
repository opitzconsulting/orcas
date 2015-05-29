@@default_include

declare

  --API:
  ------------------------------------------------------------------
  v_table_name           varchar2(2000) := upper('&1');
  v_pk_name              varchar2(2000) := upper('&2');
  v_column_list          varchar2(2000) := substr(upper('&3'),1,2000);
  v_physical_parameters  varchar2(250)   := substr(upper('&4'),1,250); --includes physical parameters
  ------------------------------------------------------------------

  v_table       ot_syex_table;
  v_columnref   ot_syex_columnref;
  
  v_weiter      boolean;
  v_comma       number;
  v_column_name varchar2(2000);
  v_syex_primarykey ot_syex_primarykey := new ot_syex_primarykey();
    
begin

 -- GET TABLE  
    v_table   := pa_orcas_sqlplus_model_holder.get_table();
    
 -- TABLE NAME    
    if v_table_name <> v_table.i_name then
      raise_application_error(-20000, 'Table-Name (Param_1) = ' || v_table_name || 
                                      ' stimmt nicht mit aktueller Tabelle = ' || v_table.i_name || ' ueberein.');
    end if;                                  

-- -- PK NAME
    if v_pk_name is not null and length(v_pk_name) > 30 then
      raise_application_error(-20000, 'PK-Name (Param_2) nicht darf nicht laenger als 30 Zeichen sein');  
    end if;
  

 -- ADD PK TO TABLE   
   v_syex_primarykey.i_consname := v_pk_name;
     
   -- COLUMN LIST
   v_syex_primarykey.i_pk_columns := pa_orcas_sqlplus_utils.get_column_list(v_column_list);
    
 -- ADD reverse
   v_syex_primarykey.i_reverse := pa_orcas_sqlplus_utils.find_reverse(v_physical_parameters);
     
 -- ADD PK TABLESPACE
   v_syex_primarykey.i_tablespace := pa_orcas_sqlplus_utils.find_tablespace(v_physical_parameters);
     
 -- SAVE TABLE  
   v_table.i_primary_key := v_syex_primarykey;
   pa_orcas_sqlplus_model_holder.save_table(v_table);

end;
/

undefine 1
undefine 2
undefine 3
undefine 4
