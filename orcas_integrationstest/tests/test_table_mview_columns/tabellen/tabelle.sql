create table tab_add_mview
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null
);

create materialized view tab_add_mview_v (col1,col2) as "select col_add_ix_1, col_add_ix_2 from tab_add_mview";


