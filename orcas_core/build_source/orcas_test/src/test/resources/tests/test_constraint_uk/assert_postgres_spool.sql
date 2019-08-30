alter table TAB_WRONG_UK_NAME drop constraint TAB_WRONG_UK_NAME_WRONG;
alter table TAB_WRONG_UK_NAME add constraint TAB_WRONG_UK_NAME unique (ID);
alter table TAB_WRONG_UK_COLUMNS drop constraint TAB_WRONG_UK_COLUMN;
alter table TAB_WRONG_UK_COLUMNS add constraint TAB_WRONG_UK_COLUMN unique (ID1,ID2);
alter table TAB_WRONG_UK_COLUMN_ORDER drop constraint TAB_WRONG_UK_COLUMN_ORDER;
alter table TAB_WRONG_UK_COLUMN_ORDER add constraint TAB_WRONG_UK_COLUMN_ORDER unique (ID1,ID2);
create table TAB_NEW_UK_STRING (  "id1 as string" NUMERIC(15) not null   )  ;
alter table TAB_NEW_UK_STRING add constraint TAB_NEW_UK_STRING unique ("id1 as string");
