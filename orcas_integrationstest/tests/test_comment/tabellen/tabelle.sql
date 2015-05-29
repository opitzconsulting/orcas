create table tab_comment
( 	
    col1		number(5),
    comment on table is "Tabellenkommentar";
);

create table tab_col_comment
( 	
    col1		number(5),
    comment on column col1 is 'Spaltenkommentar \'';
);

create table tab_drop_comment
( 	
    col1		number(5),
);
