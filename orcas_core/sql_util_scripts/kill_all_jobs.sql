declare
  v_session_count number;
begin
  for cur_job in
    (
    select job 
      from user_jobs
     where not(&1)
    ) loop
    dbms_job.broken( cur_job.job, true );
  end loop;
  commit;
  
  loop    
    for cur_session in
      (  
      select sid,
             serial# ser 
        from sys.v_$session
       where sid in
             (
             select sid
               from sys.v_$lock 
              where type = 'JQ'
                and id2 in
                    (
                    select job 
                      from user_jobs
                      where not(&1)
                    ) 
             ) 
      ) 
    loop
      begin
        execute immediate 'ALTER SYSTEM KILL SESSION ''' || cur_session.sid || ',' || cur_session.ser || ''' IMMEDIATE'; 
      exception
        when others then null;
      end;
    end loop;

    select count(1)
      into v_session_count
      from sys.v_$session
     where sid in
           (
           select sid
             from sys.v_$lock 
            where type = 'JQ'
              and id2 in
                  (
                  select job 
                    from user_jobs
                    where not(&1)
                  ) 
           )
       and status != 'KILLED';

    if( v_session_count = 0 )          
    then
      exit;
    end if;    
  end loop;

  commit;

  for cur_job in
  
    (
    select job 
      from user_jobs
     where not(&1)
    ) loop
    dbms_job.remove( cur_job.job );
  end loop;
  commit;        
  declare 
  job_count number(3);
  begin
    select count(job) into job_count from user_jobs where not(&1);
    if job_count>0 then
      raise_application_error(-20011,'Es sind noch Jobs vorhanden die evtl. Ressourcen verbrauchen, bitten ueberpruefen Sie dies!');
    end if;
  end;    
end;
/