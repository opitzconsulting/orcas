@@default_include

declare
--API:
-----------------------------------------------------------------------
  v_table_name                    varchar2(30)   := substr(upper('&1'),1,30);
  v_subpart_template_name         varchar2(30)   := substr(upper('&2'),1,30);
  v_subpart_template_columnlist   varchar2(2000) := substr('&3',1,2000);  
  v_tablespace_name               varchar2(50)   := substr(upper('&4'),1,50);
-----------------------------------------------------------------------    
  v_table                    ot_syex_table;
  
  v_rangepartitions          ot_syex_rangepartitions;
  v_listpartitions           ot_syex_listpartitions;
  
  v_rangepartition           ot_syex_rangepartition;
  v_listpartition            ot_syex_listpartition;
  
  v_rangepartitionval        ot_syex_rangepartitionval;
  v_rangepartitionval_list   ct_syex_rangepartitionval_list;
  
  v_listpartitionvalu_list   ct_syex_listpartitionvalu_list;
  
  v_rangesubpartitionlist    ct_syex_rangesubpart_list;
  v_rangesubpart             ot_syex_rangesubpart;
  v_rangesubsubpart          ot_syex_rangesubsubpart;
  
  v_listsubpartitionlist     ct_syex_listsubpart_list;
  v_listsubpart              ot_syex_listsubpart;
  v_listsubsubpart           ot_syex_listsubsubpart;
  
  v_hashsubsubpart           ot_syex_hashsubsubpart;
  
  v_subsubpart_list          ct_syex_subsubpart_list;
  
  v_comma                    number;
  v_value                    varchar2(2000);
  v_weiter                   boolean := true;
-----------------------------------------------------------------------

