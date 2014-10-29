@@create_table                  tab_partition_range  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_partition_range  col1  number(15)  mandatory  ""  ""
@@alter_table_add_column        tab_partition_range  col2  number(15)  mandatory  ""  ""

@@declare_table_partitioning2   tab_partition_range  range (col1,col2)  ""  "" 
@@declare_partition             tab_partition_range  part_10 (10,5) ""
@@declare_partition             tab_partition_range  part_20 (maxvalue,maxvalue) ""

@@create_index  tab_partition_range  add_bitmap_ix      (col1)  bitmap  local  ""  ""  ""







