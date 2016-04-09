package de.opitzconsulting.orcas.ot;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OracleOdGenerator
{
  public static void main( String[] pArgs ) throws Exception
  {
    PrintStream lPrintStream;

    if( pArgs.length > 0 )
    {
      lPrintStream = new PrintStream( new File( pArgs[0] ) );

      if( pArgs.length > 1 )
      {
        ClassDataType.setTypePrefix( pArgs[1] );
      }
    }
    else
    {
      lPrintStream = System.out;
      ClassDataType.setTypePrefix( "orig" );
    }

    TypeDataContainer lTypeDataContainer = new ClassDataParser().parse();

    for( ClassDataType lClassDataType : orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      if( !lClassDataType.isEnum() )
      {
        _writeOd( lClassDataType, lTypeDataContainer, lPrintStream );
      }
    }

    lPrintStream.close();
  }

  private static List<ClassDataType> _getDependencies( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer )
  {
    List<ClassDataType> lReturn = new ArrayList<ClassDataType>();

    if( pClassDataType.getSuperclass() != null )
    {
      lReturn.add( (ClassDataType)pTypeDataContainer.getClassData( pClassDataType.getSuperclass() ) );
    }

    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      ClassData lClassData = pTypeDataContainer.getClassData( lFieldData.getJavaType() );

      if( lClassData instanceof ClassDataType )
      {
        lReturn.add( (ClassDataType)lClassData );
      }
    }

    return lReturn;
  }

  public static List<ClassDataType> orderClassDataTypeList( List<ClassDataType> pAllClassDataTypes, TypeDataContainer pTypeDataContainer )
  {
    List<ClassDataType> lReturn = new ArrayList<ClassDataType>();

    Map<ClassDataType,List<ClassDataType>> lDependencyMap = new HashMap<ClassDataType,List<ClassDataType>>();

    List<ClassDataType> lSortedClassDataTypes = new ArrayList<ClassDataType>( pAllClassDataTypes );

    Collections.sort( lSortedClassDataTypes, new Comparator<ClassDataType>()
    {
      public int compare( ClassDataType pClassDataType1, ClassDataType pClassDataType2 )
      {
        return pClassDataType1.getSqlName().compareTo( pClassDataType2.getSqlName() );
      }
    } );

    for( ClassDataType lClassDataType : lSortedClassDataTypes )
    {
      lDependencyMap.put( lClassDataType, _getDependencies( lClassDataType, pTypeDataContainer ) );
    }

    boolean lOneFound;

    do
    {
      lOneFound = false;

      for( ClassDataType lClassDataType : new ArrayList<ClassDataType>( lDependencyMap.keySet() ) )
      {
        lDependencyMap.get( lClassDataType ).removeAll( lReturn );

        if( lDependencyMap.get( lClassDataType ).isEmpty() )
        {
          lReturn.add( lClassDataType );
          lDependencyMap.remove( lClassDataType );
          lOneFound = true;
        }
      }
    }
    while( lOneFound );

    if( !lDependencyMap.isEmpty() )
    {
      throw new RuntimeException();
    }

    return lReturn;
  }

  private static void _writeMemberSpecPrefix( ClassDataType pClassDataType, PrintStream pOut )
  {
    if( pClassDataType.isHasSubclasses() )
    {
      pOut.print( " not final" );
    }
    if( pClassDataType.getSuperclass() != null )
    {
      pOut.print( " overriding" );
    }
  }

  private static void _writeOd( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer, PrintStream pOut )
  {
    pOut.print( "create or replace type " + pClassDataType.getDiffSqlName() + " force" );

    if( pClassDataType.getSuperclass() == null )
    {
      pOut.println( " as object" );
    }
    else
    {
      pOut.println( " under " + pTypeDataContainer.getClassData( pClassDataType.getSuperclass() ).getDiffSqlName() );
    }
    pOut.println( "(" );

    boolean lIsMemberGenerated = false;

    if( pClassDataType.getFiledDataList().isEmpty() && pClassDataType.getSuperclass() == null )
    {
      pOut.println( "  " + pClassDataType.getSqlDummyFieldName() + " number" );
      lIsMemberGenerated = true;
    }
    else
    {
      for( FieldData lFieldData : pClassDataType.getFiledDataList() )
      {
        lIsMemberGenerated = true;
        ClassData lType = lFieldData.getClassData( lFieldData.getJavaType(), pTypeDataContainer );

        String lSqlTypeName;

        if( lType.isAtomicValue() )
        {
          lSqlTypeName = lType.getSqlName();

          if( isFieldNeedsListForDiff( lFieldData, pTypeDataContainer ) )
          {
            lSqlTypeName = ((ClassDataType)lType).getSqlNameCollection();
          }

        }
        else
        {
          lSqlTypeName = lType.getDiffSqlName();

          if( isFieldNeedsListForDiff( lFieldData, pTypeDataContainer ) )
          {
            lSqlTypeName = ((ClassDataType)lType).getDiffSqlNameCollection();
          }
        }

        if( lType.isAtomicValue() )
        {
          pOut.print( "  " + lFieldData.getDiffOldSqlName() + " " + lSqlTypeName );
          pOut.println( "," );
          pOut.print( "  " + lFieldData.getDiffNewSqlName() + " " + lSqlTypeName );
          pOut.println( "," );
        }
        else
        {
          pOut.print( "  " + lFieldData.getDiffChangeSqlName() + " " + lSqlTypeName );
          pOut.println( "," );
        }
        pOut.print( "  " + lFieldData.getDiffEqualFlagSqlName() + " number(1)" );

        if( lFieldData == pClassDataType.getFiledDataList().get( pClassDataType.getFiledDataList().size() - 1 ) )
        {
          pOut.println();
        }
        else
        {
          pOut.println( "," );
        }
      }
    }

    if( pClassDataType.getSuperclass() == null )
    {
      pOut.println( "," );
      pOut.println( " old_parent_index number," );
      pOut.println( " new_parent_index number," );
      pOut.println( " is_new number(1)," );
      pOut.println( " is_old number(1)," );
      pOut.println( " is_matched number(1)," );
      pOut.println( " is_parent_index_equal number(1)," );
      pOut.println( " is_all_fields_equal number(1)," );
      pOut.println( " is_equal number(1)" );
    }

    if( !pClassDataType.isHasSubclasses() )
    {
      if( lIsMemberGenerated )
      {
        pOut.println( "," );
      }
      pOut.println( "constructor function " + pClassDataType.getDiffSqlName() + "( p_new_value in " + pClassDataType.getSqlName() + " ) return self as result" );
      lIsMemberGenerated = true;
    }

    if( lIsMemberGenerated )
    {
      pOut.println( "," );
    }
    _writeMemberSpecPrefix( pClassDataType, pOut );
    pOut.println( " member procedure init_flags" );

    pOut.println( "," );
    pOut.println( " member procedure init_with_new_value( p_new_value in " + pClassDataType.getSqlName() + " )" );

    pOut.println( "," );
    _writeMemberSpecPrefix( pClassDataType, pOut );
    pOut.println( " member procedure merge_with_old_value( p_old_value in " + getBaseSuperclass( pClassDataType, pTypeDataContainer ).getSqlName() + " )" );

    pOut.println( ")" );
    if( pClassDataType.isHasSubclasses() )
    {
      pOut.println( "not final" );
    }
    pOut.println( "/" );
    pOut.println();

    pOut.println( "create or replace type body " + pClassDataType.getDiffSqlName() + " as" );
    if( !pClassDataType.isHasSubclasses() )
    {
      pOut.println( " constructor function " + pClassDataType.getDiffSqlName() + "( p_new_value in " + pClassDataType.getSqlName() + " ) return self as result is " );
      pOut.println( " begin" );
      pOut.println( " init_with_new_value(p_new_value); " );
      pOut.println( " return; " );
      pOut.println( "end;" );
    }

    writeInit_flagsMethod( pClassDataType, pTypeDataContainer, pOut );
    writeInit_with_new_valueMethod( pClassDataType, pTypeDataContainer, pOut );
    writeMerge_with_old_valueMethod( pClassDataType, pTypeDataContainer, pOut );

    pOut.println( "end;" );
    pOut.println( "/" );
    pOut.println();

    pOut.println( "create or replace type " + pClassDataType.getDiffSqlNameCollection() + " force is table of " + pClassDataType.getDiffSqlName() );
    pOut.println( "/" );
    pOut.println();
  }

  private static ClassDataType getBaseSuperclass( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer )
  {
    if( pClassDataType.getSuperclass() == null )
    {
      return pClassDataType;
    }

    return getBaseSuperclass( (ClassDataType)pTypeDataContainer.getClassData( pClassDataType.getSuperclass() ), pTypeDataContainer );
  }

  private static void writeInit_flagsMethod( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer, PrintStream pOut )
  {
    if( pClassDataType.getSuperclass() != null )
    {
      pOut.print( " overriding" );
    }
    pOut.println( " member procedure init_flags is" );
    pOut.println( " begin" );
    if( pClassDataType.getSuperclass() != null )
    {
      pOut.println( " (self as  " + pTypeDataContainer.getClassData( pClassDataType.getSuperclass() ).getDiffSqlName() + " ).init_flags();" );
    }
    else
    {
      pOut.println( " is_all_fields_equal := 1;" );
    }

    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      boolean skipThenBlock = false;

      ClassData lType = lFieldData.getClassData( lFieldData.getJavaType(), pTypeDataContainer );
      if( lType.isAtomicValue() )
      {
        if( lType instanceof ClassDataType )
        {
          pOut.println( " if( " + lType.getSqlName() + ".is_equal( " + lFieldData.getDiffNewSqlName() + ", " + lFieldData.getDiffOldSqlName() + " ) = 1 )" );
        }
        else
        {
          pOut.println( " if( " + lFieldData.getDiffOldSqlName() + " = " + lFieldData.getDiffNewSqlName() + " or ( " + lFieldData.getDiffOldSqlName() + " is null and " + lFieldData.getDiffNewSqlName() + " is null ) )" );
        }
      }
      else
      {
        if( isFieldNeedsListForDiff( lFieldData, pTypeDataContainer ) )
        {
          pOut.println( " " + lFieldData.getDiffEqualFlagSqlName() + " := 1;" );
          pOut.println( " for i in 1.." + lFieldData.getDiffChangeSqlName() + ".count() " );
          pOut.println( " loop" );
          pOut.println( " if( " + lFieldData.getDiffChangeSqlName() + "(i).is_equal = 0 )" );
          pOut.println( " then" );
          pOut.println( " " + lFieldData.getDiffEqualFlagSqlName() + " := 0;" );
          pOut.println( " is_all_fields_equal := 0;" );
          pOut.println( " end if;" );
          pOut.println( " end loop;" );
          skipThenBlock = true;
        }
        else
        {
          pOut.println( " if( " + lFieldData.getDiffChangeSqlName() + ".is_equal = 1 )" );
        }
      }

      if( !skipThenBlock )
      {
        pOut.println( " then" );
        pOut.println( " " + lFieldData.getDiffEqualFlagSqlName() + " := 1;" );
        pOut.println( " else" );
        pOut.println( " " + lFieldData.getDiffEqualFlagSqlName() + " := 0;" );
        pOut.println( " is_all_fields_equal := 0;" );
        pOut.println( " end if;" );
      }

      pOut.println( "" );
    }

    if( !pClassDataType.isHasSubclasses() )
    {
      pOut.println( " if( is_old = 1 and is_new = 1 )" );
      pOut.println( " then" );
      pOut.println( "   is_matched := 1;" );
      pOut.println( " else" );
      pOut.println( "   is_matched := 0;" );
      pOut.println( " end if;" );
      pOut.println( "" );
      pOut.println( " if( old_parent_index = new_parent_index or ( old_parent_index is null and new_parent_index is null ) )" );
      pOut.println( " then" );
      pOut.println( "   is_parent_index_equal := 1;" );
      pOut.println( " else" );
      pOut.println( "   is_parent_index_equal := 0;" );
      pOut.println( " end if;" );
      pOut.println( "" );
      pOut.println( " if( is_all_fields_equal = 1 and is_parent_index_equal = 1 )" );
      pOut.println( " then" );
      pOut.println( "   is_equal := 1;" );
      pOut.println( " else" );
      pOut.println( "   is_equal := 0;" );
      pOut.println( " end if;" );
    }

    pOut.println( "   null;" );
    pOut.println( " end;" );
    pOut.println();
    pOut.println();
  }

  private static boolean isFieldNeedsListForDiff( FieldData pFieldData, TypeDataContainer pTypeDataContainer )
  {
    if( pFieldData.isList() )
    {
      return true;
    }

    ClassData lType = pFieldData.getClassData( pFieldData.getJavaType(), pTypeDataContainer );

    if( lType.isAtomicValue() )
    {
      return false;
    }

    return ((ClassDataType)lType).isHasSubclasses();
  }

  private static void writeInit_with_new_valueMethod( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer, PrintStream pOut )
  {

    pOut.println( " member procedure init_with_new_value( p_new_value in " + pClassDataType.getSqlName() + " ) is" );
    pOut.println( " begin" );
    if( pClassDataType.getSuperclass() != null )
    {
      pOut.println( " (self as  " + pTypeDataContainer.getClassData( pClassDataType.getSuperclass() ).getDiffSqlName() + " ).init_with_new_value(p_new_value);" );
    }

    pOut.println( " if( p_new_value is not null )" );
    pOut.println( " then" );

    pOut.println( " is_new := 1;" );

    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      ClassData lType = lFieldData.getClassData( lFieldData.getJavaType(), pTypeDataContainer );
      if( lType.isAtomicValue() )
      {
        pOut.println( " " + lFieldData.getDiffNewSqlName() + " := p_new_value." + lFieldData.getSqlName() + ";" );
      }
      else
      {
        ClassDataType lClassDataType = (ClassDataType)lType;

        if( isFieldNeedsListForDiff( lFieldData, pTypeDataContainer ) )
        {
          pOut.println( " " + lFieldData.getDiffChangeSqlName() + " := new " + lClassDataType.getDiffSqlNameCollection() + "();" );

          String lIndexVar;
          String lFieldName;

          if( lFieldData.isList() )
          {
            lIndexVar = "i";
            lFieldName = lFieldData.getSqlName() + "(i)";

            pOut.println( " for i in 1..p_new_value." + lFieldData.getSqlName() + ".count() " );
            pOut.println( " loop" );
          }
          else
          {
            lIndexVar = "1";
            lFieldName = lFieldData.getSqlName();
          }

          List<ClassDataType> lAllClassDataSubTypes = getAllClassDataSubTypes( lClassDataType, pTypeDataContainer );
          if( lAllClassDataSubTypes.size() == 1 )
          {
            pOut.println( " " + lFieldData.getDiffChangeSqlName() + ".extend(1);" );
            pOut.println( " " + lFieldData.getDiffChangeSqlName() + "(" + lIndexVar + ") := new " + lClassDataType.getDiffSqlName() + "( p_new_value." + lFieldName + " );" );
          }
          else
          {
            for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
            {
              pOut.println( " if( p_new_value." + lFieldName + " is of (" + lClassDataSubType.getSqlName() + ") ) " );
              pOut.println( " then " );
              pOut.println( " " + lFieldData.getDiffChangeSqlName() + ".extend(1);" );
              pOut.println( " " + lFieldData.getDiffChangeSqlName() + "(" + lIndexVar + ") := new " + lClassDataSubType.getDiffSqlName() + "( treat( p_new_value." + lFieldName + " as " + lClassDataSubType.getSqlName() + " ) );" );
              pOut.println( " end if;" );
            }
          }

          if( lFieldData.isList() )
          {
            pOut.println( " " + lFieldData.getDiffChangeSqlName() + "(" + lIndexVar + ").new_parent_index := i;" );
            pOut.println( " end loop;" );
          }
        }
        else
        {
          pOut.println( " " + lFieldData.getDiffChangeSqlName() + " := new " + lClassDataType.getDiffSqlName() + "( p_new_value." + lFieldData.getSqlName() + " );" );
        }
      }

      pOut.println();
    }

    pOut.println( " else" );
    pOut.println( " is_new := 0;" );

    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      ClassData lType = lFieldData.getClassData( lFieldData.getJavaType(), pTypeDataContainer );
      if( !lType.isAtomicValue() )
      {
        ClassDataType lClassDataType = (ClassDataType)lType;

        if( isFieldNeedsListForDiff( lFieldData, pTypeDataContainer ) )
        {
          pOut.println( " " + lFieldData.getDiffChangeSqlName() + " := new " + lClassDataType.getDiffSqlNameCollection() + "();" );
        }
        else
        {
          pOut.println( " " + lFieldData.getDiffChangeSqlName() + " := new " + lClassDataType.getDiffSqlName() + "( null );" );
        }
      }

      pOut.println();
    }
    pOut.println( " end if;" );

    pOut.println( " end;" );
    pOut.println();
    pOut.println();
  }

  private static void writeMerge_with_old_valueMethod( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer, PrintStream pOut )
  {
    if( pClassDataType.getSuperclass() != null )
    {
      pOut.print( " overriding" );
    }
    pOut.println( " member procedure merge_with_old_value( p_old_value in " + getBaseSuperclass( pClassDataType, pTypeDataContainer ).getSqlName() + " ) is" );
    pOut.println( " v_merge_result ct_merge_result_list;" );
    pOut.println( " v_merge_type_equal number(1);" );
    pOut.println( " begin" );
    if( pClassDataType.getSuperclass() != null )
    {
      pOut.println( " (self as  " + pTypeDataContainer.getClassData( pClassDataType.getSuperclass() ).getDiffSqlName() + " ).merge_with_old_value(p_old_value);" );
    }

    pOut.println( " if( p_old_value is not null )" );
    pOut.println( " then" );

    pOut.println( " is_old := 1;" );

    String lParameterTreated = "p_old_value";

    if( pClassDataType.getSuperclass() != null )
    {
      lParameterTreated = "treat( p_old_value as " + pClassDataType.getSqlName() + " )";
    }

    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      ClassData lType = lFieldData.getClassData( lFieldData.getJavaType(), pTypeDataContainer );
      if( lType.isAtomicValue() )
      {
        pOut.println( " " + lFieldData.getDiffOldSqlName() + " := " + lParameterTreated + "." + lFieldData.getSqlName() + ";" );
      }
      else
      {
        ClassDataType lClassDataType = (ClassDataType)lType;

        if( isFieldNeedsListForDiff( lFieldData, pTypeDataContainer ) )
        {
          if( lFieldData.isList() )
          {
            pOut.println( " for i in 1..v_merge_result.count() " );
            pOut.println( " loop" );

            pOut.println( " if( v_merge_result(i).i_merge_index is null )" );
            pOut.println( " then" );
            pOut.println( " " + lFieldData.getDiffChangeSqlName() + ".extend(1);" );
            List<ClassDataType> lAllClassDataSubTypes = getAllClassDataSubTypes( lClassDataType, pTypeDataContainer );
            if( lAllClassDataSubTypes.size() == 1 )
            {
              pOut.println( " " + lFieldData.getDiffChangeSqlName() + "(" + lFieldData.getDiffChangeSqlName() + ".count()) := new " + lClassDataType.getDiffSqlName() + "( null );" );
              pOut.println( " " + lFieldData.getDiffChangeSqlName() + "(" + lFieldData.getDiffChangeSqlName() + ".count()).merge_with_old_value( " + lParameterTreated + "." + lFieldData.getSqlName() + "(i) );" );
              pOut.println( " " + lFieldData.getDiffChangeSqlName() + "(" + lFieldData.getDiffChangeSqlName() + ".count()).old_parent_index := i;" );
            }
            else
            {
              for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
              {
                pOut.println( " if( " + lParameterTreated + "." + lFieldData.getSqlName() + "(i) is of (" + lClassDataSubType.getSqlName() + ") ) " );
                pOut.println( " then" );
                pOut.println( " declare" );
                pOut.println( " v_val " + lClassDataSubType.getDiffSqlName() + ";" );
                pOut.println( " begin" );
                pOut.println( " v_val := new " + lClassDataSubType.getDiffSqlName() + "( null );" );
                pOut.println( " v_val.merge_with_old_value( treat( " + lParameterTreated + "." + lFieldData.getSqlName() + "(i) as " + lClassDataSubType.getSqlName() + " ) );" );
                pOut.println( " v_val.old_parent_index := 1;" );
                pOut.println( " " + lFieldData.getDiffChangeSqlName() + "(" + lFieldData.getDiffChangeSqlName() + ".count()) := v_val;" );
                pOut.println( " end;" );
                pOut.println( " end if;" );
              }
            }
            pOut.println( " else" );
            pOut.println( " " + lFieldData.getDiffChangeSqlName() + "(v_merge_result(i).i_merge_index).merge_with_old_value( " + lParameterTreated + "." + lFieldData.getSqlName() + "(i) );" );
            pOut.println( " " + lFieldData.getDiffChangeSqlName() + "(v_merge_result(i).i_merge_index).old_parent_index := i;" );
            pOut.println( " end if;" );
            pOut.println( " end loop;" );
          }
          else
          {
            pOut.println( " if( " + lParameterTreated + "." + lFieldData.getSqlName() + " is not null ) " );
            pOut.println( " then" );

            pOut.println( " v_merge_type_equal := 0;" );

            pOut.println( " if( " + lFieldData.getDiffChangeSqlName() + ".count() > 0 ) " );
            pOut.println( " then" );
            List<ClassDataType> lAllClassDataSubTypes = getAllClassDataSubTypes( lClassDataType, pTypeDataContainer );
            for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
            {
              pOut.println( " if( " + lFieldData.getDiffChangeSqlName() + "(1) is of (" + lClassDataSubType.getDiffSqlName() + ") ) " );
              pOut.println( " then" );
              pOut.println( " if( " + lParameterTreated + "." + lFieldData.getSqlName() + " is of (" + lClassDataSubType.getSqlName() + ") ) " );
              pOut.println( " then" );
              pOut.println( " v_merge_type_equal := 1;" );
              pOut.println( " end if;" );
              pOut.println( " end if;" );
            }
            pOut.println( " end if;" );

            pOut.println( " if( v_merge_type_equal = 1 ) " );
            pOut.println( " then" );
            pOut.println( " " + lFieldData.getDiffChangeSqlName() + "(1).merge_with_old_value( " + lParameterTreated + "." + lFieldData.getSqlName() + " );" );
            pOut.println( " else" );

            pOut.println( " " + lFieldData.getDiffChangeSqlName() + ".extend(1);" );
            
            for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
            {
              pOut.println( " if( " + lParameterTreated + "." + lFieldData.getSqlName() + " is of (" + lClassDataSubType.getSqlName() + ") ) " );
              pOut.println( " then" );
              pOut.println( " declare" );
              pOut.println( " v_val " + lClassDataSubType.getDiffSqlName() + ";" );
              pOut.println( " begin" );
              pOut.println( " v_val := new " + lClassDataSubType.getDiffSqlName() + "( null );" );
              pOut.println( " v_val.merge_with_old_value( treat( " + lParameterTreated + "." + lFieldData.getSqlName() + " as " + lClassDataSubType.getSqlName() + " ) );" );
              pOut.println( " " + lFieldData.getDiffChangeSqlName() + "(1) := v_val;" );
              pOut.println( " end;" );
              pOut.println( " end if;" );
            }

            pOut.println( " end if;" );
            pOut.println( " end if;" );
          }
        }
        else
        {
          pOut.println( " " + lFieldData.getDiffChangeSqlName() + ".merge_with_old_value( " + lParameterTreated + "." + lFieldData.getSqlName() + " );" );
        }
      }

      pOut.println();
    }

    pOut.println( " else" );
    pOut.println( " is_old := 0;" );

    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      ClassData lType = lFieldData.getClassData( lFieldData.getJavaType(), pTypeDataContainer );
      if( !lType.isAtomicValue() )
      {
        if( !isFieldNeedsListForDiff( lFieldData, pTypeDataContainer ) )
        {
          pOut.println( " " + lFieldData.getDiffChangeSqlName() + ".merge_with_old_value( null );" );
        }
      }

      pOut.println();
    }
    pOut.println( " end if;" );

    pOut.println( " init_flags();" );

    pOut.println( " end;" );
    pOut.println();
    pOut.println();
  }

  private static List<ClassDataType> getAllClassDataSubTypes( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer )
  {
    List<ClassDataType> lReturn = new ArrayList<ClassDataType>();

    if( pClassDataType.isHasSubclasses() )
    {
      for( ClassDataType lClassDataSubType : pTypeDataContainer.getAllClassDataTypes() )
      {
        if( pTypeDataContainer.getClassData( lClassDataSubType.getSuperclass() ) == pClassDataType )
        {
          lReturn.add( lClassDataSubType );
        }
      }
    }
    else
    {
      lReturn.add( pClassDataType );
    }

    return lReturn;
  }
}
