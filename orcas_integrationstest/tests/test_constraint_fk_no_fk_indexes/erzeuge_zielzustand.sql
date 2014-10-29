create table tab_a 
( 	
    tab_a_id					number(9) not null,
    constraint pk_tab_a primary key (tab_a_id)
);
	
create table tab_ref
(
	tab_a_id					number(8),
	constraint fk_tab_del_cas_tab_a_id foreign key (tab_a_id) references tab_a (tab_a_id) on delete cascade
);
	



