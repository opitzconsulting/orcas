create table tab_ref
(
  id number(15) not null,
  constraint  pk_tab_a    primary key (id),
);

create table tab_mod_cols
(
  col_set_not_nullable_index  number(15)         not null,
  col_set_not_nullable_fk     number(15)       not null,
  col_set_change_default_index  number(15) default "7",
  index col_ix (col_set_not_nullable_index),
  index col_default_ix (col_set_change_default_index),
  constraint fk_tab_ref foreign key (col_set_not_nullable_fk) references tab_ref (id),
);

