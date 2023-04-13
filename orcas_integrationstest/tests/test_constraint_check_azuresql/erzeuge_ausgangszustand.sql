create table tab_check_number
( 	
    id		numeric(5),
	constraint check_size check (id BETWEEN 1 AND 5000),
	constraint old_wrong_name check (ID BETWEEN 1 AND 99)
);
	
create table tab_check_list_elements
(
 	versand		numeric(9),
	constraint check_versand_list check(versand in ('Abholung','Post'))
);	
	
create table tab_check_wrong_column
(
	id			numeric(5),
	versand		numeric(9),
	bestand		numeric(9),
	constraint check_wrong_column check(versand between 1 AND 500)
);

create table tab_aa_old_duplicate_cons_name
(
    id            numeric(5),
    constraint check_duplicate_cons_name check(ID IS NOT NULL)
);


create table tab_check_drop
(
 	bestand		numeric(9),
	constraint check_dropme check(bestand >= 0)
);

create table tab_mixed_name_1
(
	id				numeric(9)
	constraint fk_mixed_name_2 check(ID BETWEEN 1 AND 500),
	constraint fk_mixed_name_intab_2 check(ID BETWEEN 1 AND 501),
	constraint fk_mixed_name_intab_1 check(ID BETWEEN 1 AND 502)
);

create table tab_mixed_name_2
(
	id				numeric(9),
	constraint fk_mixed_name_1 check(ID BETWEEN 1 AND 500)
);


