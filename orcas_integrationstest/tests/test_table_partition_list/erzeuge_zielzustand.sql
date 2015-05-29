create table tab_partition_list
(
  col1 number(15) not null
)
partition by list (col1) 
(
  partition part_1 values (10,5),
  partition part_2 values (7,6),
  partition part_3 values (default)
);

