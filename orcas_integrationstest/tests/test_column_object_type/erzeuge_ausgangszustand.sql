create or replace type col_test_type as object
    (  kklasse_before     varchar2(30),
       kklasse_after      varchar2(30),
       map member function concat return varchar2
    ) ;
/

