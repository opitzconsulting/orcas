create index ADD_CLOB_IX on TEXT_TABELLE ( COL_CLOB_1 ) indextype is CTXSYS.CONTEXT PARAMETERS ('') parallel;
alter index ADD_CLOB_IX noparallel;
create index INLINE_ADD_CLOB_IX on TEXT_TABELLE_INLINE ( COL_CLOB_1 ) indextype is CTXSYS.CONTEXT PARAMETERS ('') parallel;
alter index INLINE_ADD_CLOB_IX noparallel;
