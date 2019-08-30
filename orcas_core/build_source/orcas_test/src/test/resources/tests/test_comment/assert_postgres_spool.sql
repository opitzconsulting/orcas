comment on table TAB_COMMENT is 'tablecomment';
comment on column TAB_COL_COMMENT . COL1 is 'columncomment ''';
create table TAB_UMLAUT_COMMENT (  COL1 NUMERIC(5)   )  ;
comment on table TAB_UMLAUT_COMMENT is 'a_umlaut_is_ä';
alter table TAB_DROP_COL_AND_COMMENT drop column COL2;
create table TAB_COL_STRING_COMMENT (  "col1 string" NUMERIC(5)   )  ;
comment on column TAB_COL_STRING_COMMENT . "col1 string" is 'columncomment ''';
