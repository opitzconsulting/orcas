create table tab_comment
( 	
    col1		number(5),
    comment on table is "tablecomment";
);

create table tab_col_comment
( 	
    col1		number(5),
    comment on column col1 is 'columncomment \'';
);

create table tab_drop_comment
( 	
    col1		number(5),
);

create table tab_umlaut_comment
( 	
    col1		number(5),
    comment on table is "a_umlaut_is_ä";
);

create table tab_drop_column_comment
( 	
    col1		number(5)
);

create table tab_drop_col_and_comment
( 	
    col1		number(5)
);

create table tab_col_string_comment
(
    "col1 string"	number(5),
    comment on column "col1 string" is 'columncomment \'';
);

create table tab_col_newline
(
    col1	number(5)
    comment on column col1 is 'column\r\ncomment';
);


