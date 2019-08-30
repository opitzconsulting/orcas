create table TAB_PARTITION_HASH (  COL1 NUMBER(15) not null   )  partition by hash (COL1)(partition part_10,partition part_20);
