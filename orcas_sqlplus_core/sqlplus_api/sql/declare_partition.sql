@@default_include

declare
--API:
-----------------------------------------------------------------------
  v_table_name              varchar2(30)   := substr(upper('&1'),1,30);
  v_partition_name          varchar2(30)   := substr(upper('&2'),1,30);
  v_partition_values        varchar2(2000) := substr('&3',1,2000);
  v_tablespace_name         varchar2(50)   := substr(upper('&4'),1,50);
-----------------------------------------------------------------------  
  v_table                    ot_syex_table;
  
  v_rangepartitions          ot_syex_rangepartitions;
  v_listpartitions           ot_syex_listpartitions;
  v_hashpartitions           ot_syex_hashpartitions;
  
  v_rangepartition           ot_syex_rangepartition;
  v_listpartition            ot_syex_listpartition;
  v_hashpartition            ot_syex_hashpartition;
  
  v_rangepartitionval        ot_syex_rangepartitionval;
  v_rangepartitionval_list   ct_syex_rangepartitionval_list;
  
  v_listpartitionvalu        ot_syex_listpartitionvalu;
  v_listpartitionvalu_list   ct_syex_listpartitionvalu_list;
  
  v_comma                    number;
  v_maxvalue                 varchar2(2000);
  v_value                    varchar2(2000);
  v_weiter                   boolean := true;
-----------------------------------------------------------------------

begin
    v_table   := pa_orcas_sqlplus_model_holder.get_table();
    
    -- PLAUSI CHECKS
    -----------------------------------------------------------------------        
    
    -- TABLE PARTITIONING
    if ( v_table.i_tablepartitioning is null ) then
      raise_application_error(-20000, 'Tabelle ' || v_table_name || ' ist nicht partitioniert.');
    end if;            
    
    -- TABLE NAME    
    if v_table_name <> v_table.i_name then
      raise_application_error(-20000, 'Table-Name (Param_1) = ' || v_table_name || 
                                      ' stimmt nicht mit aktueller Tabelle = ' || v_table.i_name || ' ueberein.');
    end if;     
    
    -- PARTITION NAME    
    if v_partition_name is null then
      raise_application_error(-20000, 'Partition name (Param_2) nicht angegeben');
    end if;    
    
    -- PARTITION VALUES
    if ( (v_partition_values is null) and (v_table.i_tablepartitioning is not of (ot_syex_hashpartitions)) ) then
      raise_application_error(-20000, 'Partition values (Param_3) nicht angegeben');
    end if; 
    
    -- IMPLEMENTIERUNG
    -----------------------------------------------------------------------   
    if ( v_partition_values is not null ) then
      v_partition_values := pa_orcas_sqlplus_utils.entferne_klammern(v_partition_values);   
    end if;  
    
    -- RANGE PARTITION
    if ( v_table.i_tablepartitioning is of (ot_syex_rangepartitions) ) then
      v_rangepartitions := treat( v_table.i_tablepartitioning as ot_syex_rangepartitions );
      
      -- ADD RANGE PARTITION   
      if v_rangepartitions.i_partitionlist is null then
        v_rangepartitions.i_partitionlist := new ct_syex_rangepartition_list();
      end if;
        
      v_rangepartitionval := new ot_syex_rangepartitionval();
      v_rangepartitionval_list := new ct_syex_rangepartitionval_list();
 
      while v_weiter loop    
            
        if instr(v_partition_values, ',') > 0 then
          v_comma := instr(v_partition_values, ',');
          v_rangepartitionval.i_value := substr(v_partition_values, 1, v_comma - 1);
          v_partition_values := substr(v_partition_values, v_comma + 1);
        else
          v_rangepartitionval.i_value := v_partition_values;
          v_weiter := false;
        end if;
            
        v_rangepartitionval_list.extend;
        v_rangepartitionval_list(v_rangepartitionval_list.count) := v_rangepartitionval;
            
      end loop;     
              
      v_rangepartitions.i_partitionlist.extend;
        
      v_rangepartition := new ot_syex_rangepartition();
      v_rangepartition.i_name  := v_partition_name;
      v_rangepartition.i_value := v_rangepartitionval_list;     
        
      v_rangepartitions.i_partitionlist(v_rangepartitions.i_partitionlist.count) := v_rangepartition;
        
      v_table.i_tablepartitioning := v_rangepartitions;    
    end if;
      
    -- LIST PARTITION
    if ( v_table.i_tablepartitioning is of (ot_syex_listpartitions) ) then
      v_listpartitions := treat( v_table.i_tablepartitioning as ot_syex_listpartitions );
      
      -- ADD LIST PARTITION   
      if v_listpartitions.i_partitionlist is null then
        v_listpartitions.i_partitionlist := new ct_syex_listpartition_list();
      end if;
        
      v_listpartitionvalu := new ot_syex_listpartitionvalu();
      v_listpartitionvalu_list := new ct_syex_listpartitionvalu_list();
 
      while v_weiter loop    
          
        if instr(v_partition_values, ',') > 0 then
          v_comma := instr(v_partition_values, ',');
          v_listpartitionvalu.i_value := substr(v_partition_values, 1, v_comma - 1);
          v_partition_values := substr(v_partition_values, v_comma + 1);
        else
          v_listpartitionvalu.i_value := v_partition_values;
          v_weiter := false;
        end if;
          
        v_listpartitionvalu_list.extend;
        v_listpartitionvalu_list(v_listpartitionvalu_list.count) := v_listpartitionvalu;
          
      end loop;     
              
      v_listpartitions.i_partitionlist.extend;
        
      v_listpartition := new ot_syex_listpartition();
      v_listpartition.i_name  := v_partition_name;
      v_listpartition.i_value := v_listpartitionvalu_list;     
        
      v_listpartitions.i_partitionlist(v_listpartitions.i_partitionlist.count) := v_listpartition;
        
      v_table.i_tablepartitioning := v_listpartitions;    
    end if;
      
    -- HASH PARTITION
    if ( v_table.i_tablepartitioning is of (ot_syex_hashpartitions) ) then
      v_hashpartitions := treat( v_table.i_tablepartitioning as ot_syex_hashpartitions );
      
      -- ADD HASH PARTITION   
      if v_hashpartitions.i_partitionlist is null then
        v_hashpartitions.i_partitionlist := new ct_syex_hashpartition_list();
      end if;
              
      v_hashpartitions.i_partitionlist.extend;
        
      v_hashpartition := new ot_syex_hashpartition();
      v_hashpartition.i_name  := v_partition_name;
        
      v_hashpartitions.i_partitionlist(v_hashpartitions.i_partitionlist.count) := v_hashpartition;
        
      v_table.i_tablepartitioning := v_hashpartitions;    
    end if;      

    pa_orcas_sqlplus_model_holder.save_table(v_table);
end;
/

undefine 1
undefine 2
undefine 3


