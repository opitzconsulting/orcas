define column domain hist_pk_column
generate-primary-key (constraint-name(alias-name || "_PK") )
(
  number(15) not null
);

