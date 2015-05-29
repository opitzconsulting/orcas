create table tab_new
(
  id number(15)
);

create bitmap index tab_new_ix on tab_new (id);

