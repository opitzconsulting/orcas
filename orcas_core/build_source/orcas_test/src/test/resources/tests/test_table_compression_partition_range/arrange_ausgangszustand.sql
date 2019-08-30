create table tab_part_mod_compress_dl
(
  col1 number(15) not null,
  col2 number(15) not null
) 
partition by range (col1,col2) 
(
  partition part_10 values less than ("10","5"),
  partition part_20 values less than (maxvalue,maxvalue)
) nocompress;
