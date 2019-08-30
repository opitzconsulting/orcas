create table TAB_PARTITION_HASH (  COL1 NUMBER(15) not null   )  partition by hash (COL1)(partition part_1 tablespace TABLESPACE1,partition part_2);
create table TAB_PARTITION_LIST (  COL1 NUMBER(15) not null   )  partition by list (COL1)(partition part_1 values (1),partition part_2 values (2) tablespace TABLESPACE1,partition part_3 values (default) tablespace TABLESPACE1);
create table TAB_PARTITION_RANGE (  COL1 NUMBER(15) not null   )  partition by range (COL1)(partition part_1 values less than (1),partition part_2 values less than (maxvalue) tablespace TABLESPACE1);
