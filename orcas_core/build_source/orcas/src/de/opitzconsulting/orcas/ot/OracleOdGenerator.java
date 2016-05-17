package de.opitzconsulting.orcas.ot;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


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

    List<ClassDataType> lNoneEnumTypes = new ArrayList<ClassDataType>();
    for( ClassDataType lClassDataType : orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      if( !lClassDataType.isEnum() )
      {
        lNoneEnumTypes.add( lClassDataType );
      }
    }

    for( ClassDataType lClassDataType : lNoneEnumTypes )
    {
      _writeOd( lClassDataType, lTypeDataContainer, new PlSqlPrettyWriter( lPrintStream ) );
    }

    Set<ClassDataType> lOmTypesSet = getOmTypesRecursive( (ClassDataType)lTypeDataContainer.getClassData( lTypeDataContainer.getRootClass() ), new HashSet<ClassDataType>(), lTypeDataContainer );
    List<ClassDataType> lOmTypes = new ArrayList<ClassDataType>( lOmTypesSet );

    for( ClassDataType lClassDataType : lNoneEnumTypes )
    {
      _writeOm( lClassDataType, lTypeDataContainer, new PlSqlPrettyWriter( lPrintStream ), lOmTypes.contains( lClassDataType ) );
    }

    _writeOmPackage( lNoneEnumTypes, lTypeDataContainer, new PlSqlPrettyWriter( lPrintStream ), "orig" );

    lPrintStream.close();
  }

  private static Set<ClassDataType> getOmTypesRecursive( ClassDataType pClassDataType, Set<ClassDataType> pReturn, TypeDataContainer pTypeDataContainer )
  {
    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      ClassData lClassData = pTypeDataContainer.getClassData( lFieldData.getJavaType() );

      if( !lClassData.isAtomicValue() )
      {
        ClassDataType lClassDataType = (ClassDataType)lClassData;
        if( lFieldData.isList() )
        {
          pReturn.add( lClassDataType );
          if( lClassDataType.isHasSubclasses() )
          {
            pReturn.addAll( getAllClassDataSubTypes( lClassDataType, pTypeDataContainer ) );
          }
        }

        getOmTypesRecursive( lClassDataType, pReturn, pTypeDataContainer );
      }
    }

    if( pClassDataType.isHasSubclasses() )
    {
      for( ClassDataType lClassDataType : getAllClassDataSubTypes( pClassDataType, pTypeDataContainer ) )
      {
        getOmTypesRecursive( lClassDataType, pReturn, pTypeDataContainer );
      }
    }

    return pReturn;
  }

  private static void _writeOmPackage( List<ClassDataType> pOmTypes, TypeDataContainer pTypeDataContainer, PlSqlPrettyWriter pOut, String pTypePrefix )
  {
    pOut.println( "create or replace package pa_orcas_om_repository_" + pTypePrefix + " is" );
    for( ClassDataType lClassDataType : pOmTypes )
    {
      pOut.println( "function get_" + lClassDataType.getMergeSqlName() + " return " + lClassDataType.getMergeSqlName() + ";" );
    }
    pOut.println();
    for( ClassDataType lClassDataType : pOmTypes )
    {
      pOut.println( "procedure set_" + lClassDataType.getMergeSqlName() + "( p_val in " + lClassDataType.getMergeSqlName() + " );" );
    }
    pOut.println( "end;" );
    pOut.println( "/" );
    pOut.println();

    pOut.println( "create or replace package body pa_orcas_om_repository_" + pTypePrefix + " is" );

    for( ClassDataType lClassDataType : pOmTypes )
    {
      pOut.println( "pv_" + lClassDataType.getMergeSqlName() + " " + lClassDataType.getMergeSqlName() + ";" );
    }

    pOut.println();

    for( ClassDataType lClassDataType : pOmTypes )
    {
      pOut.println( "function get_" + lClassDataType.getMergeSqlName() + " return " + lClassDataType.getMergeSqlName() + " is" );
      pOut.println( "begin" );
      pOut.println( "return pv_" + lClassDataType.getMergeSqlName() + ";" );
      pOut.println( "end;" );
      pOut.println();
    }
    pOut.println();
    for( ClassDataType lClassDataType : pOmTypes )
    {
      pOut.println( "procedure set_" + lClassDataType.getMergeSqlName() + "( p_val in " + lClassDataType.getMergeSqlName() + " ) is" );
      pOut.println( "begin" );
      pOut.println( "pv_" + lClassDataType.getMergeSqlName() + " := p_val;" );
      pOut.println( "end;" );
      pOut.println();
    }

    pOut.println( "end;" );
    pOut.println( "/" );
    pOut.println();
  }

  private static void _writeOm( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer, PlSqlPrettyWriter pOut, boolean pHasCollectionHandling )
  {
    pOut.println( "create or replace type " + pClassDataType.getMergeSqlName() + " force as object" );
    pOut.println( "(" );
    boolean lIsFieldGenerated = false;
    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      ClassData lClassData = pTypeDataContainer.getClassData( lFieldData.getJavaType() );
      if( lFieldData.getJavaType() == String.class )
      {
        if( lIsFieldGenerated )
        {
          pOut.print( "  , " );
        }
        pOut.println( lFieldData.getUpperCaseFieldFlagName() + " number(1)" );
        lIsFieldGenerated = true;
      }
      if( lClassData instanceof ClassDataType )
      {
        if( ((ClassDataType)lClassData).isEnum() )
        {
          if( lIsFieldGenerated )
          {
            pOut.print( "  , " );
          }
          pOut.println( lFieldData.getDefaultValueFieldName() + " " + lClassData.getSqlName() );
          lIsFieldGenerated = true;
        }
      }
    }
    if( !lIsFieldGenerated )
    {
      pOut.println( "  " + pClassDataType.getSqlDummyFieldName() + " number" );
      pOut.println( " , constructor function " + pClassDataType.getMergeSqlName() + " return self as result" );
    }
    if( pHasCollectionHandling )
    {
      pOut.println( "  , " + (pClassDataType.isHasSubclasses() ? "" : "not final") + " member function is_child_order_relevant return number" );
      if( !pClassDataType.isHasSubclasses() )
      {
        pOut.println( "  , not final member function get_merge_result( p_diff_values in out nocopy " + pClassDataType.getDiffSqlNameCollection() + ", p_old_values in " + pClassDataType.getSqlNameCollection() + " ) return ct_merge_result_list" );
      }
    }
    pOut.println( "  , not final member function cleanup_values( p_value in out nocopy " + pClassDataType.getSqlName() + " ) return number" );
    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      ClassData lClassData = pTypeDataContainer.getClassData( lFieldData.getJavaType() );
      if( lClassData.isAtomicValue() )
      {
        pOut.println( "  , not final member function " + lFieldData.getCleanValueMethodName() + "( p_value in " + lClassData.getPlainSqlName() + " ) return " + lClassData.getPlainSqlName() );
      }
    }
    pOut.println( ") not final" );
    pOut.println( "/" );
    pOut.println();

    pOut.println( "create or replace type body " + pClassDataType.getMergeSqlName() + " as" );
    pOut.println();

    if( !lIsFieldGenerated )
    {
      pOut.println( "constructor function " + pClassDataType.getMergeSqlName() + " return self as result is" );
      pOut.println( "begin" );
      pOut.println( "return;" );
      pOut.println( "end;" );
      pOut.println( "" );
    }

    if( pHasCollectionHandling )
    {
      pOut.println( "member function is_child_order_relevant return number is" );
      if( pClassDataType.isHasSubclasses() )
      {
        pOut.println( "begin" );
        for( ClassDataType lClassDataSubType : getAllClassDataSubTypes( pClassDataType, pTypeDataContainer ) )
        {
          pOut.println( "if( pa_orcas_om_repository_orig.get_" + lClassDataSubType.getMergeSqlName() + "().is_child_order_relevant() = 0 )" );
          pOut.println( "then" );
          pOut.println( "return 0;" );
          pOut.println( "end if;" );
        }
        pOut.println( "return 1;" );
        pOut.println( "end;" );
      }
      else
      {
        pOut.println( "begin" );
        pOut.println( "return 1;" );
        pOut.println( "end;" );
      }
      pOut.println();

      if( !pClassDataType.isHasSubclasses() )
      {
        pOut.println( "member function get_merge_result( p_diff_values in out nocopy " + pClassDataType.getDiffSqlNameCollection() + ", p_old_values in " + pClassDataType.getSqlNameCollection() + " ) return ct_merge_result_list is" );
        pOut.println( "v_merge_result ct_merge_result_list := new ct_merge_result_list();" );
        pOut.println( "begin" );
        pOut.println( "v_merge_result.extend(p_old_values.count());" );
        pOut.println( "for i in 1..p_old_values.count()" );
        pOut.println( "loop" );
        pOut.println( "v_merge_result(i) := new ot_merge_result(null);" );
        pOut.println( "if( i <= p_diff_values.count() )" );
        pOut.println( "then" );
        pOut.println( "v_merge_result(i).i_merge_index := i;" );
        pOut.println( "end if;" );
        pOut.println( "end loop;" );
        pOut.println();
        pOut.println( "return v_merge_result;" );
        pOut.println( "end;" );
        pOut.println();
      }
    }

    pOut.println( " member function cleanup_values( p_value in out nocopy " + pClassDataType.getSqlName() + " ) return number is" );
    pOut.println( " begin" );

    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      ClassData lClassData = pTypeDataContainer.getClassData( lFieldData.getJavaType() );
      if( lClassData.isAtomicValue() )
      {
        pOut.println( " p_value." + lFieldData.getSqlName() + " := " + lFieldData.getCleanValueMethodName() + "( p_value." + lFieldData.getSqlName() + " );" );
      }
      else
      {
        ClassDataType lClassDataType = (ClassDataType)lClassData;
        pOut.println( " if( p_value." + lFieldData.getSqlName() + " is not null )" );
        pOut.println( " then" );

        if( lFieldData.isList() )
        {
          pOut.println( " for i in 1.. p_value." + lFieldData.getSqlName() + ".count" );
          pOut.println( " loop" );
          pOut.println( " if( pa_orcas_om_repository_orig.get_" + lClassDataType.getMergeSqlName() + "().cleanup_values(  p_value." + lFieldData.getSqlName() + "(i) ) ) = null then null; end if;" );
          pOut.println( " end loop;" );
        }
        else
        {
          pOut.println( " if( pa_orcas_om_repository_orig.get_" + lClassDataType.getMergeSqlName() + "().cleanup_values(  p_value." + lFieldData.getSqlName() + " ) ) = null then null; end if;" );
        }
        pOut.println( " end if;" );
      }
    }
    if( pClassDataType.isHasSubclasses() )
    {
      for( ClassDataType lClassDataSubType : getAllClassDataSubTypes( pClassDataType, pTypeDataContainer ) )
      {
        pOut.println( " if( p_value is of (" + lClassDataSubType.getSqlName() + ")  )" );
        pOut.println( " then" );
        pOut.println( " declare" );
        pOut.println( " v_subtyped " + lClassDataSubType.getSqlName() + ";" );
        pOut.println( " begin" );
        pOut.println( " v_subtyped := treat( p_value as " + lClassDataSubType.getSqlName() + " );" );
        pOut.println( " if( pa_orcas_om_repository_orig.get_" + lClassDataSubType.getMergeSqlName() + "().cleanup_values( v_subtyped ) ) = null then null; end if;" );
        pOut.println( " p_value := v_subtyped;" );
        pOut.println( " end;" );
        pOut.println( " end if;" );
      }
    }
    pOut.println( " return null;" );
    pOut.println( " end;" );
    pOut.println( "" );

    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      ClassData lClassData = pTypeDataContainer.getClassData( lFieldData.getJavaType() );
      if( lClassData.isAtomicValue() )
      {
        pOut.println( " member function " + lFieldData.getCleanValueMethodName() + "( p_value in " + lClassData.getPlainSqlName() + " ) return " + lClassData.getPlainSqlName() + " is" );
        pOut.println( " begin" );
        if( lFieldData.getJavaType() == int.class )
        {
          pOut.println( " if( p_value = 0 )" );
          pOut.println( " then" );
          pOut.println( " return null;" );
          pOut.println( " end if;" );
        }
        if( lFieldData.getJavaType() == String.class )
        {
          pOut.println( " if( " + lFieldData.getUpperCaseFieldFlagName() + " = 1 )" );
          pOut.println( " then" );
          pOut.println( " return upper( p_value );" );
          pOut.println( " end if;" );
        }
        if( lClassData instanceof ClassDataType )
        {
          ClassDataType lClassDataType = (ClassDataType)lClassData;
          if( lClassDataType.isEnum() )
          {
            pOut.println( " if( " + lClassData.getSqlName() + ".is_equal( p_value, " + lFieldData.getDefaultValueFieldName() + " ) = 1 )" );
            pOut.println( " then" );
            pOut.println( " return null;" );
            pOut.println( " end if;" );
          }
        }
        pOut.println( " return p_value;" );
        pOut.println( " end;" );
        pOut.println( "" );
      }
    }

    pOut.println( "end;" );
    pOut.println( "/" );
    pOut.println();
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

  private static void _writeMemberSpecPrefix( ClassDataType pClassDataType, PlSqlPrettyWriter pOut )
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

  private static void _writeOd( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer, PlSqlPrettyWriter pOut )
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

        if( !lType.isAtomicValue() && ((ClassDataType)lType).isHasSubclasses() )
        {
          List<ClassDataType> lAllClassDataSubTypes = getAllClassDataSubTypes( (ClassDataType)lType, pTypeDataContainer );

          if( lFieldData.isList() )
          {
            for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
            {
              pOut.print( "  " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + " " + lClassDataSubType.getDiffSqlNameCollection() );
              pOut.println( "," );
            }
          }
          else
          {
            for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
            {
              pOut.print( "  " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + " " + lClassDataSubType.getDiffSqlName() );
              pOut.println( "," );
            }
          }
        }
        else
        {
          String lSqlTypeName;

          if( lType.isAtomicValue() )
          {
            lSqlTypeName = lType.getSqlName();
            pOut.print( "  " + lFieldData.getDiffOldSqlName() + " " + lSqlTypeName );
            pOut.println( "," );
            pOut.print( "  " + lFieldData.getDiffNewSqlName() + " " + lSqlTypeName );
            pOut.println( "," );
          }
          else
          {
            if( lFieldData.isList() )
            {
              lSqlTypeName = ((ClassDataType)lType).getDiffSqlNameCollection();
            }
            else
            {
              lSqlTypeName = lType.getDiffSqlName();
            }

            pOut.print( "  " + lFieldData.getDiffChangeSqlName() + " " + lSqlTypeName );
            pOut.println( "," );
          }
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
      pOut.println( " is_equal number(1)," );
      pOut.println( " is_recreate_needed number(1)" );
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

  private static void writeInit_flagsMethod( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer, PlSqlPrettyWriter pOut )
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
      pOut.println( " is_recreate_needed := 0;" );
    }

    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
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
        pOut.println( " then" );
        pOut.println( " " + lFieldData.getDiffEqualFlagSqlName() + " := 1;" );
        pOut.println( " else" );
        pOut.println( " " + lFieldData.getDiffEqualFlagSqlName() + " := 0;" );
        pOut.println( " is_all_fields_equal := 0;" );
        pOut.println( " end if;" );
      }
      else
      {
        ClassDataType lClassDataType = (ClassDataType)lType;

        if( !lClassDataType.isHasSubclasses() && !lFieldData.isList() )
        {
          pOut.println( " if( " + lFieldData.getDiffChangeSqlName() + ".is_equal = 1 )" );
          pOut.println( " then" );
          pOut.println( " " + lFieldData.getDiffEqualFlagSqlName() + " := 1;" );
          pOut.println( " else" );
          pOut.println( " " + lFieldData.getDiffEqualFlagSqlName() + " := 0;" );
          pOut.println( " is_all_fields_equal := 0;" );
          pOut.println( " end if;" );
        }
        else
        {
          if( !lClassDataType.isHasSubclasses() )
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
          }
          else
          {
            List<ClassDataType> lAllClassDataSubTypes = getAllClassDataSubTypes( lClassDataType, pTypeDataContainer );

            if( lFieldData.isList() )
            {
              pOut.println( " " + lFieldData.getDiffEqualFlagSqlName() + " := 1;" );
              for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
              {
                pOut.println( " for i in 1.." + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + ".count() " );
                pOut.println( " loop" );
                pOut.println( " if( " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + "(i).is_equal = 0 )" );
                pOut.println( " then" );
                pOut.println( " " + lFieldData.getDiffEqualFlagSqlName() + " := 0;" );
                pOut.println( " is_all_fields_equal := 0;" );
                pOut.println( " end if;" );
                pOut.println( " end loop;" );
              }
            }
            else
            {
              pOut.println( " " + lFieldData.getDiffEqualFlagSqlName() + " := 1;" );
              for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
              {
                pOut.println( " if( " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + ".is_equal = 0 )" );
                pOut.println( " then" );
                pOut.println( " " + lFieldData.getDiffEqualFlagSqlName() + " := 0;" );
                pOut.println( " is_all_fields_equal := 0;" );
                pOut.println( " end if;" );
              }
            }
          }
        }
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

  private static void writeInit_with_new_valueMethod( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer, PlSqlPrettyWriter pOut )
  {

    pOut.println( " member procedure init_with_new_value( p_new_value in " + pClassDataType.getSqlName() + " ) is" );
    pOut.println( " begin" );
    if( pClassDataType.getSuperclass() != null )
    {
      pOut.println( " (self as  " + pTypeDataContainer.getClassData( pClassDataType.getSuperclass() ).getDiffSqlName() + " ).init_with_new_value(p_new_value);" );
    }

    if( pClassDataType.isHasSubclasses() && pClassDataType.getFiledDataList().isEmpty() )
    {
      pOut.println( " null;" );
    }
    else
    {
      pOut.println( " if( p_new_value is not null )" );
      pOut.println( " then" );

      if( !pClassDataType.isHasSubclasses() )
      {
        pOut.println( " is_new := 1;" );
      }

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

          if( !lClassDataType.isHasSubclasses() && !lFieldData.isList() )
          {
            pOut.println( " " + lFieldData.getDiffChangeSqlName() + " := new " + lClassDataType.getDiffSqlName() + "( p_new_value." + lFieldData.getSqlName() + " );" );
          }
          else
          {
            if( !lClassDataType.isHasSubclasses() )
            {
              pOut.println( " " + lFieldData.getDiffChangeSqlName() + " := new " + lClassDataType.getDiffSqlNameCollection() + "();" );

              pOut.println( " if( p_new_value." + lFieldData.getSqlName() + " is not null )" );
              pOut.println( " then" );
              pOut.println( " for i in 1..p_new_value." + lFieldData.getSqlName() + ".count() " );
              pOut.println( " loop" );
              pOut.println( " " + lFieldData.getDiffChangeSqlName() + ".extend(1);" );
              pOut.println( " " + lFieldData.getDiffChangeSqlName() + "(i) := new " + lClassDataType.getDiffSqlName() + "( p_new_value." + lFieldData.getSqlName() + "(i) );" );
              pOut.println( " if( pa_orcas_om_repository_orig.get_" + lClassDataType.getMergeSqlName() + "().is_child_order_relevant() = 1 )" );
              pOut.println( " then " );
              pOut.println( " " + lFieldData.getDiffChangeSqlName() + "(i).new_parent_index := i;" );
              pOut.println( " end if;" );
              pOut.println( " end loop;" );
              pOut.println( " end if;" );
            }
            else
            {
              List<ClassDataType> lAllClassDataSubTypes = getAllClassDataSubTypes( lClassDataType, pTypeDataContainer );

              if( lFieldData.isList() )
              {
                for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
                {
                  pOut.println( " " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + " := new " + lClassDataSubType.getDiffSqlNameCollection() + "();" );
                }

                pOut.println( " if( p_new_value." + lFieldData.getSqlName() + " is not null )" );
                pOut.println( " then" );
                pOut.println( " for i in 1..p_new_value." + lFieldData.getSqlName() + ".count() " );
                pOut.println( " loop" );

                for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
                {
                  pOut.println( " if( p_new_value." + lFieldData.getSqlName() + "(i) is of (" + lClassDataSubType.getSqlName() + ") ) " );
                  pOut.println( " then " );
                  pOut.println( " " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + ".extend(1);" );
                  pOut.println( " " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + "(" + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + ".count) := new " + lClassDataSubType.getDiffSqlName() + "( treat( p_new_value." + lFieldData.getSqlName() + "(i) as " + lClassDataSubType.getSqlName() + " ) );" );
                  pOut.println( " if( pa_orcas_om_repository_orig.get_" + lClassDataType.getMergeSqlName() + "().is_child_order_relevant() = 1 )" );
                  pOut.println( " then " );
                  pOut.println( " " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + "(" + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + ".count).new_parent_index := i;" );
                  pOut.println( " end if;" );
                  pOut.println( " end if;" );
                }

                pOut.println( " end loop;" );
                pOut.println( " end if;" );
              }
              else
              {
                for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
                {
                  pOut.println( " if( p_new_value." + lFieldData.getSqlName() + " is not null and p_new_value." + lFieldData.getSqlName() + " is of (" + lClassDataSubType.getSqlName() + ") ) " );
                  pOut.println( " then " );
                  pOut.println( " " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + " := new " + lClassDataSubType.getDiffSqlName() + "( treat( p_new_value." + lFieldData.getSqlName() + " as " + lClassDataSubType.getSqlName() + " ) );" );
                  pOut.println( " else " );
                  pOut.println( " " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + " := new " + lClassDataSubType.getDiffSqlName() + "( null );" );
                  pOut.println( " end if;" );
                }
              }
            }
          }
        }

        pOut.println();
      }

      pOut.println( " else" );
      if( !pClassDataType.isHasSubclasses() )
      {
        pOut.println( " is_new := 0;" );
      }
      else
      {
        pOut.println( " null;" );
      }

      for( FieldData lFieldData : pClassDataType.getFiledDataList() )
      {
        ClassData lType = lFieldData.getClassData( lFieldData.getJavaType(), pTypeDataContainer );
        if( !lType.isAtomicValue() )
        {
          ClassDataType lClassDataType = (ClassDataType)lType;

          if( lFieldData.isList() )
          {
            if( !lClassDataType.isHasSubclasses() )
            {
              pOut.println( " " + lFieldData.getDiffChangeSqlName() + " := new " + lClassDataType.getDiffSqlNameCollection() + "();" );
            }
            else
            {
              List<ClassDataType> lAllClassDataSubTypes = getAllClassDataSubTypes( lClassDataType, pTypeDataContainer );
              for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
              {
                pOut.println( " " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + " := new " + lClassDataSubType.getDiffSqlNameCollection() + "();" );
              }
            }
          }
          else
          {
            if( !lClassDataType.isHasSubclasses() )
            {
              pOut.println( " " + lFieldData.getDiffChangeSqlName() + " := new " + lClassDataType.getDiffSqlName() + "( null );" );
            }
            else
            {
              List<ClassDataType> lAllClassDataSubTypes = getAllClassDataSubTypes( lClassDataType, pTypeDataContainer );
              for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
              {
                pOut.println( " " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + " := new " + lClassDataSubType.getDiffSqlName() + "( null );" );
              }
            }
          }
        }

        pOut.println();
      }
      pOut.println( " end if;" );
    }

    pOut.println( " end;" );
    pOut.println();
  }

  private static void writeMerge_with_old_valueMethod( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer, PlSqlPrettyWriter pOut )
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

    if( pClassDataType.isHasSubclasses() && pClassDataType.getFiledDataList().isEmpty() )
    {
      pOut.println( " null;" );
    }
    else
    {
      pOut.println( " if( p_old_value is not null )" );
      pOut.println( " then" );

      if( !pClassDataType.isHasSubclasses() )
      {
        pOut.println( " is_old := 1;" );
      }

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

          if( !lClassDataType.isHasSubclasses() && !lFieldData.isList() )
          {
            pOut.println( " " + lFieldData.getDiffChangeSqlName() + ".merge_with_old_value( " + lParameterTreated + "." + lFieldData.getSqlName() + " );" );
          }
          else
          {
            if( !lClassDataType.isHasSubclasses() )
            {
              pOut.println( " if( " + lParameterTreated + "." + lFieldData.getSqlName() + " is not null )" );
              pOut.println( " then" );
              pOut.println( " v_merge_result := pa_orcas_om_repository_orig.get_" + lClassDataType.getMergeSqlName() + "().get_merge_result( " + lFieldData.getDiffChangeSqlName() + ", " + lParameterTreated + "." + lFieldData.getSqlName() + " );" );
              pOut.println( " for i in 1..v_merge_result.count() " );
              pOut.println( " loop" );
              pOut.println( " if( v_merge_result(i).i_merge_index is null )" );
              pOut.println( " then" );
              pOut.println( " " + lFieldData.getDiffChangeSqlName() + ".extend(1);" );
              pOut.println( " " + lFieldData.getDiffChangeSqlName() + "(" + lFieldData.getDiffChangeSqlName() + ".count()) := new " + lClassDataType.getDiffSqlName() + "( null );" );
              pOut.println( " if( pa_orcas_om_repository_orig.get_" + lClassDataType.getMergeSqlName() + "().is_child_order_relevant() = 1 )" );
              pOut.println( " then " );
              pOut.println( " " + lFieldData.getDiffChangeSqlName() + "(" + lFieldData.getDiffChangeSqlName() + ".count()).old_parent_index := i;" );
              pOut.println( " end if;" );
              pOut.println( " " + lFieldData.getDiffChangeSqlName() + "(" + lFieldData.getDiffChangeSqlName() + ".count()).merge_with_old_value( " + lParameterTreated + "." + lFieldData.getSqlName() + "(i) );" );
              pOut.println( " else" );
              pOut.println( " if( pa_orcas_om_repository_orig.get_" + lClassDataType.getMergeSqlName() + "().is_child_order_relevant() = 1 )" );
              pOut.println( " then " );
              pOut.println( " " + lFieldData.getDiffChangeSqlName() + "(v_merge_result(i).i_merge_index).old_parent_index := i;" );
              pOut.println( " end if;" );
              pOut.println( " " + lFieldData.getDiffChangeSqlName() + "(v_merge_result(i).i_merge_index).merge_with_old_value( " + lParameterTreated + "." + lFieldData.getSqlName() + "(i) );" );
              pOut.println( " end if;" );
              pOut.println( " end loop;" );
              pOut.println( " end if;" );
            }
            else
            {
              List<ClassDataType> lAllClassDataSubTypes = getAllClassDataSubTypes( lClassDataType, pTypeDataContainer );

              if( lFieldData.isList() )
              {
                pOut.println( " if( " + lParameterTreated + "." + lFieldData.getSqlName() + " is not null )" );
                pOut.println( " then" );

                for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
                {
                  pOut.println( " declare" );
                  pOut.println( " v_typed_list " + lClassDataSubType.getSqlNameCollection() + " := new " + lClassDataSubType.getSqlNameCollection() + "();" );
                  pOut.println( " v_merge_result_index_map ct_merge_result_list;" );
                  pOut.println( " begin" );
                  pOut.println( " if( pa_orcas_om_repository_orig.get_" + lClassDataType.getMergeSqlName() + "().is_child_order_relevant() = 1 )" );
                  pOut.println( " then" );
                  pOut.println( " v_merge_result_index_map := new ct_merge_result_list();" );
                  pOut.println( " end if;" );
                  pOut.println( " for i in 1.." + lParameterTreated + "." + lFieldData.getSqlName() + ".count() " );
                  pOut.println( " loop" );
                  pOut.println( " if( " + lParameterTreated + "." + lFieldData.getSqlName() + "(i) is of (" + lClassDataSubType.getSqlName() + ") ) " );
                  pOut.println( " then" );
                  pOut.println( " v_typed_list.extend(1);" );
                  pOut.println( " v_typed_list(v_typed_list.count) := treat( " + lParameterTreated + "." + lFieldData.getSqlName() + "(i) as " + lClassDataSubType.getSqlName() + " );" );
                  pOut.println( " if( v_merge_result_index_map is not null )" );
                  pOut.println( " then" );
                  pOut.println( " v_merge_result_index_map.extend(1);" );
                  pOut.println( " v_merge_result_index_map(v_merge_result_index_map.count) := ot_merge_result( i );" );
                  pOut.println( " end if;" );
                  pOut.println( " end if;" );
                  pOut.println( " end loop;" );
                  pOut.println( " v_merge_result := pa_orcas_om_repository_orig.get_" + lClassDataSubType.getMergeSqlName() + "().get_merge_result( " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + ", v_typed_list );" );
                  pOut.println( " for i in 1..v_merge_result.count() " );
                  pOut.println( " loop" );
                  pOut.println( " if( v_merge_result(i).i_merge_index is null )" );
                  pOut.println( " then" );
                  pOut.println( " " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + ".extend(1);" );
                  pOut.println( " " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + "(" + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + ".count) := new " + lClassDataSubType.getDiffSqlName() + "( null );" );
                  pOut.println( " if( v_merge_result_index_map is not null )" );
                  pOut.println( " then " );
                  pOut.println( " " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + "(" + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + ".count).old_parent_index := v_merge_result_index_map(i).i_merge_index;" );
                  pOut.println( " end if;" );
                  pOut.println( " " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + "(" + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + ".count).merge_with_old_value( v_typed_list(i) );" );
                  pOut.println( " else" );
                  pOut.println( " if( v_merge_result_index_map is not null )" );
                  pOut.println( " then " );
                  pOut.println( " " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + "(v_merge_result(i).i_merge_index).old_parent_index := v_merge_result_index_map(i).i_merge_index;" );
                  pOut.println( " end if;" );
                  pOut.println( " " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + "(v_merge_result(i).i_merge_index).merge_with_old_value( v_typed_list(i) );" );
                  pOut.println( " end if;" );
                  pOut.println( " end loop;" );
                  pOut.println( " end;" );
                }
                pOut.println( " end if;" );
              }
              else
              {
                for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
                {
                  pOut.println( " if( " + lParameterTreated + "." + lFieldData.getSqlName() + " is not null and " + lParameterTreated + "." + lFieldData.getSqlName() + " is of (" + lClassDataSubType.getSqlName() + ") ) " );
                  pOut.println( " then" );
                  pOut.println( " " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + ".merge_with_old_value( treat( " + lParameterTreated + "." + lFieldData.getSqlName() + " as " + lClassDataSubType.getSqlName() + " ) );" );
                  pOut.println( " else" );
                  pOut.println( " " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + ".merge_with_old_value( null );" );
                  pOut.println( " end if;" );
                }
              }
            }
          }
        }

        pOut.println();
      }

      pOut.println( " else" );
      if( !pClassDataType.isHasSubclasses() )
      {
        pOut.println( " is_old := 0;" );
      }
      else
      {
        pOut.println( " null;" );
      }

      for( FieldData lFieldData : pClassDataType.getFiledDataList() )
      {
        ClassData lType = lFieldData.getClassData( lFieldData.getJavaType(), pTypeDataContainer );
        if( !lType.isAtomicValue() )
        {
          ClassDataType lClassDataType = (ClassDataType)lType;

          if( !lFieldData.isList() )
          {
            if( !lClassDataType.isHasSubclasses() )
            {
              pOut.println( " " + lFieldData.getDiffChangeSqlName() + ".merge_with_old_value( null );" );
            }
            else
            {
              for( ClassDataType lClassDataSubType : getAllClassDataSubTypes( lClassDataType, pTypeDataContainer ) )
              {
                pOut.println( " " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + ".merge_with_old_value( null );" );
              }
            }
          }
        }

        pOut.println();
      }
      pOut.println( " end if;" );

      for( FieldData lFieldData : pClassDataType.getFiledDataList() )
      {
        if( lFieldData.isList() )
        {
          ClassDataType lClassDataType = (ClassDataType)lFieldData.getClassData( lFieldData.getJavaType(), pTypeDataContainer );

          if( !lClassDataType.isHasSubclasses() )
          {
            pOut.println( " for i in 1.." + lFieldData.getDiffChangeSqlName() + ".count() " );
            pOut.println( " loop" );
            pOut.println( " if( " + lFieldData.getDiffChangeSqlName() + "(i).is_old is null )" );
            pOut.println( " then" );
            pOut.println( " " + lFieldData.getDiffChangeSqlName() + "(i).merge_with_old_value( null );" );
            pOut.println( " end if;" );
            pOut.println( " end loop;" );
          }
          else
          {
            for( ClassDataType lClassDataSubType : getAllClassDataSubTypes( lClassDataType, pTypeDataContainer ) )
            {
              pOut.println( " for i in 1.." + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + ".count() " );
              pOut.println( " loop" );
              pOut.println( " if( " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + "(i).is_old is null )" );
              pOut.println( " then" );
              pOut.println( " " + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + "(i).merge_with_old_value( null );" );
              pOut.println( " end if;" );
              pOut.println( " end loop;" );
            }
          }
        }

        pOut.println();
      }

      if( !pClassDataType.isHasSubclasses() )
      {
        pOut.println( " init_flags();" );
      }
    }

    pOut.println( " end;" );
    pOut.println();
    pOut.println();

  }

  static List<ClassDataType> getAllClassDataSubTypes( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer )
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
