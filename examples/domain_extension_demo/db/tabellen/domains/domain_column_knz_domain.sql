define column domain knz_domain
generate-check-constraint (constraint-name (alias-name || "_" || column-name || "_KNZ_CC") check-rule(column-name || " in (0,1)"))
(
  number(1) not null
);

