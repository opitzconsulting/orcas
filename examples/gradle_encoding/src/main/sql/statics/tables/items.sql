create table ITEMS
(
  ITEM_ID number(15) not null,
  VERSION number(15) default "0" not null,
  CTGR_ID number(15) not null,
  NAME varchar2(30) not null,
  DESCRIPTION varchar2(1000) not null,
  IMAGE_LOCATION varchar2(255),
  PRICE number(8,2) not null,

  constraint ITEM_PK primary key (ITEM_ID),

  index ITEM_CTGR_FK_GEN_IX (CTGR_ID),
  constraint ITEM_UC unique (NAME),

  constraint ITEM_CTGR_FK foreign key (CTGR_ID) references CATEGORIES (CTGR_ID),

  comment on table is 'items tabelle';
  comment on column VERSION is 'default ist null';
);

