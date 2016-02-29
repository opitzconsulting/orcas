CREATE OR REPLACE PROCEDURE setup_full_supplier( p_bpar_id in number ) is
begin
  for cur_items in
  (
  select item_id
    from items
   where item_id not in
         (
         select item_id
           from items_suppliers
          where bpar_id = p_bpar_id
         )
  )
  loop
    insert into items_suppliers
           (
           item_id,
           bpar_id
           )
    values (
           cur_items.item_id,
           p_bpar_id
           );
  end loop;
end;
/

