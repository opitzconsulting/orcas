create table tab_partition_ref_master
(
  id   number(15) not null,
  col1 number(15) not null,
  constraint tab_master_pk primary key (id)
)
partition by hash (col1) 
(
  partition part_master_10,
  partition part_master_20
);

create table tab_partition_ref_detail
(
  id         number(15) not null,
  master_id  number(15) not null,
  col1       number(15) not null,
  constraint tab_detail_pk primary key (id),
  constraint fk_detail foreign key (master_id) references tab_partition_ref_master (id) on delete cascade
)
partition by reference (fk_detail)
(
  partition part_detail_10,
  partition part_detail_20
);

create index fk_detail_gen_ix
   on tab_partition_ref_detail (master_id);

