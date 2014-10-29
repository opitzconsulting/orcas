@@default_include

declare

--API:
----------------------------------------------------------------
  v_table_name                  varchar2(30)   := substr(upper('&1'),1,30);
  v_index_name                  varchar2(30)   := substr(upper('&2'),1,30);
  v_column_list                 varchar2(2000) := substr('&3',1,2000);
  v_uniqueness                  varchar2(30)   := substr(upper('&4'),1,30);
  v_partition_range             varchar2(30)   := substr(upper('&5'),1,30);
  v_domain_index_text           varchar2(2000) := substr(upper('&6'),1,2000);
  v_index_mode                  varchar2(2000) := substr(upper('&7'),1,2000);
  v_physical_parameters         varchar2(150)  := substr(upper('&8'),1,150);
----------------------------------------------------------------  

  v_tablespace                  varchar2(2000);
  v_index                       ot_syex_index;
  v_cl_tab_length               binary_integer;
  v_cl_array                    dbms_utility.lname_array;
  v_indexglobaltype             ot_syex_indexglobaltype;
  v_loggingtype                 ot_syex_loggingtype;
  v_paralleltype                ot_syex_paralleltype;
  v_paralleldegree              binary_integer;
  v_columnref_list              ct_syex_columnref_list;
  v_table                       ot_syex_table;
  v_use_domain_index_text       boolean := true;
  v_bitmap                      varchar2(6);
  
  v_weiter           boolean;
  v_comma            number;  
  v_column_name      varchar2(2000);

  ------------------------------------------  
  
begin
    -- GET TABLE  
    v_table   := pa_orcas_sqlplus_model_holder.get_table();
    
    -- TABLE NAME    
    if v_table_name <> v_table.i_name then
      raise_application_error(-20000, 'Table-Name (Param_1) = ' || v_table_name || 
                                      ' stimmt nicht mit aktueller Tabelle = ' || v_table.i_name || ' ueberein.');
    end if; 
    
     -- INDEX NAME    
    if v_index_name is null then
      raise_application_error(-20000, 'Index-Name (Param_2) nicht angegeben');
    end if;
    
    -- COLUMN LIST    
    if v_column_list is null then
      raise_application_error(-20000, 'Column-List (Param_3) nicht angegeben');
    end if;
    
    -- UNIQUENESS
    if ( nvl(v_uniqueness,'NULL')  not in ('NULL','UNIQUE','NONUNIQUE','BITMAP') ) then
      raise_application_error(-20000, 'Uniqueness falsch (Param_4) ' || v_uniqueness );
    end if;
    
    if v_uniqueness in ('NULL','NONUNIQUE') then
      v_uniqueness := null;
    end if;
    
    if v_uniqueness in ('BITMAP') then
      v_bitmap := v_uniqueness;
      v_uniqueness := null;
    end if;
    
    -- PARTITION RANGE
    if ( nvl(v_partition_range,'NULL')  not in ('NULL','LOCAL','GLOBAL') ) then
      raise_application_error(-20000, 'Partition range falsch (Param_5) ' || v_partition_range );
    end if;
    
    if v_partition_range = 'GLOBAL' then
      v_indexglobaltype := ot_syex_indexglobaltype.c_global;
    elsif v_partition_range in ('NULL','LOCAL') then
      v_indexglobaltype := ot_syex_indexglobaltype.c_local;
    end if;  
    
    -- TABLESPACE
    if v_physical_parameters is not null then
      v_tablespace := pa_orcas_sqlplus_utils.find_tablespace(v_physical_parameters);
    end if;  
    
    -- LOGGINGTYPE   
    if instr(upper(v_domain_index_text), 'NOLOGGING') > 0 then
      v_loggingtype := ot_syex_loggingtype.c_nologging;
      v_use_domain_index_text := false;
    elsif instr(upper(v_domain_index_text), 'LOGGING') > 0 then
      v_loggingtype := ot_syex_loggingtype.c_logging;
      v_use_domain_index_text := false;
    end if;       
    
    -- PARALLELTYPE UND PARALLELDEGREE 
    if instr(upper(v_domain_index_text), 'NOPARALLEL') > 0 then
      v_paralleltype := ot_syex_paralleltype.c_noparallel;
      v_use_domain_index_text := false;
    elsif instr(upper(v_domain_index_text), 'PARALLEL') > 0 then
      v_paralleltype := ot_syex_paralleltype.c_parallel;
      v_use_domain_index_text := false;
      v_domain_index_text := trim(substr(v_domain_index_text, instr(upper(v_domain_index_text), 'PARALLEL')+8));
      if ( instr(v_domain_index_text, ' ') > 0 ) then 
        v_domain_index_text := substr(v_domain_index_text, 0,  instr(v_domain_index_text, ' '));
      end if;  
      if ( length(trim(translate(v_domain_index_text, ' 0123456789', ' '))) is null ) then
        v_paralleldegree := trim(v_domain_index_text);
      end if;
    end if;        
    
    -- CREATE INDEX
    v_index := new ot_syex_index();
    v_index.i_consname := v_index_name;
    v_index.i_unique := v_uniqueness;
    v_index.i_bitmap := v_bitmap;
    v_index.i_global := v_indexglobaltype;
    if (v_use_domain_index_text = true) then
      v_index.i_domain_index_expression := trim (v_domain_index_text);
    end if;  
    v_index.i_logging := v_loggingtype;
    v_index.i_tablespace := v_tablespace;
    v_index.i_parallel := v_paralleltype;
    v_index.i_parallel_degree := v_paralleldegree;
    
    if ( instr(v_column_list,'(') = 1 ) then
   
      -- COLUMN LIST
      v_columnref_list :=  pa_orcas_sqlplus_utils.get_column_list(v_column_list);      
      v_index.i_index_columns := v_columnref_list;   
      
    else
    
      -- FUNCTIONAL INDEX
      v_index.i_function_based_expression := v_column_list;
       
    end if;  
  
    -- ADD INDEX TO TABLE   
    if v_table.i_ind_uks is null then
        v_table.i_ind_uks := new ct_syex_indexoruniquekey_list();
    end if;
    
    v_table.i_ind_uks.extend;
    v_table.i_ind_uks(v_table.i_ind_uks.count) := v_index;
 
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
undefine 8
