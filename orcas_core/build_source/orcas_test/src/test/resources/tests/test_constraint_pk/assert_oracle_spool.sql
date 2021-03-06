alter table TAB_NEW_PK add constraint TAB_NEW_PK primary key (ID);
alter table TAB_WRONG_PK_NAME rename constraint TAB_OLD_PK to WRONG_PK_NAME;
alter index TAB_OLD_PK rename to WRONG_PK_NAME;
alter table TAB_WRONG_PK_REVERSE drop constraint TAB_WRONG_PK_REVERSE;
alter table TAB_WRONG_PK_REVERSE add constraint TAB_WRONG_PK_REVERSE primary key (ID) using index reverse;
alter table TAB_WRONG_PK_COLUMN drop constraint TAB_WRONG_PK_COLUMN;
alter table TAB_WRONG_PK_COLUMN add constraint TAB_WRONG_PK_COLUMN primary key (ID1);
alter table TAB_WRONG_PK_COLUMN_ORDER drop constraint TAB_WRONG_PK_COLUMN_ORDER;
alter table TAB_WRONG_PK_COLUMN_ORDER add constraint TAB_WRONG_PK_COLUMN_ORDER primary key (ID1,ID2);
create table TAB_NONAME_PK (  ID1 NUMBER(15) not null   )  ;
alter table TAB_NONAME_PK add primary key (ID1);
create table TAB_CREATE_TAB_NAMED_PK (  ID1 NUMBER(15) not null   )  ;
alter table TAB_CREATE_TAB_NAMED_PK add constraint PK_NAME primary key (ID1);
create table TAB_NEW_PK_STRING (  "id string" NUMBER(15) not null   )  ;
alter table TAB_NEW_PK_STRING add constraint TAB_NEW_PK_STRING primary key ("id string");
