create table tab_a 
( 	
    tab_a_id					number(9) constraint pk_tab_a primary key not null
);
	
create table tab_b 
(
 	tab_b_id					number(9) constraint pk_tab_b primary key not null
);	
	
create table tab_c 
(
 	tab_c_id					number(9) constraint pk_tab_c primary key not null
);

create table tab_d
(
    "tab d id"	                number(9) constraint pk_tab_d primary key not null
);

create table tab_del_cas
(
	tab_a_id					number(8),
	tab_b_id					number(8),
	tab_c_id					number(8),
	tab_d_id					number(8),
	constraint fk_tab_del_cas_tab_a_id foreign key (tab_a_id) references tab_a (tab_a_id) on delete cascade,
	constraint fk_tab_del_cas_tab_b_id foreign key (tab_b_id) references tab_b (tab_b_id) on delete cascade,
	constraint fk_tab_del_cas_tab_c_id foreign key (tab_c_id) references tab_c (tab_c_id) on delete cascade,
	constraint fk_tab_del_cas_tab_d_id foreign key (tab_d_id) references tab_d ("tab d id") on delete cascade
);
	
create table tab_no_action
(
	tab_a_id					number(8),
	tab_b_id					number(8),
	tab_c_id					number(8),
    tab_d_id					number(8),
	constraint fk_tab_no_action_tab_a_id foreign key (tab_a_id) references tab_a (tab_a_id),
	constraint fk_tab_no_action_tab_b_id foreign key (tab_b_id) references tab_b (tab_b_id),
	constraint fk_tab_no_action_tab_c_id foreign key (tab_c_id) references tab_c (tab_c_id),
	constraint fk_tab_no_action_tab_d_id foreign key (tab_d_id) references tab_d ("tab d id")
);
	
create table tab_set_null
(
	tab_a_id					number(8),
	tab_b_id					number(8),
	tab_c_id					number(8),
	tab_d_id					number(8),
	constraint fk_tab_set_null_tab_a_id foreign key (tab_a_id) references tab_a (tab_a_id) on delete set null,
	constraint fk_tab_set_null_tab_b_id foreign key (tab_b_id) references tab_b (tab_b_id) on delete set null,
	constraint fk_tab_set_null_tab_c_id foreign key (tab_c_id) references tab_c (tab_c_id) on delete set null,
	constraint fk_tab_set_null_tab_d_id foreign key (tab_d_id) references tab_d ("tab d id") on delete set null
);

create table tab_2_column_pk
(	id1				number(20),
	id2				number(30),
	constraint pk_tab_2_column_pk primary key (id1, id2)
);

create table tab_wrong_fk_name
(
	id				number(9),
	constraint fk_tab_right_fk_name foreign key (id) references tab_a (tab_a_id)
);

create table tab_wrong_fk_column
(
	id				number(9),
	constraint fk_tab_wrong_fk_column foreign key(id) references tab_b (tab_b_id)
);

create table tab_wrong_fk_column_order
(
	id1				number(20),
	id2				number(30),
	constraint fk_tab_wrong_fk_column_order foreign key(id1, id2) references tab_2_column_pk (id1, id2)
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
    constraint  pk_tab_index_test    primary key (tab_index_test_id)
);

create table tab_index_test_ref
(
	tab_a_id				number(9),
	tab_b_id				number(9),
	constraint uk_tab_a_b_id unique (tab_a_id, tab_b_id),
	constraint fk_tab_index_test_ref_a_id foreign key (tab_a_id) references tab_index_test (tab_index_test_id) on delete cascade enable    
);
	
create index FK_TAB_DEL_CAS_TAB_A_ID_GEN_IX on tab_del_cas(tab_a_id);
create index FK_TAB_DEL_CAS_TAB_B_ID_GEN_IX on tab_del_cas(tab_b_id);
create index FK_TAB_DEL_CAS_TAB_C_ID_GEN_IX on tab_del_cas(tab_c_id);
create index FK_TAB_DEL_CAS_TAB_D_ID_GEN_IX on tab_del_cas(tab_d_id);
create index FK_TAB_NO_ACTION_TAB_A__GEN_IX on tab_no_action(tab_a_id);
create index FK_TAB_NO_ACTION_TAB_B__GEN_IX on tab_no_action(tab_b_id);
create index FK_TAB_NO_ACTION_TAB_C__GEN_IX on tab_no_action(tab_c_id);
create index FK_TAB_NO_ACTION_TAB_D__GEN_IX on tab_no_action(tab_d_id);
create index FK_TAB_SET_NULL_TAB_A_I_GEN_IX on tab_set_null(tab_a_id);
create index FK_TAB_SET_NULL_TAB_B_I_GEN_IX on tab_set_null(tab_b_id);
create index FK_TAB_SET_NULL_TAB_C_I_GEN_IX on tab_set_null(tab_c_id);
create index FK_TAB_SET_NULL_TAB_D_I_GEN_IX on tab_set_null(tab_d_id);

create index FK_TAB_WRONG_FK_COLUMN_GEN_IX  on TAB_WRONG_FK_COLUMN(id);
create index FK_TAB_WRONG_FK_COLUMN__GEN_IX on TAB_WRONG_FK_COLUMN_ORDER(id1,id2);
create index FK_TAB_RIGHT_FK_NAME_GEN_IX    on TAB_WRONG_FK_NAME(id);
create index FK_TAB_RIGHT_DEFERRED_GEN_IX   on TAB_WRONG_DEFERRED(id);

CREATE INDEX FK_MIXED_NAME_1_GEN_IX ON TAB_MIXED_NAME_1(id);
CREATE INDEX FK_MIXED_NAME_INTAB_1_GEN_IX ON TAB_MIXED_NAME_1(id_1);
CREATE INDEX FK_MIXED_NAME_INTAB_2_GEN_IX ON TAB_MIXED_NAME_1(id_2);
CREATE INDEX FK_MIXED_NAME_2_GEN_IX ON TAB_MIXED_NAME_2(id);
