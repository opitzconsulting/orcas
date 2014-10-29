create table categories alias ctgr domain default_table
(
  name domain identifier,
  description varchar2(1000) not null,

  comment on table is 'categories tabelle';
);

