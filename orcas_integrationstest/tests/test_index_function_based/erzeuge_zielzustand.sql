create table tab_index
(
  col_1 number(15) not null,
  col_2 number(15) not null
);

create index add_1_ix on tab_index (nvl(col_1,1));
create index add_2_ix on tab_index (col_1,col_2,nvl(col_1,1));
create index add_3_ix on tab_index (nvl(col_1,1),col_2);
create index add_4_ix on tab_index (nvl(to_char(col_1),'test'));
create index mod_1_ix on tab_index (nvl(to_char(col_2),'klein'));
create index add_column_expression_ix on tab_index (col_1);

create table tab_index_inline
(
  col_1 number(15) not null,
  col_2 number(15) not null
);

create index inline_add_1_ix on tab_index_inline (nvl(col_1,1));
create index inline_add_2_ix on tab_index_inline (col_1,col_2,nvl(col_1,1));
create index inline_add_3_ix on tab_index_inline (nvl(col_1,1),col_2);
create index inline_add_4_ix on tab_index_inline (nvl(to_char(col_1),'test'));
create index inline_mod_1_ix on tab_index_inline (nvl(to_char(col_2),'klein'));
create index inline_desc_ix  on tab_index_inline (col_2 desc, col_1 asc);
