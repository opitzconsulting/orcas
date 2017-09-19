create table tab_check_number
( 	
    id		number(5),
	constraint check_size check (ID BETWEEN 1 AND 10000),
	constraint wrong_name check (ID BETWEEN 1 AND 99)
);
	
create table tab_check_list_elements
(
 	versand		number(9),
	constraint check_versand_list check(VERSAND IN ('ABHOLUNG','POST','LUFTFRACHT'))
);	
	
create table tab_check_disable
(
 	bestand		number(9),
	constraint check_number check (BESTAND >= 0) enable
);

create table tab_check_wrong_column
(
	id			number(5),
	versand		number(9),
	bestand		number(9),
	constraint check_wrong_column check(ID BETWEEN 1 AND 500)
);

create table tab_check_wrong_deferred
(
 	bestand		number(9),
	constraint check_deferred check (BESTAND >= 0) deferrable initially deferred
);

create table tab_bb_new_duplicate_cons_name
(
    id            number(5),
    constraint check_duplicate_cons_name check(ID IS NOT NULL)
);

create table tab_check_drop
(
 	bestand		number(9)
);


