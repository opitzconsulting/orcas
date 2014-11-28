create table orders 
(  
  ordr_id           NUMBER(15)        not null,
  shipping_city     varchar2(30)      not null,
  shipping_state    varchar2(2)
);


insert into orders values (1, 'Berlin', 'B');
insert into orders values (2, 'Essen', 'NW');
insert into orders values (3, 'Muenchen', 'BY');

commit;

