create table tab_partition_hash
(
  col1 number(15) not null
)
partition by hash (col1) 
(
  partition part_10,
  partition part_20
);

