define column domain fk_column
generate-foreign-key (constraint-name (alias-name || "_" || column-name || "_FK") pk-column-name(column-name) on delete cascade)
(
  number(15)
);


