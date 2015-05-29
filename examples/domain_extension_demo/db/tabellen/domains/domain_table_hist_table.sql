define table domain hist_table
(
  add column append_last column-name(alias-name||"_"||column-name) (id domain pk_column),
  add column column-name(alias-name||"_"||column-name) (hist_date date   default "sysdate"   not null ),
  add column column-name(alias-name||"_"||column-name) (hist_action domain hist_action)
);

