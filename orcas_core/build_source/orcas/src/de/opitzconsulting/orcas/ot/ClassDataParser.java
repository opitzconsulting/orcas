package de.opitzconsulting.orcas.ot;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;

public class ClassDataParser
{
  private TypeDataContainer _typeDataContainer = new TypeDataContainer();

  public TypeDataContainer parse()
  {
    while( !_typeDataContainer.getMissingClasses().isEmpty() )
    {
      for( Class lClass : _typeDataContainer.getMissingClasses() )
      {
        _parseClass( lClass );
      }
    }

    _findSuperclasses();
    _addListNeededData();

    return _typeDataContainer;
  }

  private void _addListNeededData()
  {
    for( ClassDataType lClassDataType : _typeDataContainer.getAllClassDataTypes() )
    {
      for( FieldData lFieldData : lClassDataType.getFiledDataList() )
      {
        if( lFieldData.isList() )
        {
          ((ClassDataType)_typeDataContainer.getClassData( lFieldData.getJavaType() )).setListNeeded( true );
        }
      }
    }
  }

  private void _findSuperclasses()
  {
    while( _findSuperclassesRecursive( false ) )
    {
    }
  }

  private boolean _findSuperclassesRecursive( boolean pThrowExceptionIfNotFound )
  {
    int lInterfacesFound = 0;
    int lInterfacesNotFound = 0;

    List<Class> lAllClasses = _typeDataContainer.getAllClasses();

    for( Class lClass : lAllClasses )
    {
      if( _typeDataContainer.getClassData( lClass ) instanceof ClassDataType )
      {
        ClassDataType lClassDataType = (ClassDataType)_typeDataContainer.getClassData( lClass );

        if( lClassDataType.getSuperclass() != null )
        {
          continue;
        }

        List<Class> lPosibleSuperClasses = new ArrayList<Class>();

        for( Class lInterface : lClass.getInterfaces() )
        {
          if( lAllClasses.contains( lInterface ) )
          {
            lPosibleSuperClasses.add( lInterface );
          }
        }

        for( Class lInterface : new ArrayList<Class>( lPosibleSuperClasses ) )
        {
          ClassDataType lSuperClassDataType = (ClassDataType)_typeDataContainer.getClassData( lInterface );

          lSuperClassDataType.setHasSubclasses( true );

          if( lSuperClassDataType.getSuperclass() != null )
          {
            lPosibleSuperClasses.remove( lSuperClassDataType.getSuperclass() );
          }
        }

        if( lPosibleSuperClasses.size() == 1 )
        {
          lClassDataType.setSuperclass( lPosibleSuperClasses.get( 0 ) );
          lInterfacesFound++;
        }

        if( lPosibleSuperClasses.size() > 1 )
        {
          if( pThrowExceptionIfNotFound )
          {
            throw new RuntimeException( lClass + " " + lPosibleSuperClasses );
          }
          lInterfacesNotFound++;
        }
      }
    }

    if( lInterfacesFound == 0 && lInterfacesNotFound != 0 )
    {
      _findSuperclassesRecursive( true );
    }

    return lInterfacesFound != 0;
  }

  private void _parseClass( Class pClass )
  {
    ClassDataType lClassDataType = new ClassDataType( pClass.getSimpleName() );

    _typeDataContainer.addClassData( pClass, lClassDataType );

    for( Method lMethod : pClass.getDeclaredMethods() )
    {
      if( (!lMethod.getName().startsWith( "get" ) && !lMethod.getName().startsWith( "is" )) || lMethod.getParameterTypes().length != 0 )
      {
        continue;
      }

      boolean lIsList = lMethod.getReturnType().equals( EList.class );

      Class lType;

      if( lIsList )
      {
        ParameterizedType lParameterizedType = (ParameterizedType)lMethod.getGenericReturnType();

        lType = (Class)lParameterizedType.getActualTypeArguments()[0];
      }
      else
      {
        lType = lMethod.getReturnType();
      }

      lClassDataType.addFiledDataList( new FieldData( lType, lIsList, lMethod ) );
    }

    if( pClass.isEnum() )
    {
      _parseEnum( lClassDataType, pClass );
    }
  }

  private void _parseEnum( ClassDataType lClassDataType, Class pClass )
  {
    try
    {
      List<EnumData> lEnumDataList = new ArrayList<EnumData>();

      Object lValues = pClass.getMethod( "values" ).invoke( pClass );

      for( int i = 0; i < Array.getLength( lValues ); i++ )
      {
        Object lValue = Array.get( lValues, i );

        lEnumDataList.add( new EnumData( (String)pClass.getMethod( "getLiteral" ).invoke( lValue ), (String)pClass.getMethod( "getName" ).invoke( lValue ), (Integer)pClass.getMethod( "getValue" )
            .invoke( lValue ) ) );
      }

      lClassDataType.setupEnum( lEnumDataList );
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }
}
