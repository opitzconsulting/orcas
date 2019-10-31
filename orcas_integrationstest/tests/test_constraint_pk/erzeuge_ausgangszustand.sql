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

create table tab_old_disable
(
	id				number(9) not null,
	constraint tab_old_disable_cc primary key (id) enable
);

create table tab_old_enable
(
	id				number(9) not null,
	constraint tab_old_enable_cc primary key (id) disable
);

create table tab_noname_pk_drop
(
  id1 number(15) not null,
  primary key (id1)
);

