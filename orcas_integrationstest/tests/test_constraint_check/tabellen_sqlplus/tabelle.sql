
@@create_table                  tab_check_number  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_check_number  id  number(5)  ""  ""  ""
@@alter_table_add_tab_check2    tab_check_number  check_size  "ID BETWEEN 1 AND 10000"  ""

@@create_table                  tab_check_list_elements  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_check_list_elements  versand  number(9)  ""  ""  ""
@@alter_table_add_tab_check2    tab_check_list_elements  check_versand_list  "VERSAND IN (''ABHOLUNG'',''POST'',''LUFTFRACHT'')"  ""

@@create_table                  tab_check_disable  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_check_disable  bestand  number(9)  ""  ""  ""
@@alter_table_add_tab_check2    tab_check_disable  check_number  "BESTAND >= 0"  ""

@@create_table                  tab_check_wrong_column  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_check_wrong_column  id  number(5)  ""  ""  ""
@@alter_table_add_column        tab_check_wrong_column  versand  number(9)  ""  ""  ""
@@alter_table_add_column        tab_check_wrong_column  bestand  number(9)  ""  ""  ""
@@alter_table_add_tab_check2    tab_check_wrong_column  check_wrong_column  "ID BETWEEN 1 AND 500"  ""

@@create_table                  tab_check_wrong_deferred  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_check_wrong_deferred  bestand  number(9)  ""  ""  ""
@@alter_table_add_tab_check2    tab_check_wrong_deferred  check_deferred  "BESTAND >= 0"  deferred

@@create_table                  tab_bb_new_duplicate_cons_name  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_bb_new_duplicate_cons_name  id  number(5)  ""  ""  ""
@@alter_table_add_tab_check2    tab_bb_new_duplicate_cons_name  check_duplicate_cons_name  "ID IS NOT NULL"  immediate

@@create_table                  tab_check_drop  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_check_drop  bestand  number(9)  ""  ""  ""




