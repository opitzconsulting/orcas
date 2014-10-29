create or replace package body pa_orcas_sqlplus_utils is

  function entferne_klammern(v_in_string varchar2)
    return varchar2 is
    v_out_string varchar2(2000);
    v_pos        number;
  begin
    v_out_string := v_in_string;

    if instr(v_out_string, '(') > 0 then
      v_pos := instr(v_out_string, '(');
      v_out_string := substr(v_out_string, v_pos + 1);
    end if;
      
    if instr(v_out_string, ')') > 0 then
      v_pos := instr(v_out_string, ')', -1, 1);
      v_out_string := substr(v_out_string, 1, v_pos - 1);
    end if;
    
    return v_out_string;
  end;
  
  function find_reverse(v_in_string varchar2) return varchar2
  is
    v_out_string varchar2(2000);
    v_pos        number;
  begin
    v_out_string := v_in_string;

    if instr(upper(v_out_string), 'REVERSE') > 0 then
      return 'reverse';
    end if;
    
    return null;
  end;  
  
  function find_tablespace(v_in_string varchar2) return varchar2
  is
    v_out_string varchar2(2000);
    v_pos        number;
  begin
    v_out_string := v_in_string;

    if instr(upper(v_out_string), 'TABLESPACE') > 0 then
      v_pos := instr(upper(v_out_string), 'TABLESPACE');
      return lower(trim(substr(v_out_string, v_pos + 10)));
    end if;
    
    return null;
  end;  
  
  function get_column_list(v_in_string varchar2)
    return ct_syex_columnref_list is
    v_columnref_list ct_syex_columnref_list := new ct_syex_columnref_list();
    v_column_list varchar2(2000) := entferne_klammern(upper(v_in_string));
    v_column_name varchar2(2000);  
    v_comma number;
    v_weiter boolean := true;  
    
  begin
    
    while v_weiter loop
    
      if instr(v_column_list, ',') > 0 then
        v_comma := instr(v_column_list, ',');
        v_column_name := substr(v_column_list, 1, v_comma - 1);
        v_column_list := substr(v_column_list, v_comma + 1);
      else
        v_column_name := v_column_list;
        v_weiter := false;
      end if;   
  
      v_column_name := ltrim(rtrim(v_column_name));    
      v_columnref_list.extend;
      v_columnref_list(v_columnref_list.count) := new ot_syex_columnref( v_column_name );
        
    end loop;
      
    return v_columnref_list;
  end get_column_list;      
  
  function get_list_partition_valuelist(v_in_string varchar2)
    return ct_syex_listpartitionvalu_list is
    v_listpartitionvalu_list ct_syex_listpartitionvalu_list := new ct_syex_listpartitionvalu_list();
    v_listpartitionvalu ot_syex_listpartitionvalu := new ot_syex_listpartitionvalu();
    v_value_list varchar2(2000) := entferne_klammern(v_in_string);
    v_comma number;
    v_weiter boolean := true;

  begin

    while v_weiter loop

      if instr(v_value_list, ',') > 0 then
        v_comma := instr(v_value_list, ',');
        v_listpartitionvalu.i_value := substr(v_value_list, 1, v_comma - 1);
        v_value_list := substr(v_value_list, v_comma + 1);
      else
        v_listpartitionvalu.i_value := v_value_list;
        v_weiter := false;
      end if;

      v_listpartitionvalu_list.extend;
      v_listpartitionvalu_list(v_listpartitionvalu_list.count) := v_listpartitionvalu;

    end loop;

    return v_listpartitionvalu_list;
  end;
  
  function get_range_partition_valuelist(v_in_string varchar2)
    return ct_syex_rangepartitionval_list is
    v_rangepartitionval_list ct_syex_rangepartitionval_list := new ct_syex_rangepartitionval_list();
    v_rangepartitionval ot_syex_rangepartitionval := new ot_syex_rangepartitionval();
    v_value_list varchar2(2000) := entferne_klammern(v_in_string);
    v_comma number;
    v_weiter boolean := true;

  begin

    while v_weiter loop

      if instr(v_value_list, ',') > 0 then
        v_comma := instr(v_value_list, ',');
        v_rangepartitionval.i_value := substr(v_value_list, 1, v_comma - 1);
        v_value_list := substr(v_value_list, v_comma + 1);
      else
        v_rangepartitionval.i_value := v_value_list;
        v_weiter := false;
      end if;

      v_rangepartitionval_list.extend;
      v_rangepartitionval_list(v_rangepartitionval_list.count) := v_rangepartitionval;

    end loop;

    return v_rangepartitionval_list;
  end; 
  
end;
/
