@@create_table                 tab_index  ""  ""  ""  ""  ""  ""
@@alter_table_add_column       tab_index  id_nummer_mod         number(15)  mandatory  ""  "" 
@@alter_table_add_column       tab_index  id_nummer_rev         number(15)  mandatory  ""  ""
@@alter_table_add_column       tab_index  id_nummer_mod_uk      number(15)  mandatory  ""  ""
@@alter_table_add_column       tab_index  id_nummer_pk          number(15)  mandatory  ""  ""  
@@alter_table_add_column       tab_index  id_nummer_mod_def     number(15)  mandatory  ""  "" 
@@alter_table_add_column       tab_index  id_nummer_rev_def     number(15)  mandatory  ""  ""
@@alter_table_add_column       tab_index  id_nummer_mod_uk_def  number(15)  mandatory  ""  ""
@@alter_table_add_column       tab_index  id_nummer_int         number(15)  mandatory  ""  "" 
@@alter_table_add_column       tab_index  id_nummer_ext         number(15)  mandatory  ""  "" 
@@alter_table_add_column       tab_index  id_nummer_uk          number(15)  mandatory  ""  ""


@@alter_table_add_primary_key2 tab_index  id_nummer_pk_pk             (id_nummer_pk)          "tablespace replaceme1"
@@create_index                 tab_index  id_nummer_int_ix            (id_nummer_int)         ""  ""  ""  ""  "tablespace replaceme1"
@@alter_table_add_unique_key   tab_index  id_nummer_mod_uk_uk         (id_nummer_mod_uk)      replaceme2
@@alter_table_add_unique_key   tab_index  id_nummer_uk_uk             (id_nummer_uk)          replaceme1
@@create_index                 tab_index  id_nummer_ext_ix            (id_nummer_ext)         ""  ""  ""  "" "tablespace replaceme1"  
@@create_index                 tab_index  mod_tabspace_ix             (id_nummer_mod)         ""  ""  ""  "" "tablespace replaceme2"  
@@create_index                 tab_index  mod_tabspace_reverse_ix     (id_nummer_rev)         ""  ""  ""  "" "tablespace replaceme1"
@@create_index                 tab_index  mod_tabspace_def_ix         (id_nummer_mod_def)     ""  ""  ""  "" "tablespace replaceme1"  
@@create_index                 tab_index  mod_tabspace_reverse_def_ix (id_nummer_rev_def)     ""  ""  ""  "" ""
@@alter_table_add_unique_key   tab_index  id_nummer_mod_uk_uk_def     (id_nummer_mod_uk_def)  ""




