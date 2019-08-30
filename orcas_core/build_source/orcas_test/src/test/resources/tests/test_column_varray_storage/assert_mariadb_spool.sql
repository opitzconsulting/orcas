create table VARRAY_TABLE_NEW (  ID NUMERIC, COL1 T_VARCHARARRAY   ) varray COL1 store as securefile lob  ;
alter table VARRAY_TABLE_ADD add COL1 T_VARCHARARRAY varray COL1 store as securefile lob ( deduplicate compress high );
