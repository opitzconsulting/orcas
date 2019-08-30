create table tab_index
(
  id_nummer_mod number(15) not null,
  id_nummer_rev number(15) not null,
  id_nummer_mod_uk number(15) not null,
  id_nummer_pk number(15) not null,
  id_nummer_mod_def number(15) not null,
  id_nummer_rev_def number(15) not null,
  id_nummer_mod_uk_def number(15) not null,

  constraint id_nummer_pk_pk primary key (id_nummer_pk) using index tablespace TABLESPACE2,
  index mod_tabspace_ix (id_nummer_mod) tablespace TABLESPACE1,
  index mod_tabspace_reverse_ix (id_nummer_rev) tablespace TABLESPACE2,
  constraint id_nummer_mod_uk_uk unique (id_nummer_mod_uk) using index tablespace TABLESPACE1,
  index mod_tabspace_def_ix (id_nummer_mod_def),
  index mod_tabspace_reverse_def_ix (id_nummer_rev_def) tablespace TABLESPACE2,
  constraint id_nummer_mod_uk_uk_def unique (id_nummer_mod_uk_def) using index tablespace TABLESPACE1
);

