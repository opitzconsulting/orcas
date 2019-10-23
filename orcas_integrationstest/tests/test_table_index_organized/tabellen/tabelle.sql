create table tab_iot
(
  id number(15) not null,
  constraint tab_iot_pk primary key (id)
) organization index;

create table tab_iot_overflow
(
  id  number(15) not null,
  id2 number(15) not null,
  id3 number(15) not null,
  constraint tab_iot_overflow_pk primary key (id)
) tablespace replaceme1 organization index pctthreshold 20 including id2 overflow tablespace replaceme2;




