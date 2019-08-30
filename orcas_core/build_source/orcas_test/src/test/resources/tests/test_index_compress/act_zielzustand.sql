create table tab_index
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null,
  
  index mod_ix (col_add_ix_1,col_add_ix_2) unique compress parallel 4,
  index add_ix (col_add_ix_2) compress parallel 4
);



