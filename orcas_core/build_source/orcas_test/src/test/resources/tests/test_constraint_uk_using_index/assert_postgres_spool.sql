alter table TAB_NEW_UK drop constraint TAB_NEW_UK;
create unique index TAB_NEW_IX on TAB_NEW_UK ( ID1,ID2 );
alter table TAB_NEW_UK add constraint TAB_NEW_UK unique (ID1,ID2) using index TAB_NEW_IX;
alter table TAB_MOD_UK drop constraint TAB_MOD_UK;
drop index TAB_MOD_IX;
create unique index TAB_MOD_IX on TAB_MOD_UK ( ID1,ID2 );
alter table TAB_MOD_UK add constraint TAB_MOD_UK unique (ID1,ID2) using index TAB_MOD_IX;
