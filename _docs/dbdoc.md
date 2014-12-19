---
layout: docs
title: dbdoc
prev_section: examples
next_section: generate-scripts
permalink: /docs/dbdoc/
---

#dbdoc

Das Tool dbdoc ist ein Dokumentationswerkzeug für Datenbankschematas. Es erzeugt eine grafische Darstellung der Schemainformationen, um Tabellen und ihre Beziehungen zueinander übersichtlich darzustellen.
<br/>Welche Tabellen zu berücksichtigen sind und wie es dargstellt werden soll kann in einem ant-Skript festgesetzt werden.

Zur Ausführung von dbdoc wird eine Installation von [Graphviz](http://www.graphviz.org/Download_windows.php) benötigt.

<br/>

##1. Aufbau eines dbdoc-Skriptes

###1.1. Einbinden der dbdoc-Tasks

Zuerst müssen die dbdoc-Tasks und die Orcas-Default-Tasks eingebunden werden.

{% highlight xml %}
<import file="${orcas_dir}/orcas_default_tasks.xml"/>
<import file="${orcas_dbdoc_dir}/orcas_dbdoc_tasks.xml"/>
{% endhighlight %}

<br/>

###1.2. Anbinden der Datenbank

Die Datenbank wird im Task **&lt;orcas_dbdoc&gt;** ausgelesen.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|jdbcurl  |Gibt die URL zur Datenbank an. <br/>Format: jdbc :oracle:thin:host:port:sid|Yes||
|user     |Gibt den Benutzernamen des Schematas an, das abgebildet werden soll.|Yes||
|password |Gibt das Passwort zum Benutzer an. |Yes||
|outfolder|Gibt den Ordner an, in dem die erzeugten HTML-Seiten gespeichert werden|Yes||
|tmpfolder|Gibt den Ordner an, in dem temporär Daten gespeichert werden |Yes||

<br/>
{% highlight xml %}
<orcas_dbdoc jdbcurl="${jdbc_url}" user="${demo_user}" password="${demo_password}" outfolder="${output}" tmpfolder="${tmpdir}/">
{% endhighlight %}

<br/>

###1.3. Konfiguration des Ablaufs

Im Task **&lt;config&gt;** werden die Tabellengruppen, der Stil der Tabellen und Diagramme sowie die Diagrammstruktur definiert.

<br/>

####1.3.1. Konfiguration der Tabellengruppen

Alle Tabellengruppen werden im Task **&lt;tablerregistry&gt;** definiert.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|tablesrcfolder|Gibt den Ordner an, in dem die Tabellenskripte liegen. <br/>Führt zur Anzeige der SQL-Befehle in der Tabellenansicht.|No||

<br/>
Für jede Tabellengruppe wird ein Task **&lt;tablegroup&gt;** angelegt.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|name     |Gibt die Bezeichnung der Tabellengruppe an|Yes||

<br/>
Die beiden Tasks **&lt;include&gt;** und **&lt;exclude&gt;** dienen im Task **&lt;tablegroup&gt;** zum Ein- und Ausbinden von Tabellen.
<br/>Ebenfalls möglich ist das Arbeiten mit Regulären Ausdrücken. So können Filter erstellt werden um mehrere Tabellen auszuwählen.
Beispiele hierfür:

|Task|Value|Bedeutung|
|----|-----|---------|
|&lt;include name="STAMMDATEN_.*/&gt;|STAMMDATEN_.*|Alle Tabellen mit dem Präfix "Stammdaten_" werden der Tabellengruppe ADRESSE zugeordnet.|
|&lt;include name=".+TIER.*/&gt;|.+TIER.*|Alle Tabellen deren Bezeichnung "TIER" enthält (Wortanfang/-mitte/-ende)|
|&lt;exclude name=".*_TIERUEBERWACHUNG"/&gt;|.*_TIERUEBERWACHUNG|Alle Tabellen mit dem Suffic "_TIERUEBERWACHUNG" werden NICHT in die Tabellengruppe TIER aufgenommen|

<br/>
{% highlight xml %}
<tableregistry tablesrcfolder="tables">

  <tablegroup name="TIERABGANG">
    <include name=".*ABGANG.*"/>
    <include name="TIEREIGENTUEMER"/>
  </tablegroup>

  <tablegroup name="SCHLACHTUNG">
    <include name="SCHLACHT.*"/>
    <include name="TYP_HANDELSKLASSE"/>
    <include name="TYP_TIERKATEGORIE"/>
</tablegroup>
{% endhighlight %}

<br/>

####1.3.2. Konfiguration des Stils

Im Task **&lt;styles&gt;** wird die Ausgabeform der Tabellen und Diagramme definiert.

<br/>

#####1.3.2.1. Konfiguration der Tabellendarstellung

Im Task **&lt;tables&gt;** kann die Tabellendarstellung für jede Tabellengruppe einzeln angepasst werden.
<br/>Zu Beachten ist hier nur, dass keine Tabelle mehreren Tabellengruppen zugeordnet werden darf (Achtung auch bei Mehrfachselektion durch reguläre Ausdrücke!)

|Name|Description|Value|Default|
|----|-----------|--------|-------|
|color|Gibt die Tabellenrahmenfarbe an|Farbname/Hexadezimalwert|black|
|fillcolor|Gibt die Hintergrundfarbe der Tabellen an|Farbname/Hexadezimalwert|lightgrey|
|font|Gibt die Schriftart an|Schriftname||
|fontsize|Gibt die Schriftgröße an|In Punkten|14|

<br/>
Neben diesen, finden sich viele weitere Attribute auf der [Dokumentationsseite zu graphviz](http://www.graphviz.org/content/attrs).

{% highlight xml %}
<tables>
  <style name="fillcolor" value="#FFE500" tablegroup="TIER"/>
  <style name="fontsize"  value="18"      tablegroup="TIER"/>
  <style name="color"     value="green"   tablegroup="TIER"/>
  <style name="fillcolor" value="#FFE500" tablegroup="TIERNUMMERN"/>
  <style name="fillcolor" value="#FF6600" tablegroup="BETRIEBSSTAETTE"/>
  <style name="fillcolor" value="#FF9900" tablegroup="ADRESSE"/>
</tables>
{% endhighlight %}

<br/>

#####1.3.2.2. Konfiguration der Diagrammdarstellung

Alle Stilgruppen werden im Task **&lt;diagrams&gt;** definiert und können im nächsten Schritt, der Diagrammerzeugung im Task **&lt;diagram&gt;** ausgewählt werden.
<br/>Für jede Stilgruppe wird ein Task **&lt;stylegroup&gt;** angelegt.

|Name|Description|Value|Default|
|----|-----------|--------|-------|
|dotexecutable|Stilgruppenvorlage|Name der Stilgruppe|dot|

<br/>
Es gibt sechs verschiedene Stilgruppen:

<table>
  <tr>
    <td><b>dot</b></td>
    <td><b>fdp</b></td>
    <td><b>sfdp</b></td>
  </tr>
  <tr>
    <td><img src="{{site.baseurl}}/assets/Tables_dot.jpg"/></td>
    <td><img src="{{site.baseurl}}/assets/Tables_fdp.jpg"/></td>
    <td><img src="{{site.baseurl}}/assets/Tables_sfdp.jpg"/></td>
  </tr>
  <tr>
    <td><b>circo</b></td>
    <td><b>neato</b></td>
    <td><b>twopi</b></td>
  </tr>
  <tr>
    <td><img src="{{site.baseurl}}/assets/Tables_circo.jpg"/></td>
    <td><img src="{{site.baseurl}}/assets/Tables_neato.jpg"/></td>
    <td><img src="{{site.baseurl}}/assets/Tables_twopi.jpg"/></td>
  </tr>
</table>

<br/>
Jede Stilgruppe kann mit dem Task **&lt;style&gt;** angepasst werden.
<br/>Mögliche Parameter sind:

|Name|Description|Value|Default|
|----|-----------|-----|-------|
|overlap|Definiert, ob sich Tabellen bei der Darstellung überlappen dürfen|true/false|false|
|nodesep|Gibt den horizontalen Abstand zwischen Tabellen in Inches an. (Nur gültig bei dot).|1-n|1|
|ranksep|Gibt den vertikalen Abstand zwischen Tabellen in Inches an. (Nur gültig bei dot und twopi)|1-n|2|
|splines|Legt fest, wie und ob Verbindungen zwischen Tabellen dargestellt werden. |(leer), true, false, polyline|polyline|

<br/>
Neben diesen finden sich viele weitere Attribute auf der [Dokumentationsseite zu graphviz](http://www.graphviz.org/content/attrs). Es werden jedoch nicht alle Attribute von allen Stilgruppen unterstützt.

{% highlight xml %}
<diagrams>
  <stylegroup name="style1" dotexecutable="dot">
    <style name="nodesep" value="1"/>
    <style name="ranksep" value="1"/>
    <style name="splines" value="polyline"/>
  </stylegroup>
</diagrams>
{% endhighlight %}

<br/>

####1.3.3. Konfiguration der Diagrammstruktur

Im Task **&lt;diagram&gt;** können die Bezeichnungen und die Hierarchie der Diagramme festgelegt werden.

|Attribute|Description|Required|Default|
|---------|-----------|--------|-------|
|label    |Diagrammtitel, kann frei gewählt werden.|Yes||
|stylegroup|Auswahl einer angelegten Stilgruppe |No||
|subinnclude|Gibt die Diagrammdarstellungsform an. <br/>(diagrams_only/diagrams_with_tables/tables)|No||
|tablegroup|Gibt die zum Diagramm gehörenden Tabellen/-gruppen an.|No||

<br/>
Die drei Diagrammdarstellungsformen unterscheiden sich im Umfang der anzuzeigenden Inhalte.

<table>
  <tr>
    <td>
      <b>diagrams_only</b>
      <br/>
      <img src="{{site.baseurl}}/assets/Diagrams_only.jpg"/>
    </td>
    <td>
      Hier werden hierarchisch unterliegende Diagramme mit ihren beinhaltenden Tabellengruppen, sowie deren Verknüpfung zueinander angezeigt.
      <br/>Einzelne Tabellen werden nicht dargestellt.
      <br/>(Hauptsächlich für das Hauptdiagramm interessant)
    </td>
  </tr>
  <tr>
    <td>
    <b>diagrams_with_tables</b>
      <br/>
      <img src="{{site.baseurl}}/assets/Diagrams_with_tables.jpg"/>
    </td>
    <td>
      Hier werden hierarchisch unterliegende Diagramme mit zugehörigen Tabellen, sowie deren Verknüpfung zueinander, angezeigt.
      <br/>(Fehleranfällige Darstellung)
    </td>
  </tr>
  <tr>
    <td>
    <b>tables</b>
      <br/>
      <img src="{{site.baseurl}}/assets/Tables_dot.jpg"/>
    </td>
    <td>
      Hier werden alle hierarchisch unterliegende Tabellen mit deren Verknüpfung zueinander angezeigt.
      <br/>Unterliegende Diagrammstrukturen werden nicht berücksichtigt.
      <br/>(Bei vielen Tabellen schnell unübersichtlich)
    </td>
  </tr>
</table>

<br/>
Die Diagrammhierarchie wird mit der Verschachtelung des **&lt;diagram&gt;** Tasks erreicht.
<br/> das Hauptdiagramm, welches alle weiteren Diagramme beinhaltet, sollte die Darstellungsform "diagrams_only" gewählt werden, um eine übersichtliche Darstellung zu erreichen.
<br/>Um alle Tabellengruppen auszuwählen, wird bei "tablegroup" der Wert auf ".*" gesetzt.

{% highlight xml %}
<diagram label="Milcherzeugung" stylegroup="style1" subinnclude="diagrams_only" tablegroup=".*"/>
{% endhighlight %}

Würde man dies ausführen, werden (da keine weiteren, hierarchisch unterliegenden, Diagramme angelegt sind) alle Tabellengruppen mit zugehörigen Tabellen geladen und angezeigt.
<br/>Bei einer großen Anzahl an eingebundenen Tabellen kann dies schnell unübersichtlich wirken.
<br/>Deshalb können Tabellengruppen in Diagramme zusammengefasst werden, die dem Hauptdiagramm hierarchisch unterliegen.

Am Beispiel der Welt würde ein Diagramm "Die Welt" angelegt werden. Diesem Diagramm unterliegen die Diagramme der einzelnen Kontinente. Dem Kontinent-Diagramm die Diagramme der Staaten usw.

{% highlight xml %}
<diagram label="Die Welt" subinnclude="diagrams_only" tablegroup=".*">

  <diagram label="Europa" subinnclude="diagrams_with_tables">
    <diagram label="Deutschland" subinnclude="tables">
      <diagram label="Baden_Wuerttemberg" tablegroup="Baden_Wuerttemberg"/>
      <diagram label="Bayern" tablegroup="Bayern"/>
    </diagram>
    <diagram label="Spanien" tablegroup="Spanien"/>
  </diagram>

  <diagram label="Suedamerika" subinnclude="tables">
    <diagram label="Brasilien" tablegroup="Brasilien"/>
    <diagram label="Chile" tablegroup="Chile"/>
    <diagram label="Kolumbien" tablegroup="Kolumbien"/>
  </diagram>

</diagram>
{% endhighlight %}

Führt man diesen Code aus, ist dies das Diagramm "Europa" mit den unterteilten Diagrammen Spanien und Deutschland (Deutschland ist wiederum unterteilt in die Bundesländer Bayern und Baden Württemberg).
![]({{site.baseurl}}/assets/Europa.jpg)

##Beispielprojekt dbdoc_demo

Ein Beispielprojekt lässt sich unter examples\dbdoc_demo\build.xml finden, ausführen, und als Grundlage für eigene Projekte verwenden.
<br/>Als Beispieldatenschemata wurde ein Datenmodell für die Milcherzeugung verwendet.
<br/>Hier ist das Hauptdiagramm abgebildet.Die grauen Rechtecke stellen hierarchisch untergeordnete Diagramme dar.
![]({{site.baseurl}}/assets/Diagrams_only.jpg)

<br/>
Diese können ausgewählt werden und bieten auf einer extra Seite eine Darstellung der Tabellengruppe an.

![]({{site.baseurl}}/assets/Unterdiagramm.jpg)

<br/>
Auch einzelne Tabellen können ausgewählt werden und zeigen bei eingebundenen Tabellenordner die zugehörigen SQL-Befehle an.

![]({{site.baseurl}}/assets/abgang_hat_praemie.jpg)
