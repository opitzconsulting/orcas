alter table TAB_DEL_COL
  drop constraint DEL_FK_MULTI
  drop constraint DEL_FK_SINGLE_DROP_TAB;
drop table AAA_TAB_DROP;
alter table TAB_INS_COL add ( CHG_COL_ADD_NOT_NULL NUMBER(15) , CHG_COL_ADD_DEFAULT NUMBER(15) );
alter table TAB_DEL_COL
  drop constraint DEL_CHECK_MULTI
  drop constraint DEL_UK_MULTI;
alter table TAB_DEL_COL drop (PKCOL,COL_DEFAULT,COL_NOT_NULL,COL_NOT_NULL_DEFAULT);
alter table TAB_DEL_COL_NOCOMBINE drop constraint DEL_CHECK_NOCOMBINE;
alter table TAB_DEL_COL_NOCOMBINE drop (COL_DROP);
alter table TAB_DEL_COL_NOCOMBINE
  add COL_NEW NUMBER(15)
  add constraint COL_NEW_CONS check (col_new in (id,8));
alter table TAB_MAX_COMBINE
  parallel 2
  drop constraint MOD_CHECK
  drop constraint DEL_CHECK
  drop constraint DEL_UK;
alter table TAB_MAX_COMBINE
  add COL_ADD NUMBER(15)
  add constraint ADD_CHECK check (col_add in (col_drop_c,7))
  add constraint MOD_CHECK check (col_drop_c in (id,8)) deferrable initially deferred
  add constraint ADD_UK unique (COL_ADD,COL_DROP_C);
