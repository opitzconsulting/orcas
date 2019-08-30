create table tab_ins_col
(
  id number(15) not null
);

create table tab_del_col
(
  id number(15) not null,
  new_col number(15) not null
);

create table tab_drop_me
(
  id number(15) not null
);

create table tab_mod_cols
(
  col_chg_size          number(15)         not null,
  col_chg_precision     number(15)         not null,
  col_chg_type          number(15)         not null,
  col_set_nullable      varchar2(15)       not null,
  col_set_not_nullable  varchar2(15),
  col_set_default       number(15),
  col_chg_default       number(15) default "6",
  col_set_no_default    number(15) default "5",
  col_set_byte          varchar2(15 CHAR)  not null,
  col_set_char          varchar2(15 BYTE)  not null
);


create table tab_dataype_conversion
(
  col_number_to_varchar2    number(15)     not null,
  col_varchar2_to_number    varchar2(15)   not null,
  col_char_to_varchar2      char(1)        not null,
  col_varchar2_to_char      varchar2(1)    not null,
  col_clob_to_varchar2      clob           not null,
  col_varchar2_to_clob      varchar2(15)   not null,
  col_clob_to_xml           xmltype        not null,
  col_xml_to_clob           clob           not null
);

