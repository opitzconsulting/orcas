create table tab_partition_range_hash
(
  col1 number(15) not null,
  col2 number(15) not null,
  col3 number(15) not null
)
partition by range (col1,col2) 
subpartition by hash (col3)
(
  partition part_10_5 values less than ("10","5")
  ( 
    subpartition part_10_5__1,
    subpartition part_10_5__2
  ),
  partition part_m_m values less than (maxvalue,maxvalue)
  ( 
    subpartition part_m_m__1,
    subpartition part_m_m__2
  )
);


