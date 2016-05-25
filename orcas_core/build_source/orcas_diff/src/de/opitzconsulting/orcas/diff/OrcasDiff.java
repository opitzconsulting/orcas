package de.opitzconsulting.orcas.diff;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.opitzconsulting.orcas.orig.diff.ColumnDiff;
import de.opitzconsulting.orcas.orig.diff.ColumnRefDiff;
import de.opitzconsulting.orcas.orig.diff.ConstraintDiff;
import de.opitzconsulting.orcas.orig.diff.DiffRepository;
import de.opitzconsulting.orcas.orig.diff.ForeignKeyDiff;
import de.opitzconsulting.orcas.orig.diff.IndexDiff;
import de.opitzconsulting.orcas.orig.diff.IndexOrUniqueKeyDiff;
import de.opitzconsulting.orcas.orig.diff.InlineCommentDiff;
import de.opitzconsulting.orcas.orig.diff.LobStorageDiff;
import de.opitzconsulting.orcas.orig.diff.ModelDiff;
import de.opitzconsulting.orcas.orig.diff.SequenceDiff;
import de.opitzconsulting.orcas.orig.diff.TableDiff;
import de.opitzconsulting.orcas.orig.diff.UniqueKeyDiff;
import de.opitzconsulting.orcas.sql.WrapperReturnFirstValue;
import de.opitzconsulting.orcas.sql.WrapperReturnValueFromResultSet;
import de.opitzconsulting.origOrcasDsl.CompressForType;
import de.opitzconsulting.origOrcasDsl.CompressType;
import de.opitzconsulting.origOrcasDsl.DataType;
import de.opitzconsulting.origOrcasDsl.FkDeleteRuleType;
import de.opitzconsulting.origOrcasDsl.LoggingType;
import de.opitzconsulting.origOrcasDsl.Model;
import de.opitzconsulting.origOrcasDsl.ParallelType;
import de.opitzconsulting.origOrcasDsl.PermanentnessType;

public class OrcasDiff
{

  private boolean isIndexRecreate( TableDiff pTableDiff, String pIndexname )
  {
    for( IndexOrUniqueKeyDiff lIndexOrUniqueKeyDiff : pTableDiff.ind_uksIndexDiff )
    {
      if( lIndexOrUniqueKeyDiff.consNameNew.equals( pIndexname ) )
      {
        return lIndexOrUniqueKeyDiff.isRecreateNeeded;
      }
    }

    throw new RuntimeException( "index not found: " + pIndexname + " " + pTableDiff.nameNew );
  }

  private boolean isRecreateColumn( ColumnDiff pColumnDiff )
  {
    if( pColumnDiff.data_typeNew != null && pColumnDiff.data_typeOld != null )
    {
      if( !pColumnDiff.data_typeIsEqual )
      {
        return true;
      }

      if( isLessTahnOrNull( pColumnDiff.precisionNew, pColumnDiff.precisionOld ) )
      {
        return true;
      }

      if( isLessTahnOrNull( pColumnDiff.scaleNew, pColumnDiff.scaleOld ) )
      {
        return true;
      }
    }

    if( !pColumnDiff.object_typeIsEqual )
    {
      return true;
    }

    return false;
  }

  private boolean isLessTahnOrNull( Integer pValue1, Integer pValue2 )
  {
    if( pValue1 == null && pValue2 == null )
    {
      return false;
    }

    if( pValue1 == null || pValue2 == null )
    {
      return true;
    }

    return pValue1 < pValue2;
  }

