create table tab_new_uk
(
  id1 number(15) not null,
  id2 number(20) not null
);

create unique index tab_new_ix on tab_new_uk (id1,id2);

alter table tab_new_uk add constraint tab_new_uk unique (id1,id2) using index tab_new_ix;


