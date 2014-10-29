
@@create_table                  tab_partition_range  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_partition_range  col1  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_partition_range  col2  number(15)  mandatory  ""  ""

@@declare_table_partitioning2   tab_partition_range  range (col1,col2)  ""  "" 
@@declare_partition             tab_partition_range  part_10 (10,5) ""
@@declare_partition             tab_partition_range  part_20 (maxvalue,maxvalue) ""

@@set_compress                  compress

@@create_table                  tab_partition_range_tabspace  ""  ""  "tablespace SYSTEM"  ""  ""  ""
@@alter_table_add_column        tab_partition_range_tabspace  col1  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_partition_range_tabspace  col2  number(15)  mandatory  ""  ""

@@declare_table_partitioning2   tab_partition_range_tabspace  range (col1,col2)  ""  "" 
@@declare_partition             tab_partition_range_tabspace  part_10 (10,5) ""
@@declare_partition             tab_partition_range_tabspace  part_20 (maxvalue,maxvalue) ""








