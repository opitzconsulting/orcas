create table items
(
  item_id         number(15)                    not null,
  version         number(15)      default "0"   not null,
  ctgr_id         number(15)                    not null,
  name            varchar2(30)             not null,
  description     varchar2(1000)           not null,
  image_location  varchar2(255),
  price           number(8,2)  not null,
  priceok         boolean,

  constraint item_pk primary key (item_id),
  constraint item_uc unique (name),
  constraint item_ctgr_fk foreign key (ctgr_id) references categories (ctgr_id),

  comment on column version is 'default ist null';
  comment on table is 'items tabelle';  
);

