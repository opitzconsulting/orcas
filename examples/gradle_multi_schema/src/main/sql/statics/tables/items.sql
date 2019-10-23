create table orcas_orderentry_meta.items
(
  item_id         number(15)                    not null,
  version         number(15)      default "0"   not null,
  ctgr_id         number(15)                    not null,
  name            varchar2(30)             not null,
  description     varchar2(1000)           not null,
  image_location  varchar2(255),
  price           number(8,2)                   not null,

  constraint item_pk primary key (item_id) using index orcas_orderentry_data.item_pk,
  constraint item_uc unique (name),
  index orcas_orderentry_data.item_pk (item_id),
  constraint item_ctgr_fk foreign key (ctgr_id) references orcas_orderentry_meta.categories (ctgr_id),

  comment on column version is 'default ist null';
  comment on table is 'items tabelle';  
);

