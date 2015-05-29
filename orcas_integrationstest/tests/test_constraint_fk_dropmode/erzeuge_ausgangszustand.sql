create table tab_a
( 	
    tab_a_id	number(9)	not null,
    constraint pk_tab_a primary key (tab_a_id)
);

create table tab_del_cascade
(
	id        		number(8),
	tab_a_id		number(8)
);
	
create table tab_set_null
(
	id        		number(8),
	tab_a_id		number(8)
);

insert into tab_del_cascade
       (
       id,
       tab_a_id
       )	
values (
       1,
       1
       );

insert into tab_set_null
       (
       id,
       tab_a_id
       )	
values (
       1,
       1
       );

commit;

