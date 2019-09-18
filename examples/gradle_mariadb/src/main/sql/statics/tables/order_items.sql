create table order_items
(
  orit_id   number(15)                          not null,
  version   number(15)       default "0"        not null,
  ordr_id   number(15)                          not null,
  item_id   number(15)                          not null,
  price     number(8,2)                         not null,
  quantity  number(4)                           not null,

  primary key (orit_id),
  constraint orit_uc unique (ordr_id, item_id),
  constraint orit_item_fk foreign key (item_id) references items (item_id),
  constraint orit_ordr_fk foreign key (ordr_id) references orders (ordr_id) on delete cascade
);






