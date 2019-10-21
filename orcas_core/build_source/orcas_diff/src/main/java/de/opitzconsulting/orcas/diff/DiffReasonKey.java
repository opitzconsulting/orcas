package de.opitzconsulting.orcas.diff;

import java.rmi.Naming;
import java.util.HashMap;
import java.util.Map;

import de.opitzconsulting.orcas.orig.diff.AbstractDiff;
import de.opitzconsulting.orcas.orig.diff.ColumnDiff;
import de.opitzconsulting.orcas.orig.diff.ConstraintDiff;
import de.opitzconsulting.orcas.orig.diff.ForeignKeyDiff;
import de.opitzconsulting.orcas.orig.diff.IndexDiff;
import de.opitzconsulting.orcas.orig.diff.InlineCommentDiff;
import de.opitzconsulting.orcas.orig.diff.ModelDiff;
import de.opitzconsulting.orcas.orig.diff.MviewDiff;
import de.opitzconsulting.orcas.orig.diff.MviewLogDiff;
import de.opitzconsulting.orcas.orig.diff.PrimaryKeyDiff;
import de.opitzconsulting.orcas.orig.diff.SequenceDiff;
import de.opitzconsulting.orcas.orig.diff.TableDiff;
import de.opitzconsulting.orcas.orig.diff.UniqueKeyDiff;

public class DiffReasonKey
{
  private String schemaName;
  private DiffReasonEntity diffReasonEntity;
  private String name;
  private DiffReasonSubEntity diffReasonSubEntity;
  private String subName;
  private String subSchemaName;

  static DiffReasonKey parseFromXml( String pObjectType, String pObjectName, String pSubobjectType, String pSubobjectName, String pSchemaName, String pSubSchemaName )
  {
    if (pSchemaName != null && pSchemaName.length() > 0) {
      pObjectName = pSchemaName + "." + pObjectName;
    }
    if (pSubSchemaName != null && pSubSchemaName.length() > 0) {
      pSubobjectName = pSubSchemaName + "." + pSubobjectName;
    }
    DiffReasonKey lDiffReasonKey = new DiffReasonKey( DiffReasonEntity.valueOf( pObjectType.toUpperCase() ), pObjectName );

    if( pSubobjectType != null )
    {
      return new DiffReasonKey( lDiffReasonKey, DiffReasonSubEntity.valueOf( pSubobjectType.toUpperCase() ), pSubobjectName );
    }

    return lDiffReasonKey;
  }

  private DiffReasonKey()
  {
  }

  private DiffReasonKey( DiffReasonEntity pDiffReasonEntity, String pName )
  {
    diffReasonEntity = pDiffReasonEntity;
    name = pName;

    int lIndexOfDot = name.indexOf('.');
    if(lIndexOfDot>0){
      schemaName = name.substring(0,lIndexOfDot);
      name = name.substring(lIndexOfDot+1);
    }
  }

  private DiffReasonKey( DiffReasonKey pDiffReasonKey, DiffReasonSubEntity pDiffReasonSubEntity, String pSubName )
  {
    this( pDiffReasonKey.diffReasonEntity, pDiffReasonKey.name );

    if( pDiffReasonKey.schemaName != null ){
      schemaName = pDiffReasonKey.schemaName;
    }

    diffReasonSubEntity = pDiffReasonSubEntity;
    subName = pSubName;

    if (subName != null) {
      int lIndexOfDot = subName.indexOf('.');
      if (lIndexOfDot > 0) {
        subSchemaName = subName.substring(0, lIndexOfDot);
        subName = subName.substring(lIndexOfDot + 1);
      }
    }
  }

  private DiffReasonKey( SequenceDiff pSequenceDiff )
  {
    this( DiffReasonEntity.SEQUENCE, pSequenceDiff.isNew ? pSequenceDiff.sequence_nameNew : pSequenceDiff.sequence_nameOld );
  }

  private DiffReasonKey( TableDiff pTableDiff )
  {
    this( DiffReasonEntity.TABLE, pTableDiff.isNew ? pTableDiff.nameNew : pTableDiff.nameOld );
  }

  public DiffReasonKey( String pTableName )
  {
    this( DiffReasonEntity.TABLE, pTableName );
  }

  private DiffReasonKey( MviewDiff pMviewDiff )
  {
    this( DiffReasonEntity.TABLE, pMviewDiff.isNew ? pMviewDiff.mview_nameNew : pMviewDiff.mview_nameOld );
  }

  private DiffReasonKey( TableDiff pTableDiff, ForeignKeyDiff pForeignKeyDiff )
  {
    this( new DiffReasonKey( pTableDiff ), DiffReasonSubEntity.FOREIGN_KEY, pForeignKeyDiff.isNew ? pForeignKeyDiff.consNameNew : pForeignKeyDiff.consNameOld );
  }

  private DiffReasonKey( TableDiff pTableDiff, ConstraintDiff pConstraintDiff )
  {
    this( new DiffReasonKey( pTableDiff ), DiffReasonSubEntity.CONSTRAINT, pConstraintDiff.isNew ? pConstraintDiff.consNameNew : pConstraintDiff.consNameOld );
  }

  private DiffReasonKey( TableDiff pTableDiff, UniqueKeyDiff pUniqueKeyDiff )
  {
    this( new DiffReasonKey( pTableDiff ), DiffReasonSubEntity.UNIQUE_KEY, pUniqueKeyDiff.isNew ? pUniqueKeyDiff.consNameNew : pUniqueKeyDiff.consNameOld );
  }

  private DiffReasonKey( TableDiff pTableDiff, IndexDiff pIndexDiff )
  {
    this( new DiffReasonKey( pTableDiff ), DiffReasonSubEntity.INDEX, pIndexDiff.isNew ? pIndexDiff.consNameNew : pIndexDiff.consNameOld );
  }

