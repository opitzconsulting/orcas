create table tab_partition_list_hash
(
  col1 number(15) not null,
  col2 number(15) not null
)
partition by list (col1)
subpartition by hash (col2) 
(
  partition part_10_5 values ("10","5")
  ( 
    subpartition part_10_5__1,
    subpartition part_10_5__2,
    subpartition part_10_5__3
  ),
  partition part_d values (default)
  ( 
    subpartition part_d__1,
    subpartition part_d__2,
    subpartition part_d__3
  )
);




