alter table TAB_WRONG_UK_NAME rename constraint TAB_WRONG_UK_NAME_WRONG to TAB_WRONG_UK_NAME;
alter index TAB_WRONG_UK_NAME_WRONG rename to TAB_WRONG_UK_NAME;
alter table TAB_WRONG_UK_COLUMNS drop constraint TAB_WRONG_UK_COLUMN;
alter table TAB_WRONG_UK_COLUMNS add constraint TAB_WRONG_UK_COLUMN unique (ID1,ID2);
alter table TAB_WRONG_UK_COLUMN_ORDER drop constraint TAB_WRONG_UK_COLUMN_ORDER;
alter table TAB_WRONG_UK_COLUMN_ORDER add constraint TAB_WRONG_UK_COLUMN_ORDER unique (ID1,ID2);
create table TAB_NEW_UK_STRING (  "id1 as string" NUMBER(15) not null   )  ;
alter table TAB_NEW_UK_STRING add constraint TAB_NEW_UK_STRING unique ("id1 as string");
