create table business_partners
(
  bpar_id    number(15)                    not null,
  version    number(15)     default "0"    not null,
  TYPE       varchar2(10)                  not null,
  street     varchar2(50)                  not null,
  city       varchar2(30)                  not null,
  state      varchar2(2),
  zipcode    varchar2(10)                  not null,
  country    varchar2(2)                   not null,
  email      varchar2(40)                  not null,
  firstname  varchar2(30),
  lastname   varchar2(30),
  username   varchar2(15),
  password   varchar2(15),
  status     varchar2(8),
  name       varchar2(30),
  website    varchar2(40),

  primary key (bpar_id),
  constraint bpar_uc_name unique (name),
  constraint bpar_uc_username unique (username)
);


