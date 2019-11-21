create table a.tab_a 
( 	
    tab_a_id number(9) not null,
    primary key (tab_a_id) using index b.tab_a_ix,
    index b.tab_a_ix (tab_a_id) unique
);
