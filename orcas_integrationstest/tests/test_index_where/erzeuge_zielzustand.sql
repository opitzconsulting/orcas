create table tab_index
(
  col_mod_ix_1 numeric(15) not null
);

create index mod_where_ix on tab_index (col_mod_ix_1) where col_mod_ix_1 > 12;


