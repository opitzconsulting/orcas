create table tab_new_uk
(
  id1 number(15) not null,
  id2 number(20) not null,
  index tab_new_ix unique (id1,id2),
  constraint tab_new_uk unique (id1,id2) using index tab_new_ix
);
