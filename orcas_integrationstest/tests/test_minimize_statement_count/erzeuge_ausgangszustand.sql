create table aaa_tab_drop
(
  id number(15) not null,

  constraint tab_drop_uk unique (id)
);

create table tab_ins_col
(
  id number(15) not null,
  id2 number(15) not null,

  constraint uk_id unique (id),
  constraint uk_id2 unique (id,id2)
);

create table tab_del_col
(
  pkcol number(15) not null primary key,
  id number(15) not null,
  col_default number(15) default 66,
  col_not_null number(15) not null,
  col_not_null_default number(15) default 66 not null,

  constraint del_check_single check (col_default in (2,7)),
  constraint del_check_multi check (col_default in (id,7)),
  constraint del_check_multi_old check (col_default in (col_not_null,7)),

  constraint del_uk_single unique (col_not_null),
  constraint del_uk_multi unique (col_not_null,col_default,id),
  constraint del_uk_multi_old unique (col_not_null,col_default),

  constraint del_fk_single foreign key (col_default) references tab_ins_col (id),
  constraint del_fk_multi foreign key (col_default,id) references tab_ins_col (id,id2),
  constraint del_fk_multi_old foreign key (col_default,col_not_null) references tab_ins_col (id,id2),
  constraint del_fk_single_drop_tab foreign key (col_default) references aaa_tab_drop (id)
);


create index del_col_single_column on tab_del_col (col_default);
create index del_col_multi_column on tab_del_col (id,col_default);

comment on table aaa_tab_drop is 'TabellenAusgang';
comment on column tab_del_col.col_default is 'leer';

create table tab_del_col_nocombine
(
  id number(15) not null,
  col_drop number(15),

  constraint del_check_nocombine check (col_drop in (id,7))
);

create table tab_max_combine
(
  id number(15) not null,
  col_drop_c number(15),

  constraint del_check check (col_drop_c in (id,7)),
  constraint mod_check check (col_drop_c in (id,8)),
  constraint del_uk unique (col_drop_c)
) parallel 5 nologging;

