CREATE TABLE TAB_REF
(
  ID NUMERIC(15) constraint pk_tab_a primary key not null
);

CREATE TABLE TAB_MOD_COLS
(
  COL_SET_NOT_NULLABLE_INDEX  NUMERIC(15)       NOT NULL,
  COL_SET_NOT_NULLABLE_FK  NUMERIC(15)       NOT NULL,
  COL_SET_CHANGE_DEFAULT_INDEX  NUMERIC(15) DEFAULT 7,
  constraint fk_tab_ref foreign key (COL_SET_NOT_NULLABLE_FK) references TAB_REF (ID)
);

create index col_ix on TAB_MOD_COLS (COL_SET_NOT_NULLABLE_INDEX);
create index col_default_ix on TAB_MOD_COLS (COL_SET_CHANGE_DEFAULT_INDEX);
