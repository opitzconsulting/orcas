@@create_table                 tab_new_uk  ""  ""  ""  ""  ""  ""
@@alter_table_add_column       tab_new_uk  id1  number(15)  mandatory  ""  ""  
@@alter_table_add_column       tab_new_uk  id2  number(20)  mandatory  ""  "" 
@@alter_table_add_unique_key   tab_new_uk  tab_new_uk  (id1,id2)  ""

@@create_table                 tab_wrong_uk_name  ""  ""  ""  ""  ""  ""
@@alter_table_add_column       tab_wrong_uk_name  id  number(15)  mandatory  ""  ""  
@@alter_table_add_unique_key   tab_wrong_uk_name  tab_wrong_uk_name  (id)  ""

@@create_table                 tab_wrong_uk_columns  ""  ""  ""  ""  ""  ""
@@alter_table_add_column       tab_wrong_uk_columns  id1  number(15)  mandatory  ""  ""  
@@alter_table_add_column       tab_wrong_uk_columns  id2  number(15)  mandatory  ""  "" 
@@alter_table_add_column       tab_wrong_uk_columns  id3  number(15)  mandatory  ""  ""
@@alter_table_add_unique_key   tab_wrong_uk_columns  tab_wrong_uk_column  (id1,id2)  ""

@@create_table                 tab_wrong_uk_column_order  ""  ""  ""  ""  ""  ""
@@alter_table_add_column       tab_wrong_uk_column_order  id1  number(15)  mandatory  ""  ""  
@@alter_table_add_column       tab_wrong_uk_column_order  id2  number(15)  mandatory  ""  "" 
@@alter_table_add_unique_key   tab_wrong_uk_column_order  tab_wrong_uk_column_order  (id1,id2)  ""







