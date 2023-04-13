create table tab_index
(
  col_add_ix_1 numeric(15) not null,
  col_add_ix_2 numeric(15) not null,
  col_mod_ix_1 numeric(15) not null,
  col_mod_ix_2 numeric(15) not null,
  col_mod_ix_3 numeric(15) not null,
  col_mod_ix_4 numeric(15) not null,
  col_mod_ix_5 numeric(15) not null,
  col_mod_ix_6 numeric(15) not null,
  col_mod_ix_7 numeric(15) not null,
  col_mod_ix_8 numeric(15) not null,
  col_mod_ix_9 numeric(15) not null,
  col_mod_ix_10 numeric(15) not null,
  col_mod_ix_11 numeric(15) not null,
  col_mod_ix_12 numeric(15) not null,
  col_del_ix_1 numeric(15) not null,
  col_del_ix_2 numeric(15) not null
);

create index del_2col_ix on tab_index (col_del_ix_1,col_del_ix_2);
create index del_1col_ix on tab_index (col_del_ix_2);
create index mod_order_ix on tab_index (col_mod_ix_1,col_mod_ix_2);
create index mod_unique_ix on tab_index (col_mod_ix_6) ;
create unique index mod_nounique_ix on tab_index (col_mod_ix_7);
create index mod_rename_old on tab_index (col_mod_ix_12);



