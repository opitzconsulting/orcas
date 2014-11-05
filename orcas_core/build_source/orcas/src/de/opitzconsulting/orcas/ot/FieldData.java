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

  public FieldData( String pJavaName, Class pJavaType, boolean pIsList, Method pGtterMethod )
  {
    _javaName = pJavaName;
    _javaType = pJavaType;
    _isList = pIsList;
    _getterMethod = pGtterMethod;
  }

  @Override
  public String toString()
  {
    String lReturn = _javaName + " " + _isList + " " + _javaType;

    return lReturn;
  }

  public String getSqlName()
  {
    return "i_" + _javaName.toLowerCase();
  }
}
