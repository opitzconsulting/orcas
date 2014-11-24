SET ECHO OFF
SET FEEDBACK OFF
SPOOL schema.lst

--
-- Dropping tables
--

PROMPT Dropping Tables...
drop table order_items;
drop table orders;
drop table items_suppliers;
drop table items;
drop table categories;
drop table business_partners;

--
-- Dropping sequences
--

PROMPT Dropping Sequences...
drop sequence ctgr_id_seq;
drop sequence item_id_seq;
drop sequence ordr_id_seq;
drop sequence orit_id_seq;
drop sequence bpar_id_seq;
drop sequence tracking_number_seq;

--
-- Creating Tables
--

PROMPT Creating Table 'CATEGORIES' (CTGR)
CREATE TABLE CATEGORIES
 (CTGR_ID NUMBER(15,0) NOT NULL
 ,VERSION NUMBER(15,0) DEFAULT 0 NOT NULL
 ,NAME VARCHAR2(30) NOT NULL
 ,DESCRIPTION VARCHAR2(1000) NOT NULL
 )
/

PROMPT Creating Table 'BUSINESS_PARTNERS' (BPAR)
CREATE TABLE BUSINESS_PARTNERS
 (BPAR_ID NUMBER(15,0) NOT NULL
 ,VERSION NUMBER(15,0) DEFAULT 0 NOT NULL
 ,TYPE VARCHAR2(10) NOT NULL
 ,STREET VARCHAR2(50) NOT NULL
 ,CITY VARCHAR2(30) NOT NULL
 ,STATE VARCHAR2(2)
 ,ZIPCODE VARCHAR2(10) NOT NULL
 ,COUNTRY VARCHAR2(2) NOT NULL
 ,EMAIL VARCHAR2(40) NOT NULL
 ,FIRSTNAME VARCHAR2(30)
 ,LASTNAME VARCHAR2(30)
 ,USERNAME VARCHAR2(15)
 ,PASSWORD VARCHAR2(15)
 ,STATUS VARCHAR2(8)
 ,NAME VARCHAR2(30)
 ,WEBSITE VARCHAR2(40)
 )
/

PROMPT Creating Table 'ITEMS' (ITEM)
CREATE TABLE ITEMS
 (ITEM_ID NUMBER(15,0) NOT NULL
 ,VERSION NUMBER(15,0) DEFAULT 0 NOT NULL
 ,CTGR_ID NUMBER(15,0) NOT NULL
 ,NAME VARCHAR2(30) NOT NULL
 ,DESCRIPTION VARCHAR2(1000) NOT NULL
 ,IMAGE_LOCATION VARCHAR2(255)
 ,PRICE NUMBER(8,2) NOT NULL
 ,ONHAND NUMBER(4,0) NOT NULL
 )
/

PROMPT Creating Table 'ITEMS_SUPPLIERS' (ITSU)
CREATE TABLE ITEMS_SUPPLIERS
 (ITEM_ID NUMBER(15,0) NOT NULL
 ,BPAR_ID NUMBER(15,0) NOT NULL
 )
/

PROMPT Creating Table 'ORDERS' (ORDR)
CREATE TABLE ORDERS
 (ORDR_ID NUMBER(15,0) NOT NULL
 ,VERSION NUMBER(15,0) DEFAULT 0 NOT NULL
 ,BPAR_ID NUMBER(15,0) NOT NULL
 ,ORDERDATE DATE NOT NULL
 ,TRACKING_NUMBER VARCHAR2(20) NOT NULL
 ,STATUS VARCHAR2(10) NOT NULL
 ,SHIPPING_STREET VARCHAR2(50) NOT NULL
 ,SHIPPING_CITY VARCHAR2(30) NOT NULL
 ,SHIPPING_STATE VARCHAR2(2)
 ,SHIPPING_ZIPCODE VARCHAR2(10) NOT NULL
 ,SHIPPING_COUNTRY VARCHAR2(2) NOT NULL
 )
/

PROMPT Creating Table 'ORDER_ITEMS' (ORIT)
CREATE TABLE ORDER_ITEMS
 (ORIT_ID NUMBER(15,0) NOT NULL
 ,VERSION NUMBER(15,0) DEFAULT 0 NOT NULL
 ,ORDR_ID NUMBER(15,0) NOT NULL
 ,ITEM_ID NUMBER(15,0) NOT NULL
 ,PRICE NUMBER(8,2) NOT NULL
 ,QUANTITY NUMBER(4,0) NOT NULL
 )
