create or replace package body pa_domain_reverse_extension is
  procedure handle_table( p_syex_table in out nocopy ot_syex_table )
  is
    v_syex_columnref ot_syex_columnref := new ot_syex_columnref();
    v_syex_column ot_syex_column;
  begin
    for i in 1..p_syex_table.i_columns.count
    loop
      v_syex_column := p_syex_table.i_columns(i);
      if( v_syex_column.i_name = p_syex_table.i_primary_key.i_pk_columns(1).i_column_name )
      then
        v_syex_column.i_domain := ot_syex_enumcolumndomain.c_pk_column;
        v_syex_column.i_data_type := null;
        v_syex_column.i_precision := null;        
        v_syex_column.i_scale := null;                
        v_syex_column.i_notnull := null;        
        p_syex_table.i_columns(i) := v_syex_column;
      end if;
    end loop;
    
    p_syex_table.i_primary_key := null;
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


