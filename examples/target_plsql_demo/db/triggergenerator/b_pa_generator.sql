create or replace package body pa_generator is
  procedure handle_table( p_syex_table in out nocopy ot_syex_table )
  is
  begin
    if( p_syex_table.i_with_trigger is not null )
    then
      execute immediate 'create or replace trigger ' || p_syex_table.i_alias || '_bri before insert on ' || p_syex_table.i_name || ' for each row 
        begin
          if( :new.' || p_syex_table.i_alias || '_id is null ) 
          then
            :new.' || p_syex_table.i_alias || '_id := 5;
          end if;
        end;
      ';
    end if;
  end;

  procedure run( p_model in ot_syex_model )
  is
    v_syex_table ot_syex_table;
  begin   
    for i in 1..p_model.i_model_elements.count
    loop
      if( p_model.i_model_elements(i) is of (ot_syex_table) )
      then
        v_syex_table := treat( p_model.i_model_elements(i) as ot_syex_table );
        
        handle_table( v_syex_table );
      end if;
    end loop;  
  end; 
end; 
/
