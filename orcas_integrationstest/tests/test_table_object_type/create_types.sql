create or replace type test_type as object
    (  kklasse_before     varchar2(30),
       kklasse_after      varchar2(30)
    ) ;
/

create or replace type test_type_list is table of test_type
/

create or replace type tab_test_type as object
    (  kklasse_xx         varchar2(30),
       kklasse_xx2         varchar2(30),
       kklasse_list       test_type_list
    ) ;
/