  private void updateIsRecreateNeeded( ModelDiff pModelDiff )
  {
    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      if( lTableDiff.isMatched )
      {
        if( !lTableDiff.permanentnessIsEqual || !lTableDiff.transactionControlIsEqual )
        {
          lTableDiff.isRecreateNeeded = true;
        }

        for( ColumnDiff lColumnDiff : lTableDiff.columnsDiff )
        {
          if( lColumnDiff.isMatched )
          {
            if( isRecreateColumn( lColumnDiff ) )
            {
              lColumnDiff.isRecreateNeeded = true;
            }
          }
        }

        if( lTableDiff.primary_keyDiff.isMatched )
        {
          if( !lTableDiff.primary_keyDiff.consNameIsEqual //
              || !lTableDiff.primary_keyDiff.pk_columnsIsEqual || !lTableDiff.primary_keyDiff.reverseIsEqual || (!lTableDiff.primary_keyDiff.tablespaceIsEqual && isIndexmovetablespace()) )
          {
            lTableDiff.primary_keyDiff.isRecreateNeeded = true;
          }
        }

        for( IndexDiff lIndexDiff : lTableDiff.ind_uksIndexDiff )
        {
          if( lIndexDiff.isMatched )
          {
            if( ((!lIndexDiff.index_columnsIsEqual //
                  || !lIndexDiff.function_based_expressionIsEqual || !lIndexDiff.domain_index_expressionIsEqual)
            // domain index kann nicht abgeglichen werden                           
                 && lIndexDiff.domain_index_expressionNew == null) ||
                !lIndexDiff.uniqueIsEqual ||
                !lIndexDiff.bitmapIsEqual ||
                !lIndexDiff.globalIsEqual ||
                !lIndexDiff.compressionIsEqual )
            {
              lIndexDiff.isRecreateNeeded = true;
            }
          }
        }

        for( UniqueKeyDiff lUniqueKeyDiff : lTableDiff.ind_uksUniqueKeyDiff )
        {
          if( lUniqueKeyDiff.isMatched )
          {
            if( !lUniqueKeyDiff.uk_columnsIsEqual || //
                !lUniqueKeyDiff.indexnameIsEqual ||
                (!lUniqueKeyDiff.tablespaceIsEqual && isIndexmovetablespace()) )
            {
              lUniqueKeyDiff.isRecreateNeeded = true;
            }
            else
            {
              if( lUniqueKeyDiff.indexnameNew != null )
              {
                if( isIndexRecreate( lTableDiff, lUniqueKeyDiff.indexnameNew ) )
                {
                  lUniqueKeyDiff.isRecreateNeeded = true;
                }
              }
            }
          }
        }

        for( ConstraintDiff lConstraintDiff : lTableDiff.constraintsDiff )
        {
          if( lConstraintDiff.isMatched )
          {
            if( !lConstraintDiff.ruleIsEqual //
                || !lConstraintDiff.deferrtypeIsEqual )
            {
              lConstraintDiff.isRecreateNeeded = true;
            }
          }
        }

        for( ForeignKeyDiff lForeignKeyDiff : lTableDiff.foreign_keysDiff )
        {
          if( lForeignKeyDiff.isMatched )
          {
            if( !lForeignKeyDiff.isEqual )
            {
              lForeignKeyDiff.isRecreateNeeded = true;
            }
          }
        }
      }
    }
  }

  private boolean isIndexmovetablespace()
  {
    return _parameters.isIndexmovetablespace();
  }

  public DiffResult compare( Model pModelSoll, Model pModelIst, Parameters pParameters )
  {
    _parameters = pParameters;

    InitDiffRepository.init();

    DiffRepository.getModelMerge().cleanupValues( pModelIst );
    DiffRepository.getModelMerge().cleanupValues( pModelSoll );

    ModelDiff lModelDiff = new ModelDiff( pModelSoll );
    lModelDiff.mergeWithOldValue( pModelIst );

    updateIsRecreateNeeded( lModelDiff );

    // TODO dass sortieren der istdaten ist unnoetig, aber im moment die einfachste moeglichkeit die liste der tabellen zu ermitteln

    handleAllTables( lModelDiff );
    //
    handleAllSequences( lModelDiff );
    //
    //    handleAllMviews();

    return new DiffResult( pv_stmtList );
  }

  public class DiffResult
  {
    private List<String> sqlStatements = new ArrayList<String>();

    public List<String> getSqlStatements()
    {
      return sqlStatements;
    }

    private DiffResult( List<String> pSqlStatements )
    {
      sqlStatements.addAll( pSqlStatements );
    }
  }

  private void handleSequence( SequenceDiff pSequenceDiff )
  {
    BigDecimal lSollStartValue = null;

    try
    {
      if( pSequenceDiff.max_value_selectNew != null )
      {
        lSollStartValue = (BigDecimal)new WrapperReturnFirstValue( pSequenceDiff.max_value_selectNew, JdbcConnectionHandler.getCallableStatementProvider() ).executeForValue();

        lSollStartValue = lSollStartValue.add( BigDecimal.valueOf( 1 ) );
      }
    }
    catch( Exception e )
    {
      //    kann vorkommen, wenn fuer das select benoetigte Tabellen nicht exisitieren. kann erst richtig korrigiert werden, wenn auch der Tabellenabgleich auf dieses Package umgestellt wurde      
    }

    if( pSequenceDiff.isMatched == false )
    {
      pv_stmt = "create sequence " + pSequenceDiff.sequence_nameNew;
      if( pSequenceDiff.increment_byNew != null )
      {
        pv_stmt = pv_stmt + " increment by " + pSequenceDiff.increment_byNew;
      }

      if( lSollStartValue != null )
      {
        pv_stmt = pv_stmt + " start with " + lSollStartValue;
      }

      if( pSequenceDiff.maxvalueNew != null )
      {
        pv_stmt = pv_stmt + " maxvalue " + pSequenceDiff.maxvalueNew;
      }

      if( pSequenceDiff.minvalueNew != null )
      {
        pv_stmt = pv_stmt + " minvalue " + pSequenceDiff.minvalueNew;
      }

      if( pSequenceDiff.cycleNew != null )
      {
        pv_stmt = pv_stmt + " " + pSequenceDiff.cycleNew.getLiteral();
      }

      if( pSequenceDiff.cacheNew != null )
      {
        pv_stmt = pv_stmt + " cache " + pSequenceDiff.cacheNew;
      }

      if( pSequenceDiff.orderNew != null )
      {
        pv_stmt = pv_stmt + " " + pSequenceDiff.orderNew.getLiteral();
      }

      add_stmt();
    }
    else
    {
      BigDecimal lIstValue = BigDecimal.valueOf( Long.valueOf( pSequenceDiff.max_value_selectOld ) );
      if( lSollStartValue != null && lIstValue != null && lSollStartValue.compareTo( lIstValue ) > 0 )
      {
        add_stmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " increment by " + (lSollStartValue.longValue() - lIstValue.longValue()) );
        add_stmt( "declare v_dummy number; begin select " + pSequenceDiff.sequence_nameNew + ".nextval into v_dummy from dual; end;" );
        add_stmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " increment by " + nvl( pSequenceDiff.increment_byNew, 1 ) );
      }
      else
      {
        if( pSequenceDiff.increment_byIsEqual == false )
        {
          add_stmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " increment by " + nvl( pSequenceDiff.increment_byNew, 1 ) );
        }
      }

      if( pSequenceDiff.maxvalueIsEqual == false )
      {
        add_stmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " maxvalue " + pSequenceDiff.maxvalueNew );
      }

      if( pSequenceDiff.minvalueIsEqual == false )
      {
        add_stmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " minvalue " + nvl( pSequenceDiff.minvalueNew, 1 ) );
      }

      if( pSequenceDiff.cycleIsEqual == false )
      {
        add_stmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " " + pSequenceDiff.cycleNew.getLiteral() );
      }

      if( pSequenceDiff.cacheIsEqual == false )
      {
        add_stmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " cache " + nvl( pSequenceDiff.cacheNew, 20 ) );
      }

      if( pSequenceDiff.orderIsEqual == false )
      {
        add_stmt( "alter sequence " + pSequenceDiff.sequence_nameNew + " " + pSequenceDiff.orderNew.getLiteral() );
      }
    }
  }

  private void handleAllSequences( ModelDiff pModelDiff )
  {
    for( SequenceDiff lSequenceDiff : pModelDiff.model_elementsSequenceDiff )
    {
      if( lSequenceDiff.isEqual == false )
      {
        if( lSequenceDiff.isNew == true )
        {
          handleSequence( lSequenceDiff );
        }
        else
        {
          add_stmt( "drop sequence " + lSequenceDiff.sequence_nameOld );
        }
      }
    }
  }

  private void drop_table_constraint_by_name( String pTablename, String pCconstraintName )
  {
    add_stmt( "alter table " + pTablename + " drop constraint " + pCconstraintName );
  }

  private void handleAllTables( ModelDiff pModelDiff )
  {
    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      for( ForeignKeyDiff lForeignKeyDiff : lTableDiff.foreign_keysDiff )
      {
        if( lForeignKeyDiff.isOld == true && (lForeignKeyDiff.isMatched == false || lForeignKeyDiff.isRecreateNeeded == true) )
        {
          drop_table_constraint_by_name( lTableDiff.nameOld, lForeignKeyDiff.consNameOld );
        }
      }
    }

    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      if( lTableDiff.isOld == true && (lTableDiff.isMatched == false || lTableDiff.isRecreateNeeded == true) )
      {
        drop_with_dropmode_check( "select 1 from " + lTableDiff.nameOld, "drop table " + lTableDiff.nameOld );
      }
      else
      {
        for( ConstraintDiff lConstraintDiff : lTableDiff.constraintsDiff )
        {
          if( lConstraintDiff.isOld == true && (lConstraintDiff.isMatched == false || lConstraintDiff.isRecreateNeeded == true) )
          {
            drop_table_constraint_by_name( lTableDiff.nameOld, lConstraintDiff.consNameOld );
          }
        }

        if( lTableDiff.mviewLogDiff.isOld == true && (lTableDiff.mviewLogDiff.isMatched == false || lTableDiff.mviewLogDiff.isRecreateNeeded == true) )
        {
          add_stmt( "drop materialized view log on " + lTableDiff.nameOld );
        }

        for( UniqueKeyDiff lUniqueKeyDiff : lTableDiff.ind_uksUniqueKeyDiff )
        {
          if( lUniqueKeyDiff.isOld == true && (lUniqueKeyDiff.isMatched == false || lUniqueKeyDiff.isRecreateNeeded == true) )
          {
            drop_table_constraint_by_name( lTableDiff.nameOld, lUniqueKeyDiff.consNameOld );
          }
        }

        for( IndexDiff lIndexDiff : lTableDiff.ind_uksIndexDiff )
        {
          if( lIndexDiff.isOld == true && (lIndexDiff.isMatched == false || lIndexDiff.isRecreateNeeded == true) )
          {
            add_stmt( "drop index " + lIndexDiff.consNameOld );
          }
        }

        for( InlineCommentDiff lCommentDiff : lTableDiff.commentsDiff )
        {
          if( lCommentDiff.isOld == true && lCommentDiff.isMatched == false )
          {
            stmt_set( "comment on" );
            stmt_add( lCommentDiff.comment_objectOld.getName() );
            stmt_add( " " );
            stmt_add( lTableDiff.nameOld );
            if( lCommentDiff.column_nameOld != null )
            {
              stmt_add( "." );
              stmt_add( lCommentDiff.column_nameOld );
            }
            stmt_add( "is ''" );

            add_stmt();
          }
        }

        if( lTableDiff.primary_keyDiff.isOld == true && (lTableDiff.primary_keyDiff.isMatched == false || lTableDiff.primary_keyDiff.isRecreateNeeded == true) )
        {
          drop_table_constraint_by_name( lTableDiff.nameOld, lTableDiff.primary_keyDiff.consNameOld );
        }
      }
    }

    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      if( lTableDiff.isNew == true )
      {
        //    TODO Sonderbehandlung für Tabellen zu Mviews, die nicht prebuilt sind, sie dürfen nicht behandelt werden        
        handleTable( lTableDiff );
      }
    }

    for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
    {
      for( ForeignKeyDiff lForeignKeyDiff : lTableDiff.foreign_keysDiff )
      {
        if( lForeignKeyDiff.isNew == true && (lForeignKeyDiff.isMatched == false || lForeignKeyDiff.isRecreateNeeded == true) )
        {
          if( lTableDiff.isOld == false && lTableDiff.tablePartitioningRefPartitionsDiff.isNew == true && lTableDiff.tablePartitioningRefPartitionsDiff.fkNameNew.equals( lForeignKeyDiff.consNameNew ) )
          {
            // in diesem Fall haben wir eine ref-partitionierte Tabelle die in diesem Lauf angelegt wurde, und damit ist der get_fk_for_ref_partitioning schon angelegt worden.
          }
          else
          {
            createForeignKey( lTableDiff, lForeignKeyDiff );
          }
        }
      }
    }
  }

  private LobStorageDiff findLobstorage( TableDiff pTableDiff, String pColumnName )
  {
    for( LobStorageDiff lLobStorageDiff : pTableDiff.lobStoragesDiff )
    {
      if( lLobStorageDiff.column_nameNew.equals( pColumnName ) )
      {
        return lLobStorageDiff;
      }
    }

    return null;
  }

  private void handleTable( TableDiff pTableDiff )
  {
    if( pTableDiff.isMatched == true )
    {
      if( pTableDiff.tablespaceIsEqual == false && is_tablemovetablespace() == true )
      {
        stmt_set( "alter table" );
        stmt_add( pTableDiff.nameNew );
        stmt_add( "move tablespace" );
        stmt_add( nvl( pTableDiff.tablespaceNew, getDefaultTablespace() ) );
        stmt_done();
      }
    }

    if( pTableDiff.isMatched == false || pTableDiff.isRecreateNeeded == true )
    {
      createTable( pTableDiff );
    }
    else
    {
      for( ColumnDiff lColumnDiff : pTableDiff.columnsDiff )
      {
        if( lColumnDiff.isNew == true )
        {
          handleColumn( pTableDiff, lColumnDiff );
        }
        else
        {
          drop_with_dropmode_check( "select 1 from " + pTableDiff.nameOld + " where " + lColumnDiff.nameOld + " != null", "alter table " + pTableDiff.nameOld + " drop column " + lColumnDiff.nameOld );
        }
      }

      if( pTableDiff.loggingIsEqual == false )
      {
        if( pTableDiff.transactionControlNew == null )
        {
          stmt_set( "alter table" );
          stmt_add( pTableDiff.nameNew );
          if( pTableDiff.loggingNew == LoggingType.NOLOGGING )
          {
            stmt_add( "nologging" );
          }
          else
          {
            stmt_add( "logging" );
          }
          stmt_done();
        }
      }

      if( pTableDiff.parallelIsEqual == false || pTableDiff.parallel_degreeIsEqual == false )
      {
        stmt_set( "alter table" );
        stmt_add( pTableDiff.nameNew );
        if( pTableDiff.parallelNew == ParallelType.PARALLEL )
        {
          stmt_add( "parallel" );
          if( pTableDiff.parallel_degreeNew != null && pTableDiff.parallel_degreeNew > 1 )
          {
            stmt_add( " " + pTableDiff.parallel_degreeNew );
          }
        }
        else
        {
          stmt_add( "noparallel" );
        }

        stmt_done();
      }

      if( pTableDiff.permanentnessNew == PermanentnessType.PERMANENT && (pTableDiff.compressionIsEqual == false || pTableDiff.compressionForIsEqual == false) )
      {
        stmt_set( "alter table" );
        stmt_add( pTableDiff.nameNew );
        if( pTableDiff.compressionNew == CompressType.NOCOMPRESS )
        {
          stmt_add( "compress" );
          if( pTableDiff.compressionForNew != null )
          {
            stmt_add( "for " + adjust_compression_literal( pTableDiff.compressionForNew.getLiteral() ) );
          }
        }
        else
        {
          stmt_add( "nocompress" );
        }

        stmt_done();
      }
    }

    if( pTableDiff.primary_keyDiff.isNew == true )
    {
      handlePrimarykey( pTableDiff );
    }

    for( ConstraintDiff lConstraintDiff : pTableDiff.constraintsDiff )
    {
      if( lConstraintDiff.isNew == true )
      {
        handleConstraint( pTableDiff.nameNew, lConstraintDiff );
      }
    }

    for( IndexDiff lIndexDiff : pTableDiff.ind_uksIndexDiff )
    {
      if( lIndexDiff.isNew == true )
      {
        handleIndex( pTableDiff.nameNew, lIndexDiff );
      }
    }

    for( UniqueKeyDiff lUniqueKeyDiff : pTableDiff.ind_uksUniqueKeyDiff )
    {
      if( lUniqueKeyDiff.isNew == true )
      {
        handleUniquekey( pTableDiff.nameNew, lUniqueKeyDiff );
      }
    }

    for( InlineCommentDiff lInlineCommentDiff : pTableDiff.commentsDiff )
    {
      handleCmment( nvl( pTableDiff.nameNew, pTableDiff.nameOld ), lInlineCommentDiff );
    }

    //        if( pTableDiff.c_mviewlog.is_equal == false )
    //        {
    //          handle_mviewlog( pTableDiff );
    //        }

  }

  private String createColumnClause( List<ColumnDiff> pColumnListDiffList )
  {
    String v_return = null;

    for( ColumnDiff lColumnDiff : pColumnListDiffList )
    {
      if( v_return != null )
      {
        v_return = v_return + ",";
      }
      else
      {
        v_return = "";
      }

      if( lColumnDiff.isNew == true )
      {
        v_return = v_return + " " + create_column_create_part( lColumnDiff );
      }
    }

    return v_return;
  }

  private String createColumnStorageClause( List<LobStorageDiff> pLobStorageDiffList )
  {
    String v_return = "";

    for( LobStorageDiff lLobStorageDiff : pLobStorageDiffList )
    {
      if( lLobStorageDiff.isNew == true )
      {
        v_return = v_return + " lob(" + lLobStorageDiff.column_nameNew + ") store as (tablespace " + lLobStorageDiff.tablespaceNew + ")";
      }
    }

    return v_return;
  }

  private void createTable( TableDiff pTableDiff )
  {
    pv_stmt = "create";
    if( pTableDiff.permanentnessNew == PermanentnessType.GLOBAL_TEMPORARY )
    {
      pv_stmt = pv_stmt + " global " + pTableDiff.permanentnessNew.getLiteral();
    }
    pv_stmt = pv_stmt + " " + "table";
    pv_stmt = pv_stmt + " " + pTableDiff.nameNew;
    pv_stmt = pv_stmt + " " + "(";
    pv_stmt = pv_stmt + " " + createColumnClause( pTableDiff.columnsDiff );
    //    pv_stmt = pv_stmt + " " + create_ref_fk_clause( p_orig_table ); 
    pv_stmt = pv_stmt + " " + ")";
    if( pTableDiff.transactionControlNew != null )
    {
      pv_stmt = pv_stmt + " " + "on commit ";
      pv_stmt = pv_stmt + " " + pTableDiff.transactionControlNew.getLiteral();
      pv_stmt = pv_stmt + " " + "rows nocache";
    }
    else
    {
      pv_stmt = pv_stmt + " " + createColumnStorageClause( pTableDiff.lobStoragesDiff );
      if( pTableDiff.tablespaceNew != null )
      {
        pv_stmt = pv_stmt + " " + "tablespace";
        pv_stmt = pv_stmt + " " + pTableDiff.tablespaceNew;
      }
      if( pTableDiff.permanentnessNew != PermanentnessType.GLOBAL_TEMPORARY )
      {
        if( pTableDiff.loggingNew == LoggingType.NOLOGGING )
        {
          pv_stmt = pv_stmt + " " + "nologging";
        }
        else
        {
          pv_stmt = pv_stmt + " " + "logging";
        }
      }
      if( pTableDiff.compressionNew == CompressType.COMPRESS )
      {
        pv_stmt = pv_stmt + " " + "compress";
        if( pTableDiff.compressionForNew == CompressForType.ALL )
        {
          pv_stmt = pv_stmt + " " + "for all operations";
        }
        if( pTableDiff.compressionForNew == CompressForType.DIRECT_LOAD )
        {
          pv_stmt = pv_stmt + " " + "for direct_load operations";
        }
        if( pTableDiff.compressionForNew == CompressForType.QUERY_LOW )
        {
          pv_stmt = pv_stmt + " " + "for query low";
        }
        if( pTableDiff.compressionForNew == CompressForType.QUERY_HIGH )
        {
          pv_stmt = pv_stmt + " " + "for query high";
        }
        if( pTableDiff.compressionForNew == CompressForType.ARCHIVE_LOW )
        {
          pv_stmt = pv_stmt + " " + "for archive low";
        }
        if( pTableDiff.compressionForNew == CompressForType.ARCHIVE_HIGH )
        {
          pv_stmt = pv_stmt + " " + "for archive high";
        }
      }
      if( pTableDiff.compressionNew == CompressType.NOCOMPRESS )
      {
        pv_stmt = pv_stmt + " " + "nocompress";
      }
    }
    if( pTableDiff.parallelNew == ParallelType.PARALLEL )
    {
      stmt_add( "parallel" );
      if( pTableDiff.parallel_degreeNew != null && pTableDiff.parallel_degreeNew > 1 )
      {
        stmt_add( "" + pTableDiff.parallel_degreeNew );
      }
    }
    if( pTableDiff.parallelNew == ParallelType.NOPARALLEL )
    {
      stmt_add( "noparallel" );
    }
    //      pv_stmt = pv_stmt + " " + create_partitioning_clause( p_orig_table.i_tablepartitioning );

    add_stmt();
  }

  private void createForeignKey( TableDiff pTableDiff, ForeignKeyDiff pForeignKeyDiff )
  {
    String v_fk_false_data_select;
    String v_fk_false_data_where_part;

    if( is_dropmode() == true && pTableDiff.isOld == true )
    {
      v_fk_false_data_where_part = null;
      for( ColumnRefDiff lColumnRefDiffSrc : pForeignKeyDiff.srcColumnsDiff )
      {
        if( v_fk_false_data_where_part != null )
        {
          v_fk_false_data_where_part = v_fk_false_data_where_part + " or ";
        }
        else
        {
          v_fk_false_data_where_part = "";
        }
        v_fk_false_data_where_part = v_fk_false_data_where_part + lColumnRefDiffSrc.column_nameNew + " is not null ";
      }

      v_fk_false_data_where_part = "where (" + v_fk_false_data_where_part + ") and (" + get_column_list( pForeignKeyDiff.srcColumnsDiff ) + ") not in (select " + get_column_list( pForeignKeyDiff.destColumnsDiff ) + "  from " + pForeignKeyDiff.destTableNew + ")";
      v_fk_false_data_select = "select 1 from " + pTableDiff.nameNew + " " + v_fk_false_data_where_part;

      if( has_rows( v_fk_false_data_select ) == true )
      {
        if( pForeignKeyDiff.delete_ruleNew == FkDeleteRuleType.CASCADE )
        {
          add_stmt( "delete " + pTableDiff.nameNew + " " + v_fk_false_data_where_part );
          add_stmt( "commit" );
        }
        else
        {
          if( pForeignKeyDiff.delete_ruleNew == FkDeleteRuleType.SET_NULL )
          {
            add_stmt( "update " + pTableDiff.nameNew + " set " + get_column_list( pForeignKeyDiff.srcColumnsDiff ) + " = null " + v_fk_false_data_where_part );
            add_stmt( "commit" );
          }
          else
          {
            throw new RuntimeException( "Fehler beim FK Aufbau " + pForeignKeyDiff.consNameNew + " auf tabelle " + pTableDiff.nameNew + " Datenbereinigung nicht möglich, da keine delete rule. " + v_fk_false_data_select );
          }
        }
      }
    }

    stmt_set( "alter table " + pTableDiff.nameNew );
    stmt_add( "add" );
    stmt_add( createForeignKeyClause( pForeignKeyDiff ) );
    stmt_done();
  }

  private String createForeignKeyClause( ForeignKeyDiff pForeignKeyDiff )
  {
    String v_return;

    v_return = "constraint " + pForeignKeyDiff.consNameNew + " foreign key (" + get_column_list( pForeignKeyDiff.srcColumnsDiff ) + ") references " + pForeignKeyDiff.destTableNew + "(" + get_column_list( pForeignKeyDiff.destColumnsDiff ) + ")";

    if( pForeignKeyDiff.delete_ruleNew != null )
    {
      if( pForeignKeyDiff.delete_ruleNew == FkDeleteRuleType.CASCADE )
      {
        v_return = v_return + " on delete cascade";
      }
      if( pForeignKeyDiff.delete_ruleNew == FkDeleteRuleType.SET_NULL )
      {
        v_return = v_return + " on delete set null";
      }
    }

    if( pForeignKeyDiff.deferrtypeNew != null )
    {
      v_return = v_return + " deferrable initially  " + pForeignKeyDiff.deferrtypeNew.getName();
    }

    return v_return;
  }

  private void handleCmment( String pTablename, InlineCommentDiff pInlineCommentDiff )
  {
    if( pInlineCommentDiff.isEqual == false )
    {
      if( pInlineCommentDiff.isNew == true )
      {
        stmt_set( "comment on" );
        stmt_add( pInlineCommentDiff.comment_objectNew.getName() );
        stmt_add( " " );
        stmt_add( pTablename );
        if( pInlineCommentDiff.column_nameNew != null )
        {
          stmt_add( "." );
          stmt_add( pInlineCommentDiff.column_nameNew );
        }
        stmt_add( "is" );
        stmt_add( "'" + pInlineCommentDiff.commentNew.replace( "'", "''" ) + "'" );
        add_stmt();
      }
      else
      {
        stmt_set( "comment on" );
        stmt_add( pInlineCommentDiff.comment_objectOld.getName() );
        stmt_add( " " );
        stmt_add( pTablename );
        if( pInlineCommentDiff.column_nameOld != null )
        {
          stmt_add( "." );
          stmt_add( pInlineCommentDiff.column_nameOld );
        }
        stmt_add( "is" );
        stmt_add( "''" );
        add_stmt();
      }
    }
  }

  private void handleUniquekey( String pTablename, UniqueKeyDiff pUniqueKeyDiff )
  {
    if( pUniqueKeyDiff.isMatched == false || pUniqueKeyDiff.isRecreateNeeded == true )
    {
      stmt_set( "alter table " + pTablename + " add constraint " + pUniqueKeyDiff.consNameNew + " unique (" + get_column_list( pUniqueKeyDiff.uk_columnsDiff ) + ")" );
      if( pUniqueKeyDiff.tablespaceNew != null )
      {
        stmt_add( "using index tablespace " + pUniqueKeyDiff.tablespaceNew );
      }
      else
      {
        if( pUniqueKeyDiff.indexnameNew != null && !pUniqueKeyDiff.indexnameNew.equals( pUniqueKeyDiff.consNameNew ) )
        {
          stmt_add( "using index " + pUniqueKeyDiff.indexnameNew );
        }
      }
      if( pUniqueKeyDiff.statusNew != null )
      {
        stmt_add( pUniqueKeyDiff.statusNew.getName() );
      }

      add_stmt();
    }
  }

  private void handleIndex( String pTablename, IndexDiff pIndexDiff )
  {
    if( pIndexDiff.isMatched == false || pIndexDiff.isRecreateNeeded == true )
    {
      stmt_set( "create" );
      if( pIndexDiff.uniqueNew != null )
      {
        stmt_add( pIndexDiff.uniqueNew );
      }
      if( pIndexDiff.bitmapNew != null )
      {
        stmt_add( "bitmap" );
      }
      stmt_add( "index" );
      stmt_add( pIndexDiff.consNameNew );
      stmt_add( "on" );
      stmt_add( pTablename );
      stmt_add( "(" );
      if( pIndexDiff.function_based_expressionNew != null )
      {
        stmt_add( pIndexDiff.function_based_expressionNew );
      }
      else
      {
        stmt_add( get_column_list( pIndexDiff.index_columnsDiff ) );
      }
      stmt_add( ")" );
      if( pIndexDiff.domain_index_expressionNew != null )
      {
        stmt_add( pIndexDiff.domain_index_expressionNew );
      }
      else
      {
        if( pIndexDiff.loggingNew != null )
        {
          stmt_add( pIndexDiff.loggingNew.getLiteral() );
        }
      }
      if( pIndexDiff.tablespaceNew != null )
      {
        stmt_add( "tablespace" );
        stmt_add( pIndexDiff.tablespaceNew );
      }
      if( pIndexDiff.globalNew != null )
      {
        stmt_add( pIndexDiff.globalNew.getLiteral() );
      }
      if( pIndexDiff.bitmapNew == null && pIndexDiff.compressionNew == CompressType.COMPRESS )
      {
        stmt_add( "compress" );
      }
      if( pIndexDiff.compressionNew == CompressType.NOCOMPRESS )
      {
        stmt_add( "nocompress" );
      }

      if( pIndexDiff.parallelNew == ParallelType.PARALLEL || isIndexparallelcreate() )
      {
        stmt_add( "parallel" );
        if( pIndexDiff.parallel_degreeNew != null && pIndexDiff.parallel_degreeNew > 1 )
        {
          stmt_add( " " + pIndexDiff.parallel_degreeNew );
        }
      }

      add_stmt();

      if( pIndexDiff.parallelNew != ParallelType.PARALLEL && isIndexparallelcreate() )
      {
        add_stmt( "alter index " + pIndexDiff.consNameNew + " noparallel" );
      }
    }
    else
    {
      if( pIndexDiff.parallelIsEqual == false || pIndexDiff.parallel_degreeIsEqual == false )
      {
        stmt_set( "alter index" );
        stmt_add( pIndexDiff.consNameNew );
        if( pIndexDiff.parallelNew == ParallelType.PARALLEL )
        {
          stmt_add( "parallel" );
          if( pIndexDiff.parallel_degreeNew != null && pIndexDiff.parallel_degreeNew > 1 )
          {
            stmt_add( " " + pIndexDiff.parallel_degreeNew );
          }
        }
        else
        {
          stmt_add( "noparallel" );
        }

        stmt_done();
      }

      if( pIndexDiff.loggingIsEqual == false )
      {
        stmt_set( "alter index" );
        stmt_add( pIndexDiff.consNameNew );
        if( pIndexDiff.loggingNew == LoggingType.NOLOGGING )
        {
          stmt_add( "nologging" );
        }
        else
        {
          stmt_add( "logging" );
        }

        stmt_done();
      }

      if( pIndexDiff.tablespaceIsEqual == false && !(pIndexDiff.tablespaceOld == null && pIndexDiff.tablespaceNew == null) && isIndexmovetablespace() == true )
      {
        stmt_set( "alter index" );
        stmt_add( pIndexDiff.consNameNew );
        stmt_add( "rebuild tablespace" );
        stmt_add( nvl( pIndexDiff.tablespaceNew, getDefaultTablespace() ) );
        stmt_done();
      }
    }
  }

  private String getDefaultTablespace()
  {
    return InitDiffRepository.getDefaultTablespace();
  }

  private boolean isIndexparallelcreate()
  {
    return _parameters.isIndexparallelcreate();
  }

  private void handleConstraint( String pTablename, ConstraintDiff pConstraintDiff )
  {
    if( pConstraintDiff.isMatched == false || pConstraintDiff.isRecreateNeeded == true )
    {
      stmt_set( "alter table " + pTablename + " add constraint " + pConstraintDiff.consNameNew + " check (" + pConstraintDiff.ruleNew + ")" );
      if( pConstraintDiff.deferrtypeNew != null )
      {
        stmt_add( "deferrable initially " + pConstraintDiff.deferrtypeNew.getName() );
      }
      if( pConstraintDiff.statusNew != null )
      {
        stmt_add( pConstraintDiff.statusNew.getName() );
      }

      add_stmt();
    }
  }

  private void handlePrimarykey( TableDiff pTableDiff )
  {
    if( pTableDiff.primary_keyDiff.isMatched == false || pTableDiff.primary_keyDiff.isRecreateNeeded == true )
    {
      pv_stmt = "alter table " + pTableDiff.nameNew + " add";
      if( pTableDiff.primary_keyDiff.consNameNew != null )
      {
        pv_stmt = pv_stmt + " " + "constraint " + pTableDiff.primary_keyDiff.consNameNew;
      }
      pv_stmt = pv_stmt + " " + "primary key (" + get_column_list( pTableDiff.primary_keyDiff.pk_columnsDiff ) + ")";

      if( pTableDiff.primary_keyDiff.tablespaceNew != null || pTableDiff.primary_keyDiff.reverseNew != null )
      {
        pv_stmt = pv_stmt + " " + "using index";

        if( pTableDiff.primary_keyDiff.reverseNew != null )
        {
          pv_stmt = pv_stmt + " " + "reverse";
        }

        if( pTableDiff.primary_keyDiff.tablespaceNew != null )
        {
          pv_stmt = pv_stmt + " " + "tablespace " + pTableDiff.primary_keyDiff.tablespaceNew;
        }
      }

      add_stmt();
    }
  }

  private String get_column_list( List<ColumnRefDiff> pColumnRefDiffList )
  {
    String v_return = null;
    for( ColumnRefDiff lColumnRefDiff : pColumnRefDiffList )
    {
      if( lColumnRefDiff.isNew )
      {
        if( v_return != null )
        {
          v_return = v_return + ",";
        }
        else
        {
          v_return = "";
        }

        v_return = v_return + lColumnRefDiff.column_nameNew;
      }
    }

    return v_return;
  }

  private String get_column_datatype( ColumnDiff pColumnDiff )
  {
    String v_datatype = "";
    if( pColumnDiff.data_typeNew == DataType.OBJECT )
    {
      v_datatype = pColumnDiff.object_typeNew;
    }
    else
    {
      if( pColumnDiff.data_typeNew == DataType.LONG_RAW )
      {
        v_datatype = "long raw";
      }
      else
      {
        v_datatype = pColumnDiff.data_typeNew.name().toUpperCase();
      }

      if( pColumnDiff.precisionNew != null )
      {
        v_datatype = v_datatype + "(" + pColumnDiff.precisionNew;

        if( pColumnDiff.scaleNew != null )
        {
          v_datatype = v_datatype + "," + pColumnDiff.scaleNew;
        }

        if( pColumnDiff.byteorcharNew != null )
        {
          v_datatype = v_datatype + " " + pColumnDiff.byteorcharNew.getName().toUpperCase();
        }

        v_datatype = v_datatype + ")";
      }

      if( "with_time_zone".equals( pColumnDiff.with_time_zoneNew ) )
      {
        v_datatype = v_datatype + " with time zone";
      }
    }

    return v_datatype;
  }

  private String create_column_create_part( ColumnDiff pColumnDiff )
  {
    String lReturn = pColumnDiff.nameNew + " " + get_column_datatype( pColumnDiff );

    if( pColumnDiff.default_valueNew != null )
    {
      lReturn = lReturn + " default " + pColumnDiff.default_valueNew;
    }

    if( pColumnDiff.identityDiff.isNew == true )
    {
      lReturn = lReturn + " generated";
      if( pColumnDiff.identityDiff.alwaysNew != null )
      {
        lReturn = lReturn + " always";
      }
      if( pColumnDiff.identityDiff.by_defaultNew != null )
      {
        lReturn = lReturn + " by default";
      }
      if( pColumnDiff.identityDiff.on_nullNew != null )
      {
        lReturn = lReturn + " on null";
      }
      lReturn = lReturn + " as identity";

      lReturn = lReturn + " (";

      if( pColumnDiff.identityDiff.increment_byNew != null && pColumnDiff.identityDiff.increment_byNew > 0 )
      {
        lReturn = lReturn + " increment by " + pColumnDiff.identityDiff.increment_byNew;
      }
      else
      {
        lReturn = lReturn + " increment by 1";
      }

      if( pColumnDiff.identityDiff.maxvalueNew != null && pColumnDiff.identityDiff.maxvalueNew > 0 )
      {
        lReturn = lReturn + " maxvalue " + pColumnDiff.identityDiff.maxvalueNew;
      }

      if( pColumnDiff.identityDiff.minvalueNew != null && pColumnDiff.identityDiff.minvalueNew > 0 )
      {
        lReturn = lReturn + " minvalue " + pColumnDiff.identityDiff.minvalueNew;
      }

      if( pColumnDiff.identityDiff.cycleNew != null )
      {
        lReturn = lReturn + " " + pColumnDiff.identityDiff.cycleNew.getLiteral();
      }

      if( pColumnDiff.identityDiff.cacheNew != null && pColumnDiff.identityDiff.cacheNew > 0 )
      {
        lReturn = lReturn + " cache " + pColumnDiff.identityDiff.cacheNew;
      }

      if( pColumnDiff.identityDiff.orderNew != null )
      {
        lReturn = lReturn + " " + pColumnDiff.identityDiff.orderNew.getLiteral();
      }

      lReturn = lReturn + " )";
    }

    if( pColumnDiff.notnullNew == true )
    {
      lReturn = lReturn + " not null";
    }

    return lReturn;
  }

  private void handleColumn( TableDiff pTableDiff, ColumnDiff pColumnDiff )
  {

    if( pColumnDiff.isMatched == false )
    {
      pv_stmt = "alter table " + pTableDiff.nameNew + " add " + create_column_create_part( pColumnDiff );

      if( findLobstorage( pTableDiff, pColumnDiff.nameNew ) != null )
      {
        pv_stmt = pv_stmt + " " + "lob (" + pColumnDiff.nameNew + ") store as ( tablespace " + findLobstorage( pTableDiff, pColumnDiff.nameNew ).tablespaceNew + " )";
      }

      add_stmt();
    }
    else
    {
      if( pColumnDiff.isRecreateNeeded == true )
      {
        recreateColumn( pTableDiff, pColumnDiff );
      }
      else
      {
        if( pColumnDiff.byteorcharIsEqual == false || pColumnDiff.precisionIsEqual == false || pColumnDiff.scaleIsEqual == false )
        {
          add_stmt( "alter table " + pTableDiff.nameNew + " modify ( " + pColumnDiff.nameNew + " " + get_column_datatype( pColumnDiff ) + ")" );
        }

        if( pColumnDiff.default_valueIsEqual == false )
        {
          pv_stmt = "alter table " + pTableDiff.nameNew + " modify ( " + pColumnDiff.nameNew + " default";
          if( pColumnDiff.default_valueNew == null )
          {
            pv_stmt = pv_stmt + " " + "null";
          }
          else
          {
            pv_stmt = pv_stmt + " " + pColumnDiff.default_valueNew;
          }
          pv_stmt = pv_stmt + " " + ")";
          add_stmt();
        }

        if( pColumnDiff.notnullIsEqual == false )
        {
          pv_stmt = "alter table " + pTableDiff.nameNew + " modify ( " + pColumnDiff.nameNew;
          if( pColumnDiff.notnullNew == false )
          {
            pv_stmt = pv_stmt + " " + "null";
          }
          else
          {
            pv_stmt = pv_stmt + " " + "not null";
          }
          pv_stmt = pv_stmt + " " + ")";
          add_stmt();
        }
      }
    }
  }

  private void recreateColumn( TableDiff pTableDiff, ColumnDiff pColumnDiff )
  {
    String v_tmp_old_columnameNew = "DTO_" + pColumnDiff.nameNew;
    String v_tmp_new_columnameNew = "DTN_" + pColumnDiff.nameNew;

    add_stmt( "alter table " + pTableDiff.nameNew + " add " + v_tmp_new_columnameNew + " " + get_column_datatype( pColumnDiff ) );

    //      TODO    for cur_trigger in
    //            (
    //            select trigger_name
    //              from user_triggers
    //             where table_name = pTableDiff.nameNew
    //            )
    //          {
    //            add_stmt( "alter trigger " + cur_trigger.trigger_name + " disable" );
    //          }

    add_stmt( "update " + pTableDiff.nameNew + " set " + v_tmp_new_columnameNew + " = " + pColumnDiff.nameOld );
    add_stmt( "commit" );

    //          for cur_trigger in
    //            (
    //            select trigger_name
    //              from user_triggers
    //             where table_name = pTableDiff.nameNew
    //            )
    //          {
    //            add_stmt( "alter trigger " + cur_trigger.trigger_name + " enable" );
    //          }

    add_stmt( "alter table " + pTableDiff.nameNew + " rename column " + pColumnDiff.nameOld + " to " + v_tmp_old_columnameNew );
    add_stmt( "alter table " + pTableDiff.nameNew + " rename column " + v_tmp_new_columnameNew + " to " + pColumnDiff.nameNew );
    add_stmt( "alter table " + pTableDiff.nameNew + " drop column " + v_tmp_old_columnameNew );

    if( pColumnDiff.default_valueNew != null )
    {
      pv_stmt = "alter table " + pTableDiff.nameNew + " modify ( " + pColumnDiff.nameNew + " default";
      pv_stmt = pv_stmt + " " + pColumnDiff.default_valueNew;
      pv_stmt = pv_stmt + " " + ")";
      add_stmt();
    }

    if( pColumnDiff.notnullNew == true )
    {
      pv_stmt = "alter table " + pTableDiff.nameNew + " modify ( " + pColumnDiff.nameNew;
      pv_stmt = pv_stmt + " " + "not null";
      pv_stmt = pv_stmt + " " + ")";
      add_stmt();
    }
  }

  private String adjust_compression_literal( String pLiteral )
  {
    return pLiteral.replace( "_low", " low" ).replace( "_high", " high" ).replace( "_operations", " operations" );
  }

  private void stmt_done()
  {
    add_stmt();
  }

  private boolean is_tablemovetablespace()
  {
    return _parameters.isTablemovetablespace();
  }

  private void stmt_add( String pString )
  {
    pv_stmt = pv_stmt + " " + pString;
  }

  private void stmt_set( String pString )
  {
    pv_stmt = pString;
  }

  private void drop_with_dropmode_check( String pTestStatement, String pDropStatement )
  {
    if( is_dropmode() != true )
    {
      if( has_rows( pTestStatement ) == true )
      {
        if( is_dropmode_ignore() != true )
        {
          throw new RuntimeException( "drop mode ist nicht aktiv, daher kann folgendes statement nicht ausgefuehrt werden: " + pDropStatement );
        }
        else
        {
          add_stmt( "-- dropmode-ignore: " + pDropStatement );
          return;
        }
      }
    }

    add_stmt( pDropStatement );
  }

  private boolean is_dropmode_ignore()
  {
    return false;
  }

  private boolean has_rows( String pTestStatement )
  {
    return (Boolean)new WrapperReturnValueFromResultSet( pTestStatement, JdbcConnectionHandler.getCallableStatementProvider() )
    {
      @Override
      protected Object getValueFromResultSet( ResultSet pResultSet ) throws SQLException
      {
        return pResultSet.next();
      }
    }.executeForValue();
  }

  private boolean is_dropmode()
  {
    return _parameters.isDropmode();
  }

  private String pv_stmt;
  private List<String> pv_stmtList = new ArrayList<String>();
  private Parameters _parameters;

  private void add_stmt()
  {
    add_stmt( pv_stmt );
    pv_stmt = null;
  }

  private <T> T nvl( T pObject, T pDefault )
  {
    return pObject == null ? pDefault : pObject;
  }

  private void add_stmt( String pString )
  {
    pv_stmtList.add( pString );
  }
}

