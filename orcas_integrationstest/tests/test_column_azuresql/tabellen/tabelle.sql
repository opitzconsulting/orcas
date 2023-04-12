create table tab_create
(
  id number(15) not null
);

create table tab_ins_col
(
  id number(15) not null,
  new_col number(15) default "66" not null
);

create table tab_del_col
(
  id number(15) not null
);

create table tab_mod_cols
(
  col_chg_size          number(14)         not null,
  col_chg_precision     number(15,6)       not null,
  col_chg_type          varchar2(15)       not null,
  col_set_nullable      varchar2(15),
  col_set_not_nullable  varchar2(15)       not null,
  col_set_default       number(15)         default "4",
  col_chg_default       number(15)         default "7",
  col_set_no_default    number(15),
  col_set_byte          varchar2(15)  not null,
  col_set_char          varchar2(15)  not null
);

create table tab_datatypes
(
  col_number_def number,
  col_number_10 number(10),
  col_number_14_3 number(14,3),
  col_blob blob,
  col_clob clob,
  col_varchar2_10 varchar2(10),
  col_varchar2_10b varchar2(10),
  col_varchar2_10c varchar2(10),
  col_char_1 char(1),
  col_char_def char,
  col_date date,
  col_xmltype xmltype,
  col_timestamp_def timestamp,
  col_timestamp_6 timestamp(6),
  col_timestamp_def_timezone timestamp with_time_zone,
  col_timestamp_6_timezone timestamp(6) with_time_zone,
  col_raw raw(10),
  col_float_def float,
  col_with_num_# number,
  col_integer int
);

create table tab_dataype_conversion
(
  col_number_to_varchar2    varchar2(15)   not null,
  col_varchar2_to_number    number(15)     not null,
  col_char_to_varchar2      varchar2 (1)   not null,
  col_varchar2_to_char      char(1)        not null,
  col_clob_to_varchar2      varchar2(15)   not null,
  col_varchar2_to_clob      clob           not null,
);

