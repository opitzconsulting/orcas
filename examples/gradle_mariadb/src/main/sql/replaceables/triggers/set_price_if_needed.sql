create or replace trigger set_price_if_needed
before insert
on ORDER_ITEMS
for each row
begin
  if( new.QUANTITY < 0 )
  then
    signal sqlstate '45000' set message_text = 'QUANTITY is negative';
  end if;
end;
/

