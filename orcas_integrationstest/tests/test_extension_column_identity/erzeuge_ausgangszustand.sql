create table tab_add_column
(
  other number(15)
);

create table tab_add_identity
(
  col number(15) not null
);

create table tab_remove_identity
(
  col number(15) not null
);
create sequence remi_identity_seq;

create table tab_change_identity
(
  col number(15) not null
);
create sequence chng_identity_seq;
