create table tab_partition_interval
(
  col1 date not null
)
partition by range (col1) 
interval ("numtoyminterval(1, 'month')")
(
  partition part_10 values less than ("to_date('01.01.2010','DD.MM.YYYY')")
);
