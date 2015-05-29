@@create_table                  tab_index  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_index  col_1  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_index  col_2  number(15)  mandatory  ""  ""

@@create_index  tab_index  add_1_ix  NVL(COL_1,1)  ""  ""  ""  ""  ""
@@create_index  tab_index  add_2_ix  COL_1,COL_2,NVL(COL_1,1)  ""  ""  ""  ""  ""
@@create_index  tab_index  add_3_ix  NVL(COL_1,1),COL_2  ""  ""  ""  ""  ""
@@create_index  tab_index  add_4_ix  NVL(TO_CHAR(COL_1),''test'')  ""  ""  ""  "" ""  
@@create_index  tab_index  mod_1_ix  NVL(TO_CHAR(COL_2),''klein'')  ""  ""  "" "" ""

@@create_table                  tab_index_inline  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_index_inline  col_1  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_index_inline  col_2  number(15)  mandatory  ""  ""

@@create_index  tab_index_inline  inline_add_1_ix  NVL(COL_1,1)  ""  ""  ""  ""  ""
@@create_index  tab_index_inline  inline_add_2_ix  COL_1,COL_2,NVL(COL_1,1)  ""  ""  ""  ""  ""
@@create_index  tab_index_inline  inline_add_3_ix  NVL(COL_1,1),COL_2  ""  ""  ""  ""  ""
@@create_index  tab_index_inline  inline_add_4_ix  NVL(TO_CHAR(COL_1),''test'')  ""  ""  ""  "" ""  
@@create_index  tab_index_inline  inline_mod_1_ix  NVL(TO_CHAR(COL_2),''klein'')  ""  ""  "" "" ""



