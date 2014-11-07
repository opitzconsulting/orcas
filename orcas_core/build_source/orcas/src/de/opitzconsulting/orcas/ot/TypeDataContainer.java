package de.opitzconsulting.orcas.ot;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.opitzconsulting.orcasDsl.Model;

public class TypeDataContainer
{
  private Map<Class,ClassData> _typeMap = new HashMap<Class,ClassData>();

  public void addClassData( Class pClass, ClassData pClassData )
  {
    _typeMap.put( pClass, pClassData );
  }

  private List<Class> _getClasses( Package pPackage, List<Class> pUsedInterfaces )
  {
    try
    {
      ClassLoader lClassLoader = Thread.currentThread().getContextClassLoader();
      String lPath = pPackage.getName().replace( '.', '/' );
      Enumeration<URL> lResources = lClassLoader.getResources( lPath );
      List<File> lDirectories = new ArrayList<File>();
      while( lResources.hasMoreElements() )
      {
        lDirectories.add( new File( lResources.nextElement().getFile() ) );
      }
      List<Class> lReturn = new ArrayList<Class>();
      for( File lDirectory : lDirectories )
      {
        lReturn.addAll( _findClasses( lDirectory, pPackage.getName(), pUsedInterfaces ) );
      }
      return lReturn;
    }
    catch( Exception e )
    {
      throw new RuntimeException();
    }
  }

  private List<Class> _findClasses( File pDirectory, String pPackageName, List<Class> pUsedInterfaces ) throws Exception
  {
    List<Class> lReturn = new ArrayList<Class>();
    if( !pDirectory.exists() )
    {
      return lReturn;
    }
    for( File lFile : pDirectory.listFiles() )
    {
      if( lFile.getName().endsWith( ".class" ) )
      {
        Class lClass = Class.forName( pPackageName + '.' + lFile.getName().substring( 0, lFile.getName().length() - 6 ) );

        boolean lUse = false;

        for( Class lInterface : lClass.getInterfaces() )
        {
          if( pUsedInterfaces.contains( lInterface ) )
          {
            lUse = true;
          }
        }

        if( lUse )
        {
          lReturn.add( lClass );
        }
      }
    }
    return lReturn;
  }

  List<Class> getMissingClasses()
  {
    List<Class> lReturn = new ArrayList<Class>();
    List<Class> lUsedInterfaces = new ArrayList<Class>();

    List<Class> lAllwaysCheckClasses = new ArrayList<Class>();

    Class lModelClass = Model.class;
    lAllwaysCheckClasses.add( lModelClass );

    for( Class lClass : lAllwaysCheckClasses )
    {
      if( !_typeMap.containsKey( lClass ) )
      {
        lReturn.add( lClass );
      }
    }

    for( ClassData lClassData : _typeMap.values() )
    {
      if( lClassData instanceof ClassDataType )
      {
        for( FieldData lFieldData : ((ClassDataType)lClassData).getFiledDataList() )
        {
          if( !_typeMap.containsKey( lFieldData.getJavaType() ) )
          {
            lReturn.add( lFieldData.getJavaType() );
          }

          if( lFieldData.getJavaType().isInterface() )
          {
            lUsedInterfaces.add( lFieldData.getJavaType() );
          }
        }
      }
    }

    for( Class lClass : new ArrayList<Class>( lReturn ) )
    {
      if( lClass.getPackage() == lModelClass.getPackage() )
      {
        continue;
      }

      String lSqlName = null;

      if( lClass == String.class )
      {
        lSqlName = "varchar2(2000)";
      }
      if( lClass == int.class )
      {
        lSqlName = "number";
      }
      if( lClass == boolean.class )
      {
        lSqlName = "number(1)";
      }      

      if( lSqlName != null )
      {
        addClassData( lClass, new ClassDataPrimitive( lSqlName ) );
        lReturn.remove( lClass );
      }
      else
      {
        throw new RuntimeException( "" + lClass );
      }
    }

    if( lReturn.isEmpty() )
    {
      lAllwaysCheckClasses.addAll( _getClasses( lModelClass.getPackage(), lUsedInterfaces ) );

      for( Class lClass : lAllwaysCheckClasses )
      {
        if( !_typeMap.containsKey( lClass ) )
        {
          lReturn.add( lClass );
        }
      }
    }

    return lReturn;
  }

  public List<ClassDataType> getAllClassDataTypes()
  {
    List<ClassDataType> lReturn = new ArrayList<ClassDataType>();

    for( ClassData lClassData : _typeMap.values() )
    {
      if( lClassData instanceof ClassDataType )
      {
        lReturn.add( (ClassDataType)lClassData );
      }
    }
    return lReturn;
  }

  public ClassData getClassData( Class pJavaType )
  {
    return _typeMap.get( pJavaType );
  }

  public List<Class> getAllClasses()
  {
    return new ArrayList<Class>( _typeMap.keySet() );
  }
}
