create table tab_mod_compress_dl
(
  id number(15) not null
) compress for direct_load operations;

create table tab_compress_all
(
  id number(15) not null
) compress for all operations;

create table tab_compress_ql
(
  id number(15) not null
) compress for query low;

create table tab_compress_qh
(
  id number(15) not null
) compress for query high;

create table tab_compress_al
(
  id number(15) not null
) compress for archive low;

create table tab_compress_ah
(
  id number(15) not null
) compress for archive high;

