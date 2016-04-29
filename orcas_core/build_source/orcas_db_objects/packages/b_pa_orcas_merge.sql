CREATE OR REPLACE package body pa_orcas_merge is
  
  procedure init_om_repository 
  is
    v_default_orig_chartype ot_orig_chartype;
    v_default_tablespace varchar2(30);
    v_temporary_tablespace varchar2(30); 
  
    procedure load_defaults
    is
      v_nls_length_default varchar2(100);    
    begin
      select value 
        into v_nls_length_default 
        from nls_instance_parameters 
       where parameter = 'NLS_LENGTH_SEMANTICS';
         
      if( v_nls_length_default = 'BYTE' )
      then
        v_default_orig_chartype := ot_orig_chartype.c_byte;
      else
        v_default_orig_chartype := ot_orig_chartype.c_char;
      end if;    
        
      select default_tablespace,
             temporary_tablespace
        into v_default_tablespace,
             v_temporary_tablespace 
        from user_users;   
    end; 
     
  begin  
    load_defaults();
    
    pa_orcas_om_repository_orig.set_om_orig_tablesubpart( new om_orig_tablesubpart() );
    pa_orcas_om_repository_orig.set_om_orig_modelelement( new om_orig_modelelement() );
    pa_orcas_om_repository_orig.set_om_orig_indexoruniquekey( new om_orig_indexoruniquekey_impl( 1, 1, v_default_tablespace ) );
    pa_orcas_om_repository_orig.set_om_orig_subsubpart( new om_orig_subsubpart( 1, 1 ) );
    pa_orcas_om_repository_orig.set_om_orig_tablepartitioning( new om_orig_tablepartitioning() );  
    pa_orcas_om_repository_orig.set_om_orig_rangepartitionval( new om_orig_rangepartitionval( 0, 0 ) );
    pa_orcas_om_repository_orig.set_om_orig_lobstorage( new om_orig_lobstorage_impl( 1, 1 ) );
    pa_orcas_om_repository_orig.set_om_orig_refpartition( new om_orig_refpartition( 1, 1 ) );
    pa_orcas_om_repository_orig.set_om_orig_inlinecomment( new om_orig_inlinecomment_impl( 1, 0, null ) );
    pa_orcas_om_repository_orig.set_om_orig_model( new om_orig_model() );
    pa_orcas_om_repository_orig.set_om_orig_hashpartition( new om_orig_hashpartition( 1, 1 ) );
    pa_orcas_om_repository_orig.set_om_orig_columnref( new om_orig_columnref( 1 ) );
    pa_orcas_om_repository_orig.set_om_orig_primarykey( new om_orig_primarykey_impl( 1, 0, ot_orig_enabletype.c_enable, 1, v_default_tablespace ) );
    pa_orcas_om_repository_orig.set_om_orig_listpartitionvalu( new om_orig_listpartitionvalu( 0, 0 ) );
    pa_orcas_om_repository_orig.set_om_orig_listpartition( new om_orig_listpartition( 1, 1 ) );
    pa_orcas_om_repository_orig.set_om_orig_rangepartition( new om_orig_rangepartition( 1, 1 ) );
    pa_orcas_om_repository_orig.set_om_orig_listsubpart( new om_orig_listsubpart( 1 ) );
    pa_orcas_om_repository_orig.set_om_orig_rangesubpart( new om_orig_rangesubpart( 1 ) );
    pa_orcas_om_repository_orig.set_om_orig_refpartitions( new om_orig_refpartitions( 1 ) );
    pa_orcas_om_repository_orig.set_om_orig_hashsubsubpart( new om_orig_hashsubsubpart() );
    pa_orcas_om_repository_orig.set_om_orig_hashsubparts( new om_orig_hashsubparts() );
    pa_orcas_om_repository_orig.set_om_orig_comment( new om_orig_comment( null, null, null, null ) );
    pa_orcas_om_repository_orig.set_om_orig_mviewlog( new om_orig_mviewlog( 0, null, 0, null, 1, 0, 0, 0, null, 1, 0 ) );
    pa_orcas_om_repository_orig.set_om_orig_listsubsubpart( new om_orig_listsubsubpart() );
    pa_orcas_om_repository_orig.set_om_orig_foreignkey( new om_orig_foreignkey_impl( 1, ot_orig_deferrtype.c_immediate, ot_orig_fkdeleteruletype.c_no_action, 1, ot_orig_enabletype.c_enable ) );
    pa_orcas_om_repository_orig.set_om_orig_listpartitions( new om_orig_listpartitions() );
    pa_orcas_om_repository_orig.set_om_orig_sequence( new om_orig_sequence_impl( ot_orig_cycletype.c_nocycle, 0, ot_orig_ordertype.c_noorder, 1 ) );
    pa_orcas_om_repository_orig.set_om_orig_constraint( new om_orig_constraint_impl( 1, ot_orig_deferrtype.c_immediate, 0, ot_orig_enabletype.c_enable ) );
    pa_orcas_om_repository_orig.set_om_orig_hashpartitions( new om_orig_hashpartitions() );
    pa_orcas_om_repository_orig.set_om_orig_columnidentity( new om_orig_columnidentity( 0, 0, ot_orig_cycletype.c_nocycle, 0, ot_orig_ordertype.c_noorder ) );
    pa_orcas_om_repository_orig.set_om_orig_rangepartitions( new om_orig_rangepartitions( 0 ) );
    pa_orcas_om_repository_orig.set_om_orig_rangesubsubpart( new om_orig_rangesubsubpart() );
    pa_orcas_om_repository_orig.set_om_orig_listsubparts( new om_orig_listsubparts() );
    pa_orcas_om_repository_orig.set_om_orig_uniquekey( new om_orig_uniquekey_impl( 1, ot_orig_enabletype.c_enable ) );
    pa_orcas_om_repository_orig.set_om_orig_indexextable( new om_orig_indexextable( null, null, null, null, null, null, null, null, null, null, null ) );
    pa_orcas_om_repository_orig.set_om_orig_rangesubparts( new om_orig_rangesubparts() );
    pa_orcas_om_repository_orig.set_om_orig_mview( new om_orig_mview_impl( null, null, null, 1, null, null, null, null, 1, 0 ) );
    pa_orcas_om_repository_orig.set_om_orig_index( new om_orig_index_impl( 0, ot_orig_compresstype.c_nocompress, 0, 0, ot_orig_indexglobaltype.c_global, ot_orig_loggingtype.c_logging, ot_orig_paralleltype.c_noparallel, 0 ) );
    pa_orcas_om_repository_orig.set_om_orig_column( new om_orig_column_impl( v_default_orig_chartype, null, 0, 1, 1, 0 ) );
    pa_orcas_om_repository_orig.set_om_orig_table( new om_orig_table_impl( ot_orig_compresstype.c_nocompress, null, null, 1, ot_orig_paralleltype.c_noparallel, ot_orig_permanentnesstype.c_permanent, 1, ot_orig_permanentnesstran.c_on_commit_delete, v_default_tablespace ) );
  end;
  
  function find_table_position_by_name( p_table_name in varchar2, p_model in out nocopy ot_orig_model ) return number
  is
  begin
    for i in 1 .. p_model.i_model_elements.count 
    loop
      if( p_model.i_model_elements(i) is of (ot_orig_table) ) 
      then
        if( upper(treat( p_model.i_model_elements(i) as ot_orig_table ).i_name) = upper(p_table_name) )
        then
          return i;
        end if;
      end if;
    end loop;
    
    raise_application_error( -20000, 'table not found: ' || p_table_name );
  end;
  
  procedure inline_soll_ext_comments( p_model in out nocopy ot_orig_model ) 
  is
    v_orig_comment ot_orig_comment;
    v_table_index number;
    v_orig_table ot_orig_table;
    v_new_orig_inlinecomment ot_orig_inlinecomment;
  begin
    for i in 1 .. p_model.i_model_elements.count 
    loop
      if( p_model.i_model_elements(i) is of (ot_orig_comment) ) 
      then
        v_orig_comment := treat( p_model.i_model_elements(i) as ot_orig_comment );
      
        v_table_index := find_table_position_by_name( v_orig_comment.i_table_name, p_model );
        
        v_orig_table := treat( p_model.i_model_elements( v_table_index ) as ot_orig_table);
        
        v_new_orig_inlinecomment := new ot_orig_inlinecomment();
        v_new_orig_inlinecomment.i_column_name     := v_orig_comment.i_column_name;
        v_new_orig_inlinecomment.i_comment         := v_orig_comment.i_comment;          
        v_new_orig_inlinecomment.i_comment_object  := v_orig_comment.i_comment_object;                 
        
        if( v_orig_table.i_comments is null )
        then
          v_orig_table.i_comments := new ct_orig_inlinecomment_list();
        end if;
        
        v_orig_table.i_comments.extend;
        v_orig_table.i_comments( v_orig_table.i_comments.count ) := v_new_orig_inlinecomment;
        
        p_model.i_model_elements( v_table_index ) := v_orig_table;
      end if;
    end loop;
  end;
    
  procedure inline_soll_ext_indexes( p_model in out nocopy ot_orig_model ) 
  is
    v_orig_indexextable ot_orig_indexextable;
    v_table_index number;
    v_orig_table ot_orig_table;
    v_new_orig_index ot_orig_index;
  begin
    for i in 1 .. p_model.i_model_elements.count 
    loop
      if( p_model.i_model_elements(i) is of (ot_orig_indexextable) ) 
      then
        v_orig_indexextable := treat( p_model.i_model_elements(i) as ot_orig_indexextable );
      
        v_table_index := find_table_position_by_name( v_orig_indexextable.i_table_name, p_model );
        
        v_orig_table := treat( p_model.i_model_elements( v_table_index ) as ot_orig_table);
        
        v_new_orig_index := new ot_orig_index();
        v_new_orig_index.i_consname                   := v_orig_indexextable.i_index_name;
        v_new_orig_index.i_index_columns              := v_orig_indexextable.i_index_columns; 
        v_new_orig_index.i_global                     := v_orig_indexextable.i_global;           
        v_new_orig_index.i_logging                    := v_orig_indexextable.i_logging;           
        v_new_orig_index.i_parallel                   := v_orig_indexextable.i_parallel;   
        v_new_orig_index.i_parallel_degree            := v_orig_indexextable.i_parallel_degree;
        v_new_orig_index.i_bitmap                     := v_orig_indexextable.i_bitmap;           
        v_new_orig_index.i_unique                     := v_orig_indexextable.i_uniqueness;                     
        v_new_orig_index.i_function_based_expression  := v_orig_indexextable.i_function_based_expression;           
        v_new_orig_index.i_domain_index_expression    := v_orig_indexextable.i_domain_index_expression;           
        v_new_orig_index.i_tablespace                 := v_orig_indexextable.i_tablespace;
        v_new_orig_index.i_compression                := v_orig_indexextable.i_compression;
        
        if( v_orig_table.i_ind_uks is null )
        then
          v_orig_table.i_ind_uks := new ct_orig_indexoruniquekey_list();
        end if;
        
        v_orig_table.i_ind_uks.extend;
        v_orig_table.i_ind_uks( v_orig_table.i_ind_uks.count ) := v_new_orig_index;
        
        p_model.i_model_elements( v_table_index ) := v_orig_table;
      end if;
    end loop;
  end;

function get_merge_result( p_new_model in ot_orig_model, p_old_model in ot_orig_model ) return od_orig_model
is
  v_new_model ot_orig_model;
  v_old_model ot_orig_model;  
  v_od_orig_model od_orig_model;
begin
  init_om_repository();

  v_new_model := p_new_model;
  v_old_model := p_old_model;

  inline_soll_ext_indexes( v_new_model );
  inline_soll_ext_comments( v_new_model );  
    
  if( pa_orcas_om_repository_orig.get_om_orig_model().cleanup_values( v_old_model ) = null ) then null; end if;
  if( pa_orcas_om_repository_orig.get_om_orig_model().cleanup_values( v_new_model ) = null ) then null; end if;
  
  v_od_orig_model := od_orig_model( v_new_model );
  v_od_orig_model.merge_with_old_value( v_old_model );

  return v_od_orig_model;
end;

end;
/
