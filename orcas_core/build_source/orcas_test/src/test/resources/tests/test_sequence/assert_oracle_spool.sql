create table TAB_DUMMY (  COL1 NUMBER(5)   )  ;
create sequence SEQ_TEST_CREATE increment by 20 start with 12;
create sequence SEQ_TEST_DEFAULT;
alter sequence SEQ_TEST_UPDATE increment by 10;
create sequence SEQ_TEST_CYCLE;
alter sequence SEQ_TEST_UPDATE_CYCLE cycle;
create sequence SEQ_TEST_ORDER order;
alter sequence SEQ_TEST_UPDATE_ORDER order;
create sequence SEQ_TEST_MINVALUE minvalue 10;
alter sequence SEQ_TEST_UPDATE_MINVALUE minvalue 10;
create sequence SEQ_TEST_MAXVALUE maxvalue 100;
alter sequence SEQ_TEST_UPDATE_MAXVALUE maxvalue 100;
create sequence SEQ_TEST_CACHE cache 100;
create sequence SEQ_TEST_UPDATE_CACHE cache 50;
create sequence SEQ_TEST_CACHE_NOCACHE nocache ;
create sequence SEQ_TEST_NEGATIVE increment by -1 start with -1 maxvalue -1 minvalue -999;
drop sequence SEQ_DELETE;
drop sequence SEQ_IGNORE;
drop sequence SEQ_TEST_UPDATE_LV;
