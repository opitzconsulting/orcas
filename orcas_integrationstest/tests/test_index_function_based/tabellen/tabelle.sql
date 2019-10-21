create table tab_index
(
  col_1 number(15) not null,
  col_2 number(15) not null
);

create index add_1_ix on tab_index "NVL(COL_1,1)";
create index add_2_ix on tab_index "COL_1,COL_2,NVL(COL_1,1)";
create index add_3_ix on tab_index "NVL(COL_1,1),COL_2";
create index add_4_ix on tab_index "NVL(TO_CHAR(COL_1),'test')";
create index mod_1_ix on tab_index "NVL(TO_CHAR(COL_2),'klein')";
create index add_column_expression_ix on tab_index "COL_1";

create table tab_index_inline
(
  col_1 number(15) not null,
  col_2 number(15) not null,

  index inline_add_1_ix "NVL(COL_1,1)",
  index inline_add_2_ix "COL_1,COL_2,NVL(COL_1,1)",
  index inline_add_3_ix "NVL(COL_1,1),COL_2",
  index inline_add_4_ix "NVL(TO_CHAR(COL_1),'test')",
  index inline_mod_1_ix "NVL(TO_CHAR(COL_2),'klein')"
);
