package de.opitzconsulting.orcas.ot;

public class DataWriterPartEnumFunction extends DataWriterPart
{
  private String _typeName;
  private String _enumValueName;

  public DataWriterPartEnumFunction( String pEnumValueName, String pTypeName )
  {
    _enumValueName = pEnumValueName;
    _typeName = pTypeName;
  }

  @Override
  public void writeRecursive( StringBuilder pOut, int pIndentSize )
  {
    StringBuilder lStringBuilder = new StringBuilder();

    lStringBuilder.append( _typeName + ".c_" + _enumValueName );

    pOut.append( lStringBuilder );
  }
}
