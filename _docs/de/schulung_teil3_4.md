---
layout: page
title: Teil 3.4 - PL/SQL Extensions
permalink: /docs/de/schulung_teil3_4/
categories: 
- de
---

Wie bereits erwähnt liest Orcas die erstellten Tabellendefinitionen etc. in PL/SQL-Objekte ein. Dadurch ist es möglich in Form von PL/SQL-Packages über die entstandenen Objekte zu iterieren und diese ggf. zu modifizieren.
An dieser Stelle sei auch auf die Dokumentation der [Extensions]({{site.baseurl}}/docs/de/extensions/) verwiesen.

Ein Beispiel hierfür, welches (in der Java-Version) bereits im Orderentry-Beispiel verwendet wird ist die PL/SQL-Extension pa_char_column:


{% highlight sql %}
create or replace package body pa_char_column is
  procedure handle_table( p_syex_table in out nocopy ot_syex_table )
  is
    v_syex_column ot_syex_column;
  begin
   for i in 1..p_syex_table.i_columns.count
    loop
      v_syex_column := p_syex_table.i_columns(i);

      if( ot_syex_datatype.is_equal( v_syex_column.i_data_type, ot_syex_datatype.c_varchar2 ) = 1)
      then
        v_syex_column.i_byteorchar := ot_syex_chartype.c_char;
      end if;

       p_syex_table.i_columns(i) := v_syex_column;
    end loop;
  end;

  function run( p_input in ot_syex_model ) return ot_syex_model
  is
    v_input ot_syex_model := p_input;
    v_syex_table ot_syex_table;
  begin   
    for i in 1..v_input.i_model_elements.count
    loop
      if( v_input.i_model_elements(i) is of (ot_syex_table) )
      then
        v_syex_table := treat( v_input.i_model_elements(i) as ot_syex_table );
        
        handle_table( v_syex_table );
        
        v_input.i_model_elements(i) := v_syex_table;
      end if;
    end loop;
  
    return v_input;
  end;
end;
/
{% endhighlight %}

Das Package hat eine **run**-Funktion, diese muss genau so definiert sein.
In der **run**-Funktion wird über die Modellelemente iteriert und für alle Tabellen die Funktion handle_table aufgerufen. Letztere ist nur innerhalb des Packages relevant. Die **handle_table**-Funktion iteriert über die Spalten und ändert für alle Spalten die byteorchar-Angabe auf c_char, sofern der datentyp c_varchar2 ist.

## Einfache Änderung

Ändere die Extension so, dass alle varchar2-Spalten auf BYTE umgestellt werden.
Dazu muss zunächst die Java-Version der Extension (die Date CharColumn.java) entfernt werden und durch die obige Extension (mit dem Dateinamen pa_char_column.sql) ersetzt werden.

## Änderung der Precision

Erweitere die Extension so, dass die Anzahl der erlaubten Zeichen bei varchar2-Spalten mindestens 100 ist.

## Weitere Übung

Erweitere die Extension so, dass für jede Tabelle automatisch eine ID-Spalte hinzugefügt wird (analog zur Domain-Extension).





