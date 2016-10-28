
create table tab_a
(
	tab_a_id					number(9) constraint pk_tab_a primary key not null,
	tab_a_a_id					number(8),
	constraint fk_recursive foreign key (tab_a_a_id) references tab_a (tab_a_id)
);

