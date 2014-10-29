create table tab_logging_default
(
  id number(15) not null
);

create table tab_logging
(
  id number(15) not null
)
nologging;

create table tab_nologging
(
  id number(15) not null
)
logging;



