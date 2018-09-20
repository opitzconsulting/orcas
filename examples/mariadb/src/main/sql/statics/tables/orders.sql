create table orders
(
  ordr_id           number(15)                  not null,
  version           number(15)     default "0"  not null,
  bpar_id           number(15)                  not null,
  orderdate         date                        not null,
  tracking_number   varchar2(20)           not null,
  status            varchar2(10)           not null,
  shipping_street   varchar2(50)           not null,
  shipping_city     varchar2(30)           not null,
  shipping_state    varchar2(2),
  shipping_zipcode  varchar2(10)           not null,
  shipping_country  varchar2(2)            not null,

  primary key (ordr_id),
  constraint ordr_uc unique (tracking_number),
  constraint ordr_bpar_fk foreign key (bpar_id) references business_partners (bpar_id)
);




