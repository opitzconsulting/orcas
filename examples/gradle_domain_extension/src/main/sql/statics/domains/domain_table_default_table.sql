define table domain default_table extends version_table
(
  add column column-name(alias-name||"_"||column-name) (id          domain pk_column          ),
  add column column-name(alias-name||"_"||column-name) (insert_date date   default "sysdate"   not null )
);

