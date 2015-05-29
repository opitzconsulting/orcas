create table tab_new
(
  id number(15)
);

create index tab_new_ix on tab_new (id)  parallel 2;


