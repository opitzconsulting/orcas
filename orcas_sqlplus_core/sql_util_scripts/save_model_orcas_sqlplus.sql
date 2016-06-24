
begin
  delete orcas_sqlplus_model;

  insert into orcas_sqlplus_model
  values ( sys.anydata.convertobject( pa_orcas_sqlplus_model_holder.get_model() ) );

  commit;
end;
/
          
