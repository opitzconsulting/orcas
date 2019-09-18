create or replace view business_partners_v as
select bpar_id,
       get_supplied_item_count(bpar_id) as supplied_item_count
  from BUSINESS_PARTNERS
/
