create table &2..tab_b
(
	ref_tab_a_id					number(8),
	constraint fk_tab_a foreign key (ref_tab_a_id) references &1..tab_a (tab_a_id) on delete cascade
);
	
