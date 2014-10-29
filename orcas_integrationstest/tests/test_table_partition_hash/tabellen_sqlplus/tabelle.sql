
@@create_table                  tab_partition_hash  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_partition_hash  col1  number(15)  mandatory  ""  ""

@@declare_table_partitioning2   tab_partition_hash  hash (col1)  ""  "" 
@@declare_partition             tab_partition_hash  part_10 "" ""
@@declare_partition             tab_partition_hash  part_20 "" ""









