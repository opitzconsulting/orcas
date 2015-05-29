package de.opitzconsulting.orcas.extensions;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public abstract class OrcasBaseExtensionWithParameter implements OrcasExtensionWithParameter
{
  private String _parameter;

  public void setParameter( String pParameter )
  {
    _parameter = pParameter;
  }

  public String getParameterAsString()
  {
    return _parameter;
  }  

  public Map<String,String> getParameterAsMap()
  {
    Map<String,String> lReturn = new HashMap<String,String>();
    String lString = getParameterAsString();

    lString = lString.replace( "[", "" );
    lString = lString.replace( "]", "" );

    StringTokenizer lStringTokenizerEntries = new StringTokenizer( lString, "," );
    while( lStringTokenizerEntries.hasMoreTokens() )
    {
      StringTokenizer lStringTokenizerValues = new StringTokenizer( lStringTokenizerEntries.nextToken(), ":" );

      lReturn.put( lStringTokenizerValues.nextToken(), lStringTokenizerValues.nextToken() );
    }

    return lReturn;
  }  

  public String getParameterAsMap( String pKey )
  {
    return getParameterAsMap().get( pKey );
  }
}
