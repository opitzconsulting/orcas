@@create_table             tab_dummy  ""  ""  ""  ""  ""  ""
@@alter_table_add_column   tab_dummy  col1  number(5)  ""  ""  ""  

@@create_sequence2         seq_test_create  ""  ""  ""  20  ""  ""  ""
@@create_sequence2         seq_test_default  ""  ""  ""  ""  ""  ""  ""
@@create_sequence2         seq_test_update  ""  ""  ""  10  ""  ""  ""
@@create_sequence2         seq_test_update_lv  "select 12 from dual"  ""  ""  ""  ""  ""  ""
@@create_sequence2         seq_test_invalid_update  "select 15 from tab_dummy"  ""  ""  ""  ""  ""  ""
@@create_sequence2         seq_test_cycle  ""  ""  ""  ""  ""  nocycle  ""
@@create_sequence2         seq_test_update_cycle  ""  ""  999  ""  ""  cycle  ""
@@create_sequence2         seq_test_order  ""  ""  ""  ""  order  ""  ""
@@create_sequence2         seq_test_update_order  ""  ""  ""  ""  order  ""  ""
@@create_sequence2         seq_test_minvalue  ""  10  ""  ""  ""  ""  ""
@@create_sequence2         seq_test_update_minvalue  ""  10  ""  ""  ""  ""  ""
@@create_sequence2         seq_test_maxvalue  ""  ""  100  ""  ""  ""  ""
@@create_sequence2         seq_test_update_maxvalue  ""  ""  100  ""  ""  ""  ""
@@create_sequence2         seq_test_cache  ""  ""  ""  ""  ""  ""  100
@@create_sequence2         seq_test_update_cache  ""  ""  ""  ""  ""  ""  50

