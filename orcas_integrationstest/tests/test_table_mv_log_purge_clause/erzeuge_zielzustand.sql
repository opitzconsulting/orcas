create table tab_mod_new
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
);

create materialized view log on tab_mod_new with primary key purge immediate asynchronous;

create table tab_mod_immediate
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
);

create materialized view log on tab_mod_immediate with primary key purge immediate;

create table tab_mod_asynchronous
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
);

create materialized view log on tab_mod_asynchronous with primary key purge immediate asynchronous;

create table tab_mod_sw_new
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
);

create materialized view log on tab_mod_sw_new with primary key purge start with to_date('31.10.14','dd.mm.yy');

create table tab_mod_sw
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
);

create materialized view log on tab_mod_sw with primary key purge start with to_date('31.10.15','dd.mm.yy');

create table tab_mod_nsw_new
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
);

create materialized view log on tab_mod_nsw_new with primary key purge start with to_date('31.10.14','dd.mm.yy') next to_date('30.11.14','dd.mm.yy');


create table tab_mod_nsw
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
);

create materialized view log on tab_mod_nsw with primary key purge start with to_date('31.10.14','dd.mm.yy') next to_date('30.11.15','dd.mm.yy');

create table tab_mod_int_new
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
);

create materialized view log on tab_mod_int_new with primary key purge start with to_date('31.10.14','dd.mm.yy') repeat interval 3;

create table tab_mod_int
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
);

create materialized view log on tab_mod_int with primary key purge start with to_date('31.10.14','dd.mm.yy') repeat interval 2;
