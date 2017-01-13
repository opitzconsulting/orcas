create table tab_mod_compress
(
  id number(15) not null
) pctfree 0 nocompress;

create table tab_mod_compress_dl
(
  id number(15) not null
) pctfree 0 compress for all operations;

