alter table TAB_MOD_COMPRESS compress;
alter table TAB_MOD_COMPRESS_DL compress;
create table TAB_COMPRESS_ALL (  ID NUMBER(15) not null   ) compress for all operations  ;
