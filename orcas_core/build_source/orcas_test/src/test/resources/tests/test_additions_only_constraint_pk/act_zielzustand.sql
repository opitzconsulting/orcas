create table tab_new
(
  id number(15) not null,
  constraint tab_new_pk primary key (id)
);

create table tab_mod
(
  id1 number(15) not null,
  id2 number(15) not null,
  constraint tab_mod_pk primary key (id2)
);
