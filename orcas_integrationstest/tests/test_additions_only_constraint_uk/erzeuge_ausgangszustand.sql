create table tab_old
(
  id1 number(15) not null,
  id2 number(20) not null,
  constraint tab_old_uk unique (id1,id2)
);

create table tab_mod
(
  id1 number(15) not null,
  id2 number(15) not null,
  id3 number(15) not null,
  del_col number(15),
  constraint tab_mod_uk unique (id3,id2),
  constraint tab_mod_uk_old_col unique (del_col),
  constraint tab_mod_uk_old_comb unique (del_col,id1)
);


