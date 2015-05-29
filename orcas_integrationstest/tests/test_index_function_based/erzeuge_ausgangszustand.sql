create table tab_index
(
  col_1 number(15) not null,
  col_2 number(15) not null
);

create index mod_1_ix on tab_index (nvl(to_char(col_2),'klEin'));

create table tab_index_inline
(
  col_1 number(15) not null,
  col_2 number(15) not null
);

create index inline_mod_1_ix on tab_index_inline (nvl(to_char(col_2),'klEin'));