/

--
-- Inserting values
--

prompt inserting values into categories
insert into categories (ctgr_id, name, description) values (1, 'Accessories', 'Accessories');
insert into categories (ctgr_id, name, description) values (2, 'Collectibles', 'Collectibles');
insert into categories (ctgr_id, name, description) values (3, 'Dolls', 'Dolls');
insert into categories (ctgr_id, name, description) values (4, 'Games', 'Games');
insert into categories (ctgr_id, name, description) values (5, 'Models', 'Models');
insert into categories (ctgr_id, name, description) values (6, 'Party Supplies', 'Party Supplies');
insert into categories (ctgr_id, name, description) values (7, 'Puzzles', 'Puzzles');
insert into categories (ctgr_id, name, description) values (8, 'Toys', 'Toys');
commit
/

prompt inserting values into business_partners
-- customers
insert into business_partners (bpar_id, type, firstname, lastname, username, password, street, city, state, zipcode, country, email, status) values (201, 'CUSTOMER', 'Steve', 'Herbengizer', 'steve', 'steve', '101 California', 'Blueville', 'CA', '94036-3209', 'US', 'steve@steve.com', 'GOLD');
insert into business_partners (bpar_id, type, firstname, lastname, username, password, street, city, state, zipcode, country, email, status) values (202, 'CUSTOMER', 'Mike', 'Malcom', 'mike', 'mike', '420 Redmond Way', 'Greenville', 'CA', '94112-8724', 'US', 'mike@mike.com', 'SILVER');
insert into business_partners (bpar_id, type, firstname, lastname, username, password, street, city, state, zipcode, country, email, status) values (203, 'CUSTOMER', 'Susan', 'Anthony', 'susan', 'susan', '10323 Orchcust_id Lane', 'Brownsville', 'CA', '94801-1055', 'US', 'sba@brownsville.org', 'PLATINUM');
insert into business_partners (bpar_id, type, firstname, lastname, username, password, street, city, state, zipcode, country, email, status) values (204, 'CUSTOMER', 'Wai', 'Ming', 'wai', 'wai', '2345 Ravenna Ter #245', 'Redsville', 'CA', '94621-1929', 'US', 'djwai@web.com', 'PLATINUM');
insert into business_partners (bpar_id, type, firstname, lastname, username, password, street, city, state, zipcode, country, email, status) values (205, 'CUSTOMER', 'Khanh', 'Lou', 'khanh', 'khanh', '23543 Douglas Street', 'Pleasantville', 'CA', '94878-9290', 'US', 'xpat@kd.com', 'SILVER');
insert into business_partners (bpar_id, type, firstname, lastname, username, password, street, city, state, zipcode, country, email, status) values (206, 'CUSTOMER', 'Esteban', 'Martinez', 'esteban', 'esteban', '1 Caldecott Av.', 'Orangeville', 'CA', '94928-8122', 'US', 'esteban@web.com', 'GOLD');
insert into business_partners (bpar_id, type, firstname, lastname, username, password, street, city, state, zipcode, country, email, status) values (207, 'CUSTOMER', 'Moriyo', 'Komoro', 'moriyo', 'moriyo', '543 Golden Gate Bl.', 'Hazeville', 'CA', '94065-4511', 'US', 'ma01@web.org', 'SILVER');
insert into business_partners (bpar_id, type, firstname, lastname, username, password, street, city, state, zipcode, country, email, status) values (208, 'CUSTOMER', 'Pascal', 'Edward', 'pascal', 'pascal', '989 West', 'Rumorville', 'CA', '94651-7111', 'US', 'pascal@pac.org', 'SILVER');
insert into business_partners (bpar_id, type, firstname, lastname, username, password, street, city, state, zipcode, country, email, status) values (209, 'CUSTOMER', 'Nikos', 'Santos', 'nikos', 'nikos', '95875 999th', 'Blueville', 'CA', '94201-2008', 'US', 'Nik01@web.org', 'SILVER');
insert into business_partners (bpar_id, type, firstname, lastname, username, password, street, city, state, zipcode, country, email, status) values (210, 'CUSTOMER', 'Zaheda', 'Rahman', 'zaheda', 'zaheda', '298 Armor Ter #A123', 'Harmsville', 'CA', '94055-6124', 'US', 'zaheda_r@zb.org', 'SILVER');
insert into business_partners (bpar_id, type, firstname, lastname, username, password, street, city, state, zipcode, country, email, status) values (211, 'CUSTOMER', 'Preeti', 'Panwalla', 'preeti', 'preeti', '876 El Monte', 'Monteville', 'CA', '94001-3214', 'US', 'ppatel@qec.ed.uk', 'GOLD');
-- suppliers
insert into business_partners (bpar_id, type, name, street, city, state, zipcode, country, email, website) values (301, 'SUPPLIER', 'Novelty Toy Company', '101 San Antonio Way', 'Oakland', 'CA', '94606', 'US', 'info@NoveltyToyCompany.com', 'http://www.NoveltyToyCompany.com');
insert into business_partners (bpar_id, type, name, street, city, state, zipcode, country, email, website) values (302, 'SUPPLIER', 'Games for the Masses', '120 Adeline St.', 'Berkeley', 'CA', '94703', 'US', 'orders@GamesFTM.com', 'http://www.GamesFTM.com');
insert into business_partners (bpar_id, type, name, street, city, state, zipcode, country, email, website) values (303, 'SUPPLIER', 'RetroLand Merchandising', '34 Montana Ave.', 'Santa Monica', 'CA', '90403', 'US', 'orders@RetroLandMerchant.com', 'http://www.RetroLandMerchant.com');
insert into business_partners (bpar_id, type, name, street, city, state, zipcode, country, email, website) values (304, 'SUPPLIER', 'Unusual Party Favors, Inc.', '166 Avenue B', 'New York', 'NY', '10009', 'US', 'accts@UnusualParty.com', 'http://www.UnusualParty.com');
insert into business_partners (bpar_id, type, name, street, city, state, zipcode, country, email, website) values (305, 'SUPPLIER', 'Inanimate Friends Company', '16 Bogus Basin Rd', 'Boise', 'ID', '83702', 'US', 'info@InanimateFriends.com', 'http://www.InanimateFriends.com');
commit
/

