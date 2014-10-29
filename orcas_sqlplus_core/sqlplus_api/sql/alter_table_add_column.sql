@@default_include

declare

--API:
----------------------------------------------------------------
  v_table_name              varchar2(2000) := upper('&1');
  v_column_name             varchar2(2000) := upper('&2');
  v_column_type_declaration varchar2(30) := substr(upper('&3'),1,30);
  v_mandatory               varchar2(30) := substr(upper('&4'),1,30);
  v_default                 varchar2(2000) := substr('&5',1,2000);
  v_comment                 varchar2(4000) := substr('&6',1,4000);
----------------------------------------------------------------

  v_table              ot_syex_table;
  v_column             ot_syex_column;
  v_byteorchar         ot_syex_chartype;
  v_column_datatype    ot_syex_datatype;

  v_not_null_string    varchar2(30);
  
  v_column_type        varchar2(30);
  v_column_precision   number;
  v_column_scale       number;
 
  v_left_bracket       number;
  v_right_bracket      number;
  v_comma              number;
  v_content            varchar2(100);
  
begin

 -- GET TABLE  
    v_table   := pa_orcas_sqlplus_model_holder.get_table();
    
 -- TABLE NAME    
    if v_table_name <> v_table.i_name then
      raise_application_error(-20000, 'Table-Name (Param_1) = ' || v_table_name || 
                                      ' stimmt nicht mit aktueller Tabelle = ' || v_table.i_name || ' ueberein.');
    end if;                                  
    

 -- COLUMN NAME
    if v_column_name is null then
      raise_application_error(-20000, 'Column-Name (Param_2) nicht angegeben');
    end if;
  
    if length(v_column_name) > 30 then
      raise_application_error(-20000, 'Column-Name (Param_2) nicht darf nicht laenger als 30 Zeichen sein');  
    end if;

 -- COLUMN TYPE    
    if instr(v_column_type_declaration, '(') > 0 then
      v_left_bracket  := instr(v_column_type_declaration, '(');
      v_right_bracket := instr(v_column_type_declaration, ')');
      v_content       := substr(v_column_type_declaration, v_left_bracket + 1, v_right_bracket - v_left_bracket - 1);
      v_column_type   := substr(v_column_type_declaration, 1, v_left_bracket - 1);

      if instr(v_content, 'CHAR') > 0 then
        v_byteorchar := ot_syex_chartype.c_char;
        v_content := substr(v_content, 1, instr(v_content, 'CHAR') - 1);
      elsif instr(v_content, 'BYTE') > 0 then
        v_byteorchar := ot_syex_chartype.c_byte;
        v_content := substr(v_content, 1, instr(v_content, 'BYTE') - 1);
      end if;
    
      if instr(v_column_type_declaration, ',') > 0 then
        v_comma := instr(v_column_type_declaration, ',');
        v_column_precision := to_number(substr(v_column_type_declaration, v_left_bracket + 1, v_comma - v_left_bracket - 1));
        v_column_scale     := to_number(substr(v_column_type_declaration, v_comma + 1, v_right_bracket - v_comma - 1));
      else
        v_column_precision := to_number(v_content);
        v_column_scale     := NULL;
      end if;
      
    else
      v_column_type      := v_column_type_declaration;
      v_column_precision := NULL;
      v_column_scale     := NULL;  
    end if;

        
    if v_column_type = 'NUMBER' then v_column_datatype := ot_syex_datatype.c_number; end if;
    if v_column_type = 'BLOB' then v_column_datatype := ot_syex_datatype.c_blob; end if;
    if v_column_type = 'CLOB' then v_column_datatype := ot_syex_datatype.c_clob; end if;
    if v_column_type = 'NCLOB' then v_column_datatype := ot_syex_datatype.c_nclob; end if;
    if v_column_type = 'VARCHAR2' then v_column_datatype := ot_syex_datatype.c_varchar2; end if;
    if v_column_type = 'NVARCHAR2' then v_column_datatype := ot_syex_datatype.c_nvarchar2; end if;
    if v_column_type = 'CHAR' then v_column_datatype := ot_syex_datatype.c_char; end if;
    if v_column_type = 'DATE' then v_column_datatype := ot_syex_datatype.c_date; end if;
    if v_column_type = 'XMLTYPE' then v_column_datatype := ot_syex_datatype.c_xmltype; end if;
    if v_column_type = 'TIMESTAMP' then v_column_datatype := ot_syex_datatype.c_timestamp; end if;
    if v_column_type = 'ROWID' then v_column_datatype := ot_syex_datatype.c_rowid; end if;
    if v_column_type = 'RAW' then v_column_datatype := ot_syex_datatype.c_raw; end if;
    if v_column_type = 'FLOAT' then v_column_datatype := ot_syex_datatype.c_float; end if;
    if v_column_type = 'LONG' then v_column_datatype := ot_syex_datatype.c_long; end if;
    if v_column_type = 'LONG RAW' then v_column_datatype := ot_syex_datatype.c_long_raw; end if;
     
    
 -- NOT NULL        
    if v_mandatory = 'MANDATORY' then 
        v_not_null_string := 'not';
    end if;

 -- DEFAULT
    if v_column_type = 'VARCHAR2' then 
        if v_default is not null then v_default := '''' || v_default || ''''; end if;
    end if;
 

 -- CREATE NEW COLUMN
    v_column := new ot_syex_column();

    v_column.i_byteorchar := v_byteorchar;
    v_column.i_data_type := v_column_datatype;
    v_column.i_default_value := v_default;
    v_column.i_name := v_column_name;
    v_column.i_notnull := v_not_null_string;
    v_column.i_precision := v_column_precision;
    v_column.i_scale := v_column_scale;
    if v_column_datatype is null then
      v_column.i_object_type := v_column_type; 
    end if;
   
 -- ADD COLUMN TO TABLE   
    if v_table.i_columns is null then
        v_table.i_columns := new ct_syex_column_list();
    end if;
    
    v_table.i_columns.extend;
    v_table.i_columns(v_table.i_columns.count) := v_column;
    
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
