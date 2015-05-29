create table tab_partition_list_list
(
  col1 number(15) not null,
  col2 number(15) not null
)
partition by list (col1)
subpartition by list (col2) 
(
  partition part_10_5 values ("10","5")
  ( 
    subpartition part_10_5__3_4 values ("3","4"),
    subpartition part_10_5__5_7 values ("5","7"),
    subpartition part_10_5__d values (default)
  ),
  partition part_d values (default)
  ( 
    subpartition part_d__3_4 values ("3","4"),
    subpartition part_d__5_7 values ("5","7"),
    subpartition part_d__d values (default)
  )
);




