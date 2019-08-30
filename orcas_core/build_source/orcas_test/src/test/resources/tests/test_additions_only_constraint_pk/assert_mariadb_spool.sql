drop table TAB_OLD;
create table TAB_NEW (  ID NUMERIC(15) not null , primary key (ID)  )  ;
alter table TAB_MOD drop primary key;
alter table TAB_MOD add constraint TAB_MOD_PK primary key (ID2);
