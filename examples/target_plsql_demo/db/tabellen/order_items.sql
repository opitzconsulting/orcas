create table order_items
(
  orit_id   number(15)  ,
  ordr_id   number(15)  ,
  version   number(15)       default "0"        not null,
  item_id   number(15)                          not null,
  price     number(8,2)                         not null,
  quantity  number(4)                           not null
);






