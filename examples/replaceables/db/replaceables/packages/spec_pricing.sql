create or replace package pricing as
  function get_price(p_item_id in number, p_quantity in number) return number;
end;
/
