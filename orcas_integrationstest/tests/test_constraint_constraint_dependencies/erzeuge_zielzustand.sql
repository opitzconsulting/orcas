create table tab_uk
( 	
	tab_a_id				number(9) not null,
	tab_a_bezeichner		varchar2(10),
	tab_a_daten				varchar2(20),
        constraint tab_wrong_pk_reverse primary key (tab_a_id) using index reverse,
	constraint uk_tab_uk unique (tab_a_id,tab_a_bezeichner)        
);
	
create table tab_fk_ref_uk
 (
 	tab_b_id				number(9),
	tab_b_bezeichner		varchar2(10),
	tab_b_daten				varchar2(20),
	constraint fk_tab_uk_id foreign key (tab_b_id) references tab_uk (tab_a_id),
	constraint fk_tab_ref_uk foreign key (tab_b_id,tab_b_bezeichner) references tab_uk (tab_a_id,tab_a_bezeichner)
);

create table tab_fk_ref_uk_uk
 (
 	tab_b_id				number(9),
	tab_b_bezeichner		varchar2(10),
	tab_b_daten				varchar2(20),
  constraint uk_tab_ref_uk_uk unique (tab_b_id,tab_b_bezeichner),
	constraint fk_tab_ref_uk_uk foreign key (tab_b_id,tab_b_bezeichner) references tab_uk (tab_a_id,tab_a_bezeichner)
);


create table tab_comment
( 	
	tab_comment number(9)
);

comment on column tab_comment.tab_comment is 'xy';

