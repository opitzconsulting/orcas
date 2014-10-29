@@create_table                  tab_logging_default  ""  ""  ""  ""  ""  ""
@@alter_table_add_column        tab_logging_default  id  number(15)  mandatory  ""  ""

@@create_table                  tab_logging  ""  ""  "logging"  ""  ""  ""
@@alter_table_add_column        tab_logging  id  number(15)  mandatory  ""  ""

@@create_table                  tab_nologging  ""  ""  "nologging"  ""  ""  ""
@@alter_table_add_column        tab_nologging  id  number(15)  mandatory  ""  ""

