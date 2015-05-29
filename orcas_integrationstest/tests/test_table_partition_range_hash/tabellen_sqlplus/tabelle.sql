
@@create_table                  tab_partition_range_hash  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_partition_range_hash  col1  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_partition_range_hash  col2  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_partition_range_hash  col3  number(15)  mandatory  ""  ""

@@declare_table_partitioning2   tab_partition_range_hash  range_hash (col1,col2) (col3)  "" 
@@declare_partition             tab_partition_range_hash  part_10_5 (10,5) ""
@@declare_partition             tab_partition_range_hash  part_m_m (maxvalue,maxvalue) ""
@@declare_subpartition_template tab_partition_range_hash  _1 "" ""
@@declare_subpartition_template tab_partition_range_hash  _2 "" ""









