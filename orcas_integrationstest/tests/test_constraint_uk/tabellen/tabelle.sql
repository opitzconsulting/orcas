create table tab_new_uk
(
  id1 number(15) not null,
  id2 number(20) not null,
  constraint tab_new_uk unique (id1,id2),
);

create table tab_wrong_uk_name
(
  id number(15) not null,
  constraint tab_wrong_uk_name unique (id),
);

create table tab_wrong_uk_columns
(
  id1 number(15) not null,
  id2 number(15) not null,
  id3 number(15) not null,
  constraint tab_wrong_uk_column unique (id1,id2),
);

create table tab_wrong_uk_column_order
(
  id1 number(15) not null,
  id2 number(15) not null,
  constraint tab_wrong_uk_column_order unique (id1,id2),
);
