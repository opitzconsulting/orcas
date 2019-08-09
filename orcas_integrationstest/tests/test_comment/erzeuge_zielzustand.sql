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

create table tab_umlaut_comment
( 	
    col1		number(5)
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
    "col1 string"	number(5)
);

comment on table tab_comment is 'tablecomment';
comment on column tab_col_comment.col1 is 'columncomment ''';
comment on table tab_umlaut_comment is 'a_umlaut_is_ä';
comment on column tab_col_string_comment."col1 string" is 'columncomment ''';
