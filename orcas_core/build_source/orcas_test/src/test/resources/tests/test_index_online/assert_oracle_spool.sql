create table TAB_INDEX (  COL_ADD_IX_1 NUMBER(15) not null   )  ;
create index COL_ADD_IX_1_IX on TAB_INDEX ( COL_ADD_IX_1 ) parallel online;
alter index COL_ADD_IX_1_IX noparallel;
