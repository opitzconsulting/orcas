create table tab_new_pk
(
  id number(15) not null
);


create table tab_wrong_pk_name
(
  id number(15) not null,
  constraint tab_old_pk primary key (id)
);

create table tab_wrong_pk_reverse
(
  id number(15) not null,
  constraint tab_wrong_pk_reverse primary key (id)
);

create table tab_wrong_pk_column
(
  id1 number(15) not null,
  id2 number(15) not null,
  constraint tab_wrong_pk_column primary key (id2)
);

create table tab_wrong_pk_column_order
(
  id1 number(15) not null,
  id2 number(15) not null,
  constraint tab_wrong_pk_column_order primary key (id2,id1)
);



