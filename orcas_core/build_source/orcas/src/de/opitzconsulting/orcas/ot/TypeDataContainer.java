package de.opitzconsulting.orcas.ot;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

public class TypeDataContainer
{
  final String ORCAS_DSL = "../orcas/src/de/opitzconsulting/OrcasDsl.xtext";

  final String ORCAS_SYSEX_DSL = "../.gradle/_orcas_syex/orcassyex/orcas_original/src/de/opitzconsulting/OrcasDsl.xtext";
  private Map<Class,ClassData> _typeMap = new HashMap<Class,ClassData>();

  public void addClassData( Class pClass, ClassData pClassData )
  {
    _typeMap.put( pClass, pClassData );
  }

  private List<Class> _getClasses( Package pPackage, List<Class> pUsedInterfaces )
  {
    List<String> classNames = new ArrayList<String>();
    List<Class> lReturn = new ArrayList<Class>();

    try{
      BufferedReader reader;
      try {
        reader = new BufferedReader(new FileReader(ClassDataType.getTypePrefix().equals( "syex" ) ? ORCAS_SYSEX_DSL : ORCAS_DSL));
        String line = reader.readLine();
        while(line != null){
          if(line.contains(":")) {
            String className = line.substring(0, line.indexOf(":")).trim();
            classNames.add(className);
          }
          line = reader.readLine();
        }
        reader.close();
      }catch(IOException e){
        e.printStackTrace();
      }

      for (String className : classNames) {
        try {
          Class lClass = Class.forName(pPackage.getName() + "." + className);
          if (lClass.isInterface()) {
            boolean lUse = false;

            for (Class lInterface : lClass.getInterfaces()) {
              if (pUsedInterfaces.contains(lInterface)) {
                lUse = true;
              }
            }

            if (lUse) {
              lReturn.add(lClass);
            }
          }
        }catch(Exception e){
          continue;
        }
      }
      return lReturn;
    }
    catch( Exception e )
    {
      throw new RuntimeException();
    }
  }

  List<Class> getMissingClasses()
  {
    List<Class> lReturn = new ArrayList<Class>();
    List<Class> lUsedInterfaces = new ArrayList<Class>();

    List<Class> lAllwaysCheckClasses = new ArrayList<Class>();

    Class lModelClass = getRootClass();
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
      if( lClass == int.class || lClass == Integer.class || lClass == BigInteger.class)
      {
        lSqlName = "number";
      }
      if( lClass == boolean.class )
      {
        lSqlName = "number(1)";
      }

      if( lSqlName != null )
      {
        addClassData( lClass, new ClassDataPrimitive( lSqlName, lClass.getSimpleName() ) );
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

  public Class getRootClass()
  {
    Class lModelClass;
    try
    {
      lModelClass = Class.forName( ClassDataType.getTypePrefix().equals( "syex" ) ? "de.opitzconsulting.orcasDsl.Model" : "de.opitzconsulting.origOrcasDsl.Model" );
    }
    catch( ClassNotFoundException e )
    {
      throw new RuntimeException( e );
    }
    return lModelClass;
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
