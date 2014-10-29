create table tab_check_number
( 	
    id		number(5),
	constraint check_size check (id BETWEEN 1 AND 5000)
);
	
create table tab_check_list_elements
(
 	versand		number(9),
	constraint check_versand_list check(versand in ('Abholung','Post'))
);	
	
create table tab_check_disable
(
 	bestand		number(9),
	constraint check_number check(bestand >= 0) disable
);

create table tab_check_wrong_column
(
	id			number(5),
	versand		number(9),
	bestand		number(9),
	constraint check_wrong_column check(versand between 1 AND 500)
);

create table tab_check_wrong_deferred
(
 	bestand		number(9),
	constraint check_deferred check (BESTAND >= 0) deferrable initially immediate
);

create table tab_aa_old_duplicate_cons_name
(
    id            number(5),
    constraint check_duplicate_cons_name check(ID IS NOT NULL)
);


create table tab_check_drop
(
 	bestand		number(9),
	constraint check_dropme check(bestand >= 0) disable
);

