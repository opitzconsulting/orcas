create table tab_new
(
  id number(15),
  constraint tab_new_pk primary key (id)
);

create table tab_new_detail
(
  id1 number(15),
  id2 number(15),
  constraint tab_new_detail_fk foreign key (id2) references tab_new(id)
);



