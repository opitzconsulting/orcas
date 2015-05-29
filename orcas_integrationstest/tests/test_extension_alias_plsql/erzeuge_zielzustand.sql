
create table tab_a
(
  abcd_other_col  number(5),
  abcd_id         number(22) not null
);

alter table tab_a add (
  constraint abcd_pk
  primary key
  (abcd_id));

