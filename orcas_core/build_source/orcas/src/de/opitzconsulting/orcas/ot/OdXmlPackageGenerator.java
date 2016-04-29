package de.opitzconsulting.orcas.ot;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OdXmlPackageGenerator
{
  public static void main( String[] pArgs ) throws Exception
  {
    String lTypePrefix = "orig";
    PrintStream lPrintStream;

    if( pArgs.length > 0 )
    {
      lPrintStream = new PrintStream( new File( pArgs[0] ) );

      if( pArgs.length > 1 )
      {
        lTypePrefix = pArgs[1];
      }
    }
    else
    {
      lPrintStream = System.out;
    }

    PlSqlPrettyWriter lPlSqlPrettyWriter = new PlSqlPrettyWriter( lPrintStream );

    ClassDataType.setTypePrefix( lTypePrefix );
    writePackageHead( lPlSqlPrettyWriter, lTypePrefix );
    lPrintStream.println( "" );
    writePackageBody( lPlSqlPrettyWriter, lTypePrefix );

    lPrintStream.close();
  }

  private static void writePackageHead( PlSqlPrettyWriter pPrintStream, String pTypePrefix )
  {
    ClassDataType.setTypePrefix( pTypePrefix );
    TypeDataContainer lTypeDataContainer = new ClassDataParser().parse();

    pPrintStream.println( "create or replace package pa_orcas_xml_" + pTypePrefix + "_od is" );

    for( ClassDataType lClassDataType : OracleOtGenerator.orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      if( !lClassDataType.isEnum() )
      {
        generateGetMethod( lClassDataType, pPrintStream, pTypePrefix, lClassDataType.getMaxLengthName(), true, false );

        if( lClassDataType.isListNeeded() )
        {
          generateGetMethod( lClassDataType, pPrintStream, pTypePrefix, lClassDataType.getMaxLengthName(), true, true );
        }
      }
    }

    pPrintStream.println( "end;" );
    pPrintStream.println( "/" );
  }

  private static void writePackageBody( PlSqlPrettyWriter pPrintStream, String pTypePrefix )
  {
    ClassDataType.setTypePrefix( pTypePrefix );
    TypeDataContainer lTypeDataContainer = new ClassDataParser().parse();

    pPrintStream.println( "create or replace package body pa_orcas_xml_" + pTypePrefix + "_od is" );
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
    pPrintStream.println( "  procedure add_boolean( p_input in number, p_indent in number )" );
    pPrintStream.println( "  is" );
    pPrintStream.println( "  begin" );
    pPrintStream.println( "    if( p_input = 1 )" );
    pPrintStream.println( "    then" );
    pPrintStream.println( "      add_text( 'true', p_indent );" );
    pPrintStream.println( "    else" );
    pPrintStream.println( "      add_text( 'false', p_indent );" );
    pPrintStream.println( "    end if;" );
    pPrintStream.println( "  end;" );
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
      if( !lClassDataType.isEnum() )
      {
        pPrintStream.println( "procedure add_" + lClassDataType.getMaxLengthName() + "( p_input in " + lClassDataType.getDiffSqlName() + ",  p_include_equal in number, p_indent in number );" );

        pPrintStream.println( "procedure add_" + lClassDataType.getMaxLengthName() + "_list( p_input in " + lClassDataType.getDiffSqlNameCollection() + ", p_include_equal in number, p_indent in number );" );
      }
    }

    for( ClassDataType lClassDataType : OracleOtGenerator.orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      if( !lClassDataType.isEnum() )
      {
        String lNullHandling = "  if( p_input is null ) then add_text( 'null', p_indent ); return; end if;";

        pPrintStream.println( "procedure add_" + lClassDataType.getMaxLengthName() + "( p_input in " + lClassDataType.getDiffSqlName() + ", p_include_equal in number, p_indent in number )" );
        pPrintStream.println( "is" );
        if( lClassDataType.isHasSubclasses() )
        {
          pPrintStream.println( "begin" );
          pPrintStream.println( lNullHandling );

          for( ClassDataType lSubClassDataType : lTypeDataContainer.getAllClassDataTypes() )
          {
            if( lSubClassDataType.getSuperclass() != null && lTypeDataContainer.getClassData( lSubClassDataType.getSuperclass() ).equals( lClassDataType ) )
            {
              pPrintStream.println( "  if( p_input is of (" + lSubClassDataType.getDiffSqlName() + ") )" );
              pPrintStream.println( "  then" );
              pPrintStream.println( "    add_" + lSubClassDataType.getMaxLengthName() + "( treat(p_input as " + lSubClassDataType.getDiffSqlName() + "), p_include_equal, p_indent );" );
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
            pPrintStream.println( " if( p_input." + lFieldData.getDiffEqualFlagSqlName() + " = 0 or p_include_equal = 1 )" );
            pPrintStream.println( " then" );

            ClassData lClassData = lTypeDataContainer.getClassData( lFieldData.getJavaType() );

            if( lClassData.isAtomicValue() )
            {
              addAtomicField( pPrintStream, lFieldData, true, lClassData );
              addAtomicField( pPrintStream, lFieldData, false, lClassData );
            }
            else
            {
              ClassDataType lFieldClassDataType = _findClassDataType( lClassData.getSqlName(), lTypeDataContainer );

              pPrintStream.println( "  add_text( '<" + lFieldData.getJavaName() + ">', p_indent + 2 );" );
              pPrintStream.println( "  add_newline( p_indent );" );

              if( !lFieldClassDataType.isHasSubclasses() && !lFieldData.isList() )
              {
                pPrintStream.println( "  add_" + lFieldClassDataType.getMaxLengthName() + "( p_input." + lFieldData.getDiffChangeSqlName() + ", p_include_equal, p_indent + 4 );" );
              }
              else
              {
                if( !lFieldClassDataType.isHasSubclasses() )
                {
                  pPrintStream.println( "  add_" + lFieldClassDataType.getMaxLengthName() + "_list( p_input." + lFieldData.getDiffChangeSqlName() + ", p_include_equal, p_indent + 4 );" );
                }
                else
                {
                  List<ClassDataType> lAllClassDataSubTypes = OracleOdGenerator.getAllClassDataSubTypes( lFieldClassDataType, lTypeDataContainer );

                  if( lFieldData.isList() )
                  {
                    for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
                    {
                      pPrintStream.println( "  add_" + lClassDataSubType.getMaxLengthName() + "_list( p_input." + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + ", p_include_equal, p_indent + 4 );" );
                    }
                  }
                  else
                  {
                    for( ClassDataType lClassDataSubType : lAllClassDataSubTypes )
                    {
                      pPrintStream.println( "  add_" + lClassDataSubType.getMaxLengthName() + "( p_input." + lFieldData.getDiffChangeSqlNameForSubType( lClassDataSubType ) + ", p_include_equal, p_indent + 4 );" );
                    }
                  }
                }
              }
            }

            pPrintStream.println( "  add_text( '<" + lFieldData.getJavaName() + "_equal>', p_indent + 2 );" );
            pPrintStream.println( "  add_boolean( p_input." + lFieldData.getDiffEqualFlagSqlName() + ", 0 );" );
            pPrintStream.println( "  add_text( '</" + lFieldData.getJavaName() + "_equal>', 0 );" );
            pPrintStream.println( "  add_newline( p_indent );" );
            pPrintStream.println( "end if;" );
          }

          pPrintStream.println( "  if( p_input.is_parent_index_equal = 0 or p_include_equal = 1 )" );
          pPrintStream.println( "  then" );
          pPrintStream.println( "  if( p_input.old_parent_index is not null )" );
          pPrintStream.println( "  then" );
          pPrintStream.println( "  add_text( '<old_parent_index>', p_indent + 2 );" );
          pPrintStream.println( "  add_text_escaped( p_input.old_parent_index, 0 );" );
          pPrintStream.println( "  add_text( '</old_parent_index>', 0 );" );
          pPrintStream.println( "  add_newline( p_indent );" );
          pPrintStream.println( "  end if;" );
          pPrintStream.println( "  if( p_input.new_parent_index is not null )" );
          pPrintStream.println( "  then" );
          pPrintStream.println( "  add_text( '<new_parent_index>', p_indent + 2 );" );
          pPrintStream.println( "  add_text_escaped( p_input.new_parent_index, 0 );" );
          pPrintStream.println( "  add_text( '</new_parent_index>', 0 );" );
          pPrintStream.println( "  add_newline( p_indent );" );
          pPrintStream.println( "  end if;" );
          pPrintStream.println( "  add_text( '<is_parent_index_equal>', p_indent + 2 );" );
          pPrintStream.println( "  add_boolean( p_input.is_parent_index_equal, 0 );" );
          pPrintStream.println( "  add_text( '</is_parent_index_equal>', 0 );" );
          pPrintStream.println( "  add_newline( p_indent );" );
          pPrintStream.println( "  end if;" );

          pPrintStream.println( "  if( p_input.is_matched = 0 or p_include_equal = 1 )" );
          pPrintStream.println( "  then" );
          pPrintStream.println( "  add_text( '<is_new>', p_indent + 2 );" );
          pPrintStream.println( "  add_boolean( p_input.is_new, 0 );" );
          pPrintStream.println( "  add_text( '</is_new>', 0 );" );
          pPrintStream.println( "  add_newline( p_indent );" );
          pPrintStream.println( "  add_text( '<is_old>', p_indent + 2 );" );
          pPrintStream.println( "  add_boolean( p_input.is_old, 0 );" );
          pPrintStream.println( "  add_text( '</is_old>', 0 );" );
          pPrintStream.println( "  add_newline( p_indent );" );
          pPrintStream.println( "  add_text( '<is_matched>', p_indent + 2 );" );
          pPrintStream.println( "  add_boolean( p_input.is_matched, 0 );" );
          pPrintStream.println( "  add_text( '</is_matched>', 0 );" );
          pPrintStream.println( "  add_newline( p_indent );" );
          pPrintStream.println( "  end if;" );

          pPrintStream.println( "  if( p_input.is_all_fields_equal = 0 or p_include_equal = 1 )" );
          pPrintStream.println( "  then" );
          pPrintStream.println( "  add_text( '<is_all_fields_equal>', p_indent + 2 );" );
          pPrintStream.println( "  add_boolean( p_input.is_all_fields_equal, 0 );" );
          pPrintStream.println( "  add_text( '</is_all_fields_equal>', 0 );" );
          pPrintStream.println( "  add_newline( p_indent );" );
          pPrintStream.println( "  end if;" );

          pPrintStream.println( "  add_text( '<is_equal>', p_indent + 2 );" );
          pPrintStream.println( "  add_boolean( p_input.is_equal, 0 );" );
          pPrintStream.println( "  add_text( '</is_equal>', 0 );" );
          pPrintStream.println( "  add_newline( p_indent );" );

          pPrintStream.println( "  if( p_input.is_recreate_needed = 1 or p_include_equal = 1 )" );
          pPrintStream.println( "  then" );
          pPrintStream.println( "  add_text( '<is_recreate_needed>', p_indent + 2 );" );
          pPrintStream.println( "  add_boolean( p_input.is_recreate_needed, 0 );" );
          pPrintStream.println( "  add_text( '</is_recreate_needed>', 0 );" );
          pPrintStream.println( "  add_newline( p_indent );" );
          pPrintStream.println( "  end if;" );

          pPrintStream.println( "  add_text( '</" + lClassDataType.getJavaName() + ">', p_indent );" );
          pPrintStream.println( "  add_newline( p_indent );" );

          pPrintStream.println( "" );

          pPrintStream.println( "end;" );
        }

        pPrintStream.println( "" );

        pPrintStream.println( "procedure add_" + lClassDataType.getMaxLengthName() + "_list( p_input in " + lClassDataType.getDiffSqlNameCollection() + ", p_include_equal in number, p_indent in number )" );
        pPrintStream.println( "is" );
        pPrintStream.println( "begin" );
        pPrintStream.println( lNullHandling );
        pPrintStream.println( "  for i in 1..p_input.count()" );
        pPrintStream.println( "  loop" );
        pPrintStream.println( "  if( p_input(i).is_equal = 0 or p_include_equal = 1 )" );
        pPrintStream.println( "  then" );
        pPrintStream.println( "    add_" + lClassDataType.getMaxLengthName() + "( p_input(i), p_include_equal, p_indent );" );
        pPrintStream.println( "  end if;" );
        pPrintStream.println( "  end loop;" );
        pPrintStream.println( "" );
        pPrintStream.println( "end;" );
        pPrintStream.println( "" );
      }
    }

    for( ClassDataType lClassDataType : OracleOtGenerator.orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      if( !lClassDataType.isEnum() )
      {
        generateGetMethod( lClassDataType, pPrintStream, pTypePrefix, lClassDataType.getMaxLengthName(), false, false );

        if( lClassDataType.isListNeeded() )
        {
          generateGetMethod( lClassDataType, pPrintStream, pTypePrefix, lClassDataType.getMaxLengthName(), false, true );
        }
      }
    }

    pPrintStream.println( "end;" );
    pPrintStream.println( "/" );
  }

  private static void addAtomicField( PlSqlPrettyWriter pPrintStream, FieldData pFieldData, boolean pIsNew, ClassData pClassData )
  {
    String lTagname = pFieldData.getJavaName() + "_" + (pIsNew ? "new" : "old");
    String lFieldName = pIsNew ? pFieldData.getDiffNewSqlName() : pFieldData.getDiffOldSqlName();

    pPrintStream.println( "  if( p_input." + lFieldName + " is not null)" );
    pPrintStream.println( "  then" );
    pPrintStream.println( "  add_text( '<" + lTagname + ">', p_indent + 2 );" );
    if( pFieldData.isFlag() )
    {
      pPrintStream.println( "  add_boolean( p_input." + lFieldName + ", 0 );" );
    }
    else
    {
      if( pClassData instanceof ClassDataType && ((ClassDataType)pClassData).isEnum() )
      {
        pPrintStream.println( "  add_text_escaped( p_input." + lFieldName + ".i_name, 0 );" );
      }
      else
      {
        pPrintStream.println( "  add_text_escaped( p_input." + lFieldName + ", 0 );" );
      }
    }
    pPrintStream.println( "  add_text( '</" + lTagname + ">', 0 );" );
    pPrintStream.println( "  add_newline( p_indent );" );
    pPrintStream.println( "  end if;" );
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

  private static void generateGetMethod( ClassDataType pClassDataType, PlSqlPrettyWriter pPrintStream, String pTypePrefix, String pTypeName, boolean pHeaderOnly, boolean pIsList )
  {
    String lTypeName;
    if( pIsList )
    {
      lTypeName = pClassDataType.getDiffSqlNameCollection();
    }
    else
    {
      lTypeName = pClassDataType.getDiffSqlName();
    }

    pPrintStream.print( "function get_" + pTypeName + "( p_input in " + lTypeName + ", p_include_equal in number default 1, p_format in number default 1 ) return clob" );
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
      pPrintStream.println( "    add_" + pTypeName + (pIsList ? "_list" : "") + "( p_input, p_include_equal, 0 );" );
      pPrintStream.println( "  else" );
      pPrintStream.println( "    add_" + pTypeName + (pIsList ? "_list" : "") + "( p_input, p_include_equal, null );" );
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
