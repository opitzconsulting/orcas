create table tab_virtual_column
(   
  c_col1 numeric(10),
  c_col2 numeric(10),
  c_col3 as (CONVERT(VARCHAR(10),C_COL1)+' '+CONVERT(VARCHAR(10),C_COL2))
);

create table tab_add_column
(
  c_col1 numeric(10),
  c_col2 as (CONVERT(VARCHAR(10),C_COL1)+'X')
);

create table tab_change_column
(
  c_col1 numeric(10),
  c_col2 as (CONVERT(VARCHAR(10),C_COL1)+'Y')
);

create table tab_change_make_not_virtual
(
  c_col1 numeric(10),
  c_col2 varchar(41)
);

create table tab_change_make_virtual
(
  c_col1 numeric(10),
  c_col2 as (CONVERT(VARCHAR(10),C_COL1)+'Y')
);

create table tab_change_virtual_with_index
(
  c_col1 numeric(10) not null,
  c_col2 as (CONVERT(VARCHAR(10),C_COL1)+'y')
);
create index tab_change_virtual_with_index_ix on tab_change_virtual_with_index (c_col2);
