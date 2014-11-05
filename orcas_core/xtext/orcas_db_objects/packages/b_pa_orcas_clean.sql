CREATE OR REPLACE package body pa_orcas_clean is

procedure clean_table( p_table_name in varchar2 )
is
  v_table_name varchar2(100) := upper( trim( p_table_name ) );
begin
  for cur_constraint in
    (
    select fk.constraint_name,
           fk.table_name
      from user_constraints fk,
           user_constraints ref_pk
    where fk.constraint_type = 'R'
      and fk.r_constraint_name = ref_pk.constraint_name
      and ref_pk.table_name = v_table_name
    )
  loop
    execute immediate 'alter table '||cur_constraint.table_name||' drop constraint '||cur_constraint.constraint_name;
  end loop;

  for cur_constraint in
    (
    select table_name,
           constraint_name
      from user_constraints
     where constraint_type != 'P'
       and generated != 'GENERATED NAME'
       and table_name = v_table_name
    )
  loop
    execute immediate 'alter table '||cur_constraint.table_name||' drop constraint '||cur_constraint.constraint_name;
  end loop;

  for cur_constraint in
    (
    select table_name,
           constraint_name
      from user_constraints
     where constraint_type = 'P'
       and table_name = v_table_name
    )
  loop
    execute immediate 'alter table '||cur_constraint.table_name||' drop constraint '||cur_constraint.constraint_name;
  end loop;

  for cur_index in
    (
    select index_name
      from user_indexes
     where generated != 'Y'
       and table_name = v_table_name
    )
  loop
    execute immediate 'drop index '||cur_index.index_name;
  end loop;

  for cur_column in
    (
    select column_name,
           table_name
      from user_tab_columns
     where nullable = 'N'
       and table_name in
           (
           select table_name
             from user_tables
           )
       and table_name = v_table_name
    )
  loop
    execute immediate 'alter table '||cur_column.table_name||' modify( '||cur_column.column_name||' null )';
  end loop;

  for cur_constraint in
    (
    select table_name,
           constraint_name
      from user_constraints
     where constraint_type != 'P'
  --     and generated != 'GENERATED NAME'
       and table_name = v_table_name
    )
  loop
    execute immediate 'alter table '||cur_constraint.table_name||' drop constraint '||cur_constraint.constraint_name;
  end loop;

/*  for cur_column in
    (
    select column_name,
           table_name
      from user_tab_columns
     where data_default is not null
       and table_name in
           (
           select table_name
             from user_tables
           )
       and table_name = v_table_name
    )
  loop
    execute immediate 'alter table '||cur_column.table_name||' modify( '||cur_column.column_name||' default null )';
  end loop;*/

  for cur_trigger in
    (
    select trigger_name
      from user_triggers
     where table_name = v_table_name
    )
  loop
    execute immediate 'drop trigger '||cur_trigger.trigger_name;
  end loop;
end;

procedure clean_all_tables
is
begin
  for cur_table in
    (
    select table_name
      from user_tables
     where table_name not like '%$%'
     order by table_name
    )
  loop
    clean_table( cur_table.table_name );
  end loop;
end;


end;
/
