
create table tab_a alias abcd
(
  abcd_other_col  number(5)
);

create table tab_b alias abcde
(
  abcd_id  number(22),
  constraint abcde_abcd_fk foreign key (abcd_id) references tab_a (abcd_id)
);




