create table TAB_PARTITION_LIST (  COL1 NUMBER(15) not null   )  partition by list (COL1)(partition part_1 values (10,5),partition part_2 values (7,6),partition part_3 values (default));
