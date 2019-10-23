package de.opitzconsulting.orcas.ot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;

public class JavaOdGenerator extends JavaGenerator
{
  public static void main( String[] pArgs ) throws Exception
  {
    new JavaOdGenerator( pArgs ).export();
  }

  protected JavaOdGenerator( String[] pArgs )
  {
    super( pArgs );
  }

  @Override
  public void export()
  {
    final TypeDataContainer lTypeDataContainer = new ClassDataParser().parse();

    final List<ClassDataType> lNoneEnumTypes = new ArrayList<ClassDataType>();
    for( ClassDataType lClassDataType : orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      if( !lClassDataType.isEnum() )
      {
        lNoneEnumTypes.add( lClassDataType );
      }
    }

    for( final ClassDataType lClassDataType : lNoneEnumTypes )
    {
      writeJavaFile( lClassDataType.getDiffJavaName() + ".java", new DoWithWriter()
      {
        public void write( JavaPrettyWriter pJavaPrettyWriter )
        {
          _writeDiffClass( lClassDataType, lTypeDataContainer, pJavaPrettyWriter );
        }
      } );
    }

    Set<ClassDataType> lOmTypesSet = getOmTypesRecursive( (ClassDataType) lTypeDataContainer.getClassData( lTypeDataContainer.getRootClass() ), new HashSet<ClassDataType>(), lTypeDataContainer );
    final List<ClassDataType> lOmTypes = new ArrayList<ClassDataType>( lOmTypesSet );

    for( final ClassDataType lClassDataType : lNoneEnumTypes )
    {
      writeJavaFile( lClassDataType.getMergeJavaName() + ".java", new DoWithWriter()
      {
        public void write( JavaPrettyWriter pJavaPrettyWriter )
        {
          _writeMergeClass( lClassDataType, lTypeDataContainer, pJavaPrettyWriter, lOmTypes.contains( lClassDataType ) );
        }
      } );
    }

    writeJavaFile( "DiffRepository.java", new DoWithWriter()
    {
      public void write( JavaPrettyWriter pJavaPrettyWriter )
      {
        _writeDiffRepository( lNoneEnumTypes, lTypeDataContainer, pJavaPrettyWriter );
      }
    } );

    writeJavaFile( "AbstractDiff.java", new DoWithWriter()
    {
      public void write( JavaPrettyWriter pJavaPrettyWriter )
      {
        _writeAbstractDiff( pJavaPrettyWriter );
      }
    } );
  }

