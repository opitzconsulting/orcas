
@@create_table                  tab_partition_list_list  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_partition_list_list  col1  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_partition_list_list  col2  number(15)  mandatory  ""  ""

@@declare_table_partitioning2   tab_partition_list_list  list_list col1 col2  "" 
@@declare_partition             tab_partition_list_list  part_10_5 10,5 ""
@@declare_partition             tab_partition_list_list  part_d default ""
@@declare_subpartition_template tab_partition_list_list  _3_4 3,4 ""
@@declare_subpartition_template tab_partition_list_list  _5_7 5,7 ""
@@declare_subpartition_template tab_partition_list_list  _d default ""








