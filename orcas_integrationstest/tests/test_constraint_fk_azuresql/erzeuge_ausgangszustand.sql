create table tab_a
( 	
    tab_a_id		NUMERIC(9) constraint pk_tab_a primary key not null
);
	
create table tab_b
(
 	tab_b_id		NUMERIC(9) constraint pk_tab_b primary key not null
);	
	
create table tab_c
(
 	tab_c_id		NUMERIC(9) constraint pk_tab_c primary key not null
);	
	
create table tab_del_cas
(
	tab_a_id		NUMERIC(9),
	tab_b_id		NUMERIC(9),
	tab_c_id		NUMERIC(9),
	constraint fk_tab_del_cas_tab_a_id foreign key (tab_a_id) references tab_a (tab_a_id) on delete cascade,
	constraint fk_tab_del_cas_tab_b_id foreign key (tab_b_id) references tab_b (tab_b_id) on delete set null,
	constraint fk_tab_del_cas_tab_c_id foreign key (tab_c_id) references tab_c (tab_c_id)
);
	
create table tab_no_action
(
	tab_a_id		NUMERIC(9),
	tab_b_id		NUMERIC(9),
	tab_c_id		NUMERIC(9),
	constraint fk_tab_no_action_tab_a_id foreign key (tab_a_id) references tab_a (tab_a_id) on delete cascade,
	constraint fk_tab_no_action_tab_b_id foreign key (tab_b_id) references tab_b (tab_b_id) on delete set null,
	constraint fk_tab_no_action_tab_c_id foreign key (tab_c_id) references tab_c (tab_c_id)
);
	
create table tab_set_null
(
	tab_a_id		NUMERIC(9),
	tab_b_id		NUMERIC(9),
	tab_c_id		NUMERIC(9),
	constraint fk_tab_set_null_tab_a_id foreign key (tab_a_id) references tab_a (tab_a_id) on delete cascade,
	constraint fk_tab_set_null_tab_b_id foreign key (tab_b_id) references tab_b (tab_b_id) on delete set null,
	constraint fk_tab_set_null_tab_c_id foreign key (tab_c_id) references tab_c (tab_c_id)
);

create table tab_2_column_pk
(	id1				NUMERIC(9),
	id2				NUMERIC(9),
	constraint pk_tab_2_column_pk primary key (id2, id1)
);

create table tab_wrong_fk_name
(
	id				NUMERIC(9),
	constraint fk_tab_wrong_fk_name foreign key (id) references tab_a (tab_a_id)
);

create table tab_wrong_fk_column
(
	id				NUMERIC(9),
	constraint fk_tab_wrong_fk_column foreign key(id) references tab_c (tab_c_id)
);

create table tab_wrong_fk_column_order
(
	id1				NUMERIC(9),
	id2				NUMERIC(9),
	constraint fk_tab_wrong_fk_column_order foreign key(id2, id1) references tab_2_column_pk (id2, id1)
);

create table tab_mixed_name_1
(
	id				NUMERIC(9),
	id_1				NUMERIC(9),
	id_2				NUMERIC(9),
	constraint fk_mixed_name_2 foreign key (id) references tab_a (tab_a_id),
	constraint fk_mixed_name_intab_2 foreign key (id_1) references tab_a (tab_a_id),
	constraint fk_mixed_name_intab_1 foreign key (id_2) references tab_b (tab_b_id)
);

create table tab_mixed_name_2
(
	id				NUMERIC(9),
	constraint fk_mixed_name_1 foreign key (id) references tab_a (tab_a_id)
);
	
