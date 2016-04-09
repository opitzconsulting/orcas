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
    return _getterMethod.getName().startsWith("is");
  }  
  
  private String getSqlNameWthPrefix(String pPrefix)
  {
    if(isFlag())
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
	return getSqlNameWthPrefix("i_");
  }
  
  public String getDiffNewSqlName()
  {
    return getSqlNameWthPrefix("n_");
  }
  
  public String getDiffOldSqlName()
  {
    return getSqlNameWthPrefix("o_");
  }
  
  public String getDiffEqualFlagSqlName()
  {
    return getSqlNameWthPrefix("e_");
  }  
  
  public String getDiffChangeSqlName()
  {
    return getSqlNameWthPrefix("c_");
  }  

  public ClassData getClassData( Class pJavaType, TypeDataContainer pTypeDataContainer )
  {
    ClassData result = null;

    if(_javaName.toUpperCase().contains("CLOB")) {
      result = new ClassDataPrimitive( "clob" );
    } else {
      result = pTypeDataContainer.getClassData(pJavaType);
    }
    return result;
  }

  private String _getFiledNameFromMethod()
  {
    String lMethodName = _getterMethod.getName();
    
    if(lMethodName.startsWith("is"))
    {
      return lMethodName.substring( 2, 3 ).toLowerCase() + lMethodName.substring( 3 );    	
    }

    return lMethodName.substring( 3, 4 ).toLowerCase() + lMethodName.substring( 4 );
  }  
}
