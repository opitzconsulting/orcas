create table tab_index
(
  id_nummer_mod number(15) not null,
  id_nummer_rev number(15) not null,
  id_nummer_mod_uk number(15) not null,
  id_nummer_pk number(15) not null,
  id_nummer_mod_def number(15) not null,
  id_nummer_rev_def number(15) not null,
  id_nummer_mod_uk_def number(15) not null
);


create index mod_tabspace_ix on tab_index (id_nummer_mod) tablespace &1; 

create index mod_tabspace_reverse_ix on tab_index (id_nummer_rev) tablespace &2;

alter table tab_index add constraint id_nummer_mod_uk_uk unique (id_nummer_mod_uk) using index tablespace &1;

alter table tab_index add constraint id_nummer_pk_pk primary key (id_nummer_pk) using index tablespace &2;

create index mod_tabspace_def_ix on tab_index (id_nummer_mod_def); 

create index mod_tabspace_reverse_def_ix on tab_index (id_nummer_rev_def) tablespace &2;

alter table tab_index add constraint id_nummer_mod_uk_uk_def unique (id_nummer_mod_uk_def) using index tablespace &1;

