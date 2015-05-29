begin
    for del_tables in (select object_name from user_objects where object_type='TABLE' and not(&1))
    loop
        pa_orcas_clean.clean_table(del_tables.object_name);
    end loop;
end;
/
