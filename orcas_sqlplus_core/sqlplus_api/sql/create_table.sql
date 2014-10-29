@@default_include

declare
  
  --API:
  ------------------------------------------------------------------
  v_table_name         varchar2(2000) := upper('&1');
  v_table_alias        varchar2(10)   := substr(upper('&2'),1,10);
  v_permanentness      varchar2(30)   := substr(upper('&3'),1,30);
  v_parameters         varchar2(4000) := substr(upper('&4'),1,4000);
  v_id_attribute       varchar2(60)   := substr(upper('&5'),1,60);
  v_pk_name            varchar2(30)   := substr(upper('&6'),1,30);
  v_tab_comment        varchar2(4000) := substr('&7',1,4000);
  ------------------------------------------------------------------

  v_table              ot_syex_table;
  v_permanentness_type ot_syex_permanentnesstype;
  v_transactioncontrol ot_syex_permanentnesstran;
  v_paralleltype       ot_syex_paralleltype;
  v_paralleldegree     number;
  v_parameter_text     varchar2(4000);
  v_tablespace         varchar2(30);
  v_loggingtype        ot_syex_loggingtype;
 
begin

-- TABLE NAME
  if v_table_name is null then
    raise_application_error(-20000, 'Table-Name (Param_1) nicht angegeben');
  end if;
  
  if length(v_table_name) > 30 then
    raise_application_error(-20000, 'Table-Name (Param_1) nicht darf nicht laenger als 30 Zeichen sein');  
  end if;


-- PERMANENTNESS  
  if v_permanentness is null then
    v_permanentness := 'PERMANENT';
  end if;
  
  if v_permanentness not in ('PERMANENT','TEMPORARY','TEMPORARY_TRANSACTION','ORGANIZATION_INDEX') then
    raise_application_error(-20000, ' Persistenz-Schalter (Param_3) muss auf PERMANENT oder TEMPORARY oder TEMPORARY_TRANSACTION oder ORGANIZATION_INDEX stehen');
  end if;
  
  if v_permanentness = 'PERMANENT' then
      v_permanentness_type := ot_syex_permanentnesstype.c_permanent;
  elsif v_permanentness = 'TEMPORARY' then
    v_permanentness_type := ot_syex_permanentnesstype.c_global_temporary;
    v_transactioncontrol := ot_syex_permanentnesstran.c_on_commit_preserve;
  elsif v_permanentness = 'TEMPORARY_TRANSACTION' then   
    v_permanentness_type := ot_syex_permanentnesstype.c_global_temporary;
    v_transactioncontrol := ot_syex_permanentnesstran.c_on_commit_delete;
  end if;
  
  -- TABLESPACE 
  if instr(upper(v_parameters), 'TABLESPACE') > 0 then
    v_parameter_text := trim(substr(v_parameters, instr(upper(v_parameters), 'TABLESPACE')+10));
    if ( instr(v_parameter_text, ' ') > 0 ) then 
      v_parameter_text := substr(v_parameter_text, 0,  instr(v_parameter_text, ' '));
    end if;  
    v_tablespace := trim(v_parameter_text);
  end if;   
  
  -- LOGGING 
  v_loggingtype := ot_syex_loggingtype.c_logging;
  if instr(upper(v_parameters), 'NOLOGGING') > 0 then
    v_loggingtype := ot_syex_loggingtype.c_nologging;
  end if;        

  -- PARALLELTYPE UND PARALLELDEGREE 
  if instr(upper(v_parameters), 'NOPARALLEL') > 0 then
    v_paralleltype := ot_syex_paralleltype.c_noparallel;
  elsif instr(upper(v_parameters), 'PARALLEL') > 0 then
    v_paralleltype := ot_syex_paralleltype.c_parallel;
    v_parameter_text := trim(substr(v_parameters, instr(upper(v_parameters), 'PARALLEL')+8));
    if ( instr(v_parameter_text, ' ') > 0 ) then 
      v_parameter_text := substr(v_parameter_text, 0,  instr(v_parameter_text, ' '));
    end if;  
    if ( length(trim(translate(v_parameter_text, ' 0123456789', ' '))) is null ) then
      v_paralleldegree := trim(v_parameter_text);
    end if;
  end if;        

  -- CREATE TABLE
  v_table := new ot_syex_table();
  
  v_table.i_name := v_table_name;
  v_table.i_permanentness := v_permanentness_type;
  v_table.i_transactioncontrol := v_transactioncontrol;
  v_table.i_tablespace := v_tablespace;
  v_table.i_logging := v_loggingtype;
  v_table.i_parallel := v_paralleltype;
  v_table.i_parallel_degree := v_paralleldegree;
  
  pa_orcas_sqlplus_model_holder.add_model_element(v_table) ;
  
end;
/

undefine 1
undefine 2
undefine 3
undefine 4
undefine 5
undefine 6
undefine 7
