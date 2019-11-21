create table a.tab_a 
( 	
    tab_a_id	number(9)   not null,
    constraint  pk_tab_a    primary key (tab_a_id),
);
	
create table b.tab_b
(
	ref_tab_a_id					number(8),
	constraint fk_tab_a foreign key (ref_tab_a_id) references a.tab_a (tab_a_id) on delete cascade
);

