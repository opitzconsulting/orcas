create sequence seq_delete;


create sequence seq_ignore;


create sequence seq_test_update increment by 7;


create sequence seq_test_update_lv start with 4;


create sequence seq_test_update_cycle maxvalue 999 nocycle;
create sequence seq_test_update_order noorder;
create sequence seq_test_update_minvalue minvalue 99;
create sequence seq_test_update_maxvalue maxvalue 999;