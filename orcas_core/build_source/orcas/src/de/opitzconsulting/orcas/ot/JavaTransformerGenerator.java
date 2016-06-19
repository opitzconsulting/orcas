package de.opitzconsulting.orcas.ot;

import java.util.ArrayList;
import java.util.List;

public class JavaTransformerGenerator extends JavaGenerator
{
  public static void main( String[] pArgs )
  {
    new JavaTransformerGenerator( pArgs ).export();
  }

  protected JavaTransformerGenerator( String[] pArgs )
  {
    super( pArgs );
  }

  @Override
  protected String getPackageName()
  {
    return "de.opitzconsulting.orcas.syex.trans";
  }

  @Override
  public void export()
  {
    writeJavaFile( "TransformSyexOrig.java", new DoWithWriter()
    {
      public void write( JavaPrettyWriter pJavaPrettyWriter )
      {
        writeTransfromer( pJavaPrettyWriter, "syex", "orig", "TransformSyexOrig" );
      }
    } );
    writeJavaFile( "TransformOrigSyex.java", new DoWithWriter()
    {
      public void write( JavaPrettyWriter pJavaPrettyWriter )
      {
        writeTransfromer( pJavaPrettyWriter, "orig", "syex", "TransformOrigSyex" );
      }
    } );
  }

  private void writeTransfromer( JavaPrettyWriter pOut, String pSourcePrefix, String pTargetPrefix, String pClassName )
  {
    ClassDataType.setTypePrefix( "orig" );
    TypeDataContainer lTypeDataContainer = new ClassDataParser().parse();
    ClassDataType.setTypePrefix( pTargetPrefix );

    writePackage( pOut );
    pOut.println();
    pOut.println( "import java.util.*;" );
    pOut.println();
    pOut.print( "public class " + pClassName );
    pOut.println( "{" );

    String lSrcPackage = "";
    String lDstPackage = "";

    if( pSourcePrefix.equals( "syex" ) )
    {
      lSrcPackage = "de.opitzconsulting.orcasDsl.";
      lDstPackage = "de.opitzconsulting.origOrcasDsl.";
    }
    else
    {
      lSrcPackage = "de.opitzconsulting.origOrcasDsl.";
      lDstPackage = "de.opitzconsulting.orcasDsl.";
    }

    for( ClassDataType lClassDataType : OracleOtGenerator.orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      pOut.println( "public static " + lDstPackage + lClassDataType.getJavaName() + " convert" + lClassDataType.getJavaName() + "( " + lSrcPackage + lClassDataType.getJavaName() + " pInput )" );
      pOut.println( "{" );

      pOut.println( "if( pInput == null )" );
      pOut.println( "{" );
      pOut.println( "return null;" );
      pOut.println( "}" );

      if( lClassDataType.isHasSubclasses() )
      {
        for( ClassDataType lSubClassDataType : lTypeDataContainer.getAllClassDataTypes() )
        {
          if( lSubClassDataType.getSuperclass() != null && lTypeDataContainer.getClassData( lSubClassDataType.getSuperclass() ).equals( lClassDataType ) )
          {
            pOut.println( "  if( pInput instanceof " + lSrcPackage + lSubClassDataType.getJavaName() + " )" );
            pOut.println( "  {" );
            pOut.println( "    return convert" + lSubClassDataType.getJavaName() + "( (" + lSrcPackage + lSubClassDataType.getJavaName() + ")pInput );" );
            pOut.println( "  }" );
          }
        }

        pOut.println( "  return null;" );
      }
      else
      {
        if( lClassDataType.isEnum() )
        {
          for( EnumData lEnumData : lClassDataType.getEnumData() )
          {
            pOut.println( "  if( pInput == " + lSrcPackage + lClassDataType.getJavaName() + "." + lEnumData.getJavaName() + ")" );
            pOut.println( "  {" );
            pOut.println( "    return " + lDstPackage + lClassDataType.getJavaName() + "." + lEnumData.getJavaName() + ";" );
            pOut.println( "  }" );
          }
          pOut.println( "  return null;" );
        }
        else
        {
          pOut.println( lDstPackage + lClassDataType.getJavaName() + " lReturn = new " + lDstPackage + "impl." + lClassDataType.getJavaName() + "Impl();" );

          List<FieldData> lFieldDataList = new ArrayList<FieldData>();

          if( lClassDataType.getSuperclass() != null )
          {
            lFieldDataList.addAll( ((ClassDataType)lTypeDataContainer.getClassData( lClassDataType.getSuperclass() )).getFiledDataList() );
          }
          lFieldDataList.addAll( lClassDataType.getFiledDataList() );

          for( FieldData lFieldData : lFieldDataList )
          {
            ClassDataType lFieldClassDataType = _findClassDataType( lTypeDataContainer.getClassData( lFieldData.getJavaType() ).getSqlName(), lTypeDataContainer );

            if( lFieldClassDataType == null )
            {
              pOut.println( "lReturn." + lFieldData.getJavaSetterName() + "( pInput." + lFieldData.getJavaGetterCall() + " );" );
            }
            else
            {
              if( lFieldData.isList() )
              {
                pOut.println( "lReturn." + lFieldData.getJavaGetterCall() + ".addAll( convert" + lFieldClassDataType.getJavaName() + "List( pInput." + lFieldData.getJavaGetterCall() + ") );" );
              }
              else
              {
                pOut.println( "lReturn." + lFieldData.getJavaSetterName() + "( convert" + lFieldClassDataType.getJavaName() + "( pInput." + lFieldData.getJavaGetterCall() + ") );" );
              }
            }
          }
          pOut.println( "" );
          pOut.println( "  return lReturn;" );
        }
      }

      pOut.println( "}" );
      pOut.println( "" );

      if( lClassDataType.isListNeeded() )
      {
        pOut.println( "public static List<" + lDstPackage + lClassDataType.getJavaName() + "> convert" + lClassDataType.getJavaName() + "List( List<" + lSrcPackage + lClassDataType.getJavaName() + "> pInput )" );
        pOut.println( "{" );
        pOut.println( "List<" + lDstPackage + lClassDataType.getJavaName() + "> lReturn = new ArrayList();" );

        pOut.println( "  for( " + lSrcPackage + lClassDataType.getJavaName() + " lValue : pInput )" );
        pOut.println( "  {" );
        pOut.println( "    " + lDstPackage + lClassDataType.getJavaName() + " lConvertedValue = convert" + lClassDataType.getJavaName() + "( lValue  );" );
        pOut.println( "    if( lConvertedValue != null )" );
        pOut.println( "    {" );
        pOut.println( "      lReturn.add( lConvertedValue  );" );
        pOut.println( "    }" );
        pOut.println( "  }" );
        pOut.println( "" );
        pOut.println( "  return lReturn;" );
        pOut.println( "}" );
        pOut.println( "" );
      }
    }

    pOut.println( "}" );
  }

  private static ClassDataType _findClassDataType( String pSqlName, TypeDataContainer pTypeDataContainer )
  {
    for( ClassDataType lClassDataType : pTypeDataContainer.getAllClassDataTypes() )
    {
      if( lClassDataType.getSqlName().equals( pSqlName ) )
      {
        return lClassDataType;
      }
    }

    return null;
  }
}
