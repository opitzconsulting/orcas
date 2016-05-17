package de.opitzconsulting.orcas.ot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.ModelElement;

public class DataWriter
{
  private TypeDataContainer _typeDataContainer;

  public static String getSkript( Model pModel )
  {
    StringBuilder lStringBuilder = new StringBuilder();

    new DataWriter().writeToSkript( pModel, lStringBuilder );

    return lStringBuilder.toString();
  }

  public void writeToSkript( Model pModel, StringBuilder pOut )
  {
    _typeDataContainer = new ClassDataParser().parse();

    int lMaxElementCountForPackage = 100;

    List<ModelElement> lModelElementsToProcess = new ArrayList<ModelElement>( pModel.getModel_elements() );

    int lPackageIndex = 0;

    while( !lModelElementsToProcess.isEmpty() )
    {
      lPackageIndex++;

      List<ModelElement> lModelElementsToProcessForPackage;

      if( lModelElementsToProcess.size() < lMaxElementCountForPackage )
      {
        lModelElementsToProcessForPackage = new ArrayList<ModelElement>( lModelElementsToProcess );
      }
      else
      {
        lModelElementsToProcessForPackage = new ArrayList<ModelElement>( lModelElementsToProcess.subList( 0, lMaxElementCountForPackage ) );
      }
      lModelElementsToProcess.removeAll( lModelElementsToProcessForPackage );
      writeModelElementsToPlSqlPackage( lModelElementsToProcessForPackage, getBuildPackageNameForIndex( lPackageIndex ), pOut );
    }

    pOut.append( "create or replace package body pa_orcas_xtext_model is\n" );
    pOut.append( "procedure build\n" );
    pOut.append( "is\n" );
    pOut.append( "begin\n" );
    for( int i = 0; i < lPackageIndex; i++ )
    {
      pOut.append( "  " + getBuildPackageNameForIndex( i + 1 ) + ".build();\n" );
    }
    pOut.append( "end;\n" );
    pOut.append( "end;\n" );
    pOut.append( "/\n" );
  }

  private String getBuildPackageNameForIndex( int lPackageIndex )
  {
    return "pa_orcas_xtext_" + lPackageIndex;
  }

  private void writeModelElementsToPlSqlPackage( List<ModelElement> pModelElements, String pPackageName, StringBuilder pOut )
  {
    pOut.append( "create or replace package " + pPackageName + " is\n" );
    pOut.append( "procedure build;\n" );
    pOut.append( "end;\n" );
    pOut.append( "/\n" );

    pOut.append( "create or replace package body " + pPackageName + " is\n" );
    pOut.append( "procedure build\n" );
    pOut.append( "is\n" );
    pOut.append( "begin\n" );
    for( ModelElement lModelElement : pModelElements )
    {
      pOut.append( "  pa_orcas_model_holder.add_model_element( " );
      _writeRecursice( _getDataRecursive( lModelElement ), pOut );
      pOut.append( ") ;\n" );
    }
    pOut.append( "end;\n" );
    pOut.append( "end;\n" );
    pOut.append( "/\n" );
    pOut.append( "\n" );
  }

  private void _writeRecursice( DataWriterPartConstruktorCall pDataWriterPartConstruktorCall, StringBuilder pOut )
  {
    pDataWriterPartConstruktorCall.writeRecursive( pOut, 2 );
  }

