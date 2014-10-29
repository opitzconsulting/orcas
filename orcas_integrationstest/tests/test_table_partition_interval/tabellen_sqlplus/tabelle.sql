
@@create_table                  tab_partition_interval  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_partition_interval  col1  date  mandatory  ""  ""

@@declare_table_partitioning2   tab_partition_interval  range_interval (col1)  ""  numtoyminterval(1,''month'') 
@@declare_partition             tab_partition_interval  part_10 (to_date(''01.01.2010'',''DD.MM.YYYY'')) ""








