create or replace type col_test_type as object
    (  kklasse_before     varchar2(30),
       kklasse_after      varchar2(30),
       map member function concat return varchar2
    ) ;
/

create or replace type col_test_type_list is table of col_test_type
/

create or replace type col_test_type_list_list is table of col_test_type_list
/

create or replace type col_test_type_array as varray(2000) of col_test_type
/

