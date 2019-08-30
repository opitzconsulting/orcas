create table TAB_LOB (  CLOB_COLUMN CLOB, BLOB_COLUMN BLOB, CLOB_COLUMN_NORMAL CLOB   ) lob (CLOB_COLUMN) store as ( tablespace TABLESPACE1 ) lob (BLOB_COLUMN) store as ( tablespace TABLESPACE1 ) tablespace TABLESPACE2  ;
alter table TAB_MOD_TABLESPACE move tablespace TABLESPACE1;
alter table TAB_MOD_TABLESPACE_REVERSE move tablespace TABLESPACE2;
alter table TAB_MOD_TABLESPACE_DEFAULT move tablespace TABLESPACE1;
alter table TAB_MOD_TABLESPACE_REVERSE_DEF move tablespace null;
create table VARRAY_TABLE (  ID NUMBER, COL1 T_VARCHARARRAY   ) varray COL1 store as securefile lob ( tablespace TABLESPACE2 ) tablespace TABLESPACE1  ;
