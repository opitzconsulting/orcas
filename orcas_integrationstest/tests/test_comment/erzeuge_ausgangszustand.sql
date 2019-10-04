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

create table tab_drop_column_comment
( 	
    col1		number(5)
);

create table tab_drop_col_and_comment
( 	
    col1		number(5),
    col2		number(5)
);

create table tab_col_newline
(
    col1	number(5)
);

comment on table tab_comment is 'TabellenAusgang';
comment on column tab_col_comment.col1 is 'leer';
comment on table tab_drop_comment is 'Tabellenkommentar';
comment on column tab_drop_column_comment.col1 is 'drop me';
comment on column tab_drop_col_and_comment.col2 is 'dont drop me explicitly';
comment on column tab_col_newline.col1 is 'column
comment';

