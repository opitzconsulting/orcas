create table scs_updates
(
  scup_id           number(22)                  not null,
  scup_script_name  varchar2(4000 byte)         not null,
  scup_logname      varchar2(100 byte)          not null,
  scup_date         date                        not null,
  scup_schema       varchar2(30 byte)           not null
);


