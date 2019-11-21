create table &1..tab_a 
( 	
    tab_a_id					number(9) constraint pk_tab_a primary key not null
);

grant references on tab_a to &2;

