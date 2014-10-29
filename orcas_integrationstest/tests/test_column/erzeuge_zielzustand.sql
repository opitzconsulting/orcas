create table tab_create
(
  id number(15) not null
);

create table tab_ins_col
(
  id number(15) not null,
  new_col number(15) default 66 not null
);

insert into tab_ins_col
       (
       id,
       new_col
       ) 
values (
       5,
       66
       );
commit;

create table tab_del_col
(
  id number(15) not null
);

create table tab_mod_cols
(
  col_chg_precision     number(15,6)       not null,
  col_set_nullable      varchar2(15),
  col_set_not_nullable  varchar2(15)       not null,
  col_set_default       number(15) default 4,
  col_chg_default       number(15) default 7,
  col_set_no_default    number(15),
  col_set_byte          varchar2(15 byte)  not null,
  col_set_char          varchar2(15 char)  not null,
  col_chg_size          number(14)         not null,
  col_chg_type          varchar2(15)       not null
);

create table tab_datatypes
(
  col_number_def number,
  col_number_10 number(10),
  col_number_14_3 number(14,3),
  col_blob blob,
  col_clob clob,
  col_nclob nclob,
  col_varchar2_10 varchar2(10),
  col_varchar2_10b varchar2(10 BYTE),
  col_varchar2_10c varchar2(10 CHAR),
  col_nvarchar2_10 nvarchar2(10),
  col_char_1 char(1),
  col_char_def char,
  col_date date,
  col_xmltype xmltype,
  col_timestamp_def timestamp,
  col_timestamp_6 timestamp(6),
  col_timestamp_9 timestamp(9),
  col_rowid rowid,
  col_raw raw(10),
  col_float_2 float(2),
  col_float_def float,
  col_with_num_# number
);

create table tab_long
(
  col_number number,
  col_long long
);

create table tab_long_raw
(
  col_number number,
  col_long_raw long raw
);


