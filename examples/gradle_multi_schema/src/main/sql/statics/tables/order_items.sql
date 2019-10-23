create table orcas_orderentry_data.order_items
(
  orit_id   number(15)                          not null,
  version   number(15)       default "0"        not null,
  ordr_id   number(15)                          not null,
  item_id   number(15)                          not null,
  price     number(8,2)                         not null,
  quantity  number(4)                           not null,

  constraint orit_pk primary key (orit_id) using index orcas_orderentry_meta.order_items_pk_ix,
  constraint orit_uc unique (ordr_id, item_id),
  index orcas_orderentry_meta.order_items_pk_ix(orit_id),
  index orcas_orderentry_meta.price_ix(price),
  constraint orit_item_fk foreign key (item_id) references orcas_orderentry_meta.items (item_id),
  constraint orit_ordr_fk foreign key (ordr_id) references orcas_orderentry_data.orders (ordr_id) on delete cascade
);






