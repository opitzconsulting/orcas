alter table TAB_REF drop foreign key FK_WRONG_NAME;
create index FK_TAB_DEL_CAS_TAB_A_ID_GEN_IX on TAB_REF ( TAB_A_ID );
alter table TAB_REF add constraint FK_TAB_DEL_CAS_TAB_A_ID foreign key (TAB_A_ID) references TAB_A(TAB_A_ID) on delete cascade;
