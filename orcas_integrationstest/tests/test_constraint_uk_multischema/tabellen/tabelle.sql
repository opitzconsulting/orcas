create table a.tab_new_uk
(
  id1 number(15) not null,
  id2 number(20) not null,
  index b.tab_new_ix (id1,id2) unique,
  constraint tab_new_uk unique (id1,id2) using index b.tab_new_ix
);
