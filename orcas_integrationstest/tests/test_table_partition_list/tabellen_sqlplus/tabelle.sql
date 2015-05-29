
@@create_table                  tab_partition_list  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_partition_list  col1  number(15)  mandatory  ""  ""

@@declare_table_partitioning2   tab_partition_list  list (col1)  ""  "" 
@@declare_partition             tab_partition_list  part_1 (10,5) ""
@@declare_partition             tab_partition_list  part_2 (7,6) ""
@@declare_partition             tab_partition_list  part_3 (default) ""