prompt inserting values into items
insert into items (item_id, ctgr_id, name, description, price, onhand) values (401, 8, 'Robot', 'Good-natured robot walks, shakes hands, and politely says "Nice to meet you, Earthling." Blinking red lights on head. Two AA batteries included.', 24.99, 27);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (402, 8, 'Rubber Duck', 'A bathtime classic for adults, children, and even pets. When you squeeze the duck, it quacks. Our version is made from heavy duty materials that will last for generations.', 4.99, 49);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (403, 8, 'Dinosaur', 'Equally at home in a child''s playroom or on top of your computer screen, this carnivorous reptile from the past is sure to become a favorite. And a reminder that even the mightiest must someday fall.', 11.99, 16);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (404, 8, 'Windup Monkey with Cymbals', 'Wind up this musical monkey and fill the room with the cheerful patter of small cymbals.', 49.99, 120);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (405, 5, 'Rocket', 'You almost expect a super hero to emerge from this detailed, shiny, silver rocket. And it''s the perfect gift to tell your loved one you want to take them to the moon.', 15.99, 0);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (406, 5, 'Cab Model', 'Just like they drive in New York. We''ve thought of every detail when constructing this sturdy miniature: you can open the hood and trunk, and even honk the horn!', 21.99, 10);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (407, 1, 'Dice', 'Smooth, shiny, and perfectly weighted. They feel good in your hands. Twelve dice, assorted jewel colors.', 3.99, 28);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (408, 1, 'Poker Chips', 'Perfect as fake money for a saloon night fundraiser or a more serious endeavor. 100 assorted poker chips.', 14.99, 13);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (409, 4, 'Chess Set', 'Match wits with your friends and acquaintances. Beautiful inlaid hardwood board and carved stone pieces. ', 89.99, 80);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (410, 1, 'Billiard Balls', 'These built-to-last billiard balls are used in pool halls and withstand even the heaviest use.', 59.99, 62);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (411, 4, 'Dart Board', 'This dart board is made with only the densest cork and resilient dyes, for long wear. Darts not included.', 29.99, 123);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (412, 1, 'Ping Pong Paddle and Two Balls', 'Made of the finest hardwood and long-lasting red rubber.', 19.99, 198);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (413, 4, 'Marbles', '20 glass marbles. Use them in games, or as decoration in glass jars, fish bowls, or surrounding plants.', 4.99, 801);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (414, 4, 'Ball and Jacks', 'Improve hand-eye coordination with a good game of jacks.', 69, 321);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (415, 8, 'String Top', 'This easy-to-use top includes complete instructions.', 3.99, 1645);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (416, 8, 'Pump Top', 'Watch the gorgeous metallic colors spin. Great for amusing children and pets.', 4.49, 344);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (417, 8, 'Yo-yo', 'A deep ruby color. Comprehensive instruction book containing many tricks included.', 5.99, 223);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (418, 7, 'Puzzle Cube', 'Twist the cube to make each side one color.', 14.99, 145);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (419, 7, 'Jigsaw Puzzle', 'Take the wooden pieces out of the frame, and try to put them back.', 12.99, 200);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (420, 2, 'Bird Mask', 'This medium-sized adult mask is perfect for costume parties or anytime you want to be anonymous.', 25.99, 520);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (421, 6, 'Balloons', 'No celebration is complete without festive balloons! Package contains 12 uninflated balloons, in assorted colors.', 3.99, 82);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (422, 6, 'Pinata', 'Filled with candy and small toys. Blind-folded, supervised participants try, in turn, to break open the suspended pinata with a baseball bat.', 45.99, 104);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (423, 3, 'Bear', 'Soft and cuddly, this bear will be your friend for years to come.', 24.99, 54);
insert into items (item_id, ctgr_id, name, description, price, onhand) values (424, 3, 'Doll', 'If you thought they don''t make them like they used to, here''s the doll for you! Quality construction is evident throughout.', 29.99, 12);
commit
/

