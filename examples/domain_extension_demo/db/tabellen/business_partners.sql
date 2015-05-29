create table business_partners alias bpar domain default_table
(
  TYPE       varchar2(10)                  not null,
  street     varchar2(50)                  not null,
  city       varchar2(30)                  not null,
  state      varchar2(2),
  zipcode    varchar2(10)                  not null,
  country    varchar2(2)                   not null,
  email      varchar2(40)                  not null,
  firstname  varchar2(30),
  lastname   varchar2(30),
  username   domain identifier,
  password   varchar2(15),
  status     varchar2(8),
  name       domain identifier,
  website    varchar2(40)
);


