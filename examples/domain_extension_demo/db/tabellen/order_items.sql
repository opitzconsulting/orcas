create table order_items alias orit domain default_table
(
  orit_ordr_id   domain fk_column_alias_prefix                    not null,
  orit_item_id   domain fk_column_alias_prefix                    not null,
  price     domain price_domain not null,
  quantity  number(4)                           not null,

  constraint orit_uc unique (orit_ordr_id, orit_item_id)
);






