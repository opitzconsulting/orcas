create table tab_ins_col
(
  id number(15) not null
);

insert into tab_ins_col
       (
       id
       ) 
values (
       5
       );
commit;

create table tab_del_col
(
  id number(15) not null,
  new_col number(15) not null
);

create table tab_drop_me
(
  id number(15) not null
);

create table tab_mod_cols
(
  col_chg_size          number(15)         not null,
  col_chg_precision     number(15)         not null,
  col_chg_type          number(15)         not null,
  col_set_nullable      varchar2(15)       not null,
  col_set_not_nullable  varchar2(15),
  col_set_default       number(15),
  col_chg_default       number(15) default 6,
  col_set_no_default    number(15) default 5,
  col_set_byte          varchar2(15 char)  not null,
  col_set_char          varchar2(15 byte)  not null
);
