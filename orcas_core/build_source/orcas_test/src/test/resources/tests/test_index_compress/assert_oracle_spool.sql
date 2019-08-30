create unique index MOD_IX on TAB_INDEX ( COL_ADD_IX_1,COL_ADD_IX_2 ) compress parallel  4;
create index ADD_IX on TAB_INDEX ( COL_ADD_IX_2 ) compress parallel  4;