prompt inserting values into items_suppliers
insert into items_suppliers (item_id, bpar_id) values (401, 301);
insert into items_suppliers (item_id, bpar_id) values (402, 301);
insert into items_suppliers (item_id, bpar_id) values (403, 301);
insert into items_suppliers (item_id, bpar_id) values (404, 301);
insert into items_suppliers (item_id, bpar_id) values (405, 301);
insert into items_suppliers (item_id, bpar_id) values (406, 301);
insert into items_suppliers (item_id, bpar_id) values (407, 302);
insert into items_suppliers (item_id, bpar_id) values (408, 302);
insert into items_suppliers (item_id, bpar_id) values (409, 302);
insert into items_suppliers (item_id, bpar_id) values (410, 302);
insert into items_suppliers (item_id, bpar_id) values (411, 302);
insert into items_suppliers (item_id, bpar_id) values (412, 302);
insert into items_suppliers (item_id, bpar_id) values (413, 303);
insert into items_suppliers (item_id, bpar_id) values (414, 303);
insert into items_suppliers (item_id, bpar_id) values (415, 303);
insert into items_suppliers (item_id, bpar_id) values (416, 303);
insert into items_suppliers (item_id, bpar_id) values (417, 303);
insert into items_suppliers (item_id, bpar_id) values (418, 303);
insert into items_suppliers (item_id, bpar_id) values (419, 303);
insert into items_suppliers (item_id, bpar_id) values (420, 304);
insert into items_suppliers (item_id, bpar_id) values (421, 304);
insert into items_suppliers (item_id, bpar_id) values (422, 304);
insert into items_suppliers (item_id, bpar_id) values (423, 305);
insert into items_suppliers (item_id, bpar_id) values (424, 305);
-- second supplier
insert into items_suppliers (item_id, bpar_id) values (401, 302);
insert into items_suppliers (item_id, bpar_id) values (402, 302);
insert into items_suppliers (item_id, bpar_id) values (403, 302);
insert into items_suppliers (item_id, bpar_id) values (404, 302);
insert into items_suppliers (item_id, bpar_id) values (405, 302);
insert into items_suppliers (item_id, bpar_id) values (406, 302);
insert into items_suppliers (item_id, bpar_id) values (407, 303);
insert into items_suppliers (item_id, bpar_id) values (408, 303);
insert into items_suppliers (item_id, bpar_id) values (409, 303);
insert into items_suppliers (item_id, bpar_id) values (410, 303);
insert into items_suppliers (item_id, bpar_id) values (411, 303);
insert into items_suppliers (item_id, bpar_id) values (412, 303);
insert into items_suppliers (item_id, bpar_id) values (413, 304);
insert into items_suppliers (item_id, bpar_id) values (414, 304);
insert into items_suppliers (item_id, bpar_id) values (415, 304);
insert into items_suppliers (item_id, bpar_id) values (416, 304);
insert into items_suppliers (item_id, bpar_id) values (417, 304);
insert into items_suppliers (item_id, bpar_id) values (418, 304);
insert into items_suppliers (item_id, bpar_id) values (419, 304);
insert into items_suppliers (item_id, bpar_id) values (420, 305);
insert into items_suppliers (item_id, bpar_id) values (421, 305);
insert into items_suppliers (item_id, bpar_id) values (422, 305);
insert into items_suppliers (item_id, bpar_id) values (423, 301);
insert into items_suppliers (item_id, bpar_id) values (424, 301);
-- third supplier
insert into items_suppliers (item_id, bpar_id) values (401, 303);
insert into items_suppliers (item_id, bpar_id) values (402, 303);
insert into items_suppliers (item_id, bpar_id) values (403, 303);
insert into items_suppliers (item_id, bpar_id) values (404, 303);
insert into items_suppliers (item_id, bpar_id) values (405, 303);
insert into items_suppliers (item_id, bpar_id) values (406, 303);
insert into items_suppliers (item_id, bpar_id) values (407, 305);
insert into items_suppliers (item_id, bpar_id) values (408, 305);
insert into items_suppliers (item_id, bpar_id) values (409, 305);
insert into items_suppliers (item_id, bpar_id) values (410, 305);
insert into items_suppliers (item_id, bpar_id) values (411, 305);
insert into items_suppliers (item_id, bpar_id) values (412, 305);
commit
/

