create table tab_new_uk
(
  id1 number(15) not null,
  id2 number(20) not null,
  index tab_new_ix (id1,id2) unique,
  constraint tab_new_uk unique (id1,id2) using index tab_new_ix
);

create table tab_mod_uk
(
  id1 number(15) not null,
  id2 number(20) not null,
  index tab_mod_ix (id1,id2) unique,
  constraint tab_mod_uk unique (id1,id2) using index tab_mod_ix
);

