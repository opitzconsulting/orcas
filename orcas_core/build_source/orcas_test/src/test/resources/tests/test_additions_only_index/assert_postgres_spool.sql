drop table TAB_OLD;
create table TAB_NEW (  COL_1 NUMERIC(15) not null   )  ;
create index TAB_NEW_IX on TAB_NEW ( COL_1 );
alter table TAB_MOD drop column OLD_COL;
alter table TAB_MOD add NEW_COL NUMERIC(15);
create index TAB_MOD_IX_ADD on TAB_MOD ( COL_1 );
create unique index TAB_MOD_IX_ADD_UNIQUE on TAB_MOD ( COL_2 );
create unique index TAB_MOD_IX_NEW_COL_UNIQUE on TAB_MOD ( NEW_COL );