  private static Set<ClassDataType> getOmTypesRecursive( ClassDataType pClassDataType, Set<ClassDataType> pReturn, TypeDataContainer pTypeDataContainer )
  {
    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      ClassData lClassData = pTypeDataContainer.getClassData( lFieldData.getJavaType() );

      if( !lClassData.isAtomicValue() )
      {
        ClassDataType lClassDataType = (ClassDataType) lClassData;
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

  @Override
  protected String getPackageName()
  {
    return "de.opitzconsulting.orcas." + ClassDataType.getTypePrefix() + ".diff";
  }

  private void _writeDiffRepository( List<ClassDataType> pOmTypes, TypeDataContainer pTypeDataContainer, JavaPrettyWriter pOut )
  {
    writePackage( pOut );
    pOut.println();
    writeOrcasImport( pOut );
    pOut.println( "import java.util.*;" );
    pOut.println();
    pOut.print( "public class DiffRepository" );
    pOut.println( "{" );
    for( ClassDataType lClassDataType : pOmTypes )
    {
      pOut.println( "private static " + lClassDataType.getMergeJavaName() + " field" + lClassDataType.getMergeJavaName() + " = new " + lClassDataType.getMergeJavaName() + "();" );
    }

    pOut.println();

    for( ClassDataType lClassDataType : pOmTypes )
    {
      pOut.println( "public static " + lClassDataType.getMergeJavaName() + " get" + lClassDataType.getMergeJavaName() + "()" );
      pOut.println( "{" );
      pOut.println( "return field" + lClassDataType.getMergeJavaName() + ";" );
      pOut.println( "}" );
      pOut.println();
    }
    pOut.println();
    for( ClassDataType lClassDataType : pOmTypes )
    {
      pOut.println( "public static void set" + lClassDataType.getMergeJavaName() + "( " + lClassDataType.getMergeJavaName() + " pValue )" );
      pOut.println( "{" );
      pOut.println( "field" + lClassDataType.getMergeJavaName() + " = pValue;" );
      pOut.println( "}" );
      pOut.println();
    }

    pOut.println( "public static int getNullIntValue(){return -1;}" );

    pOut.println( "public static Integer getNullableIntValue( int pValue ){return pValue==-1?null:pValue;}" );

    pOut.println( "}" );
    pOut.println();
  }

  private void _writeAbstractDiff( JavaPrettyWriter pOut )
  {
    writePackage( pOut );
    pOut.println();
    writeOrcasImport( pOut );
    pOut.println( "import java.util.*;" );
    pOut.println( "import java.lang.reflect.*;" );
    pOut.println( "import org.eclipse.emf.ecore.*;" );

    pOut.println();
    pOut.print( "public class AbstractDiff" );
    pOut.println( "{" );
    pOut.println( "public " + "Integer oldParentIndex;" );
    pOut.println( "public " + "Integer newParentIndex;" );
    pOut.println( "public " + "boolean isNew;" );
    pOut.println( "public " + "Boolean isOld;" );
    pOut.println( "public " + "boolean isMatched;" );
    pOut.println( "public " + "boolean parentIndexIsEqual;" );
    pOut.println( "public " + "boolean isAllFieldsEqual;" );
    pOut.println( "public " + "boolean isEqual;" );

    pOut.println( " public boolean isFieldEqual( EStructuralFeature pEStructuralFeature ) " );
    pOut.println( " { " );
    pOut.println( "   try " );
    pOut.println( "   { " );
    pOut.println( "     Field lField = getClass().getField( pEStructuralFeature.getName() + \"IsEqual\" ); " );
    pOut.println( "     return (Boolean) lField.get( this ); " );
    pOut.println( "   } " );
    pOut.println( "   catch( Exception e ) " );
    pOut.println( "   { " );
    pOut.println( "     throw new RuntimeException( e ); " );
    pOut.println( "   } " );
    pOut.println( " } " );
    
    pOut.println( " public Object getValue( EStructuralFeature pEStructuralFeature, boolean pNewValue ) " );
    pOut.println( " { " );
    pOut.println( "   try " );
    pOut.println( "   { " );
    pOut.println( "     Field lField = getClass().getField( pEStructuralFeature.getName() + (pNewValue ? \"New\" : \"Old\" ) ); " );
    pOut.println( "     return lField.get( this ); " );
    pOut.println( "   } " );
    pOut.println( "   catch( Exception e ) " );
    pOut.println( "   { " );
    pOut.println( "     throw new RuntimeException( e ); " );
    pOut.println( "   } " );
    pOut.println( " } " );

    pOut.println( " public Object getDiff( EStructuralFeature pEStructuralFeature ) " );
    pOut.println( " { " );
    pOut.println( "   try " );
    pOut.println( "   { " );
    pOut.println( "     Field lField = getClass().getField( pEStructuralFeature.getName() + \"Diff\" ); " );
    pOut.println( "     return lField.get( this ); " );
    pOut.println( "   } " );
    pOut.println( "   catch( Exception e ) " );
    pOut.println( "   { " );
    pOut.println( "     throw new RuntimeException( e ); " );
    pOut.println( "   } " );
    pOut.println( " } " );

    pOut.println( "}" );
    pOut.println();
  }

  private static void writeOrcasImport( JavaPrettyWriter pOut )
  {
    if( ClassDataType.getTypePrefix().equals( "syex" ) )
    {
      pOut.println( "import de.opitzconsulting.orcasDsl.*;" );
    }
    else
    {
      pOut.println( "import de.opitzconsulting.origOrcasDsl.*;" );
    }
  }

  private void _writeMergeClass( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer, JavaPrettyWriter pOut, boolean pHasCollectionHandling )
  {
    writePackage( pOut );
    pOut.println();
    writeOrcasImport( pOut );
    pOut.println( "import java.util.*;" );
    pOut.println( "import java.math.*;" );
    pOut.println();
    pOut.print( "public class " + pClassDataType.getMergeJavaName() );
    pOut.println( "{" );

    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      ClassData lClassData = pTypeDataContainer.getClassData( lFieldData.getJavaType() );
      if( lFieldData.getJavaType() == String.class )
      {
        pOut.println( "public boolean " + lFieldData.getUpperCaseJavaFieldFlagName() + ";" );
      }
      if( lClassData instanceof ClassDataType )
      {
        if( ((ClassDataType) lClassData).isEnum() )
        {
          pOut.println( "public " + lClassData.getJavaName() + " " + lFieldData.getDefaultValueJavaFieldName() + ";" );
        }
      }
    }
    pOut.println();

    if( pHasCollectionHandling )
    {
      pOut.println( "public boolean isChildOrderRelevant()" );
      pOut.println( "{" );
      if( pClassDataType.isHasSubclasses() )
      {
        for( ClassDataType lClassDataSubType : getAllClassDataSubTypes( pClassDataType, pTypeDataContainer ) )
        {
          pOut.println( "if( DiffRepository.get" + lClassDataSubType.getMergeJavaName() + "().isChildOrderRelevant() )" );
          pOut.println( "{" );
          pOut.println( "return false;" );
          pOut.println( "}" );
        }
      }
      pOut.println( "return true;" );
      pOut.println( "}" );
      pOut.println();

      if( !pClassDataType.isHasSubclasses() )
      {
        pOut.println( "public List<Integer> getMergeResult( " + pClassDataType.getDiffJavaNameCollection() + " pNewDiffValues, " + pClassDataType.getJavaNameCollection() + " pOldValues )" );
        pOut.println( "{" );
        pOut.println( "List<Integer> lReturn = new ArrayList();" );
        pOut.println( "for( int i=0; i<pOldValues.size(); i++ )" );
        pOut.println( "{" );
        pOut.println( "if( i < pNewDiffValues.size() )" );
        pOut.println( "{" );
        pOut.println( "lReturn.add(i);" );
        pOut.println( "}" );
        pOut.println( "else" );
        pOut.println( "{" );
        pOut.println( "lReturn.add(null);" );
        pOut.println( "}" );

        pOut.println( "}" );
        pOut.println();
        pOut.println( "return lReturn;" );
        pOut.println( "}" );
        pOut.println();
      }
    }

    pOut.println( " public void cleanupValues( " + pClassDataType.getJavaName() + " pValue )" );
    pOut.println( " {" );

    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      ClassData lClassData = pTypeDataContainer.getClassData( lFieldData.getJavaType() );
      if( lClassData.isAtomicValue() )
      {
        pOut.println( " pValue." + lFieldData.getJavaSetterName() + "( " + lFieldData.getCleanValueJavaMethodName() + "( pValue." + lFieldData.getJavaGetterCall() + ", pValue ) );" );
      }
      else
      {
        ClassDataType lClassDataType = (ClassDataType) lClassData;
        pOut.println( " if( pValue." + lFieldData.getJavaGetterCall() + " != null )" );
        pOut.println( " {" );

        if( lFieldData.isList() )
        {
          pOut.println( " for( " + lClassDataType.getJavaName() + " lValue : pValue." + lFieldData.getJavaGetterCall() + " )" );
          pOut.println( " {" );
          pOut.println( " DiffRepository.get" + lClassDataType.getMergeJavaName() + "().cleanupValues( lValue );" );
          pOut.println( " }" );
        }
        else
        {
          pOut.println( " DiffRepository.get" + lClassDataType.getMergeJavaName() + "().cleanupValues( pValue." + lFieldData.getJavaGetterCall() + " );" );
        }
        pOut.println( " }" );
      }
    }
    if( pClassDataType.isHasSubclasses() )
    {
      for( ClassDataType lClassDataSubType : getAllClassDataSubTypes( pClassDataType, pTypeDataContainer ) )
      {
        pOut.println( " if( pValue instanceof " + lClassDataSubType.getJavaName() + " )" );
        pOut.println( " {" );
        pOut.println( " DiffRepository.get" + lClassDataSubType.getMergeJavaName() + "().cleanupValues( (" + lClassDataSubType.getJavaName() + ")pValue );" );
        pOut.println( " }" );
      }
    }
    pOut.println( " }" );
    pOut.println( "" );

    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      ClassData lClassData = pTypeDataContainer.getClassData( lFieldData.getJavaType() );
      if( lClassData.isAtomicValue() )
      {
        pOut.println( " public " + lClassData.getJavaName() + " " + lFieldData.getCleanValueJavaMethodName() + "( " + lClassData.getJavaName() + " pValue, " + pClassDataType.getJavaName() + " pObject )" );
        pOut.println( " {" );
        pOut.println( "   return " + lFieldData.getCleanValueJavaMethodName() + "( pValue );" );
        pOut.println( " }" );

        pOut.println( " public " + lClassData.getJavaName() + " " + lFieldData.getCleanValueJavaMethodName() + "( " + lClassData.getJavaName() + " pValue )" );
        pOut.println( " {" );
        if( lFieldData.isInt() )
        {
          pOut.println( " if( pValue == 0 )" );
          pOut.println( " {" );
          pOut.println( " return DiffRepository.getNullIntValue();" );
          pOut.println( " }" );
        }
        if( lFieldData.getJavaType() == String.class )
        {
          pOut.println( " if( pValue != null && " + lFieldData.getUpperCaseJavaFieldFlagName() + " )" );
          pOut.println( " {" );
          pOut.println( " return pValue.toUpperCase();" );
          pOut.println( " }" );
        }
        if( lClassData instanceof ClassDataType )
        {
          ClassDataType lClassDataType = (ClassDataType) lClassData;
          if( lClassDataType.isEnum() )
          {
            pOut.println( " if( pValue == " + lFieldData.getDefaultValueJavaFieldName() + " )" );
            pOut.println( " {" );
            pOut.println( " return null;" );
            pOut.println( " }" );
          }
        }
        pOut.println( " return pValue;" );
        pOut.println( " }" );
        pOut.println( "" );
      }
    }

