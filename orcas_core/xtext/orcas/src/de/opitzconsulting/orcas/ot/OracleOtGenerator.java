package de.opitzconsulting.orcas.ot;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OracleOtGenerator
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
    }

    TypeDataContainer lTypeDataContainer = new ClassDataParser().parse();

    for( ClassDataType lClassDataType : orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      _writeOt( lClassDataType, lTypeDataContainer, lPrintStream );
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

  private static void _writeOt( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer, PrintStream pOut )
  {
    pOut.print( "create or replace type " + pClassDataType.getSqlName() );

    if( pClassDataType.getSuperclass() == null )
    {
      pOut.println( " as object" );
    }
    else
    {
      pOut.println( " under " + pTypeDataContainer.getClassData( pClassDataType.getSuperclass() ).getSqlName() );
    }
    pOut.println( "(" );

    boolean lIsMemberGenerated = false;

    if( pClassDataType.getFiledDataList().isEmpty() && pClassDataType.getSuperclass() == null )
    {
      pOut.println( "  " + pClassDataType.getSqlDummyFieldName() + " number" );
    }
    else
    {
      for( FieldData lFieldData : pClassDataType.getFiledDataList() )
      {
        lIsMemberGenerated = true;
        ClassData lType = pTypeDataContainer.getClassData( lFieldData.getJavaType() );

        String lSqlTypeName = lType.getSqlName();

        if( lFieldData.isList() )
        {
          lSqlTypeName = ((ClassDataType)lType).getSqlNameCollection();
        }

        pOut.print( "  " + lFieldData.getSqlName() + " " + lSqlTypeName );

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

    if( pClassDataType.isEnum() )
    {
      for( EnumData lEnumData : pClassDataType.getEnumData() )
      {
        if( !lEnumData.getName().equals( "null" ) )
        {
          pOut.println( "," );
          pOut.println( "static function c_" + lEnumData.getName() + " return " + pClassDataType.getSqlName() );
        }
      }
      
      pOut.println( "," );
      pOut.println( "static function is_equal ( p_val1 " + pClassDataType.getSqlName() + ", p_val2 " + pClassDataType.getSqlName() + ", p_default " + pClassDataType.getSqlName() + " ) return number" );
      
      pOut.println( "," );
      pOut.println( "static function is_equal ( p_val1 " + pClassDataType.getSqlName() + ", p_val2 " + pClassDataType.getSqlName() + " ) return number" );

      pOut.println( "," );
      pOut.println( "map member function map_order return number" );
    }
    else
    {
      if( !pClassDataType.isHasSubclasses() )
      {
        if( lIsMemberGenerated )
        {
          pOut.println( "," );
        }
        pOut.println( "constructor function " + pClassDataType.getSqlName() + " return self as result" );
      }
    }

    pOut.println( ")" );
    if( pClassDataType.isHasSubclasses() )
    {
      pOut.println( "not final" );
    }
    pOut.println( "/" );
    pOut.println();

    if( !pClassDataType.isHasSubclasses() )
    {
      pOut.println( "create or replace type body " + pClassDataType.getSqlName() + " as" );
      if( pClassDataType.isEnum() )
      {
        for( EnumData lEnumData : pClassDataType.getEnumData() )
        {
          if( !lEnumData.getName().equals( "null" ) )
          {
            pOut.println( "static function c_" + lEnumData.getName() + " return " + pClassDataType.getSqlName() + " is" );
            pOut.println( "begin" );
            pOut.println( "  return new " + pClassDataType.getSqlName() + "( '" + lEnumData.getLiteral() + "', '" + lEnumData.getName() + "', " + lEnumData.getValue() + " );" );
            pOut.println( "end;" );
          }
        }
        
        pOut.println( "static function is_equal ( p_val1 " + pClassDataType.getSqlName() + ", p_val2 " + pClassDataType.getSqlName() + " ) return number is" );
        pOut.println( "begin" );
        pOut.println( "  if( p_val1 is null and p_val2 is null ) " );
        pOut.println( "  then " );
        pOut.println( "    return 1;" );
        pOut.println( "  end if;" );
        pOut.println( "  if( p_val1 is null or p_val2 is null ) " );
        pOut.println( "  then " );
        pOut.println( "    return 0;" );
        pOut.println( "  end if;" );
        pOut.println( "  if( p_val1.i_value = p_val2.i_value ) " );
        pOut.println( "  then " );
        pOut.println( "    return 1;" );
        pOut.println( "  else" );
        pOut.println( "    return 0;" );
        pOut.println( "  end if;" );
        pOut.println( "end;" );
        
        pOut.println( "static function is_equal ( p_val1 " + pClassDataType.getSqlName() + ", p_val2 " + pClassDataType.getSqlName() + ", p_default " + pClassDataType.getSqlName() + " ) return number is" );
        pOut.println( "begin" );
        pOut.println( "  if( p_val1 is null and p_val2 is null ) " );
        pOut.println( "  then " );
        pOut.println( "    return 1;" );
        pOut.println( "  end if;" );
        pOut.println( "  if( p_val1 is null and p_val2 is not null ) " );
        pOut.println( "  then " );
        pOut.println( "    return is_equal( p_default, p_val2 );" );
        pOut.println( "  end if;" );
        pOut.println( "  if( p_val1 is not null and p_val2 is null ) " );
        pOut.println( "  then " );
        pOut.println( "    return is_equal( p_val1, p_default );" );
        pOut.println( "  end if;" );        
        pOut.println( "  return is_equal( p_val1, p_val2 );" );        
        pOut.println( "end;" );        

        pOut.println( "map member function map_order return number is" );
        pOut.println( "begin" );
        pOut.println( "  return i_value;" );
        pOut.println( "end;" );
      }
      else
      {
        pOut.println( "constructor function " + pClassDataType.getSqlName() + " return self as result is begin return; end;" );
      }

      pOut.println( "end;" );
      pOut.println( "/" );
      pOut.println();
    }

    pOut.println( "create or replace type " + pClassDataType.getSqlNameCollection() + " is table of " + pClassDataType.getSqlName() );
    pOut.println( "/" );
    pOut.println();
  }
}
