
@@create_table                  tab_a  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_a  tab_a_id  number(9)  mandatory  ""  ""
@@alter_table_add_primary_key2  tab_a  pk_tab_a  tab_a_id  ""

@@create_table                  tab_b  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_b  tab_b_id  number(9)  mandatory  ""  ""
@@alter_table_add_primary_key2  tab_b  pk_tab_b  tab_b_id  ""

@@create_table                  tab_c  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_c  tab_c_id  number(9)  mandatory  ""  ""
@@alter_table_add_primary_key2  tab_c  pk_tab_c  tab_c_id  ""

@@create_table                  tab_del_cas  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_del_cas  tab_a_id  number(8)  ""  ""  ""
@@alter_table_add_column        tab_del_cas  tab_b_id  number(8)  ""  ""  ""
@@alter_table_add_column        tab_del_cas  tab_c_id  number(8)  ""  ""  ""
@@alter_table_add_foreign_key4  tab_del_cas  tab_a  cascade  tab_a_id  fk_tab_del_cas_tab_a_id  tab_a_id  ""
@@alter_table_add_foreign_key4  tab_del_cas  tab_b  cascade  tab_b_id  fk_tab_del_cas_tab_b_id  tab_b_id  ""
@@alter_table_add_foreign_key4  tab_del_cas  tab_c  cascade  tab_c_id  fk_tab_del_cas_tab_c_id  tab_c_id  ""

@@create_table                  tab_no_action  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_no_action  tab_a_id  number(8)  ""  ""  ""
@@alter_table_add_column        tab_no_action  tab_b_id  number(8)  ""  ""  ""
@@alter_table_add_column        tab_no_action  tab_c_id  number(8)  ""  ""  ""
@@alter_table_add_foreign_key4  tab_no_action  tab_a  ""  tab_a_id  fk_tab_no_action_tab_a_id  tab_a_id  ""
@@alter_table_add_foreign_key4  tab_no_action  tab_b  ""  tab_b_id  fk_tab_no_action_tab_b_id  tab_b_id  ""
@@alter_table_add_foreign_key4  tab_no_action  tab_c  ""  tab_c_id  fk_tab_no_action_tab_c_id  tab_c_id  ""

@@create_table                  tab_set_null  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_set_null  tab_a_id  number(8)  ""  ""  ""
@@alter_table_add_column        tab_set_null  tab_b_id  number(8)  ""  ""  ""
@@alter_table_add_column        tab_set_null  tab_c_id  number(8)  ""  ""  ""
@@alter_table_add_foreign_key4  tab_set_null  tab_a  nullify  tab_a_id  fk_tab_set_null_tab_a_id  tab_a_id  ""
@@alter_table_add_foreign_key4  tab_set_null  tab_b  nullify  tab_b_id  fk_tab_set_null_tab_b_id  tab_b_id  ""
@@alter_table_add_foreign_key4  tab_set_null  tab_c  nullify  tab_c_id  fk_tab_set_null_tab_c_id  tab_c_id  ""

@@create_table                  tab_2_column_pk  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_2_column_pk  id1  number(20)  mandatory  ""  ""
@@alter_table_add_column        tab_2_column_pk  id2  number(30)  mandatory  ""  ""
@@alter_table_add_primary_key2  tab_2_column_pk  pk_tab_2_column_pk  (id1,id2)  ""

@@create_table                  tab_wrong_fk_name  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_wrong_fk_name  id  number(9)  ""  ""  ""
@@alter_table_add_foreign_key4  tab_wrong_fk_name  tab_a  ""  id  fk_tab_right_fk_name  tab_a_id  ""

@@create_table                  tab_wrong_fk_column  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_wrong_fk_column  id  number(9)  ""  ""  ""
@@alter_table_add_foreign_key4  tab_wrong_fk_column  tab_b  ""  id  fk_tab_wrong_fk_column  tab_b_id  ""

@@create_table                  tab_wrong_fk_column_order  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_wrong_fk_column_order  id1  number(20)  ""  ""  ""
@@alter_table_add_column        tab_wrong_fk_column_order  id2  number(30)  ""  ""  ""
@@alter_table_add_foreign_key4  tab_wrong_fk_column_order  tab_2_column_pk  ""  (id1,id2)  fk_tab_wrong_fk_column_order  (id1,id2)  ""

@@create_table                  tab_wrong_deferred  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_wrong_deferred  id  number(9)  ""  ""  ""
@@alter_table_add_foreign_key4  tab_wrong_deferred  tab_a  ""  id  fk_tab_right_deferred  tab_a_id  deferred

@@create_table                  tab_mixed_name_1  ""    ""  ""  ""  ""  ""
@@alter_table_add_column        tab_mixed_name_1  id    number(9)  ""  ""  ""
@@alter_table_add_column        tab_mixed_name_1  id_1  number(9)  ""  ""  ""
@@alter_table_add_column        tab_mixed_name_1  id_2  number(9)  ""  ""  ""
@@alter_table_add_foreign_key4  tab_mixed_name_1  tab_a  ""  id    fk_mixed_name_1        tab_a_id  ""
@@alter_table_add_foreign_key4  tab_mixed_name_1  tab_a  ""  id_1  fk_mixed_name_intab_1  tab_a_id  ""
@@alter_table_add_foreign_key4  tab_mixed_name_1  tab_b  ""  id_2  fk_mixed_name_intab_2  tab_b_id  ""

@@create_table                  tab_mixed_name_2  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_mixed_name_2  id  number(9)  ""  ""  ""
@@alter_table_add_foreign_key4  tab_mixed_name_2  tab_a  ""  id  fk_mixed_name_2  tab_a_id  immediate

@@create_table                  tab_index_test  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_index_test  tab_index_test_id  number(9)  mandatory  ""  ""
@@alter_table_add_primary_key2  tab_index_test  pk_tab_index_test  tab_index_test_id  ""

@@create_table                  tab_index_test_ref  ""    ""  ""  ""  ""  ""
@@alter_table_add_column        tab_index_test_ref  tab_a_id  number(9)  ""  ""  ""
@@alter_table_add_column        tab_index_test_ref  tab_b_id  number(9)  ""  ""  ""
@@alter_table_add_unique_key    tab_index_test_ref  UK_TAB_A_B_ID  (tab_a_id,tab_b_id)  ""  
@@alter_table_add_foreign_key4  tab_index_test_ref  tab_index_test  cascade  tab_a_id  fk_tab_index_test_ref_a_id  tab_index_test_id  ""
