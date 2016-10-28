create table order_items
(
  orit_id   domain pk_column,
  ordr_id   domain fk_column,
  version   number(15)       default "0"        not null,
  item_id   number(15)                          not null,
  price     number(8,2)                         not null,
  quantity  number(4)                           not null
);






