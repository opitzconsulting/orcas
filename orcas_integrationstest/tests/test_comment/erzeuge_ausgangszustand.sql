create table tab_comment
( 	
    col1		number(5)
);

create table tab_col_comment
( 	
    col1		number(5)
);

	create table tab_drop_comment
( 	
    col1		number(5)
);

	comment on table tab_comment is 'TabellenAusgang';
	comment on column tab_col_comment.col1 is 'leer';
	comment on table tab_drop_comment is 'Tabellenkommentar';
