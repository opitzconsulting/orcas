create table tab_index
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null,
  col_mod_ix_1 number(15) not null,
  col_mod_ix_2 number(15) not null,
  col_mod_ix_3 number(15) not null,
  col_mod_ix_4 number(15) not null,
  col_mod_ix_5 number(15) not null,
  col_mod_ix_6 number(15) not null,
  col_mod_ix_7 number(15) not null,
  col_mod_ix_8 number(15) not null,
  col_mod_ix_9 number(15) not null,
  col_mod_ix_10 number(15) not null,
  col_mod_ix_11 number(15) not null,
  col_mod_ix_12 number(15) not null,
  col_del_ix_1 number(15) not null,
  col_del_ix_2 number(15) not null
);

create index del_2col_ix on tab_index (col_del_ix_1,col_del_ix_2);
create index del_1col_ix on tab_index (col_del_ix_2);
create index mod_order_ix on tab_index (col_mod_ix_1,col_mod_ix_2);
create index mod_parallel_ix on tab_index (col_mod_ix_3) noparallel;
create index mod_parallel_4_ix on tab_index (col_mod_ix_4) parallel;
create index mod_noparallel_ix on tab_index (col_mod_ix_5) parallel;
create index mod_unique_ix on tab_index (col_mod_ix_6) ;
create unique index mod_nounique_ix on tab_index (col_mod_ix_7);
create index mod_logging_ix on tab_index (col_mod_ix_10) nologging;
create index mod_logging_reverse_ix on tab_index (col_mod_ix_11) logging;
create index mod_rename_old on tab_index (col_mod_ix_12);



