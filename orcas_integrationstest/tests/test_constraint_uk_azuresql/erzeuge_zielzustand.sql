create table tab_new_uk
(
  id1 numeric(15) not null,
  id2 numeric(20) not null,
  constraint tab_new_uk_uk unique (id1,id2)
);

create table tab_wrong_uk_name
(
  id numeric(15) not null,
  constraint tab_wrong_uk_name_uk unique(id)
);

create table tab_wrong_uk_columns
(
  id1 numeric(15) not null,
  id2 numeric(15) not null,
  id3 numeric(15) not null,
  constraint tab_wrong_uk_column_uk unique (id1,id2)
);

create table tab_wrong_uk_column_order
(
  id1 numeric(15) not null,
  id2 numeric(15) not null,
  constraint tab_wrong_uk_column_order_uk unique (id1,id2)
);

create table tab_new_uk_string
(
    "id1 as string" numeric(15) not null,
    constraint tab_new_uk_string_uk unique ("id1 as string")
);


