create table TAB_B_PARTITION_RANGE (  COL1 NUMBER(15) not null   )  partition by range (COL1)(partition part_10 values less than (10),partition part_20 values less than (maxvalue));
alter table TAB_B_PARTITION_RANGE add constraint TAB_B_PK primary key (COL1);
create table TAB_A_PARTITION_REF (  COL1 NUMBER(15) not null, COL2 NUMBER(15) not null  , constraint REF_PART_FK foreign key (COL2) references TAB_B_PARTITION_RANGE(COL1) )  partition by reference (ref_part_fk)(partition part_10,partition part_20);
create index REF_PART_FK_GEN_IX on TAB_A_PARTITION_REF ( COL2 ) parallel;
alter index REF_PART_FK_GEN_IX noparallel;
