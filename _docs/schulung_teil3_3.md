---
layout: page
title: Teil 3.3 - Eigene Java Extensions
permalink: /docs/schulung_teil3_3/
---

Wie bereits erwähnt liest Orcas die erstellten Tabellendefinitionen etc. in Java-Objekte ein. Dadurch ist es möglich in Form von Javaklassen über die entstandenen Objekte zu iterieren und diese ggf. zu modifizieren.
An dieser Stelle sei auch auf die Dokumentation der [Extensions]({{site.baseurl}}/docs/extensions/) verwiesen.

Ein Beispiel hierfür, welches bereits im Orderentry-Beispiel verwendet wird ist die Java-Extension CharColmun:


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

## Einfache Änderung

Ändere die Extension so, dass alle varchar2-Spalten auf BYTE umgestellt werden.

## Änderung der Precision

Erweitere die Extension so, dass die Anzahl der erlaubten Zeichen bei varchar2-Spalten mindestens 100 ist.

## Weitere Übung

Erweitere die Extension so, dass für jede Tabelle automatisch eine ID-Spalte hinzugefügt wird (analog zur Domain-Extension).