//pv_model_diff od_orig_model;
//
//pv_stmt varchar2(32000);
//type t_varchar_list is table of varchar2(32000);
//pv_statement_list t_varchar_list;
//pv_default_orig_chartype ot_orig_chartype;
//pv_default_tablespace varchar2(30);
//pv_temporary_tablespace varchar2(30); 
//

//
//function replace_linefeed_by_space ( p_script in varchar2) return varchar2 is
//{
//return replace(replace(replace(p_script, chr(13) + chr(10)," "), chr(10)," "), chr(13)," ");
//}
//

//
//function adjust_refreshmethod_literal ( p_literal in varchar2 ) return varchar2 is
//{
//return replace(replace(p_literal, "refresh_"," refresh "), "never_","never ");
//}    
//

//
//function get_fk_for_ref_partitioning( p_orig_table in ot_orig_table ) return ot_orig_foreignkey
//is
//{
//for i in 1..p_orig_table.i_foreign_keys.count()
//{
//  if( upper(p_orig_table.i_foreign_keys(i).i_consname) = upper(treat( p_orig_table.i_tablepartitioning as ot_orig_refpartitions ).i_fkname ))
//  {
//    return p_orig_table.i_foreign_keys(i);
//  }
//}
//  
//throw new RuntimeException( -20000, "fk for refpartitioning not found" );
//}   
//
//
//function get_mview_elements( p_model_elements in ct_orig_modelelement_list ) return ct_orig_mview_list
//is
//v_return ct_orig_mview_list = new ct_orig_mview_list();
//{  
//for i in 1 .. p_model_elements.count {
//  if( p_model_elements(i) is of (ot_orig_mview) ) 
//  {
//    v_return.ext}
//    v_return(v_return.count) = treat( p_model_elements(i) as ot_orig_mview );
//  }
//}    
//
//return v_return;
//}
//
//function get_sequence_elements( p_model_elements in ct_orig_modelelement_list ) return ct_orig_sequence_list
//is
//v_return ct_orig_sequence_list = new ct_orig_sequence_list();
//{  
//for i in 1 .. p_model_elements.count {
//  if( p_model_elements(i) is of (ot_orig_sequence) ) 
//  {
//    v_return.ext}
//    v_return(v_return.count) = treat( p_model_elements(i) as ot_orig_sequence );
//  }
//}    
//
//return v_return;
//}  
//
//function sort_tables_for_ref_part( p_model_elements in ct_orig_modelelement_list ) return ct_orig_table_list
//is
//v_orig_modelelement_list ct_orig_table_list = new ct_orig_table_list();
//v_orig_table_list ct_orig_modelelement_list = new ct_orig_modelelement_list();    
//v_orig_modelelement ot_orig_modelelement;
//type t_varchar_set is table of number index by varchar2(100);
//v_tab_set t_varchar_set;
//
//procedure clean_orig_table_list
//is
//  v_new_orig_table_list ct_orig_modelelement_list = new ct_orig_modelelement_list();  
//{
//  for i in 1..v_orig_table_list.count
//  {
//    if( v_orig_table_list(i) != null )
//    {
//      v_new_orig_table_list.extend(1);
//      v_new_orig_table_list(v_new_orig_table_list.count) = v_orig_table_list(i);
//    }
//  }
//  
//  v_orig_table_list = v_new_orig_table_list;
//}    
//
//function add_orig_table_list return number
//is
//  v_orig_table ot_orig_table;    
//  v_required_table_name varchar2(100) = null; 
//{
//  for i in 1..v_orig_table_list.count
//  {
//    v_orig_table = treat( v_orig_table_list(i) as ot_orig_table );
//    
//    if( v_orig_table.i_tablepartitioning != null && v_orig_table.i_tablepartitioning is of (ot_orig_refpartitions) )
//    {
//      v_required_table_name = get_fk_for_ref_partitioning( v_orig_table ).i_desttable;
//    } else {
//      v_required_table_name = null;
//    }
//    
//    if( v_required_table_name == null || v_tab_set.exists(v_required_table_name) )
//    { 
//      v_orig_modelelement_list.extend(1);
//      v_orig_modelelement_list(v_orig_modelelement_list.count) = v_orig_table;    
//      v_tab_set(upper(v_orig_table.i_name)) == true;
//      v_orig_table_list(i) = null;
//      clean_orig_table_list();
//     
//      return 1;
//    }
//  }
//  
//  return 0;      
//}
//
//procedure add_orig_table_list_multi    
//is
//{
//  {
//    if( add_orig_table_list == false )
//    {
//      return;
//    }
//  }
//}
//{
//for i in 1 .. p_model_elements.count()
//{
//  v_orig_modelelement = p_model_elements(i);
//
//  if( v_orig_modelelement is of (ot_orig_table) ) 
//  {
//    v_orig_table_list.extend(1);
//    v_orig_table_list(v_orig_table_list.count) = treat( v_orig_modelelement as ot_orig_table );
//    
//    add_orig_table_list_multi();
//  }
//}  
//
//add_orig_table_list_multi();
//
//if( v_orig_table_list.count !== false )
//{
//  throw new RuntimeException( -20000, "possible table order not found " + v_orig_table_list.count );
//}
//
//return v_orig_modelelement_list;
//}  
//

