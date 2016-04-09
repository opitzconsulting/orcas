
create or replace type ot_merge_result as object
(
  i_merge_index number
)
/

create or replace type ct_merge_result_list is table of ot_merge_result
/

