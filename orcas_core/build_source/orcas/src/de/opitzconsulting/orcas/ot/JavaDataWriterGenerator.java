package de.opitzconsulting.orcas.ot;

import java.util.ArrayList;
import java.util.List;

public class JavaDataWriterGenerator extends JavaGenerator
{
  public static void main( String[] pArgs )
  {
    new JavaDataWriterGenerator( pArgs ).export();
  }

  protected JavaDataWriterGenerator( String[] pArgs )
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
    writeJavaFile( "DataWriter.java", new DoWithWriter()
    {
      public void write( JavaPrettyWriter pJavaPrettyWriter )
      {
        writeDataReader( pJavaPrettyWriter, "DataWriter" );
      }
    } );
  }

  private void writeDataReader( JavaPrettyWriter pOut, String pClassName )
  {
    TypeDataContainer lTypeDataContainer = new ClassDataParser().parse();

    writePackage( pOut );
    pOut.println();
    pOut.println( "import java.util.*;" );
    pOut.println( "import java.sql.*;" );
    pOut.println( "import de.opitzconsulting.orcasDsl.*;" );
    pOut.println( "import de.opitzconsulting.orcasDsl.impl.*;" );
    pOut.println( "import java.math.*;" );

    pOut.println();
    pOut.print( "public abstract class DataWriter" );
    pOut.println( "{" );

    pOut.println( "private int intNullValue;" );

    pOut.println( "public void setIntNullValue( int pIntNullValue )" );
    pOut.println( "{" );
    pOut.println( "intNullValue = pIntNullValue;" );
    pOut.println( "}" );

    pOut.println( "protected abstract Struct createStruct( String pTypeName, Object[] pAttributes );" );
    pOut.println( "protected abstract Array createArrayOf( String pTypeName, Object[] pElements );" );
    pOut.println( "protected abstract Clob createClob( String pValue );" );

    for( ClassDataType lClassDataType : OracleOtGenerator.orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      if( !lClassDataType.isEnum() )
      {
        pOut.println( "public Struct getStruct" + lClassDataType.getJavaName() + "( " + lClassDataType.getJavaName() + " pValue )" );

        pOut.println( "{" );

        if( lClassDataType.isHasSubclasses() )
        {
          List<ClassDataType> lClassDataSubTypes = JavaOdGenerator.getAllClassDataSubTypes( lClassDataType, lTypeDataContainer );

          for( ClassDataType lClassDataSubType : lClassDataSubTypes )
          {
            pOut.println( "if( pValue instanceof " + lClassDataSubType.getJavaName() + " )" );
            pOut.println( "{" );
            pOut.println( "return getStruct" + lClassDataSubType.getJavaName() + "( (" + lClassDataSubType.getJavaName() + ")pValue );" );
            pOut.println( "}" );
          }

          pOut.println( "throw new IllegalStateException();" );
        }
        else
        {
          int lFieldCount = hasDummyField( lClassDataType, lTypeDataContainer ) ? 1 : 0;
          lFieldCount += getFiledDataListRecursive( lClassDataType, lTypeDataContainer ).size();

          pOut.println( "Object[] lAttributes = new Object[" + lFieldCount + "];" );

          int i = hasDummyField( lClassDataType, lTypeDataContainer ) ? 1 : 0;
          for( FieldData lFieldData : getFiledDataListRecursive( lClassDataType, lTypeDataContainer ) )
          {
            ClassData lType = lFieldData.getClassData( lFieldData.getJavaType(), lTypeDataContainer );

            ClassDataType lFieldClassDataType = null;

            if( lType instanceof ClassDataType )
            {
              lFieldClassDataType = (ClassDataType)lType;
            }

            if( lFieldData.isList() )
            {
              pOut.println( "{" );
              pOut.println( "Object[] lArray = new Object[pValue." + lFieldData.getJavaGetterCall() + ".size()];" );
              pOut.println( "int i = 0;" );
              pOut.println( "for( " + lFieldClassDataType.getJavaName() + " lValue : pValue." + lFieldData.getJavaGetterCall() + " )" );
              pOut.println( "{" );

              pOut.println( "lArray[i] = getStruct" + lType.getJavaName() + "( lValue );" );

              pOut.println( "i++;" );
              pOut.println( "}" );

              pOut.println( "lAttributes[" + i + "] = createArrayOf( \"" + lFieldClassDataType.getSqlNameCollection() + "\", lArray );" );

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
                    lValueString = "(pValue." + lFieldData.getJavaGetterCall() + "  == null ? null : createClob(pValue." + lFieldData.getJavaGetterCall() + "))";
                  }
                  else
                  {
                    lValueString = "pValue." + lFieldData.getJavaGetterCall();
                  }
                }
                if( lType.getJavaName().equals( "int" ) )
                {
                  lValueString = "pValue." + lFieldData.getJavaGetterCall() + " == intNullValue ? null : new BigDecimal(pValue." + lFieldData.getJavaGetterCall() + ")";
                }
                if( lType.getJavaName().equals( "Integer" ) )
                {
                  lValueString = "pValue." + lFieldData.getJavaGetterCall();
                }
                if( lType.getJavaName().equals( "BigInteger" ) )
                {
                  lValueString = "pValue." + lFieldData.getJavaGetterCall();
                }
                if( lType.getJavaName().equals( "boolean" ) )
                {
                  lValueString = "pValue." + lFieldData.getJavaGetterCall() + " ? new BigDecimal(1) : new BigDecimal(0)";
                }

                if( lValueString == null )
                {
                  throw new RuntimeException( "unknown type: " + lType.getJavaName() );
                }

                pOut.println( "lAttributes[" + i + "] =" + lValueString + ";" );
              }
              else
              {
                pOut.println( "if( pValue." + lFieldData.getJavaGetterCall() + " != null )" );
                pOut.println( "{" );
                if( lFieldClassDataType.isEnum() )
                {
                  for( EnumData lEnumData : lFieldClassDataType.getEnumData() )
                  {
                    pOut.println( "if( pValue." + lFieldData.getJavaGetterCall() + " == " + lFieldClassDataType.getJavaName() + "." + lEnumData.getJavaName() + " )" );
                    pOut.println( "{" );
                    pOut.println( "lAttributes[" + i + "] = createStruct( \"" + lFieldClassDataType.getSqlName() + "\", new Object[]{ \"" + lEnumData.getLiteral() + "\", \"" + lEnumData.getName() + "\", new BigDecimal(" + lEnumData.getValue() + ") } );;" );
                    pOut.println( "}" );
                  }
                }
                else
                {
                  pOut.println( "lAttributes[" + i + "] = getStruct" + lType.getJavaName() + "( pValue." + lFieldData.getJavaGetterCall() + " );" );
                }

                pOut.println( "}" );
              }
            }

            i++;
          }

          pOut.println( "return createStruct( \"" + lClassDataType.getSqlName() + "\", lAttributes );" );
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
