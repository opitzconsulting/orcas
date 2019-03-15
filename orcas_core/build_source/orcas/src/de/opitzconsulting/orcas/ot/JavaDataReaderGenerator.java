package de.opitzconsulting.orcas.ot;

import java.util.ArrayList;
import java.util.List;

public class JavaDataReaderGenerator extends JavaGenerator
{
  public static void main( String[] pArgs )
  {
    new JavaDataReaderGenerator( pArgs ).export();
  }

  protected JavaDataReaderGenerator( String[] pArgs )
  {
    super( pArgs );
  }

  @Override
  protected String getPackageName()
  {
    return "de.opitzconsulting.orcas.syex.load";
  }

  @Override
  public void export()
  {
    writeJavaFile( "DataReader.java", new DoWithWriter()
    {
      public void write( JavaPrettyWriter pJavaPrettyWriter )
      {
        writeDataReader( pJavaPrettyWriter, "DataReader" );
      }
    } );
  }

  private void writeDataReader( JavaPrettyWriter pOut, String pClassName )
  {
    TypeDataContainer lTypeDataContainer = new ClassDataParser().parse();

    writePackage( pOut );
    pOut.println();
    pOut.println( "import java.util.*;" );
    pOut.println( "import java.io.*;" );
    pOut.println( "import java.sql.*;" );
    pOut.println( "import de.opitzconsulting.orcasDsl.*;" );
    pOut.println( "import de.opitzconsulting.orcasDsl.impl.*;" );
    pOut.println( "import java.math.*;" );

    pOut.println();
    pOut.print( "public class DataReader" );
    pOut.println( "{" );

    pOut.println( "private static int intNullValue;" );

    pOut.println();
    pOut.println( "public static String clobToString( Clob pClob )" );
    pOut.println( "{" );
    pOut.println( "  try" );
    pOut.println( "  {" );
    pOut.println( "    return pClob.getSubString( 1L, (int)pClob.length() );" );
    pOut.println( "  }" );
    pOut.println( "  catch( Exception e )" );
    pOut.println( "  {" );
    pOut.println( "    throw new RuntimeException( e );" );
    pOut.println( "  }" );
    pOut.println( "}" );

    pOut.println();
    pOut.println( "public static void setIntNullValue( int pIntNullValue )" );
    pOut.println( "{" );
    pOut.println( "intNullValue = pIntNullValue;" );
    pOut.println( "}" );

    for( ClassDataType lClassDataType : OracleOtGenerator.orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      if( !lClassDataType.isHasSubclasses() && !lClassDataType.isEnum() )
      {
        pOut.println( "public static void loadInto" + lClassDataType.getJavaName() + "( " + lClassDataType.getJavaName() + " pValue, Struct pStruct ) throws SQLException" );

        pOut.println( "{" );

        pOut.println( "Object[] lAttributes = pStruct.getAttributes();" );

        int i = hasDummyField( lClassDataType, lTypeDataContainer ) ? 1 : 0;
        for( FieldData lFieldData : getFiledDataListRecursive( lClassDataType, lTypeDataContainer ) )
        {
          ClassData lType = lFieldData.getClassData( lFieldData.getJavaType(), lTypeDataContainer );

          ClassDataType lFieldClassDataType = null;

          if( lType instanceof ClassDataType )
          {
            lFieldClassDataType = (ClassDataType)lType;
          }

          pOut.println( "if( lAttributes[" + i + "] != null )" );
          pOut.println( "{" );
          if( lFieldData.isList() )
          {
            pOut.println( "Object[] lArray = (Object[])((Array)lAttributes[" + i + "]).getArray();" );
            pOut.println( "for( int i = 0; i < lArray.length; i++ )" );
            pOut.println( "{" );
            pOut.println( "Struct lStruct = (Struct)lArray[i];" );

            if( lFieldClassDataType.isHasSubclasses() )
            {
              List<ClassDataType> lClassDataSubTypes = JavaOdGenerator.getAllClassDataSubTypes( lFieldClassDataType, lTypeDataContainer );

              for( ClassDataType lClassDataSubType : lClassDataSubTypes )
              {
                pOut.println( "if( lStruct.getSQLTypeName().endsWith( \"" + lClassDataSubType.getSqlName().toUpperCase() + "\" ) )" );
                pOut.println( "{" );
                pOut.println( lClassDataSubType.getJavaName() + " lValue = new " + lClassDataSubType.getJavaName() + "Impl();" );

                pOut.println( "loadInto" + lClassDataSubType.getJavaName() + "( lValue, lStruct );" );

                pOut.println( "pValue." + lFieldData.getJavaGetterCall() + ".add( lValue );" );
                pOut.println( "}" );
              }
            }
            else
            {
              pOut.println( lType.getJavaName() + " lValue = new " + lType.getJavaName() + "Impl();" );

              pOut.println( "loadInto" + lType.getJavaName() + "( lValue, lStruct );" );

              pOut.println( "pValue." + lFieldData.getJavaGetterCall() + ".add( lValue );" );
            }

            pOut.println( "}" );
          }
          else
          {
            if( lFieldClassDataType == null )
            {
              String lValueString = null;

              if( lType.getJavaName().equals( "String" ) )
              {
                if( lType.getSqlName().equalsIgnoreCase( "clob" ) )
                {
                  lValueString = "( lAttributes[" + i + "] == null ? null : clobToString( (Clob)lAttributes[" + i + "] ) )";
                }
                else
                {
                  lValueString = "(String)lAttributes[" + i + "]";
                }
              }
              if( lType.getJavaName().equals( "int" ) )
              {
                lValueString = "lAttributes[" + i + "] == null ? intNullValue : ((BigDecimal)lAttributes[" + i + "] ).intValue()";
              }
              if( lType.getJavaName().equals( "Integer" ) )
              {
                lValueString = "lAttributes[" + i + "] == null ? null : ((BigDecimal)lAttributes[" + i + "] ).intValue()";
              }
              if( lType.getJavaName().equals( "BigInteger" ) )
              {
                lValueString = "lAttributes[" + i + "] == null ? null : ((BigDecimal)lAttributes[" + i + "] ).toBigInteger()";
              }
              if( lType.getJavaName().equals( "boolean" ) )
              {
                lValueString = "((BigDecimal)lAttributes[" + i + "] ).intValue() == 1";
              }

              if( lValueString == null )
              {
                throw new RuntimeException( "unknown type: " + lType.getJavaName() );
              }

              pOut.println( "pValue." + lFieldData.getJavaSetterName() + "( " + lValueString + " );" );
            }
            else
            {
              pOut.println( "Struct lStruct = (Struct)lAttributes[" + i + "];" );

              pOut.println( "if( lStruct != null )" );
              pOut.println( "{" );
              if( lFieldClassDataType.isEnum() )
              {
                pOut.println( "String lName = (String)lStruct.getAttributes()[1];" );

                for( EnumData lEnumData : lFieldClassDataType.getEnumData() )
                {
                  pOut.println( "if( lName.equals( \"" + lEnumData.getName() + "\" ) )" );
                  pOut.println( "{" );
                  pOut.println( "pValue." + lFieldData.getJavaSetterName() + "(" + lFieldClassDataType.getJavaName() + "." + lEnumData.getJavaName() + ");" );
                  pOut.println( "}" );
                }
              }
              else
              {
                if( lFieldClassDataType.isHasSubclasses() )
                {
                  List<ClassDataType> lClassDataSubTypes = JavaOdGenerator.getAllClassDataSubTypes( lFieldClassDataType, lTypeDataContainer );

                  for( ClassDataType lClassDataSubType : lClassDataSubTypes )
                  {
                    pOut.println( "if( lStruct.getSQLTypeName().endsWith( \"" + lClassDataSubType.getSqlName().toUpperCase() + "\" ) )" );
                    pOut.println( "{" );
                    pOut.println( lClassDataSubType.getJavaName() + " lValue = new " + lClassDataSubType.getJavaName() + "Impl();" );

                    pOut.println( "loadInto" + lClassDataSubType.getJavaName() + "( lValue, lStruct );" );

                    pOut.println( "pValue." + lFieldData.getJavaSetterName() + "( lValue );" );
                    pOut.println( "}" );
                  }
                }
                else
                {
                  pOut.println( lType.getJavaName() + " lValue = new " + lType.getJavaName() + "Impl();" );

                  pOut.println( "loadInto" + lType.getJavaName() + "( lValue, lStruct );" );

                  pOut.println( "pValue." + lFieldData.getJavaSetterName() + "( lValue );" );
                }
              }

              pOut.println( "}" );
            }
          }
          pOut.println( "}" );

          i++;
        }

        pOut.println( "}" );
      }
    }

    pOut.println( "}" );
  }

  private List<FieldData> getFiledDataListRecursive( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer )
  {
    if( pClassDataType.getSuperclass() != null )
    {
      List<FieldData> lReturn = new ArrayList<FieldData>();

      lReturn.addAll( getFiledDataListRecursive( (ClassDataType)pTypeDataContainer.getClassData( pClassDataType.getSuperclass() ), pTypeDataContainer ) );
      lReturn.addAll( pClassDataType.getFiledDataList() );

      return lReturn;
    }
    else
    {
      return pClassDataType.getFiledDataList();
    }
  }

  private boolean hasDummyField( ClassDataType pClassDataType, TypeDataContainer pTypeDataContainer )
  {
    if( pClassDataType.getSuperclass() != null )
    {
      return hasDummyField( (ClassDataType)pTypeDataContainer.getClassData( pClassDataType.getSuperclass() ), pTypeDataContainer );
    }

    if( pClassDataType.getFiledDataList().isEmpty() )
    {
      return true;
    }

    return false;
  }
}
