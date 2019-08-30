create table TAB_PARTITION_RANGE (  COL1 NUMBER(15) not null, COL2 NUMBER(15) not null   )  partition by range (COL1,COL2)(partition part_10 values less than (10,5),partition part_20 values less than (maxvalue,maxvalue));
create bitmap index ADD_BITMAP_IX on TAB_PARTITION_RANGE ( COL1 ) local parallel;
alter index ADD_BITMAP_IX noparallel;
