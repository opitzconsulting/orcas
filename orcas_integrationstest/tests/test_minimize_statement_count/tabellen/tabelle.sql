create table tab_ins_col
(
  id number(15) not null,
  id2 number(15) not null,
  chg_col_add_not_null number(15),
  chg_col_add_default number(15),

  constraint uk_id unique (id),
  constraint uk_id2 unique (id,id2)
);

create table tab_del_col
(
  id number(15) not null
);

create table tab_del_col_nocombine
(
  id number(15) not null,
  col_new number(15),

  constraint col_new_cons check ("col_new in (id,8)")
);

create table tab_max_combine
(
  id number(15) not null,
  col_drop_c number(15),
  col_add number(15),

  constraint add_check check ("col_add in (col_drop_c,7)"),
  constraint mod_check check ("col_drop_c in (id,8)")  deferrable initially deferred,
  constraint add_uk unique (col_add,col_drop_c)
) parallel 2;



