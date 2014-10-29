create table tab_new
(
  id number(15),
  constraint tab_new_pk primary key (id)
);

create table tab_new_detail
(
  id number(15),
  constraint tab_new_detail_fk foreign key (id) references tab_new(id) on delete cascade
);