    pOut.println( " }" );

    pOut.println();
  }

  private static List<ClassDataType> _getDependencies( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer )
  {
    List<ClassDataType> lReturn = new ArrayList<ClassDataType>();

    if( pClassDataType.getSuperclass() != null )
    {
      lReturn.add( (ClassDataType) pTypeDataContainer.getClassData( pClassDataType.getSuperclass() ) );
    }

    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      ClassData lClassData = pTypeDataContainer.getClassData( lFieldData.getJavaType() );

      if( lClassData instanceof ClassDataType )
      {
        lReturn.add( (ClassDataType) lClassData );
      }
    }

    return lReturn;
  }

  public static List<ClassDataType> orderClassDataTypeList( List<ClassDataType> pAllClassDataTypes, TypeDataContainer pTypeDataContainer )
  {
    List<ClassDataType> lReturn = new ArrayList<ClassDataType>();

    Map<ClassDataType, List<ClassDataType>> lDependencyMap = new HashMap<ClassDataType, List<ClassDataType>>();

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
    } while( lOneFound );

    if( !lDependencyMap.isEmpty() )
    {
      throw new RuntimeException();
    }

    return lReturn;
  }

  private void _writeDiffClass( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer, JavaPrettyWriter pOut )
  {
    writePackage( pOut );
    pOut.println();
    writeOrcasImport( pOut );
    pOut.println( "import java.util.*;" );
    pOut.println( "import java.math.*;" );
    pOut.println();
    pOut.print( "public class " + pClassDataType.getDiffJavaName() );

    if( pClassDataType.getSuperclass() != null )
    {
      pOut.println( " extends " + ((ClassDataType) pTypeDataContainer.getClassData( pClassDataType.getSuperclass() )).getDiffJavaName() );
    }
    else
    {
      pOut.println( " extends AbstractDiff" );
    }

    pOut.println( "{" );

    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      ClassData lType = lFieldData.getClassData( lFieldData.getJavaType(), pTypeDataContainer );

      if( !lType.isAtomicValue() && ((ClassDataType) lType).isHasSubclasses() )
      {
        List<ClassDataType> lAllClassDataSubTypes = getAllClassDataSubTypes( (ClassDataType) lType, pTypeDataContainer );

        if( lFieldData.isList() )
        {
          for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
          {
            pOut.println( "public " + lClassDataSubType.getDiffJavaNameCollection() + " " + lFieldData.getDiffChangeJavaNameForSubType( lClassDataSubType ) + ";" );
          }
        }
        else
        {
          for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
          {
            pOut.println( "public " + lClassDataSubType.getDiffJavaName() + " " + lFieldData.getDiffChangeJavaNameForSubType( lClassDataSubType ) + ";" );
          }
        }
      }
      else
      {
        if( lType.isAtomicValue() )
        {
          String lTypeName = lType.getJavaName();

          if( lFieldData.isInt() )
          {
            lTypeName = "Integer";
          }

          pOut.println( "public " + lTypeName + " " + lFieldData.getDiffOldJavaName() + ";" );
          pOut.println( "public " + lTypeName + " " + lFieldData.getDiffNewJavaName() + ";" );
        }
        else
        {
          if( lFieldData.isList() )
          {
            pOut.println( "public " + ((ClassDataType) lType).getDiffJavaNameCollection() + " " + lFieldData.getDiffChangeJavaName() + ";" );
          }
          else
          {
            pOut.println( "public " + ((ClassDataType) lType).getDiffJavaName() + " " + lFieldData.getDiffChangeJavaName() + ";" );
          }
        }
      }
      pOut.println( "public " + "boolean " + lFieldData.getDiffEqualFlagJavaName() + ";" );
    }

    if( !pClassDataType.isHasSubclasses() )
    {
      pOut.println( "public " + pClassDataType.getDiffJavaName() + "( " + pClassDataType.getJavaName() + " pNewValue )" );
      pOut.println( "{" );
      pOut.println( "initWithNewValue( pNewValue ); " );
      pOut.println( "}" );
    }

    writeInitFlagsMethod( pClassDataType, pTypeDataContainer, pOut );
    writeInitWithNewValueMethod( pClassDataType, pTypeDataContainer, pOut );
    writeMergeWithOldValueMethod( pClassDataType, pTypeDataContainer, pOut );

    pOut.println( "}" );
    pOut.println();
  }

  private static ClassDataType getBaseSuperclass( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer )
  {
    if( pClassDataType.getSuperclass() == null )
    {
      return pClassDataType;
    }

    return getBaseSuperclass( (ClassDataType) pTypeDataContainer.getClassData( pClassDataType.getSuperclass() ), pTypeDataContainer );
  }

  private static void writeInitFlagsMethod( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer, JavaPrettyWriter pOut )
  {
    if( pClassDataType.getSuperclass() != null )
    {
      pOut.println( "@Override" );
    }
    pOut.println( " public void initFlags()" );
    pOut.println( " {" );
    if( pClassDataType.getSuperclass() != null )
    {
      pOut.println( " super.initFlags();" );
    }
    else
    {
      pOut.println( " isAllFieldsEqual = true;" );
    }

    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      ClassData lType = lFieldData.getClassData( lFieldData.getJavaType(), pTypeDataContainer );
      if( lType.isAtomicValue() )
      {
        pOut.println( " if( Objects.equals( " + lFieldData.getDiffOldJavaName() + ", " + lFieldData.getDiffNewJavaName() + " ) )" );
        pOut.println( " {" );
        pOut.println( " " + lFieldData.getDiffEqualFlagJavaName() + " = true;" );
        pOut.println( " }" );
        pOut.println( " else" );
        pOut.println( " {" );
        pOut.println( " " + lFieldData.getDiffEqualFlagJavaName() + " = false;" );
        pOut.println( " isAllFieldsEqual = false;" );
        pOut.println( " }" );
      }
      else
      {
        ClassDataType lClassDataType = (ClassDataType) lType;

        if( !lClassDataType.isHasSubclasses() && !lFieldData.isList() )
        {
          pOut.println( " if( " + lFieldData.getDiffChangeJavaName() + ".isEqual )" );
          pOut.println( " {" );
          pOut.println( " " + lFieldData.getDiffEqualFlagJavaName() + " = true;" );
          pOut.println( " }" );
          pOut.println( " else" );
          pOut.println( " {" );
          pOut.println( " " + lFieldData.getDiffEqualFlagJavaName() + " = false;" );
          pOut.println( " isAllFieldsEqual = false;" );
          pOut.println( " }" );
        }
        else
        {
          if( !lClassDataType.isHasSubclasses() )
          {
            pOut.println( " " + lFieldData.getDiffEqualFlagJavaName() + " = true;" );
            pOut.println( " for( " + lClassDataType.getDiffJavaName() + " lValue : " + lFieldData.getDiffChangeJavaName() + " )" );
            pOut.println( " {" );
            pOut.println( " if( !lValue.isEqual )" );
            pOut.println( " {" );
            pOut.println( " " + lFieldData.getDiffEqualFlagJavaName() + " = false;" );
            pOut.println( " isAllFieldsEqual = false;" );
            pOut.println( " }" );
            pOut.println( " }" );
          }
          else
          {
            List<ClassDataType> lAllClassDataSubTypes = getAllClassDataSubTypes( lClassDataType, pTypeDataContainer );

            if( lFieldData.isList() )
            {
              pOut.println( " " + lFieldData.getDiffEqualFlagJavaName() + " = true;" );
              for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
              {
                pOut.println( " for( " + lClassDataSubType.getDiffJavaName() + " lValue : " + lFieldData.getDiffChangeJavaNameForSubType( lClassDataSubType ) + " )" );
                pOut.println( " {" );
                pOut.println( " if( !lValue.isEqual )" );
                pOut.println( " {" );
                pOut.println( " " + lFieldData.getDiffEqualFlagJavaName() + " = false;" );
                pOut.println( " isAllFieldsEqual = false;" );
                pOut.println( " }" );
                pOut.println( " }" );
              }
            }
            else
            {
              pOut.println( " " + lFieldData.getDiffEqualFlagJavaName() + " = true;" );
              for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
              {
                pOut.println( " if( !" + lFieldData.getDiffChangeJavaNameForSubType( lClassDataSubType ) + ".isEqual )" );
                pOut.println( " {" );
                pOut.println( " " + lFieldData.getDiffEqualFlagJavaName() + " = false;" );
                pOut.println( " isAllFieldsEqual = false;" );
                pOut.println( " }" );
              }
            }
          }
        }
      }

      pOut.println( "" );
    }

    if( !pClassDataType.isHasSubclasses() )
    {
      pOut.println( "isMatched = isOld && isNew;" );
      pOut.println( "" );
      pOut.println( " parentIndexIsEqual = Objects.equals( oldParentIndex, newParentIndex );" );
      pOut.println( "" );
      pOut.println( " isEqual = isAllFieldsEqual && parentIndexIsEqual;" );
    }

    pOut.println( "}" );
    pOut.println();
  }

  private static void writeInitWithNewValueMethod( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer, JavaPrettyWriter pOut )
  {

    pOut.println( " public void initWithNewValue( " + pClassDataType.getJavaName() + " pNewValue )" );
    pOut.println( " {" );
    if( pClassDataType.getSuperclass() != null )
    {
      pOut.println( " super.initWithNewValue(pNewValue);" );
    }

    if( pClassDataType.isHasSubclasses() && pClassDataType.getFiledDataList().isEmpty() )
    {
    }
    else
    {
      pOut.println( " if( pNewValue != null )" );
      pOut.println( " {" );

      if( !pClassDataType.isHasSubclasses() )
      {
        pOut.println( " isNew = true;" );
      }

      for( FieldData lFieldData : pClassDataType.getFiledDataList() )
      {
        ClassData lType = lFieldData.getClassData( lFieldData.getJavaType(), pTypeDataContainer );
        if( lType.isAtomicValue() )
        {
          if( lFieldData.isInt() )
          {
            pOut.println( " " + lFieldData.getDiffNewJavaName() + " = DiffRepository.getNullableIntValue( pNewValue." + lFieldData.getJavaGetterCall() + " );" );
          }
          else
          {
            pOut.println( " " + lFieldData.getDiffNewJavaName() + " = pNewValue." + lFieldData.getJavaGetterCall() + ";" );
          }
        }
        else
        {
          ClassDataType lClassDataType = (ClassDataType) lType;

          if( !lClassDataType.isHasSubclasses() && !lFieldData.isList() )
          {
            pOut.println( " " + lFieldData.getDiffChangeJavaName() + " = new " + lClassDataType.getDiffJavaName() + "(pNewValue." + lFieldData.getJavaGetterCall() + ");" );
          }
          else
          {
            if( !lClassDataType.isHasSubclasses() )
            {
              pOut.println( " " + lFieldData.getDiffChangeJavaName() + " = new Array" + lClassDataType.getDiffJavaNameCollection() + "();" );

              pOut.println( " if( pNewValue." + lFieldData.getJavaGetterCall() + " != null )" );
              pOut.println( " {" );
              pOut.println( " for( " + lClassDataType.getJavaName() + " lValue : pNewValue." + lFieldData.getJavaGetterCall() + " ) " );
              pOut.println( " {" );
              pOut.println( " " + lFieldData.getDiffChangeJavaName() + ".add( new " + lClassDataType.getDiffJavaName() + "( lValue ) );" );
              pOut.println( " if( DiffRepository.get" + lClassDataType.getMergeJavaName() + "().isChildOrderRelevant() )" );
              pOut.println( " {" );
              pOut.println( " " + lFieldData.getDiffChangeJavaName() + ".get(" + lFieldData.getDiffChangeJavaName() + ".size()-1).newParentIndex = " + lFieldData.getDiffChangeJavaName() + ".size()-1;" );
              pOut.println( " }" );
              pOut.println( " }" );
              pOut.println( " }" );
            }
            else
            {
              List<ClassDataType> lAllClassDataSubTypes = getAllClassDataSubTypes( lClassDataType, pTypeDataContainer );

              if( lFieldData.isList() )
              {
                for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
                {
                  pOut.println( " " + lFieldData.getDiffChangeJavaNameForSubType( lClassDataSubType ) + " = new Array" + lClassDataSubType.getDiffJavaNameCollection() + "();" );
                }

                pOut.println( " if( pNewValue." + lFieldData.getJavaGetterCall() + " != null )" );
                pOut.println( " {" );
                pOut.println( " for( " + lClassDataType.getJavaName() + " lValue : pNewValue." + lFieldData.getJavaGetterCall() + ")" );
                pOut.println( " {" );

                for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
                {
                  pOut.println( " if( lValue instanceof " + lClassDataSubType.getJavaName() + " ) " );
                  pOut.println( " {" );
                  pOut.println( " " + lFieldData.getDiffChangeJavaNameForSubType( lClassDataSubType ) + ".add( new " + lClassDataSubType.getDiffJavaName() + "( (" + lClassDataSubType.getJavaName() + ")lValue ) );" );
                  pOut.println( " if( DiffRepository.get" + lClassDataType.getMergeJavaName() + "().isChildOrderRelevant() )" );
                  pOut.println( " {" );
                  pOut.println( " " + lFieldData.getDiffChangeJavaNameForSubType( lClassDataSubType ) + ".get( " + lFieldData.getDiffChangeJavaNameForSubType( lClassDataSubType ) + ".size()-1).newParentIndex = pNewValue." + lFieldData.getJavaGetterCall() + ".indexOf(lValue);" );
                  pOut.println( " }" );
                  pOut.println( " }" );
                }

                pOut.println( " }" );
                pOut.println( " }" );
              }
              else
              {
                for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
                {
                  pOut.println( " if( pNewValue." + lFieldData.getJavaGetterCall() + " != null && pNewValue." + lFieldData.getJavaGetterCall() + " instanceof " + lClassDataSubType.getJavaName() + " ) " );
                  pOut.println( " {" );
                  pOut.println( " " + lFieldData.getDiffChangeJavaNameForSubType( lClassDataSubType ) + " = new " + lClassDataSubType.getDiffJavaName() + "( (" + lClassDataSubType.getJavaName() + ")pNewValue." + lFieldData.getJavaGetterCall() + ");" );
                  pOut.println( " }" );
                  pOut.println( " else" );
                  pOut.println( " {" );
                  pOut.println( " " + lFieldData.getDiffChangeJavaNameForSubType( lClassDataSubType ) + " = new " + lClassDataSubType.getDiffJavaName() + "( null );" );
                  pOut.println( " }" );
                }
              }
            }
          }
        }

        pOut.println();
      }

      pOut.println( " }" );
      pOut.println( " else" );
      pOut.println( " {" );
      if( !pClassDataType.isHasSubclasses() )
      {
        pOut.println( " isNew = false;" );
      }

      for( FieldData lFieldData : pClassDataType.getFiledDataList() )
      {
        ClassData lType = lFieldData.getClassData( lFieldData.getJavaType(), pTypeDataContainer );
        if( !lType.isAtomicValue() )
        {
          ClassDataType lClassDataType = (ClassDataType) lType;

          if( lFieldData.isList() )
          {
            if( !lClassDataType.isHasSubclasses() )
            {
              pOut.println( " " + lFieldData.getDiffChangeJavaName() + " = new Array" + lClassDataType.getDiffJavaNameCollection() + "();" );
            }
            else
            {
              List<ClassDataType> lAllClassDataSubTypes = getAllClassDataSubTypes( lClassDataType, pTypeDataContainer );
              for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
              {
                pOut.println( " " + lFieldData.getDiffChangeJavaNameForSubType( lClassDataSubType ) + " = new Array" + lClassDataSubType.getDiffJavaNameCollection() + "();" );
              }
            }
          }
          else
          {
            if( !lClassDataType.isHasSubclasses() )
            {
              pOut.println( " " + lFieldData.getDiffChangeJavaName() + " = new " + lClassDataType.getDiffJavaName() + "( null );" );
            }
            else
            {
              List<ClassDataType> lAllClassDataSubTypes = getAllClassDataSubTypes( lClassDataType, pTypeDataContainer );
              for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
              {
                pOut.println( " " + lFieldData.getDiffChangeJavaNameForSubType( lClassDataSubType ) + " = new " + lClassDataSubType.getDiffJavaName() + "( null );" );
              }
            }
          }
        }

        pOut.println();
      }
      pOut.println( "}" );
    }

    pOut.println( " }" );
    pOut.println();
  }

  private static void writeMergeWithOldValueMethod( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer, JavaPrettyWriter pOut )
  {
    if( pClassDataType.getSuperclass() != null )
    {
      pOut.println( " @Override" );
    }
    pOut.println( " public void mergeWithOldValue( " + getBaseSuperclass( pClassDataType, pTypeDataContainer ).getJavaName() + " pOldValue )" );
    pOut.println( " {" );
    pOut.println( " List<Integer> lMergeResult;" );
    pOut.println( " boolean lMergeTypeEqual;" );

    if( pClassDataType.getSuperclass() != null )
    {
      pOut.println( " super.mergeWithOldValue(pOldValue);" );
    }

    pOut.println( " if( pOldValue != null )" );
    pOut.println( " {" );

    if( !pClassDataType.isHasSubclasses() )
    {
      pOut.println( " isOld = true;" );
    }

    String lParameterTreated = "pOldValue";

    if( pClassDataType.getSuperclass() != null )
    {
      lParameterTreated = "((" + pClassDataType.getJavaName() + ")pOldValue)";
    }

    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      ClassData lType = lFieldData.getClassData( lFieldData.getJavaType(), pTypeDataContainer );
      if( lType.isAtomicValue() )
      {
        if( lFieldData.isInt() )
        {
          pOut.println( " " + lFieldData.getDiffOldJavaName() + " = DiffRepository.getNullableIntValue( " + lParameterTreated + "." + lFieldData.getJavaGetterCall() + " );" );
        }
        else
        {
          pOut.println( " " + lFieldData.getDiffOldJavaName() + " = " + lParameterTreated + "." + lFieldData.getJavaGetterCall() + ";" );
        }
      }
      else
      {
        ClassDataType lClassDataType = (ClassDataType) lType;

        if( !lClassDataType.isHasSubclasses() && !lFieldData.isList() )
        {
          pOut.println( " " + lFieldData.getDiffChangeJavaName() + ".mergeWithOldValue( " + lParameterTreated + "." + lFieldData.getJavaGetterCall() + " );" );
        }
        else
        {
          if( !lClassDataType.isHasSubclasses() )
          {
            pOut.println( " if( " + lParameterTreated + "." + lFieldData.getJavaGetterCall() + " != null )" );
            pOut.println( " {" );
            pOut.println( " lMergeResult = DiffRepository.get" + lClassDataType.getMergeJavaName() + "().getMergeResult( " + lFieldData.getDiffChangeJavaName() + ", " + lParameterTreated + "." + lFieldData.getJavaGetterCall() + " );" );

            pOut.println( " for( int i = 0; i < lMergeResult.size(); i++ )" );
            pOut.println( " {" );
            pOut.println( " " + lClassDataType.getDiffJavaName() + " lValueDiff;" );
            pOut.println( " if( lMergeResult.get(i) == null )" );
            pOut.println( " {" );
            pOut.println( " lValueDiff = new " + lClassDataType.getDiffJavaName() + "( null );" );
            pOut.println( " " + lFieldData.getDiffChangeJavaName() + ".add( lValueDiff );" );
            pOut.println( " }" );
            pOut.println( " else" );
            pOut.println( " {" );
            pOut.println( " lValueDiff = " + lFieldData.getDiffChangeJavaName() + ".get(lMergeResult.get(i));" );
            pOut.println( " }" );
            pOut.println( " if( DiffRepository.get" + lClassDataType.getMergeJavaName() + "().isChildOrderRelevant() )" );
            pOut.println( " {" );
            pOut.println( " lValueDiff.oldParentIndex = i;" );
            pOut.println( " }" );
            pOut.println( " lValueDiff.mergeWithOldValue( " + lParameterTreated + "." + lFieldData.getJavaGetterCall() + ".get(i) );" );
            pOut.println( " }" );
            pOut.println( " }" );
          }
          else
          {
            List<ClassDataType> lAllClassDataSubTypes = getAllClassDataSubTypes( lClassDataType, pTypeDataContainer );

            if( lFieldData.isList() )
            {
              pOut.println( " if( " + lParameterTreated + "." + lFieldData.getJavaGetterCall() + " != null )" );
              pOut.println( " {" );

              for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
              {
                pOut.println( " {" );
                pOut.println( " " + lClassDataSubType.getJavaNameCollection() + " lTypedList = new Array" + lClassDataSubType.getJavaNameCollection() + "();" );
                pOut.println( " List<Integer> lIndexMap = null;" );
                pOut.println( " if( DiffRepository.get" + lClassDataType.getMergeJavaName() + "().isChildOrderRelevant() )" );
                pOut.println( " {" );
                pOut.println( " lIndexMap = new ArrayList<Integer>();" );
                pOut.println( " }" );
                pOut.println( " for( " + lClassDataType.getJavaName() + " lValue : " + lParameterTreated + "." + lFieldData.getJavaGetterCall() + " )" );
                pOut.println( " {" );
                pOut.println( " if( lValue instanceof " + lClassDataSubType.getJavaName() + " ) " );
                pOut.println( " {" );
                pOut.println( " lTypedList.add( (" + lClassDataSubType.getJavaName() + ")lValue );" );
                pOut.println( " if( lIndexMap != null )" );
                pOut.println( " {" );
                pOut.println( " lIndexMap.add(" + lParameterTreated + "." + lFieldData.getJavaGetterCall() + ".indexOf(lValue));" );
                pOut.println( " }" );
                pOut.println( " }" );
                pOut.println( " }" );
                pOut.println( " lMergeResult = DiffRepository.get" + lClassDataSubType.getMergeJavaName() + "().getMergeResult( " + lFieldData.getDiffChangeJavaNameForSubType( lClassDataSubType ) + ", lTypedList );" );
                pOut.println( " int i=0;" );
                pOut.println( " for(Integer lIndex : lMergeResult)" );
                pOut.println( " {" );
                pOut.println( " " + lClassDataSubType.getDiffJavaName() + " lValueDiff;" );
                pOut.println( " if( lIndex == null )" );
                pOut.println( " {" );
                pOut.println( " lValueDiff = new " + lClassDataSubType.getDiffJavaName() + "( null );" );
                pOut.println( " " + lFieldData.getDiffChangeJavaNameForSubType( lClassDataSubType ) + ".add(lValueDiff);" );
                pOut.println( " }" );
                pOut.println( " else" );
                pOut.println( " {" );
                pOut.println( " lValueDiff =" + lFieldData.getDiffChangeJavaNameForSubType( lClassDataSubType ) + ".get( lIndex );" );
                pOut.println( " }" );
                pOut.println( " if( lIndexMap != null )" );
                pOut.println( " {" );
                pOut.println( " lValueDiff.oldParentIndex = lIndexMap.get( i );" );
                pOut.println( " }" );
                pOut.println( " lValueDiff.mergeWithOldValue( lTypedList.get(i) );" );
                pOut.println( " i++;" );
                pOut.println( " }" );
                pOut.println( " }" );
              }
              pOut.println( " }" );
            }
            else
            {
              for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
              {
                pOut.println( " if( " + lParameterTreated + "." + lFieldData.getJavaGetterCall() + " != null && " + lParameterTreated + "." + lFieldData.getJavaGetterCall() + " instanceof " + lClassDataSubType.getJavaName() + " ) " );
                pOut.println( " {" );
                pOut.println( " " + lFieldData.getDiffChangeJavaNameForSubType( lClassDataSubType ) + ".mergeWithOldValue( (" + lClassDataSubType.getJavaName() + ") " + lParameterTreated + "." + lFieldData.getJavaGetterCall() + " );" );
                pOut.println( " }" );
                pOut.println( " else" );
                pOut.println( " {" );
                pOut.println( " " + lFieldData.getDiffChangeJavaNameForSubType( lClassDataSubType ) + ".mergeWithOldValue( null );" );
                pOut.println( " }" );
              }
            }
          }
        }
      }

      pOut.println();
    }

    pOut.println( " }" );
    pOut.println( " else" );
    pOut.println( " {" );
    if( !pClassDataType.isHasSubclasses() )
    {
      pOut.println( " isOld = false;" );
    }

    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      ClassData lType = lFieldData.getClassData( lFieldData.getJavaType(), pTypeDataContainer );
      if( !lType.isAtomicValue() )
      {
        ClassDataType lClassDataType = (ClassDataType) lType;

        if( !lFieldData.isList() )
        {
          if( !lClassDataType.isHasSubclasses() )
          {
            pOut.println( " " + lFieldData.getDiffChangeJavaName() + ".mergeWithOldValue( null );" );
          }
          else
          {
            for( ClassDataType lClassDataSubType : getAllClassDataSubTypes( lClassDataType, pTypeDataContainer ) )
            {
              pOut.println( " " + lFieldData.getDiffChangeJavaNameForSubType( lClassDataSubType ) + ".mergeWithOldValue( null );" );
            }
          }
        }
      }

      pOut.println();
    }
    pOut.println( " }" );

    for( FieldData lFieldData : pClassDataType.getFiledDataList() )
    {
      if( lFieldData.isList() )
      {
        ClassDataType lClassDataType = (ClassDataType) lFieldData.getClassData( lFieldData.getJavaType(), pTypeDataContainer );

        if( !lClassDataType.isHasSubclasses() )
        {
          pOut.println( " for( " + lClassDataType.getDiffJavaName() + " lValue : " + lFieldData.getDiffChangeJavaName() + " ) " );
          pOut.println( " {" );
          pOut.println( " if( lValue.isOld == null )" );
          pOut.println( " {" );
          pOut.println( " lValue.mergeWithOldValue( null );" );
          pOut.println( " }" );
          pOut.println( " }" );
        }
        else
        {
          for( ClassDataType lClassDataSubType : getAllClassDataSubTypes( lClassDataType, pTypeDataContainer ) )
          {
            pOut.println( " for( " + lClassDataSubType.getDiffJavaName() + " lValue : " + lFieldData.getDiffChangeJavaNameForSubType( lClassDataSubType ) + " ) " );
            pOut.println( " {" );
            pOut.println( " if( lValue.isOld == null )" );
            pOut.println( " {" );
            pOut.println( " lValue.mergeWithOldValue( null );" );
            pOut.println( " }" );
            pOut.println( " }" );
          }
        }
      }

      pOut.println();
    }

    if( !pClassDataType.isHasSubclasses() )
    {
      pOut.println( " initFlags();" );
    }

    pOut.println( " }" );
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
