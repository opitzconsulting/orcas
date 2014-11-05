package de.opitzconsulting.orcas.ot;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class OtJsonPackageGenerator
{
  public static void main( String[] pArgs ) throws Exception
  {
    PrintStream lPrintStream;

    if( pArgs.length > 0 )
    {
      lPrintStream = new PrintStream( new File( pArgs[0] ) );
    }
    else
    {
      lPrintStream = System.out;
    }

    writePackageHead( lPrintStream, "orig" );
    lPrintStream.println( "" );
    writePackageBody( lPrintStream, "orig" );
    lPrintStream.println( "" );
    writePackageHead( lPrintStream, "syex" );
    lPrintStream.println( "" );
    writePackageBody( lPrintStream, "syex" );

    lPrintStream.close();
  }

  private static void writePackageHead( PrintStream pPrintStream, String pTypePrefix )
  {
    ClassDataType.setTypePrefix( pTypePrefix );
    TypeDataContainer lTypeDataContainer = new ClassDataParser().parse();

    pPrintStream.println( "create or replace package pa_orcas_json_" + pTypePrefix + " is" );

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

    pPrintStream.println( "create or replace package body pa_orcas_json_" + pTypePrefix + " is" );
    pPrintStream.println( "" );
    pPrintStream.println( "  pv_clob clob;" );
    pPrintStream.println( "  pv_text_buffer varchar2(32000);" );
    pPrintStream.println( "" );
    pPrintStream.println( "  procedure flush_buffer" );
    pPrintStream.println( "  is" );
    pPrintStream.println( "  begin" );
    pPrintStream.println( "    dbms_lob.append( pv_clob, pv_text_buffer );" );
    pPrintStream.println( "    pv_text_buffer := null;" );
    pPrintStream.println( "  end;" );
    pPrintStream.println( "" );
    pPrintStream.println( "  procedure add_text( p_input in varchar2 )" );
    pPrintStream.println( "  is" );
    pPrintStream.println( "  begin" );
    pPrintStream.println( "    if( (length(p_input) + length(pv_text_buffer)) > 30000 )" );
    pPrintStream.println( "    then" );
    pPrintStream.println( "      flush_buffer();" );
    pPrintStream.println( "    end if;" );
    pPrintStream.println( "    pv_text_buffer := pv_text_buffer || p_input;" );
    pPrintStream.println( "  end;" );
    pPrintStream.println( "" );
    pPrintStream.println( "  procedure add_newline" );
    pPrintStream.println( "  is" );
    pPrintStream.println( "  begin" );
    pPrintStream.println( "    add_text( chr(13) || chr(10) );" );
    pPrintStream.println( "  end;" );
    pPrintStream.println( "" );

    for( ClassDataType lClassDataType : OracleOtGenerator.orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      pPrintStream.println( "procedure add_" + lClassDataType.getMaxLengthName() + "( p_input in ot_" + pTypePrefix + "_" + lClassDataType.getMaxLengthName() + " );" );

      if( lClassDataType.isListNeeded() )
      {
        pPrintStream.println( "procedure add_" + lClassDataType.getMaxLengthName() + "_list( p_input in ct_" + pTypePrefix + "_" + lClassDataType.getMaxLengthName() + "_list );" );
      }
    }

    for( ClassDataType lClassDataType : OracleOtGenerator.orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      String lNullHandling = "  if( p_input is null ) then add_text( 'null' ); return; end if;";

      pPrintStream.println( "procedure add_" + lClassDataType.getMaxLengthName() + "( p_input in ot_" + pTypePrefix + "_" + lClassDataType.getMaxLengthName() + " )" );
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
            pPrintStream.println( "    add_" + lSubClassDataType.getMaxLengthName() + "( treat(p_input as ot_" + pTypePrefix + "_" + lSubClassDataType.getMaxLengthName() + ") );" );
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
            pPrintStream.println( "    add_text( '\"" + lEnumData.getName() + "\"' );" );
            pPrintStream.println( "    return;" );
            pPrintStream.println( "  end if;" );
          }

          pPrintStream.println( "  raise_application_error( -20000, 'enum not found: ' || p_input.i_name );" );
        }
        else
        {
          pPrintStream.println( "  add_newline();" );
          pPrintStream.println( "  add_text( '{' );" );

          List<FieldData> lFieldDataList = new ArrayList<FieldData>();

          if( lClassDataType.getSuperclass() != null )
          {
            lFieldDataList.addAll( ((ClassDataType)lTypeDataContainer.getClassData( lClassDataType.getSuperclass() )).getFiledDataList() );
          }
          lFieldDataList.addAll( lClassDataType.getFiledDataList() );

          pPrintStream.println( "  add_text( '\"type\": \"" + lClassDataType.getJavaName() + "\"' );" );
          for( FieldData lFieldData : lFieldDataList )
          {
            pPrintStream.println( "  if( p_input." + lFieldData.getSqlName() + " is not null )" );
            pPrintStream.println( "  then" );

            pPrintStream.println( "  add_text( ',' );" );

            pPrintStream.println( "  add_text( '\"" + lFieldData.getJavaName() + "\": ' );" );

            ClassDataType lFieldClassDataType = _findClassDataType( lTypeDataContainer.getClassData( lFieldData.getJavaType() ).getSqlName(), lTypeDataContainer );

            if( lFieldClassDataType == null )
            {
              boolean lIsRequiresValueParenthesis = isRequiresValueParenthesis( lFieldData.getJavaType() );

              pPrintStream.println( "  if( p_input." + lFieldData.getSqlName() + " is null )" );
              pPrintStream.println( "  then" );
              pPrintStream.println( "    add_text( 'null' );" );
              pPrintStream.println( "  else" );
              if( lIsRequiresValueParenthesis )
              {
                pPrintStream.println( "    add_text( '\"' || p_input." + lFieldData.getSqlName() + " || '\"' );" );
              }
              else
              {
                pPrintStream.println( "    add_text( p_input." + lFieldData.getSqlName() + " );" );
              }
              pPrintStream.println( "  end if;" );
            }
            else
            {
              if( lFieldData.isList() )
              {
                pPrintStream.println( "add_" + lFieldClassDataType.getMaxLengthName() + "_list( p_input." + lFieldData.getSqlName() + ");" );
              }
              else
              {
                pPrintStream.println( "add_" + lFieldClassDataType.getMaxLengthName() + "( p_input." + lFieldData.getSqlName() + ");" );
              }
            }

            pPrintStream.println( "end if;" );
          }
          pPrintStream.println( "  add_text( '}' );" );
          pPrintStream.println( "  add_newline();" );
          pPrintStream.println( "" );
        }

        pPrintStream.println( "end;" );
      }

      pPrintStream.println( "" );

      if( lClassDataType.isListNeeded() )
      {
        pPrintStream.println( "procedure add_" + lClassDataType.getMaxLengthName() + "_list( p_input in ct_" + pTypePrefix + "_" + lClassDataType.getMaxLengthName() + "_list )" );
        pPrintStream.println( "is" );
        pPrintStream.println( "begin" );
        pPrintStream.println( lNullHandling );
        pPrintStream.println( "  add_text( '[' );" );
        pPrintStream.println( "  for i in 1..p_input.count()" );
        pPrintStream.println( "  loop" );
        pPrintStream.println( "    if( i > 1 ) then add_text( ',' ); end if;" );
        pPrintStream.println( "    add_" + lClassDataType.getMaxLengthName() + "( p_input(i) );" );
        pPrintStream.println( "  end loop;" );
        pPrintStream.println( "  add_text( ']' );" );
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

    pPrintStream.print( "function get_" + pTypeName + "( p_input in " + lTypeName + lTypePrefix + " ) return clob" );
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
      pPrintStream.println( "  pv_text_buffer := null;" );
      pPrintStream.println( "  add_" + pTypeName + lTypePrefix + "( p_input );" );
      pPrintStream.println( "  flush_buffer();" );
      pPrintStream.println( "  return pv_clob;" );
      pPrintStream.println( "end;" );
    }
  }

  private static boolean isRequiresValueParenthesis( Class pJavaType )
  {
    return pJavaType.equals( String.class );
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
