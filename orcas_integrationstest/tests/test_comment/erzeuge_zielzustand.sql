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

comment on table tab_comment is 'tablecomment';
comment on column tab_col_comment.col1 is 'columncomment ''';
comment on table tab_umlaut_comment is 'a_umlaut_is_ä';
