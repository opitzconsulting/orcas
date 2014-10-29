create global temporary table tab_delete_rows
(
  id number(15) not null
)
on commit delete rows;

create global temporary table tab_preserve_rows
(
  id number(15) not null
)
on commit preserve rows;

create table tab_permanent
(
  id number(15) not null
);

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

insert into tab_mod_parallel (id) values (5);
insert into tab_mod_parallel_4 (id) values (5);
insert into tab_mod_noparallel (id) values (5);

commit;

