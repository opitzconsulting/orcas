create table tab_a 
( 	
    tab_a_id	number(9)   not null,
    constraint  pk_tab_a    primary key (tab_a_id),
);
	
create table tab_b
(
	tab_b_id	number(9)	not null,
	constraint pk_tab_b primary key (tab_b_id),
);

create table tab_c
(
	tab_c_id	number(9)	not null,
	constraint pk_tab_c primary key (tab_c_id),
);

create table tab_del_cas
(
	tab_a_id				number(8),
	tab_b_id				number(8),
	tab_c_id				number(8),
	
	constraint fk_tab_del_cas_tab_a_id foreign key (tab_a_id) references tab_a (tab_a_id) on delete cascade enable,
	constraint fk_tab_del_cas_tab_b_id foreign key (tab_b_id) references tab_b (tab_b_id) on delete cascade enable,
	constraint fk_tab_del_cas_tab_c_id foreign key (tab_c_id) references tab_c (tab_c_id) on delete cascade enable,
);
	
create table tab_no_action
(
	tab_a_id				number(8),
	tab_b_id				number(8),
	tab_c_id				number(8),
	
	constraint fk_tab_no_action_tab_a_id foreign key (tab_a_id) references tab_a (tab_a_id) on delete no_action disable,
	constraint fk_tab_no_action_tab_b_id foreign key (tab_b_id) references tab_b (tab_b_id) on delete no_action disable,
	constraint fk_tab_no_action_tab_c_id foreign key (tab_c_id) references tab_c (tab_c_id) on delete no_action disable,
);

create table tab_set_null
(
	tab_a_id				number(8),
	tab_b_id				number(8),
	tab_c_id				number(8),
	
	constraint fk_tab_set_null_tab_a_id foreign key (tab_a_id) references tab_a (tab_a_id) on delete set_null,
	constraint fk_tab_set_null_tab_b_id foreign key (tab_b_id) references tab_b (tab_b_id) on delete set_null,
	constraint fk_tab_set_null_tab_c_id foreign key (tab_c_id) references tab_c (tab_c_id) on delete set_null,
);

create table tab_2_column_pk
(	id1				number(20) not null,
	id2				number(30) not null,
	constraint pk_tab_2_column_pk primary key (id1, id2),
);

create table tab_wrong_fk_name
(
	id				number(9),
	constraint fk_tab_right_fk_name foreign key (id) references tab_a (tab_a_id),
);

create table tab_wrong_fk_column
(
	id				number(9),
	constraint fk_tab_wrong_fk_column foreign key(id) references tab_b (tab_b_id),
);

create table tab_wrong_fk_column_order
(
	id1				number(20),
	id2				number(30),
	constraint fk_tab_wrong_fk_column_order foreign key(id1, id2) references tab_2_column_pk (id1,id2),
);

create table tab_wrong_deferred
(
	id				number(9),
	constraint fk_tab_right_deferred foreign key (id) references tab_a (tab_a_id) deferrable initially deferred
);

create table tab_mixed_name_1
(
	id				number(9),
	id_1				number(9),
	id_2				number(9),
	constraint fk_mixed_name_1 foreign key (id) references tab_a (tab_a_id),
	constraint fk_mixed_name_intab_1 foreign key (id_1) references tab_a (tab_a_id),
	constraint fk_mixed_name_intab_2 foreign key (id_2) references tab_b (tab_b_id)
);

create table tab_mixed_name_2
(
	id				number(9),
	constraint fk_mixed_name_2 foreign key (id) references tab_a (tab_a_id)
);

create table tab_index_test 
( 	
    tab_index_test_id	number(9)   not null,
    constraint  pk_tab_index_test    primary key (tab_index_test_id),
);

create table tab_index_test_ref
(
	tab_a_id				number(9),
	tab_b_id				number(9),
	constraint uk_tab_a_b_id unique (tab_a_id, tab_b_id),
	constraint fk_tab_index_test_ref_a_id foreign key (tab_a_id) references tab_index_test (tab_index_test_id) on delete cascade enable,    
);
