SET DEFINE OFF;
Insert into ORDER_ITEMS
   (ORIT_ID, 
    VERSION, ORDR_ID, ITEM_ID, PRICE, QUANTITY)
 Values
   (3, 0, 1, 5, 123, 
    3);
Insert into ORDER_ITEMS
   (ORIT_ID, 
    VERSION, ORDR_ID, ITEM_ID, PRICE, QUANTITY)
 Values
   (2, 0, 1, 8, 4.56, 
    1);
Insert into ORDER_ITEMS
   (ORIT_ID, 
    VERSION, ORDR_ID, ITEM_ID, PRICE, QUANTITY)
 Values
   (1, 0, 1, 7, 3.78, 
    3);
COMMIT;
