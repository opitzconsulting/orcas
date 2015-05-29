@@default_include

declare

--API:
-----------------------------------------------------------------------
  v_table_name               varchar2(30)   := substr(upper('&1'),1,30);
  v_partitioning_type        varchar2(100)  := substr(upper('&2'),1,100);
  v_partition_columnlist     varchar2(2000) := substr(upper('&3'),1,2000);
  v_subpartition_columnlist  varchar2(2000) := substr(upper('&4'),1,2000);
  v_partition_interval       varchar2(2000) := substr('&5',1,2000);
-----------------------------------------------------------------------  
  v_table                    ot_syex_table;
  v_rangepartitions          ot_syex_rangepartitions;
  v_listpartitions           ot_syex_listpartitions;
  v_hashpartitions           ot_syex_hashpartitions;
  
  v_rangesubparts            ot_syex_rangesubparts;
  v_listsubparts             ot_syex_listsubparts;
  v_hashsubparts             ot_syex_hashsubparts;
-----------------------------------------------------------------------

begin
  
    v_table   := pa_orcas_sqlplus_model_holder.get_table();
    
    -- PLAUSI CHECKS
    -----------------------------------------------------------------------    
    
    -- TABLE NAME    
    if v_table_name <> v_table.i_name then
      raise_application_error(-20000, 'Table-Name (Param_1) = ' || v_table_name || 
                                      ' stimmt nicht mit aktueller Tabelle = ' || v_table.i_name || ' ueberein.');
    end if;     
    
    -- PARTITIONING TYPE    
    if v_partitioning_type is null then
      raise_application_error(-20000, 'Partitioning type (Param_2) nicht angegeben');
    end if;    
    
    if (  v_partitioning_type  not in ('RANGE', 'LIST', 'HASH', 'RANGE_RANGE', 'RANGE_LIST', 'RANGE_HASH', 'RANGE_INTERVAL', 'LIST_LIST') ) then
      raise_application_error(-20000, 'Partitioning type falsch (Param_2) ' || v_partitioning_type );
    end if;
    
    -- PARTITION COLUMN LIST    
    if v_partition_columnlist is null then
      raise_application_error(-20000, 'Partition column list (Param_3) nicht angegeben');
    end if;    
    
    -- SUBPARTITION COLUMN LIST    
    if (v_partitioning_type in ('RANGE_RANGE', 'RANGE_LIST', 'RANGE_HASH', 'LIST_LIST') and v_subpartition_columnlist is null) then
      raise_application_error(-20000, 
        'Subpartition column list (Param_4) muss gefüllt sein bei partitioning type in (''RANGE_RANGE'',''RANGE_LIST'',''RANGE_HASH'',''LIST_LIST'')');
    end if;   
    
    if (v_partitioning_type in ('RANGE', 'LIST', 'HASH', 'RANGE_INTERVAL') and v_subpartition_columnlist is not null) then
      raise_application_error(-20000, 
        'Subpartition column list (Param_4) darf nicht gefüllt sein bei partitioning type in (''RANGE'', ''LIST'', ''HASH'',''RANGE_INTERVAL'')');
    end if;  
    
    -- PARTITION INTERVALL
    if (v_partitioning_type in ('RANGE_INTERVAL') and v_partition_interval is null) then
      raise_application_error(-20000, 
        'Partition intervall (Param_5) muss gefüllt sein bei partitioning type ''RANGE_INTERVAL''');
    end if;  
    
    -- IMPLEMENTIERUNG
    -----------------------------------------------------------------------   
    -- RANGE PARTITIONING
    if ( v_partitioning_type in ('RANGE', 'RANGE_RANGE', 'RANGE_LIST', 'RANGE_HASH', 'RANGE_INTERVAL') ) then
      
      v_rangepartitions := new ot_syex_rangepartitions();
      v_rangepartitions.i_columns             := pa_orcas_sqlplus_utils.get_column_list(v_partition_columnlist);      
      v_rangepartitions.i_partitionlist       := new ct_syex_rangepartition_list();
      
      -- SUBPARTITIONS
      if ( v_partitioning_type in ('RANGE_RANGE', 'RANGE_LIST', 'RANGE_HASH') ) then 
      
        if ( v_partitioning_type in ('RANGE_RANGE') ) then 
          v_rangesubparts                   := new ot_syex_rangesubparts();
          v_rangesubparts.i_columns         := pa_orcas_sqlplus_utils.get_column_list(v_subpartition_columnlist);
          v_rangepartitions.i_tablesubpart  := v_rangesubparts;         
        end if;         
        
        if ( v_partitioning_type in ('RANGE_LIST') ) then 
          v_listsubparts                    := new ot_syex_listsubparts();
          v_listsubparts.i_column           := new ot_syex_columnref( pa_orcas_sqlplus_utils.entferne_klammern(v_subpartition_columnlist) );
          v_rangepartitions.i_tablesubpart  := v_listsubparts;         
        end if;  
        
        if ( v_partitioning_type in ('RANGE_HASH') ) then 
          v_hashsubparts                    := new ot_syex_hashsubparts(); 
          v_hashsubparts.i_column           := new ot_syex_columnref( pa_orcas_sqlplus_utils.entferne_klammern(v_subpartition_columnlist) );
          v_rangepartitions.i_tablesubpart  := v_hashsubparts;   
        end if;  
        
        v_rangepartitions.i_subpartitionlist  := new ct_syex_rangesubpart_list();
      end if;
      
      -- PARTITION INTERVALL
      if ( v_partitioning_type in ('RANGE_INTERVAL') ) then 
        v_rangepartitions.i_intervalexpression  := v_partition_interval;
      end if;
      
      v_table.i_tablepartitioning := v_rangepartitions;
    end if;
    
    -- LIST PARTITIONING
    if ( v_partitioning_type in ('LIST', 'LIST_LIST') ) then
      
      v_listpartitions := new ot_syex_listpartitions();
      v_listpartitions.i_column         := new ot_syex_columnref( pa_orcas_sqlplus_utils.entferne_klammern(v_partition_columnlist) );      
      v_listpartitions.i_partitionlist  := new ct_syex_listpartition_list();
      
      -- SUBPARTITIONS
      if ( v_partitioning_type in ('LIST_LIST') ) then 
        v_listsubparts                      := new ot_syex_listsubparts();
        v_listsubparts.i_column             := new ot_syex_columnref( pa_orcas_sqlplus_utils.entferne_klammern(v_subpartition_columnlist) );
        v_listpartitions.i_tablesubpart     := v_listsubparts;         
        v_listpartitions.i_subpartitionlist := new ct_syex_listsubpart_list();
      end if;
      
      v_table.i_tablepartitioning := v_listpartitions;
    end if;    
    
    -- HASH PARTITIONING
    if ( v_partitioning_type in ('HASH') ) then
      
      v_hashpartitions := new ot_syex_hashpartitions();
      v_hashpartitions.i_column         := new ot_syex_columnref( pa_orcas_sqlplus_utils.entferne_klammern(v_partition_columnlist) );      
      v_hashpartitions.i_partitionlist  := new ct_syex_hashpartition_list();
        
      v_table.i_tablepartitioning := v_hashpartitions;
    end if;        
    
    pa_orcas_sqlplus_model_holder.save_table(v_table);
end;
/

undefine 1
undefine 2
undefine 3
undefine 4
undefine 5

