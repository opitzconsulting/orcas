create or replace function get_supplied_item_count( p_bpar_id bigint ) returns bigint
begin
  return ( select count(1) from items_suppliers where bpar_id = p_bpar_id );
end;
/
