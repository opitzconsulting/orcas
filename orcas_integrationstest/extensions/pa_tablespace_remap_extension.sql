create or replace package body pa_tablespace_remap_extension is

  function replace_tablespace( p_tablespace in varchar2 ) return varchar2 is
  begin
    if( 'replaceme1' = lower(trim(p_tablespace)) )
    then
      return pa_orcas_extension_parameter.get_extension_parameter_entry('tablespace1');
    elsif ( 'replaceme2' = lower(trim(p_tablespace)) )
    then
      return pa_orcas_extension_parameter.get_extension_parameter_entry('tablespace2');
    else
      return p_tablespace;
    end if;
  end;
  
  procedure handle_table( p_syex_table in out nocopy ot_syex_table )
  is
    v_syex_column ot_syex_column := new ot_syex_column();
    v_syex_columnref ot_syex_columnref := new ot_syex_columnref();
    
    function handle_indexoruniquekey( p_syex_indexoruniquekey ot_syex_indexoruniquekey ) return ot_syex_indexoruniquekey
    is
      v_syex_index ot_syex_index;
      v_syex_uniquekey ot_syex_uniquekey;
    begin
      if( p_syex_indexoruniquekey is of (ot_syex_index) )
      then
        v_syex_index := treat( p_syex_indexoruniquekey as ot_syex_index );
        
        v_syex_index.i_tablespace := replace_tablespace( v_syex_index.i_tablespace );
        
        return v_syex_index;
      end if;
      if( p_syex_indexoruniquekey is of (ot_syex_uniquekey) )
      then
        v_syex_uniquekey := treat( p_syex_indexoruniquekey as ot_syex_uniquekey );
        
        v_syex_uniquekey.i_tablespace := replace_tablespace( v_syex_uniquekey.i_tablespace );
        
        return v_syex_uniquekey;
      end if;
    end;
    
    function handle_tablepartitioning( p_syex_tablepartitioning ot_syex_tablepartitioning ) return ot_syex_tablepartitioning
    is
      v_syex_rangepartitions ot_syex_rangepartitions;
      v_syex_listpartitions ot_syex_listpartitions;
      v_syex_hashpartitions ot_syex_hashpartitions;
      v_syex_refpartitions ot_syex_refpartitions;
    
    begin
      if( p_syex_tablepartitioning is of (ot_syex_rangepartitions) )
      then
        v_syex_rangepartitions := treat( p_syex_tablepartitioning as ot_syex_rangepartitions );
        
        if ( v_syex_rangepartitions.i_partitionlist is not null )
        then
          for i in 1..v_syex_rangepartitions.i_partitionlist.count 
          loop
            v_syex_rangepartitions.i_partitionlist(i).i_tablespace := replace_tablespace(v_syex_rangepartitions.i_partitionlist(i).i_tablespace);
          end loop;
        end if;  
       
        return v_syex_rangepartitions;
      end if;
      
      if( p_syex_tablepartitioning is of (ot_syex_listpartitions) )
      then
        v_syex_listpartitions := treat( p_syex_tablepartitioning as ot_syex_listpartitions );
        
        if ( v_syex_listpartitions.i_partitionlist is not null )
        then
          for i in 1..v_syex_listpartitions.i_partitionlist.count 
          loop
            v_syex_listpartitions.i_partitionlist(i).i_tablespace := replace_tablespace(v_syex_listpartitions.i_partitionlist(i).i_tablespace);
          end loop;
        end if;  
       
        return v_syex_listpartitions;
      end if;
      
      if( p_syex_tablepartitioning is of (ot_syex_hashpartitions) )
      then
        v_syex_hashpartitions := treat( p_syex_tablepartitioning as ot_syex_hashpartitions );
        
        if ( v_syex_hashpartitions.i_partitionlist is not null )
        then
          for i in 1..v_syex_hashpartitions.i_partitionlist.count 
          loop
            v_syex_hashpartitions.i_partitionlist(i).i_tablespace := replace_tablespace(v_syex_hashpartitions.i_partitionlist(i).i_tablespace);
          end loop;
        end if;  
       
        return v_syex_hashpartitions;
      end if;
      
      if( p_syex_tablepartitioning is of (ot_syex_refpartitions) )
      then
        v_syex_refpartitions := treat( p_syex_tablepartitioning as ot_syex_refpartitions );
        
        if ( v_syex_refpartitions.i_partitionlist is not null )
        then
          for i in 1..v_syex_refpartitions.i_partitionlist.count 
          loop
            v_syex_refpartitions.i_partitionlist(i).i_tablespace := replace_tablespace(v_syex_refpartitions.i_partitionlist(i).i_tablespace);
          end loop;
        end if;  
       
        return v_syex_refpartitions;
      end if;     
      
      return p_syex_tablepartitioning;
    end;
    
  begin
    if( p_syex_table.i_primary_key is not null )
    then
      p_syex_table.i_primary_key.i_tablespace := replace_tablespace(p_syex_table.i_primary_key.i_tablespace);
    end if;
    p_syex_table.i_tablespace := replace_tablespace(p_syex_table.i_tablespace);   
    
    if( p_syex_table.i_ind_uks is not null )
    then
      for i in 1..p_syex_table.i_ind_uks.count 
      loop
        p_syex_table.i_ind_uks(i) := handle_indexoruniquekey( p_syex_table.i_ind_uks(i) );
      end loop;
    end if;    
    
    if( p_syex_table.i_lobstorages is not null )
    then
      for i in 1..p_syex_table.i_lobstorages.count 
      loop
        p_syex_table.i_lobstorages(i).i_tablespace := replace_tablespace(p_syex_table.i_lobstorages(i).i_tablespace);
      end loop;
    end if; 
    
    if( p_syex_table.i_tablepartitioning is not null )
    then
      p_syex_table.i_tablepartitioning := handle_tablepartitioning( p_syex_table.i_tablepartitioning );             
    end if;  
      
  end;  

  function run( p_input in ot_syex_model ) return ot_syex_model
  is
    v_input ot_syex_model := p_input;
    v_syex_table ot_syex_table;
    v_syex_indexextable ot_syex_indexextable;    
  begin   
    for i in 1..v_input.i_model_elements.count
    loop
      if( v_input.i_model_elements(i) is of (ot_syex_table) )
      then
        v_syex_table := treat( v_input.i_model_elements(i) as ot_syex_table );
        
        handle_table( v_syex_table );
        
        v_input.i_model_elements(i) := v_syex_table;
      end if;
      if( v_input.i_model_elements(i) is of (ot_syex_indexextable) )
      then
        v_syex_indexextable := treat( v_input.i_model_elements(i) as ot_syex_indexextable );
        
        v_syex_indexextable.i_tablespace := replace_tablespace( v_syex_indexextable.i_tablespace );
        
        v_input.i_model_elements(i) := v_syex_indexextable;
      end if;      
    end loop;
  
    return v_input;
  end;
end;
/
