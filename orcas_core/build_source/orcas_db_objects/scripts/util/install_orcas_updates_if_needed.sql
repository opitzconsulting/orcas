-- installing this table should be done using orcas (which is a bit difficult becaus it is currently beeing installed...)
-- try rename from scs_updates which ist the old name of the table
begin
  execute immediate 'alter table scs_updates rename to orcas_updates';
exception
  when others then
    if (sqlcode != -942) 
    then
      raise;
    end if;
end;
/

-- create if needed
begin
  execute immediate 'create table orcas_updates ( scup_id           number(22)                  not null, scup_script_name  varchar2(4000 byte)         not null, scup_logname      varchar2(100 byte)          not null, scup_date         date                        not null, scup_schema       varchar2(30 byte)           not null)';
exception
  when others then
    if (sqlcode != -955) 
    then
      raise;
    end if;
end;
/

