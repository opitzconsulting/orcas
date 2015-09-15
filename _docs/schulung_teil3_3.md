---
layout: page
title: Teil 3.3 - Eigene Java Extensions
permalink: /docs/schulung_teil3_3/
---

Wie bereits erwähnt liest ORCAS die erstellten Tabellendefinitionen etc. in JAVA-Objekte ein. Dadurch ist es möglich in Forma von Javaklassen über die entstandenen Objekte zu iterieren und diese ggf. zu modifizieren.
An dieser Stelle sei auf die Dokumentation der [Extensions]({{site.baseurl}}/docs/extensions/) verwiesen

Ein Beispiel hierfür, welches bereits im Orderentry-Beispiel verwendet wird ist die CharColmun.


{% highlight java %}
package de.opitzconsulting.orcas.extensions;

import de.opitzconsulting.orcasDsl.*;

public class CharColumn extends TableVisitorExtension
{
  @Override
  protected void handleTable( Table pTable )
  {
    for( Column lColumn : pTable.getColumns() )
    {
      if( lColumn.getData_type() == DataType.VARCHAR2 )
      {
        lColumn.setByteorchar( CharType.CHAR );
      }
    }
  }
}
{% endhighlight %}

Die Klasse erbt von **TableVisitorExtension** und überschreibt hier die Methode **handleTable**, dadurch wir sie auf alle Tabellen angewendet. Sie iteriert über sämtliche Spalten und prüft den Datentyp auf VARCHAR2. Im Falle eines Treffers wird der Datentyp auf Zeichen umgesetzt, ohne diese Extensions wären die Angaben in Byte.


