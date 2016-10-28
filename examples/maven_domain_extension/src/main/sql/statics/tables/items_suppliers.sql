create table items_suppliers alias itsp domain version_table
(
  item_id  domain fk_column  not null,
  bpar_id  domain fk_column  not null,

  constraint itsp_pk primary key (item_id, bpar_id)
);

