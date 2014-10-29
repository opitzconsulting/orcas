define column domain fk_column
generate-foreign-key (constraint-name (alias-name || "_" || column-name || "_FK") pk-column-name(alias-name || "_id") on delete cascade)
(
  number(15)
);


