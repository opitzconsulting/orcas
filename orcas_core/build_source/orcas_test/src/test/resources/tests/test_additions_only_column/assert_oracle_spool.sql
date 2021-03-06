-- ignore-additionsonly: drop table TAB_DEL;
create table TAB_NEW (  ID NUMBER(15) not null, COL_DEFAULT NUMBER(15) default 66, COL_NOT_NULL NUMBER(15) not null, COL_NOT_NULL_DEFAULT NUMBER(15) default 66 not null   )  ;
-- ignore-additionsonly: alter table TAB_INS_COL modify ( CHG_COL_ADD_NOT_NULL not null );
alter table TAB_INS_COL modify ( CHG_COL_ADD_DEFAULT default 33 );
alter table TAB_INS_COL modify ( CHG_COL_ADD_DEFAULT_NOT_NULL default 44 );
-- ignore-additionsonly: alter table TAB_INS_COL modify ( CHG_COL_ADD_DEFAULT_NOT_NULL not null );
-- ignore-additionsonly: alter table TAB_INS_COL modify ( CHG_COL_DEL_DEFAULT default null );
alter table TAB_INS_COL modify ( CHG_COL_DEL_NOT_NULL null );
alter table TAB_INS_COL modify ( CHG_COL_INC_PRECISION NUMBER(15));
alter table TAB_INS_COL modify ( CHG_COL_INC_SCALE NUMBER(15,10));
alter table TAB_INS_COL add NEW_COL_DEFAULT NUMBER(15) default 11;
-- ignore-additionsonly: alter table TAB_INS_COL add NEW_COL_NOT_NULL NUMBER(15) not null;
alter table TAB_INS_COL add NEW_COL_NOT_NULL NUMBER(15) -- additionsonly-alternative;
alter table TAB_INS_COL add NEW_COL_NOT_NULL_DEFAULT NUMBER(15) default 22 not null;
-- ignore-additionsonly: alter table TAB_DEL_COL drop (COL_DEFAULT);
-- ignore-additionsonly: alter table TAB_DEL_COL drop (COL_NOT_NULL);
alter table TAB_DEL_COL modify ( COL_NOT_NULL null ) -- additionsonly-alternative;
-- ignore-additionsonly: alter table TAB_DEL_COL drop (COL_NOT_NULL_DEFAULT);
