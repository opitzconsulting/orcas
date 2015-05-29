
create table tab_dummy
( 	
    col1		number(5)
);


create sequence seq_test_create increment by 20;

create sequence seq_test_default;

create sequence seq_test_update increment by 10;

create sequence seq_test_update_lv orcas_ext_max_value_select "select 12 from dual";

create sequence seq_test_invalid_update orcas_ext_max_value_select "select 15 from tab_dummy";

create sequence seq_test_cycle nocycle;

create sequence seq_test_update_cycle maxvalue 999 cycle;

create sequence seq_test_order order;

create sequence seq_test_update_order order;

create sequence seq_test_minvalue minvalue 10;

create sequence seq_test_update_minvalue minvalue 10;

create sequence seq_test_maxvalue maxvalue 100;

create sequence seq_test_update_maxvalue maxvalue 100;

create sequence seq_test_cache cache 100;

create sequence seq_test_update_cache cache 50;
