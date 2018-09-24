spool build/drop_replaceables.sql

    select concat( 'drop view ', table_name, ';' )
      from information_schema.views
     where table_schema = database()
    union
    select concat( 'drop trigger ', trigger_name, ';' )
      from information_schema.triggers
     where trigger_schema = database()
    union
    select concat( 'drop function ', routine_name, ';' )
      from information_schema.routines
     where routine_schema = database()
       and routine_type = 'FUNCTION'
    union
    select concat( 'drop procedure ', routine_name, ';' )
      from information_schema.routines
     where routine_schema = database()
       and routine_type = 'PROCEDURE';

spool off

@build/drop_replaceables.sql

