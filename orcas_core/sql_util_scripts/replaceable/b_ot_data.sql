create or replace type body ot_data_rd as
constructor function ot_data_rd( p_simple_texts in ct_data_vc2 ) return self as result
is
begin
  i_data_celldata_list := ct_data_cdl();
  i_data_celldata_list.extend( p_simple_texts.count );
  for i in 1..p_simple_texts.count
  loop
    i_data_celldata_list(i) := ot_data_cd( p_simple_texts(i) );
  end loop;
  return;
end;
end;
/

create or replace type body ot_data_fk_metadata as
constructor function ot_data_fk_metadata( p_table_name in varchar2, p_data_col_metadata_list in ct_data_col_metadata_list ) return self as result
is
begin
  i_table_name := p_table_name;
  i_data_col_metadata_list := p_data_col_metadata_list;
  return;
end;
end;
/

create or replace type body ot_data_col_metadata_text as
constructor function ot_data_col_metadata_text( p_column_name in varchar2 ) return self as result
is
begin
  i_column_name := p_column_name;
  i_dummy_null_value := '''##SCS_NULL##''';
  i_sql_text_escape := 1;
  return;
end;
end;
/

create or replace type body ot_data_col_metadata_number as
constructor function ot_data_col_metadata_number( p_column_name in varchar2 ) return self as result
is
begin
  i_column_name := p_column_name;
  i_dummy_null_value := '-4711';
  return;
end;
end;
/

create or replace type body ot_data_col_metadata_fk as
constructor function ot_data_col_metadata_fk( p_column_name in varchar2, p_scs_fk_metadata in ot_data_fk_metadata ) return self as result
is
begin
  i_column_name := p_column_name;
  i_data_fk_metadata := p_scs_fk_metadata;
  i_dummy_null_value := '-4711';
  return;
end;
end;
/

create or replace type body ot_data_col_metadata_decode as
constructor function ot_data_col_metadata_decode( p_column_name in varchar2, p_scs_decode_metadata_list in ct_data_decode_metadata_list ) return self as result
is
begin
  i_column_name := p_column_name;
  i_dummy_null_value := '-4711';
  i_data_decode_metadata_list := p_scs_decode_metadata_list;
  return;
end;
end;
/

create or replace type body ot_data_col_metadata_cons as
constructor function ot_data_col_metadata_cons( p_column_name in varchar2, p_const_expression in varchar2 ) return self as result
is
begin
  i_column_name := p_column_name;
  i_const_expression := p_const_expression;
  return;
end;
end;
/

