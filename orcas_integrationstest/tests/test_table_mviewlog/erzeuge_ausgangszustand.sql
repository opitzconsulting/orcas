create table tab_mod_view
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null,
  col_add_ix_3 number(15) not null,
  primary key (col_add_ix_1)
);

create materialized view log on tab_mod_view with primary key, sequence(col_add_ix_2), commit scn;

create table tab_mod_mview
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null
); 

create materialized view tab_mod_mview on prebuilt table refresh fast on commit enable query rewrite as select col_add_ix_1, col_add_ix_2 from tab_mod_view;

create materialized view mview_add_fast compress for all operations refresh fast on demand as select col_add_ix_1, col_add_ix_2 from tab_mod_view;

create table tab_mod_scn
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null,
  col_add_ix_3 number(15) not null,
  primary key (col_add_ix_1)
);

create materialized view log on tab_mod_scn with primary key, sequence(col_add_ix_2, col_add_ix_3);

create table tab_mod_newvals
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null,
  col_add_ix_3 number(15) not null,
  primary key (col_add_ix_1)
);

create materialized view log on tab_mod_newvals with primary key, sequence(col_add_ix_2, col_add_ix_3), commit scn excluding new values;

create table tab_mod_pk
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null,
  col_add_ix_3 number(15) not null,
  primary key (col_add_ix_1)
);

create materialized view log on tab_mod_pk with rowid;

create table tab_mod_rowid
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null,
  col_add_ix_3 number(15) not null,
  primary key (col_add_ix_1)
);

create materialized view log on tab_mod_rowid;

create table tab_mod_seq
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null,
  col_add_ix_3 number(15) not null,
  primary key (col_add_ix_1)
);

create materialized view log on tab_mod_seq;

create table tab_mod_parallel_4
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
);

create materialized view log on tab_mod_parallel_4 parallel 8;

create table tab_mod_noparallel
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
);

create materialized view log on tab_mod_noparallel parallel 4;

create table tab_mod_tablespace
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
);

create materialized view log on tab_mod_tablespace tablespace USERS;

create table tab_mod_tablespace_reverse
(
  col_add_ix_1 number(15) not null,
  primary key (col_add_ix_1)
);

create materialized view log on tab_mod_tablespace_reverse tablespace SYSTEM;




