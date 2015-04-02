create table tab_always
(
  col number(15) not null
);
create sequence alw_identity_seq;

create table tab_by_default
(
  col number(15) not null
);
create sequence def_identity_seq;

create table tab_by_default_on_null
(
  col number(15) not null
);
create sequence defn_identity_seq;

create table tab_parameter
(
  col number(15) not null
);
create sequence para_identity_seq increment by 10 maxvalue 1000 minvalue 30 nocycle cache 12 order;

create table tab_add_column
(
  other number(15),
  col number(15)  not null
);
create sequence addc_identity_seq;

create table tab_add_identity
(
  col number(15) not null
);
create sequence addi_identity_seq;

create table tab_remove_identity
(
  col number(15) not null
);

create table tab_change_identity
(
  col number(15) not null
);
create sequence chng_identity_seq;


