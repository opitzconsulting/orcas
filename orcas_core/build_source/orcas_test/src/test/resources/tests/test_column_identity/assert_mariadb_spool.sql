create table TAB_ALWAYS (  COL NUMERIC(15) not null AUTO_INCREMENT   )  ;
create table TAB_BY_DEFAULT (  COL NUMERIC(15) not null AUTO_INCREMENT   )  ;
create table TAB_BY_DEFAULT_ON_NULL (  COL NUMERIC(15) not null AUTO_INCREMENT   )  ;
create table TAB_PARAMETER (  COL NUMERIC(15) not null AUTO_INCREMENT   )  ;
alter table TAB_ADD_COLUMN add COL NUMERIC(15) not null AUTO_INCREMENT;
