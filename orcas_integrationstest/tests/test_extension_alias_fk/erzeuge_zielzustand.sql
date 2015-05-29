
create table tab_a
(
  abcd_id         number(22) not null,
  abcd_other_col  number(5)
);

alter table tab_a add (
  constraint abcd_pk
  primary key
  (abcd_id));

create table tab_b
(
  abcde_id         number(22) not null,
  abcd_id          number(22)
);  

alter table tab_b add (
  constraint abcde_pk
  primary key
  (abcde_id),
  constraint abcde_abcd_fk
  foreign key
  (abcd_id)
  references tab_a (abcd_id)
);

create index abcde_abcd_fk_gen_ix on tab_b (abcd_id) logging;  