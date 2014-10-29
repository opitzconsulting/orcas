create table orders alias ordr domain default_table
(
  bpar_id           domain fk_column       not null,
  orderdate         date                   not null,
  tracking_number   varchar2(20)           not null,
  status            varchar2(10)           not null,
  shipping_street   varchar2(50)           not null,
  shipping_city     varchar2(30)           not null,
  shipping_state    varchar2(2),
  shipping_zipcode  varchar2(10)           not null,
  shipping_country  varchar2(2)            not null,

  constraint ordr_uc unique (tracking_number)
);




