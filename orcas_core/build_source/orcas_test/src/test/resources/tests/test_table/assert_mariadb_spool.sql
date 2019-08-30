drop table TAB_DELETE_ROWS;
drop table TAB_PRESERVE_ROWS;
drop table TAB_PERMANENT;
create global temporary table TAB_DELETE_ROWS (  ID NUMERIC(15) not null   )  ;
create global temporary table TAB_PRESERVE_ROWS (  ID NUMERIC(15) not null   ) on commit  preserve rows nocache  ;
create table TAB_PERMANENT (  ID NUMERIC(15) not null   )  ;
alter table TAB_MOD_PARALLEL parallel;
alter table TAB_MOD_PARALLEL_4 parallel 4;
alter table TAB_MOD_NOPARALLEL noparallel;
