alter table TAB_MOD_COMPRESS_DL compress;
create table TAB_COMPRESS_ALL (  ID NUMBER(15) not null   ) compress for all operations  ;
create table TAB_COMPRESS_QL (  ID NUMBER(15) not null   ) compress for query low  ;
create table TAB_COMPRESS_QH (  ID NUMBER(15) not null   ) compress for query high  ;
create table TAB_COMPRESS_AL (  ID NUMBER(15) not null   ) compress for archive low  ;
create table TAB_COMPRESS_AH (  ID NUMBER(15) not null   ) compress for archive high  ;
