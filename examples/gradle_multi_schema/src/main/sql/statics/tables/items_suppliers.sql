create table orcas_orderentry_data.items_suppliers
(
  item_id  number(15)                           not null,
  bpar_id  number(15)                           not null,

  constraint itsu_pk primary key (item_id, bpar_id),
  constraint itsu_bpar_fk foreign key (bpar_id) references orcas_orderentry_meta.business_partners (bpar_id),
  constraint itsu_item_fk foreign key (item_id) references orcas_orderentry_meta.items (item_id)
);

