@@create_table                  tab_new_pk  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_new_pk  id  number(15)  mandatory  ""  ""
@@alter_table_add_primary_key2  tab_new_pk  tab_new_pk  id  ""

@@create_table                  tab_wrong_pk_name  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_wrong_pk_name  id  number(15)  mandatory  ""  ""
@@alter_table_add_primary_key2  tab_wrong_pk_name  tab_wrong_pk_name  id  ""

@@create_table                  tab_wrong_pk_reverse  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_wrong_pk_reverse  id  number(15)  mandatory  ""  ""
@@alter_table_add_primary_key2  tab_wrong_pk_reverse  tab_wrong_pk_reverse  id  "reverse"

@@create_table                  tab_wrong_pk_column  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_wrong_pk_column  id1  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_wrong_pk_column  id2  number(15)  mandatory  ""  ""
@@alter_table_add_primary_key2  tab_wrong_pk_column  tab_wrong_pk_column  id1  ""

@@create_table                  tab_wrong_pk_column_order  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_wrong_pk_column_order  id1  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_wrong_pk_column_order  id2  number(15)  mandatory  ""  ""
@@alter_table_add_primary_key2  tab_wrong_pk_column_order  tab_wrong_pk_column_order  (id1,id2)  ""

@@create_table                  tab_noname_pk  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_noname_pk  id1  number(15)  mandatory  ""  ""
@@alter_table_add_primary_key2  tab_noname_pk  ""  id1  ""

@@create_table                  tab_create_tab_named_pk  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_create_tab_named_pk  id1  number(15)  mandatory  ""  ""
@@alter_table_add_primary_key2  tab_create_tab_named_pk  pk_name  id1  ""

