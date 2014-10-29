create table order_items alias orit domain default_table
(
  ordr_id   domain fk_column                    not null,
  item_id   domain fk_column                    not null,
  price     domain price_domain not null,
  quantity  number(4)                           not null,

  constraint orit_uc unique (ordr_id, item_id)
);






