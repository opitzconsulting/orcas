define column domain fk_column_alias_prefix
generate-foreign-key (constraint-name (alias-name || "_" || column-name || "_FK") pk-column-name(remove-next || alias-name || remove-next || "_" || column-name) on delete cascade)
(
  number(15)
);


