create or replace package body pa_reverse_22_remove_defaults is
  pv_default_chartype ot_syex_chartype;
  pv_default_tablespace varchar2(100);

  function hanlde_table( p_input in ot_syex_table ) return ot_syex_table
  is
    v_table ot_syex_table;  
    v_index ot_syex_index;
    v_uniquekey ot_syex_uniquekey;
  begin
    v_table := p_input;

    for i in 1..v_table.i_columns.count
    loop
      if( ot_syex_chartype.is_equal( v_table.i_columns(i).i_byteorchar, pv_default_chartype ) = 1 )
      then
        v_table.i_columns(i).i_byteorchar := null;
      end if;
    end loop;
    
    if( v_table.i_primary_key is not null )
    then
      if( v_table.i_primary_key.i_tablespace = pv_default_tablespace )
      then
        v_table.i_primary_key.i_tablespace := null;
      end if;
    end if;
    
    if( v_table.i_ind_uks is not null )
    then
      for i in 1..v_table.i_ind_uks.count
      loop
        if( v_table.i_ind_uks(i) is of (ot_syex_index) ) 
        then
          v_index := treat( v_table.i_ind_uks(i) as ot_syex_index );
          
          if( v_index.i_tablespace = pv_default_tablespace )            
          then
            v_index.i_tablespace := null;
            v_table.i_ind_uks(i) := v_index;
          end if;
        else
          v_uniquekey := treat( v_table.i_ind_uks(i) as ot_syex_uniquekey );        
          
          if( v_uniquekey.i_tablespace = pv_default_tablespace )            
          then
            v_uniquekey.i_tablespace := null;
            v_table.i_ind_uks(i) := v_uniquekey;
          end if;          
        end if;
      end loop;    
    end if;      
    
    if( ot_syex_compresstype.is_equal( v_table.i_compression, ot_syex_compresstype.c_nocompress ) = 1 )
    then
      v_table.i_compression := null;
    end if;
      
    if( ot_syex_paralleltype.is_equal( v_table.i_parallel, ot_syex_paralleltype.c_noparallel ) = 1 )
    then
      v_table.i_parallel := null;
    end if;
      
    return v_table;
  end;

  function run( p_input in ot_syex_model ) return ot_syex_model
  is
    v_input ot_syex_model;  
    
    v_nls_length_default varchar2(100);    
  begin
    select value 
      into v_nls_length_default 
      from nls_instance_parameters 
     where parameter = 'NLS_LENGTH_SEMANTICS';
     
    if( v_nls_length_default = 'BYTE' )
    then
      pv_default_chartype := ot_syex_chartype.c_byte;
    else
      pv_default_chartype := ot_syex_chartype.c_char;
    end if;      
    
    select default_tablespace 
      into pv_default_tablespace
      from user_users;
  
    v_input := p_input;
    
    for i in 1..v_input.i_model_elements.count
    loop
      if( v_input.i_model_elements(i) is of (ot_syex_table) )
      then
        v_input.i_model_elements(i) := hanlde_table( treat( v_input.i_model_elements(i) as ot_syex_table ) );
      end if;
    end loop;
    
    return v_input;
  end;
end;
/