--
-- Defining PK constraints
--

PROMPT Creating Primary Key on 'CATEGORIES'
ALTER TABLE CATEGORIES
 ADD CONSTRAINT CTGR_PK PRIMARY KEY
  (CTGR_ID)
/

PROMPT Creating Primary Key on 'BUSINESS_PARTNERS'
ALTER TABLE BUSINESS_PARTNERS
 ADD CONSTRAINT BPAR_PK PRIMARY KEY
  (BPAR_ID)
/

PROMPT Creating Primary Key on 'ITEMS'
ALTER TABLE ITEMS
 ADD CONSTRAINT ITEM_PK PRIMARY KEY
  (ITEM_ID)
/

PROMPT Creating Primary Key on 'ITEMS_SUPPLIERS'
ALTER TABLE ITEMS_SUPPLIERS
 ADD CONSTRAINT ITSU_PK PRIMARY KEY
  (ITEM_ID, BPAR_ID)
/

PROMPT Creating Primary Key on 'ORDERS'
ALTER TABLE ORDERS
 ADD CONSTRAINT ORDR_PK PRIMARY KEY
  (ORDR_ID)
/

PROMPT Creating Primary Key on 'ORDER_ITEMS'
ALTER TABLE ORDER_ITEMS
 ADD CONSTRAINT ORIT_PK PRIMARY KEY
  (ORIT_ID)
/

--
-- Defining UK constraints
--

PROMPT Creating Unique Constraint on 'CATEGORIES'
ALTER TABLE CATEGORIES ADD CONSTRAINT
 CTGR_UC UNIQUE
  (NAME)
/

PROMPT Creating Unique Constraints on 'BUSINESS_PARTNERS'
ALTER TABLE BUSINESS_PARTNERS ADD CONSTRAINT
 BPAR_UC_USERNAME UNIQUE
  (USERNAME)
/
ALTER TABLE BUSINESS_PARTNERS ADD CONSTRAINT
 BPAR_UC_NAME UNIQUE
  (NAME)
/

PROMPT Creating Unique Constraint on 'ITEMS'
ALTER TABLE ITEMS ADD CONSTRAINT
 ITEM_UC UNIQUE
  (NAME)
/

PROMPT Creating Unique Constraint on 'ORDERS'
ALTER TABLE ORDERS ADD CONSTRAINT
 ORDR_UC UNIQUE
  (TRACKING_NUMBER)
/

PROMPT Creating Unique Constraint on 'ORDER_ITEMS'
ALTER TABLE ORDER_ITEMS ADD CONSTRAINT
 ORIT_UC UNIQUE
  (ORDR_ID, ITEM_ID)
/

--
-- Defining FK constraints
--

PROMPT Creating Foreign Keys on 'ITEMS'
ALTER TABLE ITEMS ADD CONSTRAINT
 ITEM_CTGR_FK FOREIGN KEY
  (CTGR_ID) REFERENCES CATEGORIES
  (CTGR_ID)
