create table TAB_A (  TAB_A_ID NUMERIC(9) not null, TAB_A_A_ID NUMERIC(8)   )  ;
alter table TAB_A add constraint PK_TAB_A primary key (TAB_A_ID);
create index FK_RECURSIVE_GEN_IX on TAB_A ( TAB_A_A_ID );
alter table TAB_A add constraint FK_RECURSIVE foreign key (TAB_A_A_ID) references TAB_A(TAB_A_ID);
