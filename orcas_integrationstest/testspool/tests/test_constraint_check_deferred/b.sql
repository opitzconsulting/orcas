create table tab_new
(
  id number(15),
  constraint tab_new_chk check (id > 5) deferrable initially immediate
);


