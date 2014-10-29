
create or replace type ot_data_cd as object
(
  i_cell_value varchar2(2000)
)
/

create or replace type ct_data_cdl is table of ot_data_cd
/

create or replace type ct_data_vc2 is table of varchar2(2000)
/

create or replace type ot_data_rd as object
(
  i_data_celldata_list ct_data_cdl,
  constructor function ot_data_rd( p_simple_texts in ct_data_vc2 ) return self as result
)
/

create or replace type ct_data_rdl is table of ot_data_rd
/

create or replace type ot_data_col_metadata_dummy as object
(
  i_dummy number
)
not instantiable
not final
/

create or replace type ct_data_col_metadata_list is table of ot_data_col_metadata_dummy
/

create or replace type ot_data_fk_metadata as object
(
  i_table_name varchar2(100),
  i_data_col_metadata_list ct_data_col_metadata_list,
  i_pk_column_name varchar2(100),

  constructor function ot_data_fk_metadata( p_table_name in varchar2, p_data_col_metadata_list in ct_data_col_metadata_list ) return self as result
)
/

create or replace type ot_data_decode_metadata as object
(
  i_column_value varchar2(2000),
  i_value varchar2(2000)
)
/

create or replace type ct_data_decode_metadata_list is table of ot_data_decode_metadata
/

create or replace type ot_data_col_metadata
under ot_data_col_metadata_dummy 
(
  i_column_name varchar2(100),
  i_wrapper_function_prefix varchar2(1000),
  i_wrapper_function_postfix varchar2(1000),
  i_data_fk_metadata ot_data_fk_metadata,
  i_dummy_null_value varchar2(100),
  i_data_decode_metadata_list ct_data_decode_metadata_list,
  i_const_expression varchar2(1000),
  i_sql_text_escape number(1)
)
not final
/

create or replace type ot_data_col_metadata_text
under ot_data_col_metadata 
(
  constructor function ot_data_col_metadata_text( p_column_name in varchar2 ) return self as result
)
/

create or replace type ot_data_col_metadata_number
under ot_data_col_metadata 
(
  constructor function ot_data_col_metadata_number( p_column_name in varchar2 ) return self as result
)
/

create or replace type ot_data_col_metadata_fk
under ot_data_col_metadata 
(
  constructor function ot_data_col_metadata_fk( p_column_name in varchar2, p_scs_fk_metadata in ot_data_fk_metadata ) return self as result
)
/

create or replace type ot_data_col_metadata_decode
under ot_data_col_metadata 
(
  constructor function ot_data_col_metadata_decode( p_column_name in varchar2, p_scs_decode_metadata_list in ct_data_decode_metadata_list ) return self as result
)
/

create or replace type ot_data_col_metadata_cons
under ot_data_col_metadata 
(
  constructor function ot_data_col_metadata_cons( p_column_name in varchar2, p_const_expression in varchar2 ) return self as result
)
/

create or replace type ct_data_col_metadata_cons_list is table of ot_data_col_metadata_cons
/

create or replace type ot_data_metadata as object
(
  i_table_name varchar2(100),
  i_delete_knz number,
  i_delete_include_where varchar2(2000),
  i_data_key_columns ct_data_col_metadata_list,
  i_data_add_columns ct_data_col_metadata_list,
  i_data_def_columns ct_data_col_metadata_list,
  i_data_def_const_columns ct_data_col_metadata_cons_list
)
/

create or replace type ct_data_metadata_list is table of ot_data_metadata
/

