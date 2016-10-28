define column domain pk_column
generate-primary-key (constraint-name(alias-name || "_PK") sequence-name(alias-name || "_seq"))
(
  number(15) not null
);

