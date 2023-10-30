create table tab_new_constraint
(
  id number(15) constraint create_xy not null
);

create table tab_modify_constraint
(
  id number(15) constraint update_xy not null
);

create table tab_remove_name
(
  id number(15) not null
);

create table tab_remove_constraint
(
  id number(15)
);

create table tab_recreate_column
(
  id number(15) constraint correct_name not null
);

create table tab_with_two_not_null_con
(
  id number(15) constraint one_name not null,
  constraint other_name check ("ID" IS NOT NULL)
);

create table tab_noname_not_null_con
(
  id number(15) not null
);

create table tab_with_separate_not_null_con
(
  id number(15),
  constraint other_name2 check ("ID" IS NOT NULL)
);

create table tab_with_unnamed_and_sec
(
  id number(15) not null,
  constraint other_name3 check ("ID" IS NOT NULL)
);
