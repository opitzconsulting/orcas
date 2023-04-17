create table tab_virtual_column
(   
  c_col1 number(10),
  c_col2 number(10),
  c_col3 varchar2(21) as ("CONVERT(VARCHAR(10),C_COL1)+' '+CONVERT(VARCHAR(10),C_COL2)") virtual
);

create table tab_add_column
(
  c_col1 number(10),
  c_col2 varchar2(11) as ("CONVERT(VARCHAR(10),C_COL1)+'X'") virtual
);

create table tab_change_column
(
  c_col1 number(10),
  c_col2 varchar2(11) as ("CONVERT(VARCHAR(10),C_COL1)+'Y'") virtual
);

create table tab_change_make_not_virtual
(
  c_col1 number(10),
  c_col2 varchar2(41)
);

create table tab_change_make_virtual
(
  c_col1 number(10),
  c_col2 varchar2(11) as ("CONVERT(VARCHAR(10),C_COL1)+'Y'") virtual
);

create table tab_change_virtual_with_index
(
  c_col1 number(10) not null,
  c_col2 varchar2(11) as ("CONVERT(VARCHAR(10),C_COL1)+'y'") virtual,
  index tab_change_virtual_with_index_ix (c_col2)
);
