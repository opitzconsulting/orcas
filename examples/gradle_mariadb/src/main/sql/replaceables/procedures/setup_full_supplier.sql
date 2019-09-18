create or replace procedure setup_full_supplier( p_bpar_id bigint )
begin
  declare done int default false;
  declare v_item_id bigint;
  declare cur cursor for 
    select item_id
      from items
     where item_id not in
           (
           select item_id
             from items_suppliers
            where bpar_id = p_bpar_id
           );
  declare continue handler for not found set done = true;

  open cur;

  read_loop: loop
    fetch cur into v_item_id;
    if done then
      leave read_loop;
    end if;

    insert into items_suppliers
           (
           item_id,
           bpar_id
           )
    values (
           v_item_id,
           p_bpar_id
           );
  end loop;
end;
/

