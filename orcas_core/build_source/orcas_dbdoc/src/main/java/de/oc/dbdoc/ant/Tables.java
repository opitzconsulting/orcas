package de.oc.dbdoc.ant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tables
{
  private List<Style> _styles = new ArrayList<Style>();

  public Style createStyle()
  {
    Style lStyle = new Style();

    addStyle( lStyle );

    return lStyle;
  }

  public void addStyle( Style pStyle )
  {
    _styles.add( pStyle );
  }

  String getStyleForTable( String pTableName, Tableregistry pTableregistry )
  {
    Map<String, String> lDoubleValueCheck = new HashMap<>();

    String lReturn = "";

    for( Style lStyle : _styles )
    {
      if( lStyle.isForTable( pTableName, pTableregistry ) )
      {
        if( lReturn.length() != 0 )
        {
          lReturn += ", ";
        }

        if( lDoubleValueCheck.containsKey( lStyle.getName() ) )
        {
          if( !lDoubleValueCheck.get( lStyle.getName() ).equals( lStyle.getValue() ) )
          {
            throw new RuntimeException( "style ambiguous (" + pTableName + "): " + lStyle.getName() + " " + lStyle.getValue() + " " + lDoubleValueCheck.get( lStyle.getName() ) );
          }
        }
        else
        {
          lDoubleValueCheck.put( lStyle.getName(), lStyle.getValue() );

          lReturn += lStyle.getName() + "=\"" + lStyle.getValue() + "\"";
        }
      }
    }

    return lReturn;
  }
}