//

//
//function has_rows_ignore_errors( p_test_stmt in varchar2 ) return number
//is
//{
//return has_rows( p_test_stmt );
//exception
//when others {
//  return 0; 
//}    

//
//
//procedure handle_mview( p_mview_diff od_orig_mview )
//is     
//v_orig_refreshmodetype ot_orig_refreshmodetype;
//v_refreshmode varchar2(10);
//  
//procedure create_mview
//is
//{
//  stmt_set( "create materialized view" );
//  stmt_add( p_mview_diff.n_mview_name );
//  
//  if( ot_orig_buildmodetype.is_equal( p_mview_diff.n_buildmode, ot_orig_buildmodetype.c_prebuilt, ot_orig_buildmodetype.c_immediate ) == true )
//  {
//    stmt_add( "on prebuilt table" );
//  } else {
//    -- Physical properties nur, wenn nicht prebuilt
//    if( p_mview_diff.n_tablespace != null )
//    {
//      stmt_add( "tablespace" ); 
//      stmt_add( p_mview_diff.n_tablespace );            
//    }   
//
//    if( ot_orig_compresstype.is_equal( p_mview_diff.n_compression, ot_orig_compresstype.c_compress, ot_orig_compresstype.c_nocompress ) == true )
//    {
//      stmt_add( "compress" );
//      if( ot_orig_compressfortype.is_equal( p_mview_diff.n_compressionfor, ot_orig_compressfortype.c_all ) == true )
//      {
//        stmt_add( "for all operations" );
//      elsif ( ot_orig_compressfortype.is_equal( p_mview_diff.n_compressionfor, ot_orig_compressfortype.c_direct_load ) == true )
//        {
//        stmt_add( "for direct_load operations" );
//      elsif ( ot_orig_compressfortype.is_equal( p_mview_diff.n_compressionfor, ot_orig_compressfortype.c_query_low ) == true )
//        {
//        stmt_add( "for query low" );  
//      elsif ( ot_orig_compressfortype.is_equal( p_mview_diff.n_compressionfor, ot_orig_compressfortype.c_query_high ) == true )
//        {
//        stmt_add( "for query high" );   
//      elsif ( ot_orig_compressfortype.is_equal( p_mview_diff.n_compressionfor, ot_orig_compressfortype.c_archive_low ) == true )
//        {
//        stmt_add( "for archive low" );  
//      elsif ( ot_orig_compressfortype.is_equal( p_mview_diff.n_compressionfor, ot_orig_compressfortype.c_archive_high ) == true )
//        {
//        stmt_add( "for archive high" );             
//      }
//    } else {
//      stmt_add( "nocompress" );
//    }
//    
//    if( ot_orig_paralleltype.is_equal( p_mview_diff.n_parallel, ot_orig_paralleltype.c_parallel ) == true )
//    {
//      stmt_add( "parallel" ); 
//      if ( p_mview_diff.n_parallel_degree > 1 )
//      {
//        stmt_add( p_mview_diff.n_parallel_degree );
//      }
//    } else {
//      stmt_add( "noparallel" );               
//    }      
//      
//    if( p_mview_diff.n_buildmode != null )
//    {   
//      stmt_add( "build" );
//      stmt_add( p_mview_diff.n_buildmode.i_literal );   
//    }  
//  }
//    
//  if( p_mview_diff.n_refreshmethod != null )
//  {
//    stmt_add( adjust_refreshmethod_literal(p_mview_diff.n_refreshmethod.i_literal) );    
//    
//    if( p_mview_diff.n_refreshmode != null )
//    {
//      stmt_add( "on" );
//      stmt_add( p_mview_diff.n_refreshmode.i_literal );            
//    } 
//  }               
//
//  if( ot_orig_enabletype.is_equal( p_mview_diff.n_queryrewrite, ot_orig_enabletype.c_enable ) == true )
//  {
//    stmt_add( "enable query rewrite" ); 
//  }  
//    
//  stmt_add( "as" ); 
//  stmt_add( replace_linefeed_by_space(p_mview_diff.n_viewselectclob) );   
//  add_stmt(); 
//}
//
//{
//if( p_mview_diff.isMatched == false )
//{
//  create_mview();
//} else {
//  if(    
//       p_mview_diff.e_tablespace == false
//    || p_mview_diff.e_viewselectclob == false 
//    || p_mview_diff.e_buildmode == false 
//    )                                        
//  {
//    add_stmt( "drop materialized view " + p_mview_diff.o_mview_name );   
//        
//    create_mview();
//  } else { 
//    if( p_mview_diff.e_queryrewrite == false )
//    {
//      add_stmt( "alter materialized view " + p_mview_diff.n_mview_name + " " + p_mview_diff.n_queryrewrite.i_literal + " query rewrite");  
//    }
//    
//    if( p_mview_diff.e_refreshmode == false || p_mview_diff.e_refreshmethod == false )
//    {
//      v_orig_refreshmodetype = p_mview_diff.n_refreshmode;
//      if ( v_orig_refreshmodetype == null ) 
//      {
//        v_refreshmode = "";
//      } else {
//        v_refreshmode = " on " + v_orig_refreshmodetype.i_literal;
//      }
//      add_stmt( "alter materialized view " + p_mview_diff.n_mview_name + " " + adjust_refreshmethod_literal(p_mview_diff.n_refreshmethod.i_literal) + v_refreshmode );  
//    }
//    
//    -- Physical parameters nur, wenn nicht prebuilt
//    if ( ot_orig_buildmodetype.is_equal(  p_mview_diff.n_buildmode,  ot_orig_buildmodetype.c_prebuilt, ot_orig_buildmodetype.c_immediate ) !== true )
//    {        
//      if( p_mview_diff.e_parallel == false || p_mview_diff.e_parallel_degree == false )
//      {
//        stmt_set( "alter materialized view" );
//        stmt_add( p_mview_diff.n_mview_name );        
//        if( ot_orig_paralleltype.is_equal( p_mview_diff.n_parallel, ot_orig_paralleltype.c_parallel ) == true )
//        {
//          stmt_add( "parallel" ); 
//          if ( p_mview_diff.n_parallel_degree > 1 )
//          {
//            stmt_add( p_mview_diff.n_parallel_degree );
//          }
//        } else {
//          stmt_add( "noparallel" );         
//        }         
//        
//        stmt_done();
//      }    
//      
//      if( p_mview_diff.e_compression == false || p_mview_diff.e_compressionfor == false )
//      {
//        stmt_set( "alter materialized view" );
//        stmt_add( p_mview_diff.n_mview_name );        
//        if( ot_orig_compresstype.is_equal( p_mview_diff.n_compression, ot_orig_compresstype.c_compress, ot_orig_compresstype.c_nocompress ) == true )
//        {
//          stmt_add( "compress" ); 
//          if ( p_mview_diff.n_compressionFor != null )
//          {
//            stmt_add( "for " + adjust_compression_literal(p_mview_diff.n_compressionFor.i_literal));
//          }
//        } else {
//          stmt_add( "nocompress" );         
//        }         
//        
//        stmt_done();
//      }  
//    }  
//  }     
//}  
//}            
//
//procedure handle_all_mviews
//is
//{      
//for i in 1 .. pv_model_diff.c_model_elements_mview.count()
//{
//  if( pv_model_diff.c_model_elements_mview(i).is_equal == false )
//  {        
//    if( pv_model_diff.c_model_elements_mview(i).isNew == true )
//    {                
//      handle_mview( pv_model_diff.c_model_elements_mview(i) );
//    } else {
//      add_stmt( "drop materialized view " + pv_model_diff.c_model_elements_mview(i).o_mview_name );          
//    }
//  }
//}  
//}
//

