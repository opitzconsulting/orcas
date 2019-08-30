create table tab_index
(
  id_nummer_mod number(15) not null,
  id_nummer_rev number(15) not null,
  id_nummer_mod_uk number(15) not null,
  id_nummer_pk number(15) not null, 
  id_nummer_mod_def number(15) not null,
  id_nummer_rev_def number(15) not null,
  id_nummer_mod_uk_def number(15) not null,      
  id_nummer_int number(15) not null,
  id_nummer_ext number(15) not null,
  id_nummer_uk number(15) not null,  

  constraint id_nummer_pk_pk primary key (id_nummer_pk) using index tablespace TABLESPACE1,
  index id_nummer_int_ix (id_nummer_int) tablespace TABLESPACE1,
  constraint id_nummer_mod_uk_uk unique (id_nummer_mod_uk) using index tablespace TABLESPACE2,
  constraint id_nummer_uk_uk unique (id_nummer_uk) using index tablespace TABLESPACE1,
  index mod_tabspace_ix (id_nummer_mod) tablespace TABLESPACE2,
  index mod_tabspace_reverse_ix (id_nummer_rev) tablespace TABLESPACE1,
  index mod_tabspace_def_ix (id_nummer_mod_def) tablespace TABLESPACE1,
  index mod_tabspace_reverse_def_ix (id_nummer_rev_def),  
  constraint id_nummer_mod_uk_uk_def unique (id_nummer_mod_uk_def)
);

create index id_nummer_ext_ix on tab_index (id_nummer_ext) tablespace TABLESPACE1;

