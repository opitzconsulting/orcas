create table TAB_PARTITION_RANGE_LIST (  COL1 NUMBER(15) not null, COL2 NUMBER(15) not null, COL3 NUMBER(15) not null   )  partition by range (COL1,COL2) subpartition by list (COL3)(partition part_10_5 values less than (10,5)(subpartition part_10_5__3_4 values (3,4),subpartition part_10_5__5_7 values (5,7),subpartition part_10_5__d values (default)),partition part_m_m values less than (maxvalue,maxvalue)(subpartition part_m_m__3_4 values (3,4),subpartition part_m_m__5_7 values (5,7),subpartition part_m_m__d values (default)));