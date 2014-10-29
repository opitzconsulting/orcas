
@@create_table                  tab_index  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_index  col_add_ix_1  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_index  col_add_ix_2  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_index  col_mod_ix_1  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_index  col_mod_ix_2  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_index  col_mod_ix_3  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_index  col_mod_ix_4  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_index  col_mod_ix_5  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_index  col_mod_ix_6  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_index  col_mod_ix_7  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_index  col_mod_ix_8  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_index  col_mod_ix_9  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_index  col_mod_ix_10  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_index  col_mod_ix_11  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_index  col_del_ix_1  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_index  col_del_ix_2  number(15)  mandatory  ""  ""

@@create_index  tab_index  add_2col_ix        (col_add_ix_1,col_add_ix_2)  ""  ""  ""  ""  ""
@@create_index  tab_index  add_1col_ix        (col_add_ix_2)  ""  ""  ""  ""  ""
@@create_index  tab_index  mod_order_ix       (col_mod_ix_2,col_mod_ix_1)  ""  ""  ""  ""  ""
@@create_index  tab_index  mod_parallel_ix    (col_mod_ix_3)  ""  ""  parallel  "" ""  
@@create_index  tab_index  mod_parallel_4_ix  (col_mod_ix_4)  ""  ""  "parallel 4" "" ""
@@create_index  tab_index  mod_noparallel_ix  (col_mod_ix_5)  ""  ""  noparallel  "" ""  
@@create_index  tab_index  mod_unique_ix      (col_mod_ix_6)  unique  ""  ""  ""  ""
@@create_index  tab_index  mod_nounique_ix    (col_mod_ix_7)  nonunique  ""  ""  ""  ""
@@create_index  tab_index  mod_logging_ix     (col_mod_ix_10)  ""  ""  "logging" "" ""
@@create_index  tab_index  mod_logging_reverse_ix  (col_mod_ix_11) ""  ""  "nologging" "" ""







