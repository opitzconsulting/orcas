create table tab_new
(
  id1 number(15) not null,
  id2 number(20) not null,
  constraint tab_new_uk unique (id1,id2)
);

create table tab_mod
(
  id1 number(15) not null,
  id2 number(15) not null,
  id3 number(15) not null,
  new_col number(15),
  constraint tab_mod_uk unique (id1,id2),
  constraint tab_mod_add_uk unique (id3),
  constraint tab_mod_uk_new_col unique (new_col),
  constraint tab_mod_uk_new_comb unique (new_col,id3)
);