//
//procedure create_table( p_table_diff od_orig_table )
//is

//    
//function create_range_valuelist( p_orig_rangepartitionval_list ct_orig_rangepartitionval_list ) return varchar2
//is
//  v_return varchar2(32000);
//  v_orig_rangepartitionval ot_orig_rangepartitionval; 
//{
//  for j in 1..p_orig_rangepartitionval_list.count()
//  {
//    v_orig_rangepartitionval = p_orig_rangepartitionval_list(j);
//    if(j!=1)
//    {
//      v_return = v_return + ",";              
//    }
//      
//    if( v_orig_rangepartitionval.i_value != null )
//    {
//      v_return = v_return + v_orig_rangepartitionval.i_value;                    
//    } else {
//      v_return = v_return + "maxvalue";
//    }
//  }       
//  
//  return v_return;   
//}  
//
//function create_list_valuelist( p_orig_listpartitionvalu_list ct_orig_listpartitionvalu_list ) return varchar2
//is
//  v_return varchar2(32000);
//  v_orig_listpartitionvalu ot_orig_listpartitionvalu; 
//{
//  for j in 1..p_orig_listpartitionvalu_list.count()
//  {
//    v_orig_listpartitionvalu = p_orig_listpartitionvalu_list(j);
//    if(j!=1)
//    {
//      v_return = v_return + ",";              
//    }
//      
//    if( v_orig_listpartitionvalu.i_value != null )
//    {
//      v_return = v_return + v_orig_listpartitionvalu.i_value;                    
//    } else {
//      v_return = v_return + "default";
//    }
//  }       
//  
//  return v_return;   
//}    
//
//function create_sub_range_clause( p_orig_rangesubsubpart ot_orig_rangesubsubpart ) return varchar2
//is
//  v_return varchar2(32000);
//{  
//  v_return = v_return + "subpartition " + p_orig_rangesubsubpart.i_name + " values less than (";
//    
//  v_return = v_return + create_range_valuelist(p_orig_rangesubsubpart.i_value );
//    
//  v_return = v_return + ")";                    
//    
//  if( p_orig_rangesubsubpart.i_tablespace != null )
//  {
//    v_return = v_return + " tablespace " + p_orig_rangesubsubpart.i_tablespace;
//  }      
//
//  return v_return;
//}  
//
//function create_sub_list_clause( p_orig_listsubsubpart ot_orig_listsubsubpart ) return varchar2
//is
//  v_return varchar2(32000);
//{  
//  v_return = v_return + "subpartition " + p_orig_listsubsubpart.i_name + " values (";
//    
//  v_return = v_return + create_list_valuelist(p_orig_listsubsubpart.i_value );
//    
//  v_return = v_return + ")";                    
//    
//  if( p_orig_listsubsubpart.i_tablespace != null )
//  {
//    v_return = v_return + " tablespace " + p_orig_listsubsubpart.i_tablespace;
//  }      
//
//  return v_return;
//}  
//
//function create_sub_hash_clause( p_orig_hashsubsubpart ot_orig_hashsubsubpart ) return varchar2
//is
//  v_return varchar2(32000);
//{  
//  v_return = v_return + "subpartition " + p_orig_hashsubsubpart.i_name;
//          
//  if( p_orig_hashsubsubpart.i_tablespace != null )
//  {
//    v_return = v_return + " tablespace " + p_orig_hashsubsubpart.i_tablespace;
//  }      
//
//  return v_return;
//}  
//
//function create_subpartitions( p_orig_subsubpart_list ct_orig_subsubpart_list ) return varchar2
//is
//  v_return varchar2(32000);
//  v_orig_subsubpart ot_orig_subsubpart;
//{
//  v_return = v_return + "(";              
//  for i in 1..p_orig_subsubpart_list.count
//  {    
//    v_orig_subsubpart = p_orig_subsubpart_list(i);
//    
//    if(i!=1)
//    {
//      v_return = v_return + ",";              
//    }
//    
//    if( v_orig_subsubpart is of (ot_orig_rangesubsubpart) )
//    {
//      v_return = v_return + create_sub_range_clause( treat( v_orig_subsubpart as ot_orig_rangesubsubpart) );
//    elsif( v_orig_subsubpart is of (ot_orig_listsubsubpart) )
//    {
//      v_return = v_return + create_sub_list_clause( treat( v_orig_subsubpart as ot_orig_listsubsubpart) );        
//    elsif( v_orig_subsubpart is of (ot_orig_hashsubsubpart) )
//    {
//      v_return = v_return + create_sub_hash_clause( treat( v_orig_subsubpart as ot_orig_hashsubsubpart) );          
//    } else {    
//      throw new RuntimeException(-20000,"subpartitionstyp unbekannt");
//    }
//  }
//  
//  v_return = v_return + ")";              
//
//  return v_return;
//}
//
//function create_range_sub_parts( p_orig_rangesubparts ot_orig_rangesubparts ) return varchar2
//is
//  v_return varchar2(32000);
//{
//  v_return = v_return + " subpartition by range (";              
//  for i in 1..p_orig_rangesubparts.i_columns.count()
//  {
//    if(i!=1)
//    {
//      v_return = v_return + ",";              
//    }
//    v_return = v_return + p_orig_rangesubparts.i_columns(i).i_column_name;
//  }
//  v_return = v_return + ")";           
//
//  return v_return;
//}  
//
//function create_list_sub_parts( p_orig_listsubparts ot_orig_listsubparts ) return varchar2
//is
//  v_return varchar2(32000);
//{
//  v_return = v_return + " subpartition by list (" + p_orig_listsubparts.i_column.i_column_name + ")";           
//
//  return v_return;
//}    
//
//function create_hash_sub_parts( p_orig_hashsubparts ot_orig_hashsubparts ) return varchar2
//is
//  v_return varchar2(32000);
//{
//  v_return = v_return + " subpartition by hash (" + p_orig_hashsubparts.i_column.i_column_name + ")";           
//
//  return v_return;
//}    
//
//function create_table_sub_parts( p_orig_tablesubpart ot_orig_tablesubpart ) return varchar2
//is
//  v_return varchar2(32000);
//{
//  if( p_orig_tablesubpart is of (ot_orig_rangesubparts) )
//  {
//    v_return = v_return + create_range_sub_parts( treat( p_orig_tablesubpart as ot_orig_rangesubparts) );
//  elsif( p_orig_tablesubpart is of (ot_orig_listsubparts) )
//  {
//    v_return = v_return + create_list_sub_parts( treat( p_orig_tablesubpart as ot_orig_listsubparts) );
//  elsif( p_orig_tablesubpart is of (ot_orig_hashsubparts) )
//  {
//    v_return = v_return + create_hash_sub_parts( treat( p_orig_tablesubpart as ot_orig_hashsubparts) );
//  } else {    
//    throw new RuntimeException(-20000,"subpartitionstyp unbekannt");
//  }
//  
//  return v_return;
//}
//
//function create_range_clause( p_orig_rangepartitions ot_orig_rangepartitions ) return varchar2
//is
//  v_return varchar2(32000);
//  v_orig_rangepartition ot_orig_rangepartition;         
//  v_orig_rangesubpart ot_orig_rangesubpart;
//{
//  v_return = v_return + "partition by range (";      
//  for i in 1..p_orig_rangepartitions.i_columns.count()
//  {
//    if(i!=1)
//    {
//      v_return = v_return + ",";              
//    }
//    v_return = v_return + p_orig_rangepartitions.i_columns(i).i_column_name;
//  }
//  v_return = v_return + ")";      
//  
//  if( p_orig_rangepartitions.i_intervalexpression != null )
//  {
//    v_return = v_return + "interval (" + p_orig_rangepartitions.i_intervalexpression + ")";      
//  }
//  
//  if( p_orig_rangepartitions.i_tablesubpart != null )
//  {
//    v_return = v_return + create_table_sub_parts( p_orig_rangepartitions.i_tablesubpart );
//  
//    v_return = v_return + "(";
//          
//    for i in 1..p_orig_rangepartitions.i_subpartitionlist.count()
//    {
//      v_orig_rangesubpart = p_orig_rangepartitions.i_subpartitionlist(i);
//      if(i!=1)
//      {
//        v_return = v_return + ",";              
//      }
//      v_return = v_return + "partition " + v_orig_rangesubpart.i_name + " values less than (";
//      
//      v_return = v_return + create_range_valuelist(v_orig_rangesubpart.i_value );        
//      
//      v_return = v_return + ")";         
//      
//      v_return = v_return + create_subpartitions( v_orig_rangesubpart.i_subpartlist );        
//    }
//    v_return = v_return + ")";       
//  } else {
//    v_return = v_return + "(";
//    for i in 1..p_orig_rangepartitions.i_partitionlist.count()
//    { 
//      v_orig_rangepartition = p_orig_rangepartitions.i_partitionlist(i);
//      if(i!=1)
//      {
//        v_return = v_return + ",";              
//      }
//      v_return = v_return + "partition " + v_orig_rangepartition.i_name + " values less than (";
//    
//      v_return = v_return + create_range_valuelist(v_orig_rangepartition.i_value );
//    
//      v_return = v_return + ")";                    
//    
//      if( v_orig_rangepartition.i_tablespace != null )
//      {
//        v_return = v_return + " tablespace " + v_orig_rangepartition.i_tablespace;
//      }      
//    } 
//    v_return = v_return + ")";        
//  }             
//
//  return v_return; 
//} 
//
//function create_list_clause( p_orig_listpartitions ot_orig_listpartitions ) return varchar2
//is
//  v_return varchar2(32000);
//  v_orig_listpartition ot_orig_listpartition;
//  v_orig_listpartitionvalu ot_orig_listpartitionvalu; 
//  v_orig_listsubpart ot_orig_listsubpart;
//{
//  v_return = v_return + "partition by list (" + p_orig_listpartitions.i_column.i_column_name; 
//  v_return = v_return + ")";   
//  
//  if( p_orig_listpartitions.i_tablesubpart != null )
//  {
//    v_return = v_return + create_table_sub_parts( p_orig_listpartitions.i_tablesubpart );
//  
//    v_return = v_return + "(";
//          
//    for i in 1..p_orig_listpartitions.i_subpartitionlist.count()
//    {
//      v_orig_listsubpart = p_orig_listpartitions.i_subpartitionlist(i);
//      if(i!=1)
//      {
//        v_return = v_return + ",";              
//      }
//      v_return = v_return + "partition " + v_orig_listsubpart.i_name + " values (";
//      
//      v_return = v_return + create_list_valuelist(v_orig_listsubpart.i_value );        
//      
//      v_return = v_return + ")";         
//      
//      v_return = v_return + create_subpartitions( v_orig_listsubpart.i_subpartlist );        
//    }
//    v_return = v_return + ")";       
//  } else {
//    v_return = v_return + "(";    
//  
//    for i in 1..p_orig_listpartitions.i_partitionlist.count()
//    {
//      v_orig_listpartition = p_orig_listpartitions.i_partitionlist(i);
//      if(i!=1)
//      {
//        v_return = v_return + ",";              
//      }
//      v_return = v_return + "partition " + v_orig_listpartition.i_name + " values (";
//    
//      v_return = v_return + create_list_valuelist( v_orig_listpartition.i_value );        
//    
//      v_return = v_return + ")";    
//      
//      if( v_orig_listpartition.i_tablespace != null )
//      {
//        v_return = v_return + " tablespace " + v_orig_listpartition.i_tablespace;
//      }                      
//    }      
//    v_return = v_return + ")";                  
//  }
//
//  return v_return; 
//}   
//
//function create_hash_clause( p_orig_hashpartitions ot_orig_hashpartitions ) return varchar2
//is
//  v_return varchar2(32000);
//  v_orig_hashpartition ot_orig_hashpartition;
//{
//  v_return = v_return + "partition by hash (" + p_orig_hashpartitions.i_column.i_column_name; 
//  v_return = v_return + ")(";      
//  for i in 1..p_orig_hashpartitions.i_partitionlist.count()
//  {
//    v_orig_hashpartition = p_orig_hashpartitions.i_partitionlist(i);
//    if(i!=1)
//    {
//      v_return = v_return + ",";              
//    }
//    v_return = v_return + "partition " + v_orig_hashpartition.i_name;
//    
//    if( v_orig_hashpartition.i_tablespace != null )
//    {
//      v_return = v_return + " tablespace " + v_orig_hashpartition.i_tablespace;
//    }
//  }      
//  v_return = v_return + ")";                  
//
//  return v_return; 
//}     
//
//function create_ref_clause( p_orig_refpartitions ot_orig_refpartitions ) return varchar2
//is
//  v_return varchar2(32000);
//  v_orig_refpartition ot_orig_refpartition;
//{
//  v_return = v_return + "partition by reference (" + p_orig_refpartitions.i_fkname; 
//  v_return = v_return + ")(";      
//  for i in 1..p_orig_refpartitions.i_partitionlist.count()
//  {
//    v_orig_refpartition = p_orig_refpartitions.i_partitionlist(i);
//    if(i!=1)
//    {
//      v_return = v_return + ",";              
//    }
//    v_return = v_return + "partition " + v_orig_refpartition.i_name;
//    
//    if( v_orig_refpartition.i_tablespace != null )
//    {
//      v_return = v_return + " tablespace " + v_orig_refpartition.i_tablespace;
//    }
//  }      
//  v_return = v_return + ")";                  
//
//  return v_return; 
//}     
//
//function create_partitioning_clause( p_orig_tablepartitioning ot_orig_tablepartitioning ) return varchar2
//is
//  v_return varchar2(32000);
//{
//  if( p_orig_tablepartitioning == null )
//  {
//    return null;
//  }
//
//  v_return = v_return + " ";
//
//  if( p_orig_tablepartitioning is of (ot_orig_rangepartitions) )
//  {
//    v_return = v_return + create_range_clause( treat( p_orig_tablepartitioning as ot_orig_rangepartitions) );
//  elsif( p_orig_tablepartitioning is of (ot_orig_listpartitions) )
//  {
//    v_return = v_return + create_list_clause( treat( p_orig_tablepartitioning as ot_orig_listpartitions) );    
//  elsif( p_orig_tablepartitioning is of (ot_orig_hashpartitions) )
//  {
//    v_return = v_return + create_hash_clause( treat( p_orig_tablepartitioning as ot_orig_hashpartitions) );          
//  elsif( p_orig_tablepartitioning is of (ot_orig_refpartitions) )
//  {
//    v_return = v_return + create_ref_clause( treat( p_orig_tablepartitioning as ot_orig_refpartitions) );                
//  } else {    
//    throw new RuntimeException(-20000,"partitionstyp unbekannt");
//  }
//
//  return v_return; 
//}
//
//function create_ref_fk_clause( p_orig_table ot_orig_table ) return varchar2
//is
//  v_return varchar2(32000);
//  v_orig_refpartition ot_orig_refpartition;
//  v_orig_foreignkey ot_orig_foreignkey;
//{
//  if( p_orig_table.i_tablepartitioning is of (ot_orig_refpartitions) )
//  {
//    v_orig_foreignkey = get_fk_for_ref_partitioning( p_orig_table );
//  
//--        v_return = v_return + ", " + create_foreign_key_clause( v_orig_foreignkey );
//  }
//  return v_return; 
//}   

