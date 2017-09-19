create table tab_index
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null,
  col_mod_ix_1 number(15) not null,
  col_mod_ix_2 number(15) not null,
  col_mod_ix_3 number(15) not null,
  col_mod_ix_4 number(15) not null,
  col_mod_ix_5 number(15) not null,
  col_mod_ix_6 number(15) not null,
  col_mod_ix_7 number(15) not null,
  col_mod_ix_8 number(15) not null,
  col_mod_ix_9 number(15) not null,
  col_mod_ix_10 number(15) not null,
  col_mod_ix_11 number(15) not null,
  col_mod_ix_12 number(15) not null,
  col_del_ix_1 number(15) not null,
  col_del_ix_2 number(15) not null,

  index add_2col_ix (col_add_ix_1,col_add_ix_2),
  index add_1col_ix (col_add_ix_2),
  index mod_order_ix (col_mod_ix_2,col_mod_ix_1),
  index mod_parallel_ix (col_mod_ix_3) parallel,
  index mod_parallel_4_ix (col_mod_ix_4) parallel 4,  
  index mod_noparallel_ix (col_mod_ix_5) noparallel,
  index mod_unique_ix (col_mod_ix_6) unique,
  index mod_nounique_ix (col_mod_ix_7),
  index mod_logging_ix (col_mod_ix_10) logging,
  index mod_logging_reverse_ix (col_mod_ix_11) nologging,
  index mod_rename (col_mod_ix_12)
);



