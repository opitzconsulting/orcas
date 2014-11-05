package de.opitzconsulting.orcas.ot;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class OtTransformerPackageGenerator
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

    writePackageBody( lPrintStream, "syex", "orig" );
    lPrintStream.println( "" );
    writePackageBody( lPrintStream, "orig", "syex" );
    lPrintStream.close();
  }

  private static void writePackageBody( PrintStream pPrintStream, String pSourcePrefix, String pTargetPrefix )
  {
    ClassDataType.setTypePrefix( pTargetPrefix );
    TypeDataContainer lTypeDataContainer = new ClassDataParser().parse();

    pPrintStream.println( "create or replace package body pa_orcas_trans_" + pSourcePrefix + "_" + pTargetPrefix + " is" );
    pPrintStream.println( "" );

    for( ClassDataType lClassDataType : OracleOtGenerator.orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      pPrintStream.println( "function convert_"
                            + lClassDataType.getMaxLengthName()
                            + "( p_input in ot_"
                            + pSourcePrefix
                            + "_"
                            + lClassDataType.getMaxLengthName()
                            + " ) return ot_"
                            + pTargetPrefix
                            + "_"
                            + lClassDataType.getMaxLengthName()
                            + ";" );

      if( lClassDataType.isListNeeded() )
      {
        pPrintStream.println( "function convert_"
                              + lClassDataType.getMaxLengthName()
                              + "_list( p_input in ct_"
                              + pSourcePrefix
                              + "_"
                              + lClassDataType.getMaxLengthName()
                              + "_list ) return ct_"
                              + pTargetPrefix
                              + "_"
                              + lClassDataType.getMaxLengthName()
                              + "_list;" );
      }
    }

    for( ClassDataType lClassDataType : OracleOtGenerator.orderClassDataTypeList( lTypeDataContainer.getAllClassDataTypes(), lTypeDataContainer ) )
    {
      String lNullHandling = "  if( p_input is null ) then return null; end if;";

      pPrintStream.println( "function convert_"
                            + lClassDataType.getMaxLengthName()
                            + "( p_input in ot_"
                            + pSourcePrefix
                            + "_"
                            + lClassDataType.getMaxLengthName()
                            + " ) return ot_"
                            + pTargetPrefix
                            + "_"
                            + lClassDataType.getMaxLengthName() );
      pPrintStream.println( "is" );
      if( lClassDataType.isHasSubclasses() )
      {
        pPrintStream.println( "begin" );
        pPrintStream.println( lNullHandling );

        for( ClassDataType lSubClassDataType : lTypeDataContainer.getAllClassDataTypes() )
        {
          if( lSubClassDataType.getSuperclass() != null && lTypeDataContainer.getClassData( lSubClassDataType.getSuperclass() ).equals( lClassDataType ) )
          {
            pPrintStream.println( "  if( p_input is of (ot_" + pSourcePrefix + "_" + lSubClassDataType.getMaxLengthName() + ") )" );
            pPrintStream.println( "  then" );
            pPrintStream.println( "    return convert_" + lSubClassDataType.getMaxLengthName() + "( treat(p_input as ot_" + pSourcePrefix + "_" + lSubClassDataType.getMaxLengthName() + ") );" );
            pPrintStream.println( "  end if;" );
          }
        }

        pPrintStream.println( "  return null;" );
        pPrintStream.println( "end;" );
      }
      else
      {
        if( lClassDataType.isEnum() )
        {
          pPrintStream.println( "begin" );
          pPrintStream.println( lNullHandling );

          for( EnumData lEnumData : lClassDataType.getEnumData() )
          {
            pPrintStream.println( "  if( '" + lEnumData.getName() + "' = p_input.i_name )" );
            pPrintStream.println( "  then" );
            pPrintStream.println( "    return ot_" + pTargetPrefix + "_" + lClassDataType.getMaxLengthName() + ".c_" + lEnumData.getName() + "();" );
            pPrintStream.println( "  end if;" );
          }

          pPrintStream.println( "  raise_application_error( -20000, 'enum not found: ' || p_input.i_name );" );
          pPrintStream.println( "end;" );
        }
        else
        {
          pPrintStream.println( "  v_return ot_" + pTargetPrefix + "_" + lClassDataType.getMaxLengthName() + " := new ot_" + pTargetPrefix + "_" + lClassDataType.getMaxLengthName() + "();" );
          pPrintStream.println( "begin" );
          pPrintStream.println( lNullHandling );

          List<FieldData> lFieldDataList = new ArrayList<FieldData>();

          if( lClassDataType.getSuperclass() != null )
          {
            lFieldDataList.addAll( ((ClassDataType)lTypeDataContainer.getClassData( lClassDataType.getSuperclass() )).getFiledDataList() );
          }
          lFieldDataList.addAll( lClassDataType.getFiledDataList() );

          for( FieldData lFieldData : lFieldDataList )
          {
            ClassDataType lFieldClassDataType = _findClassDataType( lTypeDataContainer.getClassData( lFieldData.getJavaType() ).getSqlName(), lTypeDataContainer );

            pPrintStream.print( "  v_return." + lFieldData.getSqlName() + " := " );

            if( lFieldClassDataType == null )
            {
              pPrintStream.println( "p_input." + lFieldData.getSqlName() + ";" );
            }
            else
            {
              if( lFieldData.isList() )
              {
                pPrintStream.println( "convert_" + lFieldClassDataType.getMaxLengthName() + "_list( p_input." + lFieldData.getSqlName() + ");" );
              }
              else
              {
                pPrintStream.println( "convert_" + lFieldClassDataType.getMaxLengthName() + "( p_input." + lFieldData.getSqlName() + ");" );
              }
            }
          }
          pPrintStream.println( "" );
          pPrintStream.println( "  return v_return;" );
          pPrintStream.println( "end;" );
        }
      }

      pPrintStream.println( "" );

      if( lClassDataType.isListNeeded() )
      {
        pPrintStream.println( "function convert_"
                              + lClassDataType.getMaxLengthName()
                              + "_list( p_input in ct_"
                              + pSourcePrefix
                              + "_"
                              + lClassDataType.getMaxLengthName()
                              + "_list ) return ct_"
                              + pTargetPrefix
                              + "_"
                              + lClassDataType.getMaxLengthName()
                              + "_list" );
        pPrintStream.println( "is" );
        pPrintStream.println( "  v_return ct_" + pTargetPrefix + "_" + lClassDataType.getMaxLengthName() + "_list := new ct_" + pTargetPrefix + "_" + lClassDataType.getMaxLengthName() + "_list();" );
        pPrintStream.println( "begin" );
        pPrintStream.println( lNullHandling );
        pPrintStream.println( "  for i in 1..p_input.count()" );
        pPrintStream.println( "  loop" );
        pPrintStream.println( "    v_return.extend;" );        
        pPrintStream.println( "    v_return(v_return.count()) := convert_" + lClassDataType.getMaxLengthName() + "( p_input(i) );" );
        pPrintStream.println( "    if( v_return(v_return.count()) is null )");
        pPrintStream.println( "    then");
        pPrintStream.println( "      v_return.delete(v_return.count());");
        pPrintStream.println( "    end if;");
        pPrintStream.println( "  end loop;" );
        pPrintStream.println( "" );
        pPrintStream.println( "  return v_return;" );
        pPrintStream.println( "end;" );
        pPrintStream.println( "" );
      }
    }

    pPrintStream.println( "function trans_" + pSourcePrefix + "_" + pTargetPrefix + "( p_input in ot_" + pSourcePrefix + "_model ) return ot_" + pTargetPrefix + "_model" );
    pPrintStream.println( "is" );
    pPrintStream.println( "begin" );
    pPrintStream.println( "  return convert_model( p_input );" );
    pPrintStream.println( "end;" );

    pPrintStream.println( "end;" );
    pPrintStream.println( "/" );
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
