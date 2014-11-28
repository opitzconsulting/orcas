create table orders
(
  ordr_id              number(15)                  not null,
  version              number(15)     default "0"  not null,
  shipping_street      varchar2(50),
  shipping_state_city  varchar2(50)                not null,
  shipping_zipcode     varchar2(10),
  shipping_country     varchar2(2),

  constraint ordr_pk primary key (ordr_id)
);




