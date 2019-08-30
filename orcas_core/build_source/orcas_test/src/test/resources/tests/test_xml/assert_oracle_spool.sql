create index ADD_XML_IX on XML_TABELLE ( COL_XML_1 ) indextype is CTXSYS.CONTEXT PARAMETERS ('') parallel;
alter index ADD_XML_IX noparallel;
create index ADD_TXT_IX on XML_TABELLE ( COL_XML_1 ) indextype is XDB.XMLINDEX PARAMETERS ('PATH TABLE XML_TABELLE$D') parallel;
alter index ADD_TXT_IX noparallel;
create index INDEX_ADD_XML_IX on XML_TABELLE_INLINE ( COL_XML_1 ) indextype is CTXSYS.CONTEXT PARAMETERS ('') parallel;
alter index INDEX_ADD_XML_IX noparallel;
create index INDEX_ADD_TXT_IX on XML_TABELLE_INLINE ( COL_XML_1 ) indextype is XDB.XMLINDEX PARAMETERS ('PATH TABLE XML_TABELLE_INLINE$D') parallel;
alter index INDEX_ADD_TXT_IX noparallel;
