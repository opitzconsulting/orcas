package de.oc.dbdoc.ant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stylegroup
{
  private String _name;
  private List<StylegroupStyle> _stylegroupStyles = new ArrayList<StylegroupStyle>();
  private String _dotexecutable = "dot";

  public void setDotexecutable( String pDotexecutable )
  {
    _dotexecutable = pDotexecutable;
  }

  public void setName( String pName )
  {
    _name = pName;
  }

  public StylegroupStyle createStyle()
  {
    StylegroupStyle lStylegroupStyle = new StylegroupStyle();

    _stylegroupStyles.add( lStylegroupStyle );

    return lStylegroupStyle;
  }

  public String getName()
  {
    return _name;
  }

  public String getStyleForStylegroup()
  {
    Map<String,String> lValueMap = new HashMap();

    lValueMap.put( "overlap", "false" );
    lValueMap.put( "ranksep", "2" );
    lValueMap.put( "nodesep", "1" );
    lValueMap.put( "splines", "polyline" );

    String lReturn = "";

    for( StylegroupStyle lStyle : _stylegroupStyles )
    {
      lValueMap.put( lStyle.getName(), lStyle.getValue() );
    }

    List<String> lKeyList = new ArrayList( lValueMap.keySet() );
    Collections.sort( lKeyList );

    for( String lKey : lKeyList )
    {
      if( lReturn.length() != 0 )
      {
        lReturn += ", ";
      }

      lReturn += lKey + "=\"" + lValueMap.get( lKey ) + "\"";
    }

    return lReturn;
  }

  public String getDotExecutable()
  {
    return _dotexecutable;
  }
}
