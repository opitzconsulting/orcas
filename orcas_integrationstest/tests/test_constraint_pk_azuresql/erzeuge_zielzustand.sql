create table tab_new_pk
(
  id numeric(15) not null,
  constraint tab_new_pk_pk primary key (id)
);

create table tab_wrong_pk_name
(
  id numeric(15) not null,
  constraint wrong_pk_name primary key (id)
);

create table tab_wrong_pk_column
(
  id1 numeric(15) not null,
  id2 numeric(15) not null,
  constraint tab_wrong_pk_column_pk primary key (id1)
);

create table tab_wrong_pk_column_order
(
  id1 numeric(15) not null,
  id2 numeric(15) not null,
  constraint tab_wrong_pk_column_order_pk primary key (id1,id2)
);

create table tab_noname_pk
(
  id1 numeric(15) not null,
  primary key (id1)
);

create table tab_create_tab_named_pk
(
  id1 numeric(15) not null,
  constraint pk_name primary key (id1)
);

create table tab_new_pk_string
(
    "id string" numeric(15) not null,
    constraint tab_new_pk_string_pk primary key ("id string")
);

create table tab_noname_pk_drop
(
  id1 numeric(15) not null
);
