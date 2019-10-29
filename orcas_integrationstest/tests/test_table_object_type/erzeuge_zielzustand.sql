@@create_types.sql

create table tab_object_type of tab_test_type
nested table kklasse_list store as tab_nested_table_list
/

create table tab_object_type_complex of tab_test_type
(
  constraint tab_object_type_complex_ck check (KKLASSE_XX2 != 'xy'),
  constraint tab_object_type_complex_uk unique(kklasse_xx2)
)
nested table kklasse_list store as tab_nested_table_list_complex
/

create index tab_object_type_complex_ix on tab_object_type_complex (kklasse_xx);
alter table tab_object_type_complex add constraint tab_object_type_complex_pk primary key (kklasse_xx) using index tab_object_type_complex_ix;

comment on table tab_object_type_complex is 'tablecomment';
comment on column tab_object_type_complex.kklasse_xx is 'columncomment';