/

PROMPT Creating Foreign Keys on 'ITEMS_SUPPLERS'
ALTER TABLE ITEMS_SUPPLIERS ADD CONSTRAINT
 ITSU_BPAR_FK FOREIGN KEY
  (BPAR_ID) REFERENCES BUSINESS_PARTNERS
  (BPAR_ID)
 ADD CONSTRAINT
 ITSU_ITEM_FK FOREIGN KEY
  (ITEM_ID) REFERENCES ITEMS
  (ITEM_ID)
/

PROMPT Creating Foreign Keys on 'ORDER_ITEMS'
ALTER TABLE ORDER_ITEMS ADD CONSTRAINT
 ORIT_ITEM_FK FOREIGN KEY
  (ITEM_ID) REFERENCES ITEMS
  (ITEM_ID)
 ADD CONSTRAINT
 ORIT_ORDR_FK FOREIGN KEY
  (ORDR_ID) REFERENCES ORDERS
  (ORDR_ID) ON DELETE CASCADE
/

PROMPT Creating Foreign Keys on 'ORDERS'
ALTER TABLE ORDERS ADD CONSTRAINT
 ORDR_BPAR_FK FOREIGN KEY
  (BPAR_ID) REFERENCES BUSINESS_PARTNERS
  (BPAR_ID)
/

--
-- Further Constraints
--

ALTER TABLE ITEMS ADD CONSTRAINT
ITEM_ONHAND_CK CHECK
  (ONHAND >= 0)
/

--
-- Creating Sequences
--

prompt Creating sequence BPAR_ID_SEQ
create sequence bpar_id_seq start with 5001;

prompt Creating sequence CTGR_ID_SEQ
create sequence ctgr_id_seq start with 5001;

prompt Creating sequence ITEM_ID_SEQ
create sequence item_id_seq start with 5001;

prompt Creating sequence ORDR_ID_SEQ
create sequence ordr_id_seq start with 5001;

prompt Creating sequence ORIT_ID_SEQ
create sequence orit_id_seq start with 5001;

prompt Creating sequence TRACKING_NUMBER_SEQ;
create sequence tracking_number_seq start with 10001;


--
-- Creating Triggers
--

PROMPT Creating Trigger 'BPAR_BRI'
CREATE OR REPLACE TRIGGER BPAR_BRI
 BEFORE INSERT
 ON BUSINESS_PARTNERS
 REFERENCING OLD AS OLD NEW AS NEW
 FOR EACH ROW
begin
    if :new.bpar_id is null then
      select bpar_id_seq.nextval
        into :new.bpar_id
        from dual ;
    end if;
  end;
/

PROMPT Creating Trigger 'CTGR_BRI'
CREATE OR REPLACE TRIGGER CTGR_BRI
 BEFORE INSERT
 ON CATEGORIES
 REFERENCING OLD AS OLD NEW AS NEW
 FOR EACH ROW
begin
    if :new.ctgr_id is null then
      select ctgr_id_seq.nextval
        into :new.ctgr_id
        from dual ;
    end if;
  end;
/

PROMPT Creating Trigger 'ITEM_BRI'
CREATE OR REPLACE TRIGGER ITEM_BRI
 BEFORE INSERT
 ON ITEMS
 REFERENCING OLD AS OLD NEW AS NEW
 FOR EACH ROW
begin
    if :new.item_id is null then
      select item_id_seq.nextval
        into :new.item_id
        from dual ;
    end if;
  end;
/

PROMPT Creating Trigger 'ORDR_BRI'
CREATE OR REPLACE TRIGGER ORDR_BRI
 BEFORE INSERT
 ON ORDERS
 REFERENCING OLD AS OLD NEW AS NEW
 FOR EACH ROW
begin
    if :new.ordr_id is null then
      select ordr_id_seq.nextval
        into :new.ordr_id
        from dual ;
    end if;
  end;
/

PROMPT Creating Trigger 'ORIT_BRI'
CREATE OR REPLACE TRIGGER ORIT_BRI
 BEFORE INSERT
 ON ORDER_ITEMS
 REFERENCING OLD AS OLD NEW AS NEW
 FOR EACH ROW
begin
    if :new.orit_id is null then
      select orit_id_seq.nextval
        into :new.orit_id
        from dual ;
    end if;
  end;
/

PROMPT ...done.
SPOOL OFF
SET FEEDBACK ON
SET ECHO ON

