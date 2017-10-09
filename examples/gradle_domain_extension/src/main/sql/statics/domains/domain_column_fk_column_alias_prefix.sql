define column domain fk_column_alias_prefix
generate-foreign-key (constraint-name (alias-name || "_" || column-name || "_FK") pk-column-name(column-name regexp ".....(....).*" replace "$1_id") on delete cascade)
(
  number(15)
);


