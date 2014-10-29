create table tab_index
(
  col_mod_ix_1 number(15) not null
);

create bitmap index mod_bitmap_ix on tab_index (col_mod_ix_1);


