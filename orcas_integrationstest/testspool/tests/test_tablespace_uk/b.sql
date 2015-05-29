create table tab_new
(
  id number(15),
  constraint tab_new_uk unique (id) using index tablespace &1
);

