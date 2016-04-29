create table tab_new_uk
(
  id1 number(15) not null,
  id2 number(20) not null
);

alter table tab_new_uk add constraint tab_new_uk unique (id1,id2);
create table tab_mod_uk
(
  id1 number(15) not null,
  id2 number(20) not null
);

create unique index tab_mod_ix on tab_mod_uk (id2,id1);

alter table tab_mod_uk add constraint tab_mod_uk unique (id1,id2) using index tab_mod_ix;




