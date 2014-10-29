@@default_include

declare

--API:
----------------------------------------------------------------
  v_table_name          varchar2(30)   := substr(upper('&1'),1,30);
  v_uk_name             varchar2(30)   := substr(upper('&2'),1,30);
  v_column_list         varchar2(2000) := substr(upper('&3'),1,2000);
  v_tablespace_name     varchar2(2000) := substr(upper('&4'),1,2000);
----------------------------------------------------------------  
  v_table               ot_syex_table;
  v_unique_key          ot_syex_uniquekey;
  v_columnref_list      ct_syex_columnref_list;
  v_tablespace          varchar2(2000);
----------------------------------------------------------------  
begin
  
    v_table   := pa_orcas_sqlplus_model_holder.get_table();
    
    -- TABLE NAME    
    if v_table_name <> v_table.i_name then
      raise_application_error(-20000, 'Table-Name (Param_1) = ' || v_table_name || 
                                      ' stimmt nicht mit aktueller Tabelle = ' || v_table.i_name || ' ueberein.');
    end if;     
    
     -- UNIQUE KEY NAME    
    if v_uk_name is null then
      raise_application_error(-20000, 'Unique Key-Name (Param_2) nicht angegeben');
    end if;
    
    -- COLUMN LIST    
    if v_column_list is null then
      raise_application_error(-20000, 'Column-List (Param_3) nicht angegeben');
    end if;    
    
    -- TABLESPACE
    if v_tablespace_name is not null then
      v_tablespace := v_tablespace_name;
    end if;  
    
    -- COLUMN LIST
    v_columnref_list :=  pa_orcas_sqlplus_utils.get_column_list(v_column_list);      
   
    -- add unique key  
    v_unique_key := new ot_syex_uniquekey();
    v_unique_key.i_consname := v_uk_name;
    v_unique_key.i_tablespace := v_tablespace;
    v_unique_key.i_uk_columns := v_columnref_list;
    
    -- ADD UK TO TABLE   
    if v_table.i_ind_uks is null then
        v_table.i_ind_uks := new ct_syex_indexoruniquekey_list();
    end if;
    
    v_table.i_ind_uks.extend;
    v_table.i_ind_uks(v_table.i_ind_uks.count) := v_unique_key;
    
    pa_orcas_sqlplus_model_holder.save_table(v_table);
end;
/

undefine 1
undefine 2
undefine 3
undefine 4
