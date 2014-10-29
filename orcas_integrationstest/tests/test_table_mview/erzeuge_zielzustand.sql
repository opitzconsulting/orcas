create table tab_view
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null
);

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
