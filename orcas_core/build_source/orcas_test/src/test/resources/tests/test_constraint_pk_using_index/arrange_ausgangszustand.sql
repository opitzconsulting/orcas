create table tab_new_pk
(
  id1 number(15) not null,
  id2 number(20) not null,
  constraint tab_new_pk primary key (id1)
);

create table tab_mod_pk
(
  id1 number(15) not null,
  id2 number(20) not null,
  constraint tab_mod_pk primary key (id1) using index tab_mod_ix,
  index tab_mod_ix (id1) unique
);


create table tab_wrong_ix
(
  id1 number(15) not null,
  id2 number(20) not null,
  constraint tab_wrong_ix_pk primary key (id1) using index tab_wrong_ix_ix,
  index tab_wrong_ix_ix (id1) unique
);

create table tab_wrong_pk
(
  id1 number(15) not null,
  id2 number(20) not null,
  constraint tab_wrong_pk_pk primary key (id1) using index tab_wrong_pk_ix,
  index tab_wrong_pk_ix (id1) unique
);
 
