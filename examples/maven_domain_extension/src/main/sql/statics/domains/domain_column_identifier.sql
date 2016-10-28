define column domain identifier
generate-unique-key (constraint-name (alias-name || "_" || column-name || "_UK"))
(
  varchar2(30) not null
);


