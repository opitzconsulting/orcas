package de.opitzconsulting.orcas.ot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JavaXmlGenerator extends JavaGenerator
{
  public static void main( String[] pArgs )
  {
    new JavaXmlGenerator( pArgs ).export();
  }

  @Override
  public void export()
  {
    writeJavaFile( "XmlExport.java", new DoWithWriter()
    {
      public void write( JavaPrettyWriter pJavaPrettyWriter )
      {
        writeXmlExport( pJavaPrettyWriter, "syex" );
      }
    } );
  }

  protected JavaXmlGenerator( String[] pArgs )
  {
    super( pArgs );
  }

  private void writeXmlExport( JavaPrettyWriter pOut, String pTypePrefix )
  {
    TypeDataContainer lTypeDataContainer = new ClassDataParser().parse();

    writePackage( pOut );
    pOut.println();
    pOut.println( "import java.util.*;" );
    pOut.println( "import de.opitzconsulting.orcasDsl.*;" );
    pOut.println( "import org.apache.commons.lang3.StringEscapeUtils;" );    
    pOut.println();
    pOut.print( "public class XmlExport" );
    pOut.println( "{" );

    pOut.println( "" );
    pOut.println( "  private StringBuilder _stringBuilder;" );
    pOut.println( "" );
    pOut.println( "  private void initStringBuilder()" );
    pOut.println( "  {" );
    pOut.println( "    _stringBuilder = new StringBuilder();" );
    pOut.println( "  }" );
    pOut.println( "" );
    pOut.println( "  private void appendToBuffer( String pInput )" );
    pOut.println( "  {" );
    pOut.println( "    _stringBuilder.append( pInput );" );
    pOut.println( "  }" );
    pOut.println( "" );
    pOut.println( "  private void addText( String pInput, Integer pIndent )" );
    pOut.println( "  {" );
    pOut.println( "    if( pIndent != null )" );
    pOut.println( "    {" );
    pOut.println( "    for( int i=0; i<pIndent; i++ )" );
    pOut.println( "    {" );
    pOut.println( "    _stringBuilder.append( ' ' );" );
    pOut.println( "    }" );
    pOut.println( "    }" );
    pOut.println( "    appendToBuffer( pInput ); " );
    pOut.println( "  }" );
    pOut.println( "" );
    pOut.println( "" );
    pOut.println( "  private void addText( boolean pInput, Integer pIndent )" );
    pOut.println( "  {" );
    pOut.println( "    if( pInput )" );
    pOut.println( "    {" );
    pOut.println( "      addText( \"true\", pIndent );" );
    pOut.println( "    }" );
    pOut.println( "    else " );
    pOut.println( "    {" );
    pOut.println( "      addText( \"false\", pIndent );" );
    pOut.println( "    }" );
    pOut.println( "  }" );
    pOut.println( "" );
    pOut.println( "  private void addTextEscaped( String pInput, Integer pIndent )" );
    pOut.println( "  {" );
    pOut.println( "    addText( StringEscapeUtils.escapeXml( pInput ), pIndent );" );
    pOut.println( "  }" );
    pOut.println( "" );
    pOut.println( "  private void addTextEscaped( int pInput, Integer pIndent )" );
    pOut.println( "  {" );
    pOut.println( "    addText( \"\"+pInput, pIndent );" );
    pOut.println( "  }" );
    pOut.println( "" );
    pOut.println( "  private void addNewline( Integer pIndent )" );
    pOut.println( "  {" );
    pOut.println( "    if( pIndent != null )" );
    pOut.println( "    {" );
    pOut.println( "      addText( \"\\n\", 0 );" );
    pOut.println( "    }" );
    pOut.println( "  }" );
    pOut.println( "" );

    for( ClassDataType lClassDataType : OracleOtGenerator.orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      String lNullHandling = "  if( pInput == null ) { addText( \"null\", pIndent ); return; }";

      pOut.println( "private void add" + lClassDataType.getJavaName() + "( " + lClassDataType.getJavaName() + " pInput , Integer pIndent )" );
      if( lClassDataType.isHasSubclasses() )
      {
        pOut.println( "{" );
        pOut.println( lNullHandling );

        for( ClassDataType lSubClassDataType : lTypeDataContainer.getAllClassDataTypes() )
        {
          if( lSubClassDataType.getSuperclass() != null && lTypeDataContainer.getClassData( lSubClassDataType.getSuperclass() ).equals( lClassDataType ) )
          {
            pOut.println( "  if( pInput instanceof " + lSubClassDataType.getJavaName() + " )" );
            pOut.println( "  {" );
            pOut.println( "    add" + lSubClassDataType.getJavaName() + "( (" + lSubClassDataType.getJavaName() + ") pInput, pIndent );" );
            pOut.println( "    return;" );
            pOut.println( "  }" );
          }
        }

        pOut.println( "  throw new RuntimeException( \"cant find class for input\" );" );
        pOut.println( "}" );
      }
      else
      {
        pOut.println( "{" );
        pOut.println( lNullHandling );
        if( lClassDataType.isEnum() )
        {
          for( EnumData lEnumData : lClassDataType.getEnumData() )
          {
            pOut.println( "  if( \"" + lEnumData.getName() + "\".equals( pInput.getName() ) )" );
            pOut.println( "  {" );
            pOut.println( "    addText( \"" + lEnumData.getName() + "\", pIndent );" );
            pOut.println( "    return;" );
            pOut.println( "  }" );
          }

          pOut.println( "  throw new RuntimeException( \"enum not found: \" + pInput.getName()  );" );
        }
        else
        {
          List<FieldData> lFieldDataList = new ArrayList<FieldData>();

          if( lClassDataType.getSuperclass() != null )
          {
            lFieldDataList.addAll( ((ClassDataType)lTypeDataContainer.getClassData( lClassDataType.getSuperclass() )).getFiledDataList() );
          }
          lFieldDataList.addAll( lClassDataType.getFiledDataList() );

          pOut.println( "  addText( \"<" + lClassDataType.getJavaName() + ">\", pIndent );" );
          pOut.println( "  addNewline( pIndent );" );
          for( FieldData lFieldData : sortFieldDataList( lFieldDataList ) )
          {
            ClassDataType lFieldClassDataType = _findClassDataType( lTypeDataContainer.getClassData( lFieldData.getJavaType() ).getSqlName(), lTypeDataContainer );

            if( !lFieldData.isFlag() )
            {
              if( lFieldData.isList() )
              {
                pOut.println( "  if( pInput." + lFieldData.getJavaGetterCall() + " != null && !pInput." + lFieldData.getJavaGetterCall() + ".isEmpty() )" );
              }
              else
              {
                if( lFieldData.getJavaType() == int.class )
                {
                  pOut.println( "  if( pInput." + lFieldData.getJavaGetterCall() + " != -1 && pInput." + lFieldData.getJavaGetterCall() + " != 0 )" );
                }
                else
                {
                  pOut.println( "  if( pInput." + lFieldData.getJavaGetterCall() + " != null )" );
                }
              }
            }
            pOut.println( "  {" );

            pOut.println( "  addText( \"<" + lFieldData.getJavaName() + ">\", pIndent + 2 );" );

            boolean lIsSingleLine = lFieldClassDataType == null || lFieldClassDataType.isEnum();

            if( !lIsSingleLine )
            {
              pOut.println( "  addNewline( pIndent );" );
            }

            if( lFieldClassDataType == null )
            {
              if( lFieldData.isFlag() )
              {
                pOut.println( "  addText( pInput." + lFieldData.getJavaGetterCall() + ", 0 );" );
              }
              else
              {
                pOut.println( "  addTextEscaped( pInput." + lFieldData.getJavaGetterCall() + ", 0 );" );
              }
            }
            else
            {
              if( lFieldData.isList() )
              {
                pOut.println( "add" + lFieldClassDataType.getJavaName() + "List( pInput." + lFieldData.getJavaGetterCall() + ", pIndent + 4 );" );
              }
              else
              {
                pOut.println( "add" + lFieldClassDataType.getJavaName() + "( pInput." + lFieldData.getJavaGetterCall() + ", " + (lIsSingleLine ? "0" : "pIndent + 4") + " );" );
              }
            }
            pOut.println( "  addText( \"</" + lFieldData.getJavaName() + ">\", " + (lIsSingleLine ? "0" : "pIndent + 2 ") + " );" );

            pOut.println( "  addNewline( pIndent );" );

            pOut.println( "}" );
          }
          pOut.println( "  addText( \"</" + lClassDataType.getJavaName() + ">\", pIndent );" );
          pOut.println( "  addNewline( pIndent );" );

          pOut.println( "" );
        }

        pOut.println( "}" );
      }

      pOut.println( "" );

      if( lClassDataType.isListNeeded() )
      {
        pOut.println( "private void add" + lClassDataType.getJavaName() + "List( " + lClassDataType.getJavaNameCollection() + " pInput , Integer pIndent )" );
        pOut.println( "{" );
        pOut.println( lNullHandling );
        pOut.println( "  for( " + lClassDataType.getJavaName() + " lValue : pInput )" );
        pOut.println( "  {" );
        pOut.println( "    add" + lClassDataType.getJavaName() + "( lValue, pIndent );" );
        pOut.println( "  }" );
        pOut.println( "" );
        pOut.println( "}" );
        pOut.println( "" );
      }
    }

    for( ClassDataType lClassDataType : OracleOtGenerator.orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      generateGetMethod( pOut, pTypePrefix, lClassDataType, false );

      if( lClassDataType.isListNeeded() )
      {
        //        generateGetMethod( pOut, pTypePrefix, lClassDataType, true );
      }
    }

    pOut.println( "}" );
  }

  private static List<FieldData> sortFieldDataList( List<FieldData> pFieldDataList )
  {
    List<FieldData> lReturn = new ArrayList<FieldData>( pFieldDataList );

    Collections.sort( lReturn, new Comparator<FieldData>()
    {
      private String getSortString( FieldData pFieldData )
      {
        int lNumber;

        if( pFieldData.getJavaName().toLowerCase().equals( "name" ) )
        {
          lNumber = 0;
        }
        else
        {
          if( pFieldData.isList() )
          {
            lNumber = 9;
          }
          else
          {
            if( pFieldData.getJavaName().toLowerCase().contains( "name" ) )
            {
              lNumber = 1;
            }
            else
            {
              lNumber = 5;
            }
          }
        }

        return lNumber + " " + pFieldData.getJavaName();
      }

      public int compare( FieldData pFieldData1, FieldData pFieldData2 )
      {
        return getSortString( pFieldData1 ).compareTo( getSortString( pFieldData2 ) );
      }
    } );

    return lReturn;
  }

  private static void generateGetMethod( JavaPrettyWriter pPrintStream, String pTypePrefix, ClassDataType pClassDataType, boolean pIsList )
  {
    pPrintStream.println( "public String get" + pClassDataType.getJavaName() + "( " + pClassDataType.getJavaName() + " pInput, boolean pFormat )" );
    pPrintStream.println( "{" );
    pPrintStream.println( "  initStringBuilder();" );
    pPrintStream.println( "  if( pFormat )" );
    pPrintStream.println( "  {" );
    pPrintStream.println( "    add" + pClassDataType.getJavaName() + "( pInput, 0 );" );
    pPrintStream.println( "  }" );
    pPrintStream.println( "  else " );
    pPrintStream.println( "  {" );
    pPrintStream.println( "    add" + pClassDataType.getJavaName() + "( pInput, null );" );
    pPrintStream.println( "  }" );
    pPrintStream.println( "  return _stringBuilder.toString();" );
    pPrintStream.println( "}" );
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

  @Override
  protected String getPackageName()
  {
    return "de.opitzconsulting.orcas.syex.xml";
  }
}
