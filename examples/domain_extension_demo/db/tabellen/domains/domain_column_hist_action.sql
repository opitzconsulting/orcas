define column domain hist_action
generate-check-constraint (constraint-name (alias-name || "_" || column-name || "_ck") check-rule(column-name || " in ('I','U','D')"))
(
  varchar2(1) not null
);

