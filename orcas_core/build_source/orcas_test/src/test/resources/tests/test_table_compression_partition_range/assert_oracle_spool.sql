alter table TAB_PART_MOD_COMPRESS_DL compress;
create table TAB_PART_COMPRESS_ALL (  COL1 NUMBER(15) not null, COL2 NUMBER(15) not null   ) compress for all operations  partition by range (COL1,COL2)(partition part_10 values less than (10,5),partition part_20 values less than (maxvalue,maxvalue));
