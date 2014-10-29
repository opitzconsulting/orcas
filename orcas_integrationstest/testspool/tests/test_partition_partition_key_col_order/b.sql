create table tab_new
(
col1 number(10),
col2 number(10)
)
partition by range (col2,col1) 
(
partition xy10 values less than (10,5),
partition xy20 values less than (maxvalue,maxvalue)
);



