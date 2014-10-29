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

create materialized view tab_add_mview prebuilt refresh_complete on demand enable query rewrite as "select col_add_ix_1, col_add_ix_2 from tab_view";
     
create table tab_mod_mview
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null
); 

create materialized view tab_mod_mview prebuilt never_refresh enable query rewrite as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view tab_mod_rewrite never_refresh disable query rewrite as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_mod_select as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_mod_build_mode immediate as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_mod_compress compress for direct_load_operations immediate as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_mod_parallel parallel immediate as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_mod_parallel_4 parallel 4 immediate as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_mod_noparallel noparallel immediate as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_mod_tabspace tablespace SYSTEM noparallel immediate as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_mod_tabspace_reverse tablespace USERS noparallel immediate as "select col_add_ix_1, col_add_ix_2 from tab_view";


create materialized view mview_add_immediate immediate refresh_complete disable query rewrite as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_add_deferred deferred refresh_complete on demand as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_add_force deferred refresh_force on demand as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_add_compress compress for all_operations refresh_force on demand as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_add_default_tabspace tablespace USERS compress refresh_force as "select col_add_ix_1, col_add_ix_2 from tab_view";

create materialized view mview_add_domain_tabspace tablespace SYSTEM compress refresh_force as "select col_add_ix_1, col_add_ix_2 from tab_view";








