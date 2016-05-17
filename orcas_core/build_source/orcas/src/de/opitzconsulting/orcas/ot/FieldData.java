package de.opitzconsulting.orcas.ot;

import java.lang.reflect.Method;

public class FieldData
{
  private String _javaName;
  private Class _javaType;
  private boolean _isList;
  private Method _getterMethod;

  public Method getGetterMethod()
  {
    return _getterMethod;
  }

  public String getJavaName()
  {
    return _javaName;
  }

  public boolean isInt()
  {
    return getJavaType() == int.class;
  }

  public String getJavaGetterCall()
  {
    if( isFlag() )
    {
      return "is" + _javaName.substring( 0, 1 ).toUpperCase() + _javaName.substring( 1 ) + "()";
    }
    else
    {
      return "get" + _javaName.substring( 0, 1 ).toUpperCase() + _javaName.substring( 1 ) + "()";
    }
  }

  public String getJavaSetterName()
  {
    return "set" + _javaName.substring( 0, 1 ).toUpperCase() + _javaName.substring( 1 );
  }

  public Class getJavaType()
  {
    return _javaType;
  }

  public boolean isList()
  {
    return _isList;
  }

  public FieldData( Class pJavaType, boolean pIsList, Method pGtterMethod )
  {
    _javaType = pJavaType;
    _isList = pIsList;
    _getterMethod = pGtterMethod;
    _javaName = _getFiledNameFromMethod();
  }

  @Override
  public String toString()
  {
    String lReturn = _javaName + " " + _isList + " " + _javaType;

    return lReturn;
  }

  public boolean isFlag()
  {
    return _getterMethod.getName().startsWith( "is" );
  }

  private String getSqlNameWthPrefix( String pPrefix )
  {
    if( isFlag() )
    {
      return pPrefix + _javaName.toLowerCase() + "_flg";
    }
    else
    {
      return pPrefix + _javaName.toLowerCase();
    }
  }

  public String getSqlName()
  {
    return getSqlNameWthPrefix( "i_" );
  }

  public String getDiffNewSqlName()
  {
    return getSqlNameWthPrefix( "n_" );
  }

  public String getDiffOldSqlName()
  {
    return getSqlNameWthPrefix( "o_" );
  }

  public String getDiffEqualFlagSqlName()
  {
    return getSqlNameWthPrefix( "e_" );
  }

  public String getDiffChangeSqlName()
  {
    return getSqlNameWthPrefix( "c_" );
  }

  public String getDiffNewJavaName()
  {
    return _javaName + "New";
  }

  public String getDiffOldJavaName()
  {
    return _javaName + "Old";
  }

  public String getDiffEqualFlagJavaName()
  {
    return _javaName + "IsEqual";
  }

  public String getDiffChangeJavaName()
  {
    return _javaName + "Diff";
  }

  public String getDiffChangeSqlNameForSubType( ClassDataType pClassDataType )
  {
    String lReturn = getSqlNameWthPrefix( "c_" ) + "_" + pClassDataType.getJavaName().toLowerCase();
    return lReturn.length() > 30 ? lReturn.substring( 0, 30 ) : lReturn;
  }

  public String getDiffChangeJavaNameForSubType( ClassDataType pClassDataType )
  {
    return _javaName + pClassDataType.getJavaName() + "Diff";
  }

  public String getCleanValueMethodName()
  {
    return getSqlNameWthPrefix( "d_" );
  }

  public String getCleanValueJavaMethodName()
  {
    return _javaName + "CleanValueIfNeeded";
  }

  public String getDefaultValueFieldName()
  {
    return getSqlNameWthPrefix( "def_" );
  }

  public String getUpperCaseFieldFlagName()
  {
    return getSqlNameWthPrefix( "up_" );
  }

  public String getUpperCaseJavaFieldFlagName()
  {
    return _javaName + "IsConvertToUpperCase";
  }

  public String getDefaultValueJavaFieldName()
  {
    return _javaName + "DefaultValue";
  }

  public ClassData getClassData( Class pJavaType, TypeDataContainer pTypeDataContainer )
  {
    ClassData result = null;

    if( _javaName.toUpperCase().contains( "CLOB" ) )
    {
      result = new ClassDataPrimitive( "clob", "String" );
    }
    else
    {
      result = pTypeDataContainer.getClassData( pJavaType );
    }
    return result;
  }

  private String _getFiledNameFromMethod()
  {
    String lMethodName = _getterMethod.getName();

    if( lMethodName.startsWith( "is" ) )
    {
      return lMethodName.substring( 2, 3 ).toLowerCase() + lMethodName.substring( 3 );
    }

    return lMethodName.substring( 3, 4 ).toLowerCase() + lMethodName.substring( 4 );
  }
}
