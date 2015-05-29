create table tab_always
(
  col number(15) generated always as identity
);

create table tab_by_default
(
  col number(15) generated by default as identity
);

create table tab_by_default_on_null
(
  col number(15) generated by default on null as identity
);

create table tab_parameter
(
  col number(15) generated always as identity (increment by 10 maxvalue 1000 minvalue 30 nocycle cache 12 order)
);

create table tab_add_column
(
  other number(15),
  col number(15) generated always as identity not null
);

create table tab_add_identity
(
  col number(15) generated always as identity not null
);

create table tab_remove_identity
(
  col number(15) not null
);

create table tab_change_identity
(
  col number(15) generated always as identity not null
);


