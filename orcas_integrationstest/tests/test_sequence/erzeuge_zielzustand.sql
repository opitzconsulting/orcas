create table tab_dummy
( 	
    col1		number(5)
);

create sequence seq_test_create start with 12 increment by 20;

create sequence seq_test_default increment by 1;

create sequence seq_ignore;

/* 4 ist eigentlich egal, wird aber durch das update erzeugt */
create sequence seq_test_update increment by 10 start with 4; 

create sequence seq_test_update_lv start with 13;

create sequence seq_test_invalid_update start with 1;

create sequence seq_test_cycle nocycle;

create sequence seq_test_update_cycle maxvalue 999 cycle;

create sequence seq_test_order order;

create sequence seq_test_update_order order;

create sequence seq_test_minvalue minvalue 10;

/* 99 ist eigentlich egal, wird aber durch das update erzeugt */
create sequence seq_test_update_minvalue minvalue 10 start with 99;

create sequence seq_test_maxvalue maxvalue 100;

create sequence seq_test_update_maxvalue maxvalue 100;

create sequence seq_test_cache cache 100;

create sequence seq_test_update_cache cache 50;

create sequence seq_test_cache_nocache cache 0;