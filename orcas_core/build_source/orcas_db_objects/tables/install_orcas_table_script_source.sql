-- installing this table should be done using orcas (which is a bit difficult becaus it is currently beeing installed...)
begin
  execute immediate 'drop table orcas_table_script_source';
exception
  when others then
    if (sqlcode != -942) 
    then
      raise;
    end if;
end;
/

create table orcas_table_script_source
(
  id      number(15)                            primary key,
  script  varchar2(4000 byte)                   not null
);


