create table items alias item domain default_table
(
  ctgr_id         domain fk_column,
  name            domain identifier,
  description     varchar2(1000)           not null,
  image_location  varchar2(255),
  price domain    price_domain not null,
  in_stock_knz    domain knz_domain,

  comment on table is 'items tabelle';  
);