  private DiffReasonKey( TableDiff pTableDiff, InlineCommentDiff pCommentDiff )
  {
    this( new DiffReasonKey( pTableDiff ), //
    pCommentDiff.isNew ? (pCommentDiff.column_nameNew == null ? DiffReasonSubEntity.TABLE_COMMENT : DiffReasonSubEntity.COLUMN_COMMENT) : (pCommentDiff.column_nameOld == null ? DiffReasonSubEntity.TABLE_COMMENT : DiffReasonSubEntity.COLUMN_COMMENT), //
    pCommentDiff.isNew ? pCommentDiff.column_nameNew : pCommentDiff.column_nameOld );
  }

  private DiffReasonKey( TableDiff pTableDiff, PrimaryKeyDiff pPrimary_keyDiff )
  {
    this( new DiffReasonKey( pTableDiff ), DiffReasonSubEntity.PRIMARY_KEY, null );
  }

  private DiffReasonKey( TableDiff pTableDiff, ColumnDiff pColumnDiff )
  {
    this( new DiffReasonKey( pTableDiff ), DiffReasonSubEntity.COLUMN, pColumnDiff.isNew ? pColumnDiff.nameNew : pColumnDiff.nameOld );
  }

  private DiffReasonKey( TableDiff pTableDiff, MviewLogDiff pMviewLogDiff )
  {
    this( new DiffReasonKey( pTableDiff ), DiffReasonSubEntity.MV_LOG, null );
  }

  public String getTextKey()
  {
    return "" + diffReasonEntity.name().toLowerCase() + (diffReasonSubEntity == null ? "" : "_" + diffReasonSubEntity.name().toLowerCase()) + ":" + name + (subName == null ? "" : "." + subName);
  }

  public String getTextObjectType()
  {
    return diffReasonEntity.name().toLowerCase();
  }

  public String getTextSubobjectType()
  {
    return diffReasonSubEntity == null ? null : diffReasonSubEntity.name().toLowerCase();
  }

  public String getTextObjectName()
  {
    return name;
  }

  public String getTextSchemaName()
  {
    return schemaName;
  }

  public String getTextSubSchemaName()
  {
    return subSchemaName;
  }

  public String getTextSubobjectName()
  {
    return subName;
  }

  public enum DiffReasonEntity
  {
    TABLE, SEQUENCE
  }

  public enum DiffReasonSubEntity
  {
    UNIQUE_KEY, PRIMARY_KEY, FOREIGN_KEY, CONSTRAINT, INDEX, TABLE_COMMENT, COLUMN_COMMENT, COLUMN, MV_LOG
  }

  public static class DiffReasonKeyRegistry
  {
    private Map<AbstractDiff, DiffReasonKey> diffReasonKeyMap = new HashMap<>();

    public DiffReasonKey getDiffReasonKey( AbstractDiff pDiff )
    {
      DiffReasonKey lReturn = diffReasonKeyMap.get( pDiff );

      if( lReturn == null )
      {
        throw new IllegalStateException( "DiffReasonKey not found: " + pDiff );
      }

      return lReturn;
    }

    public DiffReasonKeyRegistry( ModelDiff pModelDiff )
    {
      for( TableDiff lTableDiff : pModelDiff.model_elementsTableDiff )
      {
        diffReasonKeyMap.put( lTableDiff, new DiffReasonKey( lTableDiff ) );

        for( ColumnDiff lColumnDiff : lTableDiff.columnsDiff )
        {
          diffReasonKeyMap.put( lColumnDiff, new DiffReasonKey( lTableDiff, lColumnDiff ) );
        }

        diffReasonKeyMap.put( lTableDiff.primary_keyDiff, new DiffReasonKey( lTableDiff, lTableDiff.primary_keyDiff ) );

        for( IndexDiff lIndexDiff : lTableDiff.ind_uksIndexDiff )
        {
          diffReasonKeyMap.put( lIndexDiff, new DiffReasonKey( lTableDiff, lIndexDiff ) );
        }

        for( UniqueKeyDiff lUniqueKeyDiff : lTableDiff.ind_uksUniqueKeyDiff )
        {
          diffReasonKeyMap.put( lUniqueKeyDiff, new DiffReasonKey( lTableDiff, lUniqueKeyDiff ) );
        }

        for( ConstraintDiff lConstraintDiff : lTableDiff.constraintsDiff )
        {
          diffReasonKeyMap.put( lConstraintDiff, new DiffReasonKey( lTableDiff, lConstraintDiff ) );
        }

        for( ForeignKeyDiff lForeignKeyDiff : lTableDiff.foreign_keysDiff )
        {
          diffReasonKeyMap.put( lForeignKeyDiff, new DiffReasonKey( lTableDiff, lForeignKeyDiff ) );
        }

        diffReasonKeyMap.put( lTableDiff.mviewLogDiff, new DiffReasonKey( lTableDiff, lTableDiff.mviewLogDiff ) );

        for( InlineCommentDiff lCommentDiff : lTableDiff.commentsDiff )
        {
          diffReasonKeyMap.put( lCommentDiff, new DiffReasonKey( lTableDiff, lCommentDiff ) );
        }
      }

      for( MviewDiff lMviewDiff : pModelDiff.model_elementsMviewDiff )
      {
        diffReasonKeyMap.put( lMviewDiff, new DiffReasonKey( lMviewDiff ) );
      }
      for( SequenceDiff lSequenceDiff : pModelDiff.model_elementsSequenceDiff )
      {
        diffReasonKeyMap.put( lSequenceDiff, new DiffReasonKey( lSequenceDiff ) );
      }
    }
  }
}
