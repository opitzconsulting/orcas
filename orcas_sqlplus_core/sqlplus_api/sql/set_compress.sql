@@default_include

declare

  --API:
  ----------------------------------------------------------------
  v_compress                  varchar2(10)   := substr(lower('&1'),1,10);
  ------------------------------------------  
  
  v_table                       ot_syex_table;
  
begin
    -- GET TABLE  
    v_table   := pa_orcas_sqlplus_model_holder.get_table();
    
    -- PLAUSI CHECK
    if ( nvl(v_compress,'NULL')  not in ('compress','nocompress') ) then
      raise_application_error(-20000, 'Compress falsch (Param_1) ' || v_compress );
    end if;
    
    if (v_compress = 'compress') then
      v_table.i_compression := ot_syex_compresstype.c_compress;
    elsif (v_compress = 'nocompress') then
      v_table.i_compression := ot_syex_compresstype.c_nocompress;
    end if;
    
    -- SAVE TABLE  
    pa_orcas_sqlplus_model_holder.save_table(v_table);     
end;
/

undefine 1