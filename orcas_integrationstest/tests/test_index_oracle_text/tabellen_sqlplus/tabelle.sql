
@@create_table                  text_tabelle  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        text_tabelle  col_clob_1  clob  mandatory  ""  ""
@@alter_table_add_column        text_tabelle  col_clob_2  clob  mandatory  ""  ""
@@alter_table_add_column        text_tabelle  col_clob_3  clob  mandatory  ""  ""
@@alter_table_add_column        text_tabelle  col_clob_4  clob  mandatory  ""  ""

@@create_index  text_tabelle  add_clob_ix    (col_clob_1)  ""  ""  "indextype is CTXSYS.CONTEXT PARAMETERS ('''')"  "" ""   

@@create_table                  text_tabelle_inline  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        text_tabelle_inline  col_clob_1  clob  mandatory  ""  ""
@@alter_table_add_column        text_tabelle_inline  col_clob_2  clob  mandatory  ""  ""
@@alter_table_add_column        text_tabelle_inline  col_clob_3  clob  mandatory  ""  ""
@@alter_table_add_column        text_tabelle_inline  col_clob_4  clob  mandatory  ""  ""  

@@create_index  text_tabelle_inline  inline_add_clob_ix    (col_clob_1)  ""  ""  "indextype is CTXSYS.CONTEXT PARAMETERS ('''')"  "" "" 






