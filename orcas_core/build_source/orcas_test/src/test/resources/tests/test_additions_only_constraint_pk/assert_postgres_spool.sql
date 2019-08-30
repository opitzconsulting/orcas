drop table TAB_OLD;
create table TAB_NEW (  ID NUMERIC(15) not null   )  ;
alter table TAB_NEW add constraint TAB_NEW_PK primary key (ID);
alter table TAB_MOD drop constraint TAB_MOD_PK;
alter table TAB_MOD add constraint TAB_MOD_PK primary key (ID2);
