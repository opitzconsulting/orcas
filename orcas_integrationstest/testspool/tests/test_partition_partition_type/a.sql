create table tab_new
(
col1 number(10)
)
partition by range (col1) 
(
partition xy10 values less than (10),
partition xy20 values less than (20)
);

