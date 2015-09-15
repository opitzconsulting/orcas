create or replace package body pa_char_column is
  procedure handle_table( p_syex_table in out nocopy ot_syex_table )
  is
    v_syex_column ot_syex_column;
  begin
   for i in 1..p_syex_table.i_columns.count
    loop
      v_syex_column := p_syex_table.i_columns(i);

      if( ot_syex_datatype.is_equal( v_syex_column.i_data_type, ot_syex_datatype.c_varchar2 ) = 1)
      then
        v_syex_column.i_byteorchar := ot_syex_chartype.c_char;
      end if;

       p_syex_table.i_columns(i) := v_syex_column;
    end loop;
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


