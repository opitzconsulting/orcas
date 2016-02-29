SET DEFINE OFF;
Insert into ITEMS
   (ITEM_ID, 
    VERSION, CTGR_ID, NAME, DESCRIPTION, IMAGE_LOCATION, 
    PRICE)
 Values
   (5, 0, 3, 'Pizza', 'Pizza', 
    NULL, 5);
Insert into ITEMS
   (ITEM_ID, 
    VERSION, CTGR_ID, NAME, DESCRIPTION, IMAGE_LOCATION, 
    PRICE)
 Values
   (6, 0, 3, 'Hamburger', 'Hamburger', 
    NULL, 6);
Insert into ITEMS
   (ITEM_ID, 
    VERSION, CTGR_ID, NAME, DESCRIPTION, IMAGE_LOCATION, 
    PRICE)
 Values
   (7, 0, 2, 'Football', 'Football', 
    NULL, 13.53);
Insert into ITEMS
   (ITEM_ID, 
    VERSION, CTGR_ID, NAME, DESCRIPTION, IMAGE_LOCATION, 
    PRICE)
 Values
   (8, 0, 1, 'Jeans', 'Jeans', 
    NULL, 24.99);
COMMIT;
