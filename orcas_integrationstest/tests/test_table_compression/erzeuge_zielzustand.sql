create table tab_mod_compress
(
  id number(15) not null
) compress;

create table tab_mod_compress_dl
(
  id number(15) not null
) compress for direct_load operations;

create table tab_compress_all
(
  id number(15) not null
) compress for all operations;



