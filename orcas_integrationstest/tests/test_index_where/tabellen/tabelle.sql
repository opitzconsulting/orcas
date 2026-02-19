create table tab_index
(
  col_mod_ix_1 number(15) not null,

  index mod_where_ix (col_mod_ix_1) where "col_mod_ix_1 > 12"
);

