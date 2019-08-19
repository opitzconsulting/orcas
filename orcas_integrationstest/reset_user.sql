alter session set "_ORACLE_SCRIPT"=true;
drop user  &1  cascade;
create user &1 identified by &2 default tablespace &3;
grant create session to &1;
grant connect to 		&1;
grant resource to 		&1;
grant create materialized view to &1;
grant unlimited tablespace to &1;

