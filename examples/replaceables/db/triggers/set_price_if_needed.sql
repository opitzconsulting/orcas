CREATE OR REPLACE TRIGGER set_price_if_needed
before insert or update
on order_items 
referencing new as new old as old
for each row
declare
begin
  if( :new.price is null ) 
  then
    :new.price := pricing.get_price( :new.item_id, :new.quantity );
  end if;
end ;
/

