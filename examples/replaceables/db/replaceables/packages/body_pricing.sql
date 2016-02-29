create or replace package body pricing as
  function get_price(p_item_id in number, p_quantity in number) return number
  is
    v_return number;
  begin
    select items.price * p_quantity
      into v_return
      from items
     where p_item_id = items.item_id;

    return v_return;
  end;
end;
/
