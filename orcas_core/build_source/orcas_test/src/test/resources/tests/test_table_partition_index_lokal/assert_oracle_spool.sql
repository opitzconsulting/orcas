create table TAB_PARTITION_HASH (  COL1 NUMBER(15) not null, COL2 NUMBER(15) not null, COL3 NUMBER(15) not null, COL4 NUMBER(15) not null   )  partition by hash (COL1)(partition part_10,partition part_20);
create index IDX_LOCAL_INNER on TAB_PARTITION_HASH ( COL4 ) local compress parallel;
alter index IDX_LOCAL_INNER noparallel;
create index IDX_LOCAL on TAB_PARTITION_HASH ( COL1 ) local parallel;
alter index IDX_LOCAL noparallel;
create index IDX_GLOBAL on TAB_PARTITION_HASH ( COL2 ) compress parallel;
alter index IDX_GLOBAL noparallel;
create index IDX_DEFAULT on TAB_PARTITION_HASH ( COL3 ) parallel;
alter index IDX_DEFAULT noparallel;
