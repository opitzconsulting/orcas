create table tab_add_column
(
  c_col1 number(10)
);

create table tab_change_column
(
  c_col1 number(10),
  c_col2 varchar2(41) generated always as (TO_CHAR(C_COL1)||'X') virtual
);

create table tab_change_make_not_virtual
(
  c_col1 number(10),
  c_col2 varchar2(41) generated always as (TO_CHAR(C_COL1)||'X') virtual
);

create table tab_change_make_virtual
(
  c_col1 number(10),
  c_col2 varchar2(41)
);

create table tab_change_virtual_with_index
(
  c_col1 number(10) not null,
  c_col2 varchar2(41) generated always as (TO_CHAR(C_COL1)||'X') virtual not null
);
create index tab_change_virtual_with_index_ix on tab_change_virtual_with_index (c_col2);
