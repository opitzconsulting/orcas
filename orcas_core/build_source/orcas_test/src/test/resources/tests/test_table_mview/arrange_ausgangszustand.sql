create table tab_view
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null
);

create table tab_mod_mview
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null
);

create materialized view tab_mod_mview prebuilt refresh_complete on demand enable query rewrite as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view tab_mod_rewrite never_refresh enable query rewrite as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_mod_select as "select col_add_ix_1 from tab_view";

create materialized view mview_mod_build_mode deferred as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_mod_compress nocompress immediate as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_mod_parallel immediate as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_mod_parallel_4 parallel 8 immediate as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_mod_noparallel parallel 4 immediate as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_mod_tabspace noparallel  immediate as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_mod_tabspace_reverse tablespace SYSTEM noparallel immediate as "select col_add_ix_1, col_add_ix_2 from tab_view";




