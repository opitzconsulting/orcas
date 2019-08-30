create table tab_partition_hash
(
  col1 number(15) not null
)
partition by hash (col1) 
(
  partition part_1 tablespace TABLESPACE1,
  partition part_2
);

create table tab_partition_list
(
  col1 number(15) not null
)
partition by list (col1) 
(
  partition part_1 values ("1"),
  partition part_2 values ("2") tablespace TABLESPACE1,
  partition part_3 values (default) tablespace TABLESPACE1
);

create table tab_partition_range
(
  col1 number(15) not null
)
partition by range (col1) 
(
  partition part_1 values less than ("1"),
  partition part_2 values less than (maxvalue) tablespace TABLESPACE1
);


