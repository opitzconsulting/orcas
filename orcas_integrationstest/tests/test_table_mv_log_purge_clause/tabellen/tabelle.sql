create table tab_mod_new
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
) materialized view log with primary key purge immediate asynchronous;

create table tab_mod_immediate
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
) materialized view log with primary key purge immediate;

create table tab_mod_asynchronous
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
) materialized view log with primary key purge immediate asynchronous;

create table tab_mod_sw_new
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
) materialized view log with primary key purge start with '31.10.14';

create table tab_mod_sw
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
) materialized view log with primary key purge start with '31.10.15';

create table tab_mod_nsw_new
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
) materialized view log with primary key purge start with '31.10.14' next '30.11.14';

create table tab_mod_nsw
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
) materialized view log with primary key purge start with '31.10.14' next '30.11.15';

create table tab_mod_int_new
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
) materialized view log with primary key purge start with '31.10.14' repeat interval 3;

create table tab_mod_int
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
) materialized view log with primary key purge start with '31.10.14' repeat interval 2;
