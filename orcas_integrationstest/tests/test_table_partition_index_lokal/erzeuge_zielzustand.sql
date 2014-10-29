create table tab_partition_hash
(
  col1 number(15) not null,
  col2 number(15) not null,
  col3 number(15) not null,
  col4 number(15) not null
)
partition by hash (col1) 
(
  partition part_10,
  partition part_20
);

create index idx_local 
  on tab_partition_hash (col1)
  local;

create index idx_local_inner 
  on tab_partition_hash (col4)
  compress local;

create index idx_global 
  on tab_partition_hash (col2)
  compress global;

create index idx_default 
  on tab_partition_hash (col3);


