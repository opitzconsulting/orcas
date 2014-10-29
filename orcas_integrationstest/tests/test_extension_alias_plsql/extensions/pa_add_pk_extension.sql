create or replace package body pa_add_pk_extension is
  procedure handle_table( p_syex_table in out nocopy ot_syex_table )
  is
    v_syex_column ot_syex_column := new ot_syex_column();
    v_syex_columnref ot_syex_columnref := new ot_syex_columnref();
    v_syex_primarykey ot_syex_primarykey := new ot_syex_primarykey();
  begin
    v_syex_column.i_name := p_syex_table.i_alias || '_id';
    v_syex_column.i_data_type := ot_syex_datatype.c_number;
    v_syex_column.i_precision := 22;
    v_syex_column.i_notnull := 'not';
  
    p_syex_table.i_columns.extend;
    p_syex_table.i_columns(p_syex_table.i_columns.count) := v_syex_column;
    
    v_syex_columnref.i_column_name := v_syex_column.i_name;

    v_syex_primarykey.i_pk_columns := new ct_syex_columnref_list( v_syex_columnref );
    v_syex_primarykey.i_consname := p_syex_table.i_alias || '_pk';
    p_syex_table.i_primary_key := v_syex_primarykey;
  end;

  function run( p_input in ot_syex_model ) return ot_syex_model
  is
    v_input ot_syex_model := p_input;
    v_syex_table ot_syex_table;
  begin   
    for i in 1..v_input.i_model_elements.count
    loop
      if( v_input.i_model_elements(i) is of (ot_syex_table) )
      then
        v_syex_table := treat( v_input.i_model_elements(i) as ot_syex_table );
        
        handle_table( v_syex_table );
        
        v_input.i_model_elements(i) := v_syex_table;
      end if;
    end loop;
  
    return v_input;
  end;
end;
/
