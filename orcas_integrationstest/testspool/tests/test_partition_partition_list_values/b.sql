create table tab_new
(
col1 number(10)
)
partition by list (col1) 
(
partition xy10 values (10,5),
partition xy20 values (6)
);



