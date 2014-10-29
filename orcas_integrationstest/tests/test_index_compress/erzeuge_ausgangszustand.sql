create table tab_index
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null
);

create unique index mod_ix on tab_index (col_add_ix_1,col_add_ix_2) parallel 4;