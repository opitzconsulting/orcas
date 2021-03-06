create table TAB_ALWAYS (  COL NUMBER(15) generated always as identity ( increment by 1 ) not null   )  ;
create table TAB_BY_DEFAULT (  COL NUMBER(15) generated by default as identity ( increment by 1 ) not null   )  ;
create table TAB_BY_DEFAULT_ON_NULL (  COL NUMBER(15) generated by default on null as identity ( increment by 1 ) not null   )  ;
create table TAB_PARAMETER (  COL NUMBER(15) generated always as identity ( increment by 10 maxvalue 1000 minvalue 30 nocycle cache 12 order ) not null   )  ;
alter table TAB_ADD_COLUMN add COL NUMBER(15) generated always as identity ( increment by 1 ) not null;
