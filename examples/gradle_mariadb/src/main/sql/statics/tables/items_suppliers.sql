create table items_suppliers
(
  item_id  number(15)                           not null,
  bpar_id  number(15)                           not null,

  primary key (item_id, bpar_id),
  constraint itsu_bpar_fk foreign key (bpar_id) references business_partners (bpar_id),
  constraint itsu_item_fk foreign key (item_id) references items (item_id)
);

