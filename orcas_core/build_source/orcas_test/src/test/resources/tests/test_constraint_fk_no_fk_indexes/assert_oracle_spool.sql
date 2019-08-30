create index FK_TAB_DEL_CAS_TAB_A_ID_GEN_IX on TAB_REF ( TAB_A_ID ) parallel;
alter index FK_TAB_DEL_CAS_TAB_A_ID_GEN_IX noparallel;
alter table TAB_REF rename constraint FK_WRONG_NAME to FK_TAB_DEL_CAS_TAB_A_ID;
