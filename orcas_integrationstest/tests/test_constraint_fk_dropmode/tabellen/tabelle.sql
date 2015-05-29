create table tab_a
( 	
    tab_a_id	number(9)	not null,
    constraint pk_tab_a primary key (tab_a_id)
);

create table tab_del_cascade
(
	id        		number(8),
	tab_a_id		number(8),
	constraint fk_tab_del_cascade foreign key (tab_a_id) references tab_a (tab_a_id) on delete cascade
);
	
create table tab_set_null
(
	id        		number(8),
	tab_a_id		number(8),
	constraint fk_tab_set_null foreign key (tab_a_id) references tab_a (tab_a_id) on delete set_null
);

