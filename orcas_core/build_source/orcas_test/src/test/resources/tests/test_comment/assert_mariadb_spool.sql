alter table TAB_COMMENT comment 'tablecomment';
create table TAB_UMLAUT_COMMENT (  COL1 NUMERIC(5)   )  ;
alter table TAB_UMLAUT_COMMENT comment 'a_umlaut_is_ä';
alter table TAB_DROP_COL_AND_COMMENT  drop COL2;
create table TAB_COL_STRING_COMMENT (  "col1 string" NUMERIC(5)   )  ;