begin
    v_table   := pa_orcas_sqlplus_model_holder.get_table();
    
    -- TABLE NAME    
    if v_table_name <> v_table.i_name then
      raise_application_error(-20000, 'Table-Name (Param_1) = ' || v_table_name || 
                                      ' stimmt nicht mit aktueller Tabelle = ' || v_table.i_name || ' ueberein.');
    end if; 
    
    -- TABLE PARTITIONING
    if ( v_table.i_tablepartitioning is null ) then
      raise_application_error(-20000, 'Tabelle ' || v_table_name || ' ist nicht partitioniert.');
    end if;         
    
    -- PARTITIONING TYPE
    if ( v_table.i_tablepartitioning is of (ot_syex_hashpartitions) ) then
      raise_application_error(-20000, 'Partitioning type der Tabelle ' || v_table_name || ' ist HASH.');
    end if;     
    
    -- IMPLEMENTIERUNG
    -----------------------------------------------------------------------
    
    v_subpart_template_columnlist := pa_orcas_sqlplus_utils.entferne_klammern(v_subpart_template_columnlist);   
    
    -- RANGE PARTITION
    if ( v_table.i_tablepartitioning is of (ot_syex_rangepartitions) ) then
      v_rangepartitions := treat( v_table.i_tablepartitioning as ot_syex_rangepartitions );
      
      -- TABLE PARTITIONING
      if ( v_rangepartitions.i_tablesubpart is null ) then
        raise_application_error(-20000, 'Tabelle ' || v_table_name || ' ist nicht subpartitioniert.');
      end if;         
     
      -- Nur RANGE_RANGE, RANGE_HASH und RANGE_LIST Partitioning wird unterstützt
      if ( v_rangepartitions.i_tablesubpart is not of (ot_syex_listsubparts) and v_rangepartitions.i_tablesubpart is not of (ot_syex_rangesubparts)
            and v_rangepartitions.i_tablesubpart is not of (ot_syex_hashsubparts) ) then
        raise_application_error(-20000, 'Nur RANGE_RANGE, RANGE_HASH und RANGE_LIST wird unterstützt für RANGE Partionining.');
      end if;     
      
      -- SUBPARTITION TEMPLATE COLUMNLIST: Nur bei RANGE_HASH darf die Columnlist leer sein
      if ( v_subpart_template_columnlist is null and v_rangepartitions.i_tablesubpart is not of (ot_syex_hashsubparts) ) then
        raise_application_error(-20000, 'Subpartition template columnlist (Param_3) nicht angegeben');
      end if; 
      
      if v_rangepartitions.i_partitionlist is null then
          raise_application_error(-20000, 'Subpartionierung ist nicht möglich, da noch keine Partitionen existieren.');     
      end if;
      
      -- ADD SUBPARTITIONLIST   
      if v_rangepartitions.i_subpartitionlist is null then
        v_rangesubpartitionlist := new ct_syex_rangesubpart_list();
      else 
        v_rangesubpartitionlist := v_rangepartitions.i_subpartitionlist;
      end if;
      
      -- RANGE_LIST Partitioning
      if v_rangepartitions.i_tablesubpart is of (ot_syex_listsubparts) then  
      
        v_listpartitionvalu_list := pa_orcas_sqlplus_utils.get_list_partition_valuelist(v_subpart_template_columnlist); 
                
        -- LOOP OVER PARTITIONS AND ADD SUBPARTITIONS        
        for i in 1 .. v_rangepartitions.i_partitionlist.count loop
          v_rangepartition := v_rangepartitions.i_partitionlist(i);
            
          if v_rangesubpartitionlist.count < i then
            v_rangesubpartitionlist.extend;
            v_rangesubpart := new ot_syex_rangesubpart();
            v_subsubpart_list := new ct_syex_subsubpart_list();
            v_rangesubpart.i_name  := v_rangepartition.i_name;
            v_rangesubpart.i_value := v_rangepartition.i_value;            
          else
            v_rangesubpart := v_rangesubpartitionlist(i);
              
            if v_rangesubpart.i_subpartlist is null then
              v_subsubpart_list := new ct_syex_subsubpart_list();
            else
              v_subsubpart_list := v_rangesubpart.i_subpartlist;
            end if;           
          end if;
  
          v_subsubpart_list.extend;
          v_listsubsubpart := new ot_syex_listsubsubpart();
          v_listsubsubpart.i_name := v_rangepartition.i_name || '_' || v_subpart_template_name;
          v_listsubsubpart.i_value := v_listpartitionvalu_list;
          if (v_tablespace_name is not null) then
            v_listsubsubpart.i_tablespace := v_tablespace_name;
          end if;          
          v_subsubpart_list(v_subsubpart_list.count) := v_listsubsubpart;                      
            
          v_rangesubpart.i_subpartlist := v_subsubpart_list;
          v_rangesubpartitionlist(i) := v_rangesubpart;       
        end loop;     
      end if;
      
      -- RANGE_HASH Partitioning
      if v_rangepartitions.i_tablesubpart is of (ot_syex_hashsubparts) then        
          
        -- LOOP OVER PARTITIONS AND ADD SUBPARTITIONS        
        for i in 1 .. v_rangepartitions.i_partitionlist.count loop
          v_rangepartition := v_rangepartitions.i_partitionlist(i);
            
          if v_rangesubpartitionlist.count < i then
            v_rangesubpartitionlist.extend;
            v_rangesubpart := new ot_syex_rangesubpart();
            v_subsubpart_list := new ct_syex_subsubpart_list();
            v_rangesubpart.i_name  := v_rangepartition.i_name;
            v_rangesubpart.i_value := v_rangepartition.i_value;            
          else
            v_rangesubpart := v_rangesubpartitionlist(i);
              
            if v_rangesubpart.i_subpartlist is null then
              v_subsubpart_list := new ct_syex_subsubpart_list();
            else
              v_subsubpart_list := v_rangesubpart.i_subpartlist;
            end if;           
          end if;
  
          v_subsubpart_list.extend;
          v_hashsubsubpart := new ot_syex_hashsubsubpart();
          v_hashsubsubpart.i_name := v_rangepartition.i_name || '_' || v_subpart_template_name;
          if (v_tablespace_name is not null) then
            v_hashsubsubpart.i_tablespace := v_tablespace_name;
          end if;
          v_subsubpart_list(v_subsubpart_list.count) := v_hashsubsubpart;                      
            
          v_rangesubpart.i_subpartlist := v_subsubpart_list;
          v_rangesubpartitionlist(i) := v_rangesubpart;       
        end loop;     
      end if;      
      
      -- RANGE_RANGE Partitioning
      if v_rangepartitions.i_tablesubpart is of (ot_syex_rangesubparts) then  
        
        v_rangepartitionval_list := pa_orcas_sqlplus_utils.get_range_partition_valuelist(v_subpart_template_columnlist);
                          
        -- LOOP OVER PARTITIONS AND ADD SUBPARTITIONS        
        for i in 1 .. v_rangepartitions.i_partitionlist.count loop
          v_rangepartition := v_rangepartitions.i_partitionlist(i);
            
          if v_rangesubpartitionlist.count < i then
            v_rangesubpartitionlist.extend;
            v_rangesubpart := new ot_syex_rangesubpart();
            v_subsubpart_list := new ct_syex_subsubpart_list();
            v_rangesubpart.i_name  := v_rangepartition.i_name;
            v_rangesubpart.i_value := v_rangepartition.i_value;            
          else
            v_rangesubpart := v_rangesubpartitionlist(i);
              
            if v_rangesubpart.i_subpartlist is null then
              v_subsubpart_list := new ct_syex_subsubpart_list();
            else
              v_subsubpart_list := v_rangesubpart.i_subpartlist;
            end if;           
          end if;
  
          v_subsubpart_list.extend;
          v_rangesubsubpart := new ot_syex_rangesubsubpart();
          v_rangesubsubpart.i_name := v_rangepartition.i_name || '_' || v_subpart_template_name;
          v_rangesubsubpart.i_value := v_rangepartitionval_list;
          if (v_tablespace_name is not null) then
            v_rangesubsubpart.i_tablespace := v_tablespace_name;
          end if;
          v_subsubpart_list(v_subsubpart_list.count) := v_rangesubsubpart;                      
            
          v_rangesubpart.i_subpartlist := v_subsubpart_list;
          v_rangesubpartitionlist(i) := v_rangesubpart;       
        end loop;                     
      end if;  
      
      v_rangepartitions.i_subpartitionlist := v_rangesubpartitionlist;
      v_table.i_tablepartitioning := v_rangepartitions;    
    end if;
      
    -- LIST_LIST Partitioning
    if ( v_table.i_tablepartitioning is of (ot_syex_listpartitions) ) then
      v_listpartitions := treat( v_table.i_tablepartitioning as ot_syex_listpartitions );
      
      -- TABLE PARTITIONING TYPE
      if ( v_listpartitions.i_tablesubpart is null ) then
        raise_application_error(-20000, 'Tabelle ' || v_table_name || ' ist nicht subpartitioniert.');
      end if;   
      
      -- Nur LIST_LIST Partitioning wird unterstützt
      if ( v_listpartitions.i_tablesubpart is not of (ot_syex_listsubparts) ) then
        raise_application_error(-20000, 'Nur LIST_LIST wird unterstützt für LIST Partionining.');
      end if;   
      
      -- SUBPARTITION TEMPLATE COLUMNLIST: Nur bei RANGE_HASH darf die Columnlist leer sein
      if ( v_subpart_template_columnlist is null ) then
        raise_application_error(-20000, 'Subpartition template columnlist (Param_3) nicht angegeben');
      end if; 
      
      v_listpartitionvalu_list := pa_orcas_sqlplus_utils.get_list_partition_valuelist(v_subpart_template_columnlist);
              
        
      if v_listpartitions.i_partitionlist is null then
        raise_application_error(-20000, 'Subpartionierung ist nicht möglich, da noch keine Partitionen existieren.');     
      else
        -- ADD SUBPARTITIONLIST   
        if v_listpartitions.i_subpartitionlist is null then
          v_listsubpartitionlist := new ct_syex_listsubpart_list();
        else 
          v_listsubpartitionlist := v_listpartitions.i_subpartitionlist;
        end if;
        
        -- LOOP OVER PARTITIONS AND ADD SUBPARTITIONS        
        for i in 1 .. v_listpartitions.i_partitionlist.count loop
          v_listpartition := v_listpartitions.i_partitionlist(i);
          
          if v_listsubpartitionlist.count < i then
            v_listsubpartitionlist.extend;
            v_listsubpart := new ot_syex_listsubpart();
            v_subsubpart_list := new ct_syex_subsubpart_list();
            v_listsubpart.i_name  := v_listpartition.i_name; -- || '_' || v_subpart_template_name;
            v_listsubpart.i_value := v_listpartition.i_value;            
          else
            v_listsubpart := v_listsubpartitionlist(i);
            
            if v_listsubpart.i_subpartlist is null then
              v_subsubpart_list := new ct_syex_subsubpart_list();
            else
              v_subsubpart_list := v_listsubpart.i_subpartlist;
            end if;           
          end if;

          v_subsubpart_list.extend;
          v_listsubsubpart := new ot_syex_listsubsubpart();
          v_listsubsubpart.i_name := v_listpartition.i_name || '_' || v_subpart_template_name;
          v_listsubsubpart.i_value := v_listpartitionvalu_list;
          if (v_tablespace_name is not null) then
            v_listsubsubpart.i_tablespace := v_tablespace_name;
          end if;
          v_subsubpart_list(v_subsubpart_list.count) := v_listsubsubpart;                      
          
          v_listsubpart.i_subpartlist := v_subsubpart_list;
          v_listsubpartitionlist(i) := v_listsubpart;       
        end loop;  
        
        v_listpartitions.i_subpartitionlist := v_listsubpartitionlist;  
      end if;
        
      v_table.i_tablepartitioning := v_listpartitions;    
    end if;

    pa_orcas_sqlplus_model_holder.save_table(v_table);
end;
/

undefine 1
undefine 2
undefine 3
undefine 4

