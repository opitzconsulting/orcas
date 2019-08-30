create table tab_new
(
  col_1 number(15) not null,

  index tab_new_ix (col_1),
);


create table tab_mod
(
  col_1 number(15),
  col_2 number(15),
  col_3 number(15),
  col_4 number(15),
  new_col number(15),

  index tab_mod_ix_add (col_1),
  index tab_mod_ix_add_unique (col_2) unique,
  index tab_mod_ix_new_col_unique (new_col) unique
);




