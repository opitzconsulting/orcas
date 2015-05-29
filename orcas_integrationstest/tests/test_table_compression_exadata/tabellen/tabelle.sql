create table tab_mod_compress_dl
(
  id number(15) not null
) compress for direct_load_operations;

create table tab_compress_all
(
  id number(15) not null
) compress for all_operations;

create table tab_compress_ql
(
  id number(15) not null
) compress for query_low;

create table tab_compress_qh
(
  id number(15) not null
) compress for query_high;

create table tab_compress_al
(
  id number(15) not null
) compress for archive_low;

create table tab_compress_ah
(
  id number(15) not null
) compress for archive_high;