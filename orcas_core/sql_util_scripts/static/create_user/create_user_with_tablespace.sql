create user &1 identified by &2 default tablespace &3;
grant connect to &1;
grant resource to &1;
grant create any synonym to &1;
grant unlimited tablespace to &1;
grant drop any synonym to &1;
grant select any table to &1;
grant create materialized view to &1;

