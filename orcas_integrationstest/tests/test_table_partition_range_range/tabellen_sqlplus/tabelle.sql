
@@create_table                  tab_partition_range_range  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_partition_range_range  col1  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_partition_range_range  col2  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_partition_range_range  col3  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_partition_range_range  col4  number(15)  mandatory  ""  ""

@@declare_table_partitioning2   tab_partition_range_range  range_range (col1,col2) (col3,col4)  "" 
@@declare_partition             tab_partition_range_range  part_10_5 (10,5) ""
@@declare_partition             tab_partition_range_range  part_m_m (maxvalue,maxvalue) ""
@@declare_subpartition_template tab_partition_range_range  _3_4 (3,4) ""
@@declare_subpartition_template tab_partition_range_range  _3_7 (3,7) ""
@@declare_subpartition_template tab_partition_range_range  _5_m (5,maxvalue) ""
@@declare_subpartition_template tab_partition_range_range  _m_m (maxvalue,maxvalue) ""








