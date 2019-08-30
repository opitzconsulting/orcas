create table TAB_INDEX (  COL_ADD_IX_1 NUMERIC(15) not null, COL_ADD_IX_2 NUMERIC(15) not null   )  ;
create unique index ADD_IX on TAB_INDEX ( COL_ADD_IX_1,COL_ADD_IX_2 ) nologging compress parallel  4;
