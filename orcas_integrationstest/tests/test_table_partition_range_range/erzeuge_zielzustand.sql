create table tab_partition_range_range
(
  col1 number(15) not null,
  col2 number(15) not null,
  col3 number(15) not null,
  col4 number(15) not null
)
partition by range (col1,col2) 
subpartition by range (col3,col4)
(
  partition part_10_5 values less than (10,5)
  ( 
    subpartition part_10_5__3_4 values less than (3,4),
    subpartition part_10_5__3_7 values less than (3,7),
    subpartition part_10_5__5_m values less than (5,maxvalue),
    subpartition part_10_5__m_m values less than (maxvalue,maxvalue)
  ),
  partition part_m_m values less than (maxvalue,maxvalue)
  ( 
    subpartition part_m_m__3_4 values less than (3,4),
    subpartition part_m_m__3_7 values less than (3,7),
    subpartition part_m_m__5_m values less than (5,maxvalue),
    subpartition part_m_m__m_m values less than (maxvalue,maxvalue)
  )
);


