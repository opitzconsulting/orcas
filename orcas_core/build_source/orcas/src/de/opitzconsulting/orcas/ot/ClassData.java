package de.opitzconsulting.orcas.ot;

public abstract class ClassData
{
  abstract public String getSqlName();

  abstract public String getPlainSqlName();

  abstract public String getDiffSqlName();

  abstract public boolean isAtomicValue();
}
