
create table tab_old
(
  col_1 number(15) not null
);

create index tab_old_ix on tab_old (col_1);



create table tab_new
(
  col_1 number(15) not null
);

create index tab_new_ix on tab_new (col_1);

create table tab_mod
(
  col_1 number(15),
  col_2 number(15),
  col_3 number(15),
  col_4 number(15),
  old_col number(15),
  new_col number(15)
);

create index tab_mod_ix_del on tab_mod (col_3);
create index tab_mod_ix_add on tab_mod (col_1);
create unique index tab_mod_ix_new_col_unique on tab_mod (new_col);
create unique index tab_mod_ix_old_col_unique on tab_mod (old_col);


