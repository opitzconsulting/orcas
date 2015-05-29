create table tab_partition_list_range
(
  col1 number(15) not null,
  col2 number(15) not null,
  col3 number(15) not null
)
partition by list (col1)
subpartition by range (col2,col3) 
(
  partition part_10_5 values (10,5)
  ( 
    subpartition part_10_5__3_4 values less than (3,4),
    subpartition part_10_5__3_7 values less than (3,7),
    subpartition part_10_5__4_m values less than (4,maxvalue)
  ),
  partition part_d values (default)
  ( 
    subpartition part_d__3_4 values less than (3,4),
    subpartition part_d__3_7 values less than (3,7),
    subpartition part_d__4_m values less than (4,maxvalue)
  )
);



