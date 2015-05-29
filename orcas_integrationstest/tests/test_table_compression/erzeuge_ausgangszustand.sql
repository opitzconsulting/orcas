create table tab_mod_compress
(
  id number(15) not null
) nocompress;

create table tab_mod_compress_dl
(
  id number(15) not null
) compress for all operations;

