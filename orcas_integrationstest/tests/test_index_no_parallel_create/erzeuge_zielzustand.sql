create table tab_index
(
  col_mod_ix_3 number(15) not null,
  col_mod_ix_4 number(15) not null
);

create index mod_parallel_ix on tab_index (col_mod_ix_3) parallel;
create index mod_noparallel_ix on tab_index (col_mod_ix_4) noparallel;

create table tab_index_inline
(
  col_mod_ix_3 number(15) not null,
  col_mod_ix_4 number(15) not null
);

create index inline_mod_parallel_ix on tab_index_inline (col_mod_ix_3) parallel;
create index inline_mod_noparallel_ix on tab_index_inline (col_mod_ix_4) noparallel;



