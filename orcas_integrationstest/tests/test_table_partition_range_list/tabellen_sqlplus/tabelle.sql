
@@create_table                  tab_partition_range_list  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_partition_range_list  col1  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_partition_range_list  col2  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_partition_range_list  col3  number(15)  mandatory  ""  ""

@@declare_table_partitioning2   tab_partition_range_list  range_list (col1,col2) (col3)  "" 
@@declare_partition             tab_partition_range_list  part_10_5 (10,5) ""
@@declare_partition             tab_partition_range_list  part_m_m (maxvalue,maxvalue) ""
@@declare_subpartition_template tab_partition_range_list  _3_4 3,4 ""
@@declare_subpartition_template tab_partition_range_list  _5_7 5,7 ""
@@declare_subpartition_template tab_partition_range_list  _d (default) ""








