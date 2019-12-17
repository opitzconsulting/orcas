create table tab_view
(
  col_add_ix_1 number(15) not null,
  col_add_ix_2 number(15) not null
);

create materialized view mview as select col_add_ix_1, col_add_ix_2 from tab_view;

create sequence seq increment by 15;



