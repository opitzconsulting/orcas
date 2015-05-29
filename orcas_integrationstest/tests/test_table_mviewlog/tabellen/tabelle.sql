create table tab_add_view
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null,
  col_add_ix_3 number(15) not null
) materialized view log including new values with rowid, sequence (col_add_ix_1, col_add_ix_2);

create table tab_mod_view
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null,
  col_add_ix_3 number(15) not null,
  primary key (col_add_ix_1)
) materialized view log excluding new values with primary key, sequence (col_add_ix_2, col_add_ix_3), commit_scn;

create table tab_mod_mview
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null
); 

create materialized view tab_mod_mview prebuilt refresh_fast on commit enable query rewrite as "select col_add_ix_1, col_add_ix_2 from tab_mod_view";

create materialized view mview_add_fast compress for all_operations refresh_fast on demand as "select col_add_ix_1, col_add_ix_2 from tab_mod_view";

create table tab_mod_scn
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null,
  col_add_ix_3 number(15) not null,
  primary key (col_add_ix_1)
) materialized view log with primary key, sequence (col_add_ix_2, col_add_ix_3), commit_scn;

create table tab_mod_newvals
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null,
  col_add_ix_3 number(15) not null,
  primary key (col_add_ix_1)
) materialized view log including new values with primary key, sequence (col_add_ix_2, col_add_ix_3), commit_scn;

create table tab_mod_pk
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null,
  col_add_ix_3 number(15) not null,
  primary key (col_add_ix_1)
) materialized view log with primary key;

create table tab_mod_rowid
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null,
  col_add_ix_3 number(15) not null,
  primary key (col_add_ix_1)
) materialized view log with rowid;

create table tab_mod_seq
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null,
  col_add_ix_3 number(15) not null,
  primary key (col_add_ix_1)
) materialized view log with sequence;

create table tab_mod_parallel_4
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
) materialized view log parallel 4;


create table tab_mod_noparallel
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
) materialized view log noparallel;

create table tab_mod_tablespace
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
) materialized view log tablespace SYSTEM;

create table tab_mod_tablespace_reverse
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
) materialized view log tablespace USERS;



