create global temporary table tab_delete_rows
(
  id number(15) not null,
)
on commit delete rows;

create global temporary table tab_preserve_rows
(
  id number(15) not null,
)
on commit preserve rows;

create table tab_permanent
(
  id number(15) not null
);

create global temporary table tab_new_with_index
(
  id number(15) not null,
  index tab_new_with_index_index(id)
)
on commit delete rows;

create table tab_mod_parallel
(
  id number(15) not null
) parallel;

create table tab_mod_parallel_4
(
  id number(15) not null
) parallel 4;

create table tab_mod_noparallel
(
  id number(15) not null
) noparallel;