  private DataWriterPart _getDataWriterPart( Object pObject )
  {
    if( pObject == null || (pObject.getClass().isEnum() && ((Enumerator)pObject).getName().equals( "null" )) )
    {
      return new DataWriterPartFixedValue( "null" );
    }
    else
    {
      if( pObject instanceof Collection )
      {
        Collection lValues = (Collection)pObject;

        if( lValues.isEmpty() )
        {
          return new DataWriterPartFixedValue( "null" );
        }
        else
        {
          ClassDataType lClassDataTypeValue = (ClassDataType)_typeDataContainer.getClassData( _findInterfaceClass( lValues.iterator().next().getClass() ) );

          while( !lClassDataTypeValue.isListNeeded() )
          {
            lClassDataTypeValue = (ClassDataType)_typeDataContainer.getClassData( lClassDataTypeValue.getSuperclass() );
          }

          List<DataWriterPart> lCollectionelements = new ArrayList<DataWriterPart>();

          for( Object lCollectionValue : lValues )
          {
            lCollectionelements.add( _getDataWriterPart( lCollectionValue ) );
          }

          return new DataWriterPartCollection( lCollectionelements, lClassDataTypeValue.getSqlNameCollection() );
        }
      }
      else
      {
        ClassData lClassData = _typeDataContainer.getClassData( _findInterfaceClass( pObject.getClass() ) );

        if( pObject.getClass().isEnum() )
        {
          return new DataWriterPartEnumFunction( ((Enumerator)pObject).getName(), lClassData.getSqlName() );
        }

        if( lClassData instanceof ClassDataType )
        {
          return _getDataRecursive( pObject );
        }
        else
        {
          return new DataWriterPartFixedValue( _getFixedValue( pObject ) );
        }
      }
    }
  }

  private DataWriterPartConstruktorCall _getDataRecursive( Object pObject )
  {
    ClassDataType lClassDataType = (ClassDataType)_typeDataContainer.getClassData( _findInterfaceClass( pObject.getClass() ) );

    int lDummyConstruktorValueCount = 0;

    List<FieldData> lFieldDataList = new ArrayList<FieldData>();

    ClassDataType lClassDataTypeSuper = lClassDataType;
    while( lClassDataTypeSuper != null )
    {
      lFieldDataList.addAll( 0, lClassDataTypeSuper.getFiledDataList() );

      if( lClassDataTypeSuper.isHasSubclasses() && lClassDataTypeSuper.getFiledDataList().isEmpty() && lClassDataTypeSuper.getSuperclass() == null )
      {
        lDummyConstruktorValueCount++;
      }

      if( lClassDataTypeSuper.getSuperclass() == null )
      {
        lClassDataTypeSuper = null;
      }
      else
      {
        lClassDataTypeSuper = (ClassDataType)_typeDataContainer.getClassData( lClassDataTypeSuper.getSuperclass() );
      }
    }

    List<DataWriterPart> lArguments = new ArrayList<DataWriterPart>();

    for( FieldData lFieldData : lFieldDataList )
    {
      Object lValue;
      try
      {
        lValue = lFieldData.getGetterMethod().invoke( pObject );
      }
      catch( Exception e )
      {
        throw new RuntimeException( e );
      }

      lArguments.add( _getDataWriterPart( lValue ) );
    }

    return new DataWriterPartConstruktorCall( lDummyConstruktorValueCount, lArguments, lClassDataType.getSqlName() );
  }

  private String _getFixedValue( Object pValue )
  {
    if( pValue instanceof String )
    {
      return "'" + ((String)pValue).replaceAll( "'", "''" ) + "'";
    }
    if( pValue instanceof Integer )
    {
      return "" + pValue;
    }
    if( pValue instanceof Boolean )
    {
      return ((Boolean)pValue) ? "1" : "0";
    }    

    throw new RuntimeException( "Value unbekannt: " + pValue + " " + pValue.getClass() );
  }

  private Class _findInterfaceClass( Class pImplementingClass )
  {
    List<Class> lAllClasses = _typeDataContainer.getAllClasses();

    Class lSearchClass = pImplementingClass;

    do
    {
      for( Class lInterfaceClass : lSearchClass.getInterfaces() )
      {
        if( lAllClasses.contains( lInterfaceClass ) )
        {
          ClassDataType lClassDataType = (ClassDataType)_typeDataContainer.getClassData( lInterfaceClass );

          if( !lClassDataType.isHasSubclasses() )
          {
            return lInterfaceClass;
          }
        }
      }

      lSearchClass = lSearchClass.getSuperclass();
    }
    while( lSearchClass != null );

    if( _typeDataContainer.getClassData( pImplementingClass ) != null )
    {
      return pImplementingClass;
    }

    if( pImplementingClass == Integer.class )
    {
      return pImplementingClass;
    }
    
    if( pImplementingClass == Boolean.class )
    {
      return pImplementingClass;
    }    

    throw new RuntimeException( "Class nicht gefunden: " + pImplementingClass );
  }
}
