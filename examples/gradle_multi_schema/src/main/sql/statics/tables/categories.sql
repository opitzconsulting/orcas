create table orcas_orderentry_meta.categories
(
  ctgr_id      number(15)                       not null,
  version      number(15)      default "0"      not null,
  name         varchar2(30)                not null,
  description  varchar2(1000)              not null,

  constraint ctgr_pk primary key (ctgr_id),
  constraint ctgr_uc unique (name),
  index orcas_orderentry_data.ctgr_description_ix (description),

  comment on column version is 'default ist null';
  comment on table is 'categories tabelle';
);



