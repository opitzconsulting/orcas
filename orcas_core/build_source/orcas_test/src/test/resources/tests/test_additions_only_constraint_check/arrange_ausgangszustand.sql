create table tab_old
(  
  id  number(5),
  constraint tab_old_check_size check ("ID BETWEEN 1 AND 10000")
);
 
create table tab_chg
(
  id  number(5),
  constraint tab_chg_check_size_keep check ("ID BETWEEN 1 AND 11"),
  constraint tab_chg_check_size_chg check ("ID BETWEEN 1 AND 1000"),
  constraint tab_chg_check_size_old check ("ID BETWEEN 1 AND 100")
); 
 
