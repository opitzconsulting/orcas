create table categories
(
  ctgr_id      number(15)                       not null,
  version      number(15)      default "0"      not null,
  name         varchar2(30)                not null,
  description  varchar2(1000)              not null,

  primary key (ctgr_id),
  constraint ctgr_uc unique (name)
);


comment on column categories.version is 'default ist null';
comment on table categories is 'categories tabelle';

