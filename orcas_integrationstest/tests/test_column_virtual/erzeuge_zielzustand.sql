create table tab_virtual_column
(   
  c_col1 number(10),
  c_col2 number(10),
  c_col3 varchar2(100) generated always as (TO_CHAR(C_COL1)||' '||TO_CHAR(C_COL2)) virtual
);


