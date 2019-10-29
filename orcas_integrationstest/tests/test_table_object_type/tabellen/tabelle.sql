create table tab_object_type of tab_test_type
nested table kklasse_list store as tab_nested_table_list;

create table tab_object_type_complex of tab_test_type
(
  constraint tab_object_type_complex_pk primary key (kklasse_xx) using index tab_object_type_complex_ix,
  constraint tab_object_type_complex_ck check ("KKLASSE_XX2 != 'xy'"),
  constraint tab_object_type_complex_uk unique(kklasse_xx2),
  index tab_object_type_complex_ix(kklasse_xx),
  comment on table is 'tablecomment';
  comment on column kklasse_xx is 'columncomment';
)
nested table kklasse_list store as tab_nested_table_list_complex;


