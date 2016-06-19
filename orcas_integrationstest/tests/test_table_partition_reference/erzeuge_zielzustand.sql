create table tab_b_partition_range
(
  col1 number(15) not null,
  constraint tab_b_pk primary key (col1)
) 
partition by range (col1) 
(
  partition part_10 values less than (10),
  partition part_20 values less than (maxvalue)
);

create table tab_a_partition_ref
(
  col1 number(15) not null,
  col2 number(15) not null,
  constraint ref_part_fk foreign key (col2) references tab_b_partition_range(col1)
) 
partition by reference (ref_part_fk)
(
  partition part_10,
  partition part_20
);

