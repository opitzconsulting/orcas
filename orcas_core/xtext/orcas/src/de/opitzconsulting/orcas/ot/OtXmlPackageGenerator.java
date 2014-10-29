package de.opitzconsulting.orcas.ot;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OtXmlPackageGenerator
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

    writePackageHead( lPrintStream, pArgs[1] );
    lPrintStream.println( "" );
    writePackageBody( lPrintStream, pArgs[1] );

    lPrintStream.close();
  }

  private static void writePackageHead( PrintStream pPrintStream, String pTypePrefix )
  {
    ClassDataType.setTypePrefix( pTypePrefix );
    TypeDataContainer lTypeDataContainer = new ClassDataParser().parse();

    pPrintStream.println( "create or replace package pa_orcas_xml_" + pTypePrefix + " is" );

    for( ClassDataType lClassDataType : OracleOtGenerator.orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      generateGetMethod( pPrintStream, pTypePrefix, lClassDataType.getMaxLengthName(), true, false );

      if( lClassDataType.isListNeeded() )
      {
        generateGetMethod( pPrintStream, pTypePrefix, lClassDataType.getMaxLengthName(), true, true );
      }
    }

    pPrintStream.println( "end;" );
    pPrintStream.println( "/" );
  }

  private static void writePackageBody( PrintStream pPrintStream, String pTypePrefix )
  {
    ClassDataType.setTypePrefix( pTypePrefix );
    TypeDataContainer lTypeDataContainer = new ClassDataParser().parse();

    pPrintStream.println( "create or replace package body pa_orcas_xml_" + pTypePrefix + " is" );
    pPrintStream.println( "" );
    pPrintStream.println( "  pv_clob clob;" );
    pPrintStream.println( "  pv_text_buffer varchar2(32000);" );
    pPrintStream.println( "  pv_text_buffer_length number := 0;" );
    pPrintStream.println( "" );
    pPrintStream.println( "  procedure init_buffer" );
    pPrintStream.println( "  is" );
    pPrintStream.println( "  begin" );
    pPrintStream.println( "    pv_text_buffer := null;" );
    pPrintStream.println( "    pv_text_buffer_length := 0;" );
    pPrintStream.println( "  end;" );
    pPrintStream.println( "" );
    pPrintStream.println( "  procedure flush_buffer" );
    pPrintStream.println( "  is" );
    pPrintStream.println( "  begin" );
    pPrintStream.println( "    dbms_lob.append( pv_clob, pv_text_buffer );" );
    pPrintStream.println( "    init_buffer();" );
    pPrintStream.println( "  end;" );
    pPrintStream.println( "" );
    pPrintStream.println( "  procedure add_text_to_buffer( p_input in varchar2 )" );
    pPrintStream.println( "  is" );
    pPrintStream.println( "  begin" );
    pPrintStream.println( "    if( (length(p_input) + pv_text_buffer_length) > 30000 )" );
    pPrintStream.println( "    then" );
    pPrintStream.println( "      flush_buffer();" );
    pPrintStream.println( "    end if;" );
    pPrintStream.println( "    pv_text_buffer := pv_text_buffer || p_input;" );
    pPrintStream.println( "    pv_text_buffer_length := pv_text_buffer_length + length(p_input);" );
    pPrintStream.println( "  end;" );
    pPrintStream.println( "" );
    pPrintStream.println( "  procedure add_text( p_input in varchar2, p_indent in number )" );
    pPrintStream.println( "  is" );
    pPrintStream.println( "  begin" );
    pPrintStream.println( "    if( p_indent is not null )" );
    pPrintStream.println( "    then" );
    pPrintStream.println( "      add_text_to_buffer( lpad( p_input, length( p_input ) + p_indent ) ); " );
    pPrintStream.println( "    else" );
    pPrintStream.println( "      add_text_to_buffer( p_input ); " );
    pPrintStream.println( "    end if;" );
    pPrintStream.println( "  end;" );
    pPrintStream.println( "" );
    pPrintStream.println( "" );
    pPrintStream.println( "  procedure add_text_escaped( p_input in varchar2, p_indent in number )" );
    pPrintStream.println( "  is" );
    pPrintStream.println( "  begin" );
    pPrintStream.println( "    add_text( dbms_xmlgen.convert( p_input ), p_indent );" );
    pPrintStream.println( "  end;" );
    pPrintStream.println( "" );
    pPrintStream.println( "  procedure add_newline( p_indent in number )" );
    pPrintStream.println( "  is" );
    pPrintStream.println( "  begin" );
    pPrintStream.println( "    if( p_indent is not null )" );
    pPrintStream.println( "    then" );
    pPrintStream.println( "      add_text( chr(13) || chr(10), 0 );" );
    pPrintStream.println( "    end if;" );
    pPrintStream.println( "  end;" );
    pPrintStream.println( "" );

    for( ClassDataType lClassDataType : OracleOtGenerator.orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      pPrintStream.println( "procedure add_" + lClassDataType.getMaxLengthName() + "( p_input in ot_" + pTypePrefix + "_" + lClassDataType.getMaxLengthName() + ", p_indent in number );" );

      if( lClassDataType.isListNeeded() )
      {
        pPrintStream.println( "procedure add_" + lClassDataType.getMaxLengthName() + "_list( p_input in ct_" + pTypePrefix + "_" + lClassDataType.getMaxLengthName() + "_list, p_indent in number );" );
      }
    }

    for( ClassDataType lClassDataType : OracleOtGenerator.orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      String lNullHandling = "  if( p_input is null ) then add_text( 'null', p_indent ); return; end if;";

      pPrintStream.println( "procedure add_" + lClassDataType.getMaxLengthName() + "( p_input in ot_" + pTypePrefix + "_" + lClassDataType.getMaxLengthName() + ", p_indent in number )" );
      pPrintStream.println( "is" );
      if( lClassDataType.isHasSubclasses() )
      {
        pPrintStream.println( "begin" );
        pPrintStream.println( lNullHandling );

        for( ClassDataType lSubClassDataType : lTypeDataContainer.getAllClassDataTypes() )
        {
          if( lSubClassDataType.getSuperclass() != null && lTypeDataContainer.getClassData( lSubClassDataType.getSuperclass() ).equals( lClassDataType ) )
          {
            pPrintStream.println( "  if( p_input is of (ot_" + pTypePrefix + "_" + lSubClassDataType.getMaxLengthName() + ") )" );
            pPrintStream.println( "  then" );
            pPrintStream.println( "    add_" + lSubClassDataType.getMaxLengthName() + "( treat(p_input as ot_" + pTypePrefix + "_" + lSubClassDataType.getMaxLengthName() + "), p_indent );" );
            pPrintStream.println( "    return;" );
            pPrintStream.println( "  end if;" );
          }
        }

        pPrintStream.println( "  raise_application_error( -20000, 'cant find class for input' );" );
        pPrintStream.println( "end;" );
      }
      else
      {
        pPrintStream.println( "begin" );
        pPrintStream.println( lNullHandling );
        if( lClassDataType.isEnum() )
        {
          for( EnumData lEnumData : lClassDataType.getEnumData() )
          {
            pPrintStream.println( "  if( '" + lEnumData.getName() + "' = p_input.i_name )" );
            pPrintStream.println( "  then" );
            pPrintStream.println( "    add_text( '" + lEnumData.getName() + "', p_indent );" );
            pPrintStream.println( "    return;" );
            pPrintStream.println( "  end if;" );
          }

          pPrintStream.println( "  raise_application_error( -20000, 'enum not found: ' || p_input.i_name );" );
        }
        else
        {
          List<FieldData> lFieldDataList = new ArrayList<FieldData>();

          if( lClassDataType.getSuperclass() != null )
          {
            lFieldDataList.addAll( ((ClassDataType)lTypeDataContainer.getClassData( lClassDataType.getSuperclass() )).getFiledDataList() );
          }
          lFieldDataList.addAll( lClassDataType.getFiledDataList() );

          pPrintStream.println( "  add_text( '<" + lClassDataType.getJavaName() + ">', p_indent );" );
          pPrintStream.println( "  add_newline( p_indent );" );
          for( FieldData lFieldData : sortFieldDataList( lFieldDataList ) )
          {
            ClassDataType lFieldClassDataType = _findClassDataType( lTypeDataContainer.getClassData( lFieldData.getJavaType() ).getSqlName(), lTypeDataContainer );

            if( lFieldData.isList() )
            {
              pPrintStream.println( "  if( p_input." + lFieldData.getSqlName() + " is not null and p_input." + lFieldData.getSqlName() + ".count != 0 )" );
            }
            else
            {
              pPrintStream.println( "  if( p_input." + lFieldData.getSqlName() + " is not null )" );
            }
            pPrintStream.println( "  then" );

            pPrintStream.println( "  add_text( '<" + lFieldData.getJavaName() + ">', p_indent + 2 );" );

            boolean lIsSingleLine = lFieldClassDataType == null || lFieldClassDataType.isEnum();

            if( !lIsSingleLine )
            {
              pPrintStream.println( "  add_newline( p_indent );" );
            }

            if( lFieldClassDataType == null )
            {
              pPrintStream.println( "  add_text_escaped( p_input." + lFieldData.getSqlName() + ", 0 );" );
            }
            else
            {
              if( lFieldData.isList() )
              {
                pPrintStream.println( "add_" + lFieldClassDataType.getMaxLengthName() + "_list( p_input." + lFieldData.getSqlName() + ", p_indent + 4 );" );
              }
              else
              {
                pPrintStream.println( "add_" + lFieldClassDataType.getMaxLengthName() + "( p_input." + lFieldData.getSqlName() + ", " + (lIsSingleLine ? "0" : "p_indent + 4") + " );" );
              }
            }
            pPrintStream.println( "  add_text( '</" + lFieldData.getJavaName() + ">', " + (lIsSingleLine ? "0" : "p_indent + 2 ") + " );" );

            pPrintStream.println( "  add_newline( p_indent );" );

            pPrintStream.println( "end if;" );
          }
          pPrintStream.println( "  add_text( '</" + lClassDataType.getJavaName() + ">', p_indent );" );
          pPrintStream.println( "  add_newline( p_indent );" );

          pPrintStream.println( "" );
        }

        pPrintStream.println( "end;" );
      }

      pPrintStream.println( "" );

      if( lClassDataType.isListNeeded() )
      {
        pPrintStream.println( "procedure add_" + lClassDataType.getMaxLengthName() + "_list( p_input in ct_" + pTypePrefix + "_" + lClassDataType.getMaxLengthName() + "_list, p_indent in number )" );
        pPrintStream.println( "is" );
        pPrintStream.println( "begin" );
        pPrintStream.println( lNullHandling );
        pPrintStream.println( "  for i in 1..p_input.count()" );
        pPrintStream.println( "  loop" );
        pPrintStream.println( "    add_" + lClassDataType.getMaxLengthName() + "( p_input(i), p_indent );" );
        pPrintStream.println( "  end loop;" );
        pPrintStream.println( "" );
        pPrintStream.println( "end;" );
        pPrintStream.println( "" );
      }
    }

    for( ClassDataType lClassDataType : OracleOtGenerator.orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      generateGetMethod( pPrintStream, pTypePrefix, lClassDataType.getMaxLengthName(), false, false );

      if( lClassDataType.isListNeeded() )
      {
        generateGetMethod( pPrintStream, pTypePrefix, lClassDataType.getMaxLengthName(), false, true );
      }
    }

    pPrintStream.println( "end;" );
    pPrintStream.println( "/" );
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

  private static void generateGetMethod( PrintStream pPrintStream, String pTypePrefix, String pTypeName, boolean pHeaderOnly, boolean pIsList )
  {
    String lTypeName;
    String lTypePrefix;
    if( pIsList )
    {
      lTypeName = "ct_" + pTypePrefix + "_" + pTypeName;
      lTypePrefix = "_list";
    }
    else
    {
      lTypeName = "ot_" + pTypePrefix + "_" + pTypeName;
      lTypePrefix = "";
    }

    pPrintStream.print( "function get_" + pTypeName + "( p_input in " + lTypeName + lTypePrefix + ", p_format in number default 1 ) return clob" );
    if( pHeaderOnly )
    {
      pPrintStream.println( ";" );
    }
    else
    {
      pPrintStream.println( "" );
      pPrintStream.println( "is" );
      pPrintStream.println( "begin" );
      pPrintStream.println( "  dbms_lob.createtemporary( pv_clob, true );" );
      pPrintStream.println( "  init_buffer();" );
      pPrintStream.println( "  if( p_format = 1 )" );
      pPrintStream.println( "  then" );
      pPrintStream.println( "    add_" + pTypeName + lTypePrefix + "( p_input, 0 );" );
      pPrintStream.println( "  else" );
      pPrintStream.println( "    add_" + pTypeName + lTypePrefix + "( p_input, null );" );
      pPrintStream.println( "  end if;" );
      pPrintStream.println( "  flush_buffer();" );
      pPrintStream.println( "  return pv_clob;" );
      pPrintStream.println( "end;" );
    }
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
