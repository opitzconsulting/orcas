---
layout: page
title: Teil 3.5 - Syntax Extensions
permalink: /docs/schulung_teil3_5/
---

Extensions sollen im Wesentlichen der Vereinheitlichung und Vereinfachung dienen.
Dabei ist es oft möglich wiederkehrende Informationen wegzulassen. Manchmal kommt dabei die Notwendigkeit auf, einfachere Zusatzinformationen zu haben.

Beispielsweise wenn wir die Extension nehmen, die automatisch allen Tabellen eine ID-Spalte hinzufügt, dann kann es bestimmte Tabellen geben, die dann doch keine ID-Spalte haben sollen (z.B. Log-Tabellen oder Zuordnungstabellen). 
In der Extension muss es somit eine Fallunterscheidung geben, die besagt ob die ID-Spalte erstellt werden soll oder nicht. Eine solche Unterscheidung könnte man noch über eine Namenskonvention abbilden (alle Tabellen die mit log_ anfangen bekommen keine ID-Spalte...). Besser wäre es aber ein Flag zu haben, dass man in der Tabellendefinition mit angeben kann (z.B. "create table mytable with-id-column").

Ein weiteres Problem tritt auf, wenn man daran denkt, dass der Name der ID-Spalten zu lang werden kann. Zumindest dann, wenn man den Tabellenname immer davor schreibt. Die ID-Spalten kann man natürlich auch immer nur ID nennen, aber spätestens wenn man auch noch Sequenzen generieren möchte, wird wieder ein eindeutiger Name gebraucht.

Meist sind Syntax-Extensions die beste Lösung für solche Aufgabenstellungen, sie ermöglichen die Erweiterung der Syntax und sorgen damit auch dafür, dass die neuen Informationen in die Objekte mit aufgenommen werden.

Eine einfache Syntax-Extension, die oft verwendet wird, ist die AliasSyntaxExtension (Dateiname: AliasSyntaxExtension.java):


{% highlight java %}
package de.opitzconsulting.orcas.syntax_extensions;

public class AliasSyntaxExtension extends BaseSyntaxExtension
{
  public void run()
  {
    addField( new FieldReference( "Table", "name" ), new NewFieldDataIdentifier( "alias", false ) );
  }
}

{% endhighlight %}

Sie liest sich wie folgt:

1. addField 
 - Füge ein Feld hinzu.
1. FieldReference( "Table", "name" ) 
 - Füge das Feld nach dem "name" der "Table" ein.
1. NewFieldDataIdentifier( "alias", false ) 
 - Das Feld soll "alias" hesissen und auch mit dem Schlüsselwort "alias" angegeben werden. 
 - Das Feld soll zudem optional (false) sein.

Diese Syntax-Extension führt erst mal nur dazu, dass Tabellen mit einem Alias versehen werden können. Z.B. wie folgt:

{% highlight sql %}
create table orders alias ordr
(
  orderdate         date                   not null,
  tracking_number   varchar2(20)           not null,
  shipping_country  varchar2(2)            not null
);
{% endhighlight %}

Die Angabe von "alias ordr" ist jetzt erlaubt (würde ohne die Syntax-Extension zu einer Fehlermeldung führen). Die Alias-Angabe hat aber ansosnten erst mal keinen Effekt, das sie von Orcas einfach nur ignoriert wird. Die über Syntax-Extensions zur Verfügung gestellten Zusatzinformationen sind nur dann sinnvoll, wenn es auch eine entsprechende Extension gibt, die diese Informationen auswertet.

Syntax-Extensions müssen immer in Java erstellt werden. Die zusätzlichen Informationen stehen aber sowohl für Java-Extensions als auch für PL/SQL-Extensions zur Verfügung.

Die nachfolgenden Übungen können somit in Java oder PL/SQL realisiert werden.

## Übung
Erstelle eine Extension, die für jede Tabelle automatisch eine ID-Spalte hinzugefügt wird (analog zur Domain-Extension). 
Dabei soll dies nur für Tabellen erfolgen, die einen Alias haben und die Spalte soll "Alias"_ID heißen.

## Weitere Übung

Erstelle eine Extension, die prüft, ob alle Alias-Angaben genau 4-Stellig und eindeutig sind.

Hinweis: Wenn nicht, dann sollte eine Exception geworfen werden (in Java throw new RuntimeException... ; in PL/SQL raise_application_error... ).





