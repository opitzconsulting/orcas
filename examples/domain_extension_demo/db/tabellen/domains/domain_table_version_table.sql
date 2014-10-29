define table domain version_table
(
  add column column-name(column-name) (version   number(20) default "0" not null),
  add history-table
  ( 
    table-name( table-name||"_h" )
    alias-name( alias-name||"_h" )
    primary-key-mode append( hist_pk_column )
    domain hist_table    
  )
);

