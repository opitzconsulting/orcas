create table text_tabelle
(
  col_clob_1 clob not null,
  col_clob_2 clob not null,
  col_clob_3 clob not null,
  col_clob_4 clob not null
);

create index del_clob_ix on text_tabelle (col_clob_4) domain_index "indextype is CTXSYS.CONTEXT PARAMETERS ('')";


create table text_tabelle_inline
(
  col_clob_1 clob not null,
  col_clob_2 clob not null,
  col_clob_3 clob not null,
  col_clob_4 clob not null
);

create index inline_del_clob_ix on text_tabelle_inline (col_clob_4) domain_index "indextype is CTXSYS.CONTEXT PARAMETERS ('')";


