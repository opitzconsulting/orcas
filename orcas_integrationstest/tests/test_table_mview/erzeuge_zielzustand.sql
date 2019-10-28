create table tab_view
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null
);

create materialized view mview_add_clob_viewselect as
 select col_add_ix_1, col_add_ix_2, rpad('111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111', 150, '1') as col_3,
rpad('111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111', 150, '1') as col_4,
rpad('111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111', 150, '1') as col_5,
rpad('111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111', 150, '1') as col_6,
rpad('111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111', 150, '1') as col_7,
rpad('111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111', 150, '1') as col_8,
rpad('111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111', 150, '1') as col_9,
rpad('111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111', 150, '1') as col_10,
rpad('111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111', 150, '1') as col_11,
rpad('111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111', 150, '1') as col_12,
rpad('111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111', 150, '1') as col_13,
rpad('111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111', 150, '1') as col_14,
rpad('111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111', 150, '1') as col_15,
rpad('111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111', 150, '1') as col_16,
rpad('111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111', 150, '1') as col_17,
rpad('111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111', 150, '1') as col_18
from tab_view;

create table tab_add_mview
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null
);

create materialized view tab_add_mview on prebuilt table refresh complete on demand enable query rewrite as select col_add_ix_1, col_add_ix_2 from tab_view;

create table tab_mod_mview
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null
);

create materialized view tab_mod_mview on prebuilt table never refresh enable query rewrite as select col_add_ix_1, col_add_ix_2 from tab_view;

create materialized view tab_mod_rewrite never refresh disable query rewrite as select col_add_ix_1, col_add_ix_2 from tab_view;

create materialized view mview_mod_select as select col_add_ix_1, col_add_ix_2 from tab_view;

create materialized view mview_mod_build_mode build immediate as select col_add_ix_1, col_add_ix_2 from tab_view;

create materialized view mview_mod_compress compress for direct_load operations build immediate as select col_add_ix_1, col_add_ix_2 from tab_view;

create materialized view mview_mod_parallel parallel build immediate as select col_add_ix_1, col_add_ix_2 from tab_view;

create materialized view mview_mod_parallel_4 parallel 4 build immediate as select col_add_ix_1, col_add_ix_2 from tab_view;

create materialized view mview_mod_noparallel noparallel build immediate as select col_add_ix_1, col_add_ix_2 from tab_view;

create materialized view mview_mod_tabspace tablespace SYSTEM noparallel build immediate as select col_add_ix_1, col_add_ix_2 from tab_view;

create materialized view mview_mod_tabspace_reverse noparallel build immediate as select col_add_ix_1, col_add_ix_2 from tab_view;


create materialized view mview_add_immediate build immediate refresh complete disable query rewrite as select col_add_ix_1, col_add_ix_2 from tab_view;

create materialized view mview_add_deferred build deferred refresh complete on demand as select col_add_ix_1, col_add_ix_2 from tab_view;

create materialized view mview_add_force build deferred refresh force on demand as select col_add_ix_1, col_add_ix_2 from tab_view;

create materialized view mview_add_compress compress for all operations refresh force on demand as select col_add_ix_1, col_add_ix_2 from tab_view;

create materialized view mview_add_default_tabspace tablespace USERS compress refresh force as select col_add_ix_1, col_add_ix_2 from tab_view;

create materialized view mview_add_domain_tabspace tablespace SYSTEM compress refresh force as select col_add_ix_1, col_add_ix_2 from tab_view;

create table tab_add_mview_with_pk
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null,
  
  primary key(col_add_ix_1)
);
create materialized view log on tab_add_mview_with_pk with primary key;

create table tab_add_mview_without_pk
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null
);
create materialized view log on tab_add_mview_without_pk with rowid;

create materialized view tab_add_mview_with_pk_refresh_pk refresh fast on demand with primary key as select col_add_ix_1, col_add_ix_2 from tab_add_mview_with_pk;

create materialized view tab_add_mview_without_pk_refresh_rowid refresh fast on commit with rowid as select col_add_ix_1, col_add_ix_2 from tab_add_mview_without_pk;

