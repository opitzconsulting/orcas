create table tab_new1
(
  id number(15),
  constraint tab_new_pk1 primary key (id)
);

create table tab_new2
(
  id number(15),
  constraint tab_new_pk2 primary key (id)
);

create table tab_new_detail
(
  id number(15),
  constraint tab_new_detail_fk foreign key (id) references tab_new2(id)
);



