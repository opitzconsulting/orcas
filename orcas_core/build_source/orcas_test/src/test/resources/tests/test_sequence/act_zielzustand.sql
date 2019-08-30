
create table tab_dummy
( 	
    col1		number(5)
);


create sequence seq_test_create start with 12 increment by 20;

create sequence seq_test_default;

create sequence seq_test_update increment by 10;

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

create sequence seq_test_cache_nocache cache 0;

create sequence seq_test_negative start with -1 increment by -1 maxvalue -1 minvalue -999;
