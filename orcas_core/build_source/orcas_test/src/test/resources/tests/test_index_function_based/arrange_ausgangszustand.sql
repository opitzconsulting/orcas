create table tab_index
(
  col_1 number(15) not null,
  col_2 number(15) not null,

  index mod_1_ix "(nvl(to_char(col_2),'klEin'))"
);

create table tab_index_inline
(
  col_1 number(15) not null,
  col_2 number(15) not null,

  index inline_mod_1_ix "(nvl(to_char(col_2),'klEin'))"
);




