package de.opitzconsulting.orcas.ot;


public class DataWriterPartFixedValue extends DataWriterPart
{
  @Override
  public void writeRecursive( StringBuilder pOut, int pIndentSize )
  {
    pOut.append( _value );
  }

  private String _value;

  public DataWriterPartFixedValue( String pValue )
  {
    _value = pValue;
  }
}
