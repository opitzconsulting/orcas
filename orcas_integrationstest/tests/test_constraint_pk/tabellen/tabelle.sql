create table tab_new_pk
(
  id number(15) not null,
  constraint tab_new_pk primary key (id),
);

create table tab_wrong_pk_name
(
  id number(15) not null,
  constraint tab_wrong_pk_name primary key (id),
);

create table tab_wrong_pk_reverse
(
  id number(15) not null,
  constraint tab_wrong_pk_reverse primary key (id) using index reverse
);

create table tab_wrong_pk_column
(
  id1 number(15) not null,
  id2 number(15) not null,
  constraint tab_wrong_pk_column primary key (id1),
);

create table tab_wrong_pk_column_order
(
  id1 number(15) not null,
  id2 number(15) not null,
  constraint tab_wrong_pk_column_order primary key (id1,id2),
);

create table tab_noname_pk
(
  id1 number(15) not null,
  primary key (id1)
);


