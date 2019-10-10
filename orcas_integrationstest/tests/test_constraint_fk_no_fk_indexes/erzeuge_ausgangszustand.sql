create table tab_a 
( 	
    tab_a_id					number(9) not null,
    constraint pk_tab_a primary key (tab_a_id)
);
	
create table tab_ref
(
	tab_a_id					number(8),
	constraint fk_wrong_name foreign key (tab_a_id) references tab_a (tab_a_id) on delete cascade
);
	

create table tab_old_disable
(
	id				number(9) not null,
	constraint tab_old_disable_cc foreign key (id) references tab_a (tab_a_id) enable
);

create table tab_old_enable
(
	id				number(9) not null,
	constraint tab_old_enable_cc foreign key (id) references tab_a (tab_a_id) disable
);
	




