create table &1..tab_a 
( 	
    tab_a_id number(9) not null
);

grant index on &1..tab_a to &2;

create unique index &2..tab_a_ix on &1..tab_a (tab_a_id);

alter table &1..tab_a add primary key (tab_a_id) using index &2..tab_a_ix;

