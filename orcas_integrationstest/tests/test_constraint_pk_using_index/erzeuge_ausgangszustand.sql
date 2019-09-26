create table tab_new_pk
(
  id1 number(15) not null,
  id2 number(20) not null
);



alter table tab_new_pk add constraint tab_new_pk primary key (id1);


create table tab_mod_pk
(
  id1 number(15) not null,
  id2 number(20) not null
);

create unique index tab_mod_ix on tab_mod_pk (id1);

alter table tab_mod_pk add constraint tab_mod_pk primary key (id1) using index tab_mod_ix;


create table tab_wrong_ix
(
  id1 number(15) not null,
  id2 number(20) not null
);

create unique index tab_wrong_ix_ix on tab_wrong_ix (id1);

alter table tab_wrong_ix add constraint tab_wrong_ix_pk primary key (id1) using index tab_wrong_ix_ix;


create table tab_wrong_pk
(
  id1 number(15) not null,
  id2 number(20) not null
);

create unique index tab_wrong_pk_ix on tab_wrong_pk (id1);

alter table tab_wrong_pk add constraint tab_wrong_pk_pk primary key (id1) using index tab_wrong_pk_ix;

create table tab_wrong_pk_and_wrong_ix
(
  id1 number(15) not null,
  id2 number(20) not null
);

create unique index tab_wrong_pk_and_wrong_ix_ixw on tab_wrong_pk_and_wrong_ix (id2);

alter table tab_wrong_pk_and_wrong_ix add constraint tab_wrong_pk_and_wrong_ix_pk primary key (id2) using index tab_wrong_pk_and_wrong_ix_ixw;

