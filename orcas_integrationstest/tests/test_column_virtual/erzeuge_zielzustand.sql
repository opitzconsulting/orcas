create table tab_virtual_column
(   
  c_col1 number(10),
  c_col2 number(10),
  c_col3 varchar2(100) generated always as (TO_CHAR(C_COL1)||' '||TO_CHAR(C_COL2)) virtual
);

create table tab_add_column
(
  c_col1 number(10),
  c_col2 varchar2(41) generated always as (TO_CHAR(C_COL1)||'X') virtual
);

create table tab_change_column
(
  c_col1 number(10),
  c_col2 varchar2(41) generated always as (TO_CHAR(C_COL1)||'Y') virtual
);

create table tab_change_make_not_virtual
(
  c_col1 number(10),
  c_col2 varchar2(41)
);

create table tab_change_make_virtual
(
  c_col1 number(10),
  c_col2 varchar2(41) generated always as (TO_CHAR(C_COL1)||'Y') virtual
);


