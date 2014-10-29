@@create_table                  tab_delete_rows  ""  temporary_transaction  ""  ""  ""  ""
@@alter_table_add_column        tab_delete_rows  id  number(15) mandatory  ""  ""

@@create_table                  tab_preserve_rows  ""  temporary  ""  ""  ""  ""
@@alter_table_add_column        tab_preserve_rows  id  number(15)  mandatory  ""  ""

@@create_table                  tab_permanent  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_permanent  id  number(15)  mandatory  ""  ""

@@create_table                  tab_mod_parallel  ""  ""  "parallel"  ""  ""  ""
@@alter_table_add_column        tab_mod_parallel  id  number(15)  mandatory  ""  ""

@@create_table                  tab_mod_parallel_4  ""  ""  "parallel 4"  ""  ""  ""
@@alter_table_add_column        tab_mod_parallel_4  id  number(15)  mandatory  ""  ""

@@create_table                  tab_mod_noparallel  ""  ""  "noparallel"  ""  ""  ""
@@alter_table_add_column        tab_mod_noparallel  id  number(15)  mandatory  ""  ""