//procedure handle_mviewlog( p_table_diff in out nocopy od_orig_table )
//is     
//v_default_tablespace varchar2(30);
//c_date_format constant varchar2(30) = pa_orcas_run_parameter.get_dateformat();
//
//procedure create_mviewlog
//is
//{
//  stmt_set( "create materialized view log on" );       
//  stmt_add( p_table_diff.n_name ); 
//  
//  if( p_table_diff.c_mviewlog.n_tablespace != null )
//  {
//    stmt_add( "tablespace" ); 
//    stmt_add( p_table_diff.c_mviewlog.n_tablespace );            
//  }   
//  
//  if( ot_orig_paralleltype.is_equal( p_table_diff.c_mviewlog.n_parallel, ot_orig_paralleltype.c_parallel ) == true )
//  {
//    stmt_add( "parallel" ); 
//    if ( p_table_diff.c_mviewlog.n_parallel_degree > 1 )
//    {
//      stmt_add( p_table_diff.c_mviewlog.n_parallel_degree );
//    }
//  }
//  
//  stmt_add( "with" );
//  
//  if( nvl(p_table_diff.c_mviewlog.n_primarykey,"null") = "primary" 
//      || nvl(p_table_diff.c_mviewlog.n_rowid,"null") != "rowid"
//  )
//  {
//    stmt_add( "primary key" ); 
//    if( nvl(p_table_diff.c_mviewlog.n_rowid,"null") = "rowid" )
//    {
//      stmt_add( "," );
//    }     
//  }  
//  
//  if( nvl(p_table_diff.c_mviewlog.n_rowid,"null") = "rowid" )
//     { 
//     stmt_add( "rowid" );
//  }  
//  
//  if( nvl(p_table_diff.c_mviewlog.n_withsequence,"null") = "sequence" )
//     {
//     stmt_add( "," );
//     stmt_add( "sequence" );
//  }           
//                    
//  if( p_table_diff.c_mviewlog.c_columns.count() > 0 )
//  {
//    stmt_add( "(" );
//    stmt_add( get_column_list( p_table_diff.c_mviewlog.c_columns ) ); 
//    stmt_add( ")" ); 
//  }     
//           
//  if( nvl(p_table_diff.c_mviewlog.n_commitscn,"null") = "commit_scn" )
//     {
//     stmt_add( "," );
//     stmt_add( "commit scn" );
//  }      
//
//  if( p_table_diff.c_mviewlog.n_newvalues != null )
//     {
//     stmt_add( p_table_diff.c_mviewlog.n_newvalues.i_literal );
//     stmt_add( "new values" );
//  }    
//  
//  if (   p_table_diff.c_mviewlog.n_startwith != null 
//      || p_table_diff.c_mviewlog.n_next != null 
//      || (p_table_diff.c_mviewlog.n_repeatInterval != null && p_table_diff.c_mviewlog.n_repeatInterval !== false))
//    {
//      stmt_add( "purge" );
//      if (p_table_diff.c_mviewlog.n_startwith != null)
//        {
//          stmt_add( "start with" );
//          stmt_add( "to_date(""" + p_table_diff.c_mviewlog.n_startwith + """,""" + c_date_format + """)" );
//      }
//      if (p_table_diff.c_mviewlog.n_next != null)
//        {
//          stmt_add( "next" );
//          stmt_add( "to_date(""" + p_table_diff.c_mviewlog.n_next + """,""" + c_date_format + """)" );
//        } else { 
//          if (p_table_diff.c_mviewlog.n_repeatInterval != null && p_table_diff.c_mviewlog.n_repeatInterval !== false)
//          {
//              stmt_add( "repeat interval" );
//              stmt_add( p_table_diff.c_mviewlog.n_repeatInterval );
//          }
//      }
//    } else {
//      if( ot_orig_synchronoustype.is_equal( p_table_diff.c_mviewlog.n_synchronous, ot_orig_synchronoustype.c_asynchronous ) == true )
//        { 
//         stmt_add( "purge immediate asynchronous" );
//      }
//  }
//  
//  add_stmt();
//  
//}
//
//{
//if( p_table_diff.c_mviewlog.isMatched == false || p_table_diff.c_mviewlog.isOld == false )
//{
//  if( p_table_diff.c_mviewlog.isNew == true )
//  {
//    create_mviewlog();
//  }
//} else {
//  select distinct(default_tablespace) into v_default_tablespace from user_users;
//  if(   
//     p_table_diff.c_mviewlog.e_columns == false
//     || 
//     (  is_equal_ignore_case(              "rowid",                              p_table_diff.c_mviewlog.n_rowid                                   ) == true and
//        p_table_diff.c_mviewlog.e_primarykey == false
//     ) 
//     || 
//     (  p_table_diff.c_mviewlog.n_rowid == null and
//        is_equal_ignore_case(              p_table_diff.c_mviewlog.o_primarykey,     "primary"                                                  ) !== true 
//     )    
//     || p_table_diff.c_mviewlog.e_rowid == false
//     || p_table_diff.c_mviewlog.e_withsequence == false
//     || p_table_diff.c_mviewlog.e_commitscn == false
//     || p_table_diff.c_mviewlog.e_tablespace == false              
//    )                                        
//  {
//    add_stmt( "drop materialized view log on " + p_table_diff.o_name );   
//    
//    create_mviewlog();
//  } else {
//    if(    p_table_diff.c_mviewlog.e_parallel == false
//        || p_table_diff.c_mviewlog.e_parallel_degree == false
//      )
//    {
//      stmt_set( "alter materialized view log on" );
//      stmt_add( p_table_diff.n_name );        
//      if( ot_orig_paralleltype.is_equal( p_table_diff.c_mviewlog.n_parallel, ot_orig_paralleltype.c_parallel ) == true )
//      {
//        stmt_add( "parallel" ); 
//        if ( p_table_diff.c_mviewlog.n_parallel_degree > 1 )
//        {
//          stmt_add( p_table_diff.c_mviewlog.n_parallel_degree );
//        }
//      } else {
//        stmt_add( "noparallel" );         
//      }         
//      
//      stmt_done();
//    }
//    
//    if(  p_table_diff.c_mviewlog.e_newvalues == false
//      )
//    {
//      stmt_set( "alter materialized view log on" );
//      stmt_add( p_table_diff.n_name );       
//      
//      if( ot_orig_newvaluestype.is_equal( p_table_diff.c_mviewlog.n_newvalues, ot_orig_newvaluestype.c_including, ot_orig_newvaluestype.c_excluding ) == true )
//      {
//        stmt_add( "including" ); 
//      } else {
//        stmt_add( "excluding" );         
//      }                    
//      stmt_add( "new values" );
//      
//      stmt_done();
//    }
//    
//    if (   p_table_diff.c_mviewlog.e_startwith == false
//        || p_table_diff.c_mviewlog.e_next == false
//        || p_table_diff.c_mviewlog.e_repeatInterval == false
//        )
//      {
//          stmt_add( "alter materialized view log on" ); 
//          stmt_add( p_table_diff.n_name );    
//          stmt_add( "purge" );   
//          if( p_table_diff.c_mviewlog.e_startwith == false )
//          {
//              stmt_add( "start with" );
//              stmt_add( "to_date(""" + p_table_diff.c_mviewlog.n_startwith + """,""" + c_date_format + """)" );
//          }
//          if( p_table_diff.c_mviewlog.e_next == false )
//          {
//              stmt_add( "next" );
//              stmt_add( "to_date(""" + p_table_diff.c_mviewlog.n_next + """,""" + c_date_format +""")" );
//          } else { 
//              if (p_table_diff.c_mviewlog.e_repeatInterval == false)
//              {
//                  stmt_add( "repeat interval" );
//                  stmt_add( p_table_diff.c_mviewlog.n_repeatInterval );
//              }
//          }
//         
//      stmt_done();
//    } else {
//    
//        if( p_table_diff.c_mviewlog.e_synchronous == false )
//        {
//          stmt_add( "alter materialized view log on" ); 
//          stmt_add( p_table_diff.n_name );       
//          if( ot_orig_synchronoustype.is_equal( p_table_diff.c_mviewlog.n_synchronous, ot_orig_synchronoustype.c_asynchronous, ot_orig_synchronoustype.c_synchronous ) == true )
//          {
//              stmt_add( "purge immediate asynchronous" ); 
//          } else {
//              stmt_add( "purge immediate synchronous" ); 
//          }
//          
//          stmt_done();
//        }
//        
//    }
//    
//  }
//}        
//}         
//

//

//
//procedure execute_all_statements is    
//{
//commit;
//
//for i in 1..pv_statement_list.count
//{
//--      dbms_output.put_line( pv_statement_list(i) );
//  pa_orcas_exec_log.exec_stmt( pv_statement_list(i) );   
//}
//}
//
//
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
