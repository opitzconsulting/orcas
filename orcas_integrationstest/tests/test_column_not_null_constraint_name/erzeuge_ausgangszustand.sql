create table tab_modify_constraint
(
  id number(15) constraint wrong_name not null
);

create table tab_remove_name
(
  id number(15) constraint remove_name not null
);

create table tab_remove_constraint
(
  id number(15) constraint remove_name2 not null
);

create table tab_recreate_column
(
  id number(20) constraint correct_name not null
);
