create or replace function get_supplied_item_count( p_bpar_id in number ) return number is
  v_return number;
begin
  select count(1)
    into v_return
    from items_suppliers
   where bpar_id = p_bpar_id;

  return v_return;
end;
/
