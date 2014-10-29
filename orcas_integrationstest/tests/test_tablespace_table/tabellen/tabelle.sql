create table tab_lob
(
  clob_column  clob, 
  blob_column  blob,
  clob_column_normal clob
)
lob ( clob_column )
store as 
(
  tablespace replaceme1
),
lob ( blob_column )
store as 
(
  tablespace replaceme1
) tablespace replaceme1;

create table tab_mod_tablespace
(
  id number(15) not null
) tablespace replaceme1;

create table tab_mod_tablespace_reverse
(
  id number(15) not null
) tablespace replaceme2;

create table tab_lob_2
(
  clob_column  clob, 
  blob_column  blob,
  clob_column_normal clob
)
tablespace replaceme1;

create table tab_mod_tablespace_default
(
  id number(15) not null
) tablespace replaceme1;

create table tab_mod_tablespace_reverse_def
(
  id number(15) not null
);


