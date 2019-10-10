create table tab_new_uk
(
  id1 number(15) not null,
  id2 number(20) not null,
  constraint tab_new_uk unique (id1,id2)
);

create table tab_wrong_uk_name
(
  id number(15) not null,
  constraint tab_wrong_uk_name unique(id)
);

create table tab_wrong_uk_columns
(
  id1 number(15) not null,
  id2 number(15) not null,
  id3 number(15) not null,
  constraint tab_wrong_uk_column unique (id1,id2)
);

create table tab_wrong_uk_column_order
(
  id1 number(15) not null,
  id2 number(15) not null,
  constraint tab_wrong_uk_column_order unique (id1,id2)
);

create table tab_new_uk_string
(
    "id1 as string" number(15) not null,
    constraint tab_new_uk_string unique ("id1 as string")
);

create table tab_new_disable
(
	id				number(9) not null,
	constraint tab_new_disable_cc unique (id) disable
);

create table tab_old_disable
(
	id				number(9) not null,
	constraint tab_old_disable_cc unique (id) disable
);

create table tab_old_enable
(
	id				number(9) not null,
	constraint tab_old_enable_cc unique (id) enable
);

