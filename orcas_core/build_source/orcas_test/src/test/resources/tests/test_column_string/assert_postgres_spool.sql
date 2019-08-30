create table TAB_CREATE_COLUMN_STRING (  "string as name" NUMERIC(15)   )  ;
alter table TAB_MOD_COLUMN_STRING drop column STRING_AS_NAME;
alter table TAB_MOD_COLUMN_STRING add "string as name" NUMERIC(15);
