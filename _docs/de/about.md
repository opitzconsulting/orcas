---
layout: index
title: Was ist Orcas?
permalink: /de/
navigation: false
titlepage: true
categories:
- de
---

<div id="titlepage-title"><h1>Was ist Orcas?</h1></div>
<div class="clearfix" id="short-description-container">
    <div>
        <img src="../assets/db_icon.png">
        <h4 class="long-header">DEPLOYMENTFRAMEWORK</h4>
        <h4 class="separated-header">DEPLOYMENT-FRAMEWORK</h4>
        <p>Überführung von Datenbankschemas in gewünschten Sollzustand</p>
    </div>
    <div>
        <img src="../assets/script_icon.png"/>
        <h4 class="long-header">VERSIONSVERWALTUNG</h4>
        <h4 class="separated-header">VERSIONS-VERWALTUNG</h4>
        <p>Vereinfachte Datenbankschema-Verwaltung durch Integration mittels Textdateien</p>
    </div>
    <div>
        <img  src="../assets/cc_icon.png">
        <h4>OPEN SOURCE</h4>
        <p>Oracle Adaptive Systems ist frei verfügbar und mit Oracle RDBMS kompatibel</p>
    </div>
</div>

<div class="clearfix" id="container">
<h2>BEI WELCHEN PROBLEMEN KANN ORCAS UNTERSTÜTZEN?</h2>
    <div><h1>Development</h1></div>
    <div>
        <div id="container-first-child">
            <ul>
                <li>Unterschiedliche Spaltengrößen bei Testsystemen und Entwicklerdatenbanken</li>
                <li>Fragen zur SVN/Git Versionskompatibilität der Entwicklerdatenbank</li>
                <li>Indexfehler der Produktionsdatenbank</li>
                <li>Nachverfolgen von Veränderungen</li>
                <li>Weniger umständliche Syntax</li>
                <li>Primärschlüsselfehler</li>
             </ul>
        </div>
    </div>
    <div><h1>Go-LIVE Date</h1></div>
    <div>
        <div id="container-second-child">
            <ul>
                <li>Vereinheitlichte Version zwischen Entwicklungs-, Test- und Produktionsdatenbanken</li>
                <li>Versionieren, branchen und mergen von Datenbanken</li>
                <li>Paralleler Zugriff auf Entwicklerdatenbanken</li>
                <li>Umsetzung von Datenbankkonventionen</li>
                <li>Betrieb von agilen Datenbanken</li>
            </ul>
        </div>
    </div>
</div>

<div class="clearfix" id="description-panel">
    <h2>EINFÜHRUNG</h2>
    <div class="description-pt1">
    Orcas ist ein Deploymentframework mit dem ein bestehendes Schema in einen in Orcas beschriebenen Soll-Zustand überführt werden kann. Der Zustand des bestehenden Schemas ist dabei größtenteils irrelevant. Bei Bedarf werden "überflüssige" Indizes, Constraints, Spalten oder Tabellen verworfen bzw. neue Tabellen oder Spalten hinzugefügt.
    </div>
    <input id="expand" type="checkbox" class="panel">
    <label for="expand" id="expand-title">mehr</label>
    <div class="description-pt2"> 
    Änderungen von Datentypen werden, sofern möglich, durchgeführt. Der Soll-Zustand wird dabei in Form von einfachen SQL Skriptdateien vorgehalten, die in ihrer Syntax stark an die "CREATE TABLE" Syntax angelehnt sind.
    Die Nutzung von Orcas hat viele Vorteile. Ein großer Vorteil ist, dass die Tabellenskripte versioniert werden können, was bei einem Projektteam eine enorme Erleichterung ist, da Änderungen nachvollzogen und auch rückgängig gemacht werden können. Ein weiterer Vorteil ist, dass ohne große Umstände auf verschiedenen Datenbanken deployed werden und somit ein einheitlicher Datenbankstand auf beliebig vielen Schemata hergestellt werden kann.
    <br>
    <br>
    Weitere Informationen finden Sie es in unserem <a href="https://www.opitz-consulting.com/fileadmin/user_upload/Collaterals/Artikel/whitepaper-orcas_sicher.pdf" target="_blank">Whitepaper:</a> Orcas - Continuous Delivery für die Datenbank
    </div>
</div>

<div class="clearfix" id="documentation-list">
    <h2>DOKUMENTATION</h2>
    <p>Hier sind die wichtigsten Bereiche der Dokumentation mit kurzer Beschreibung:</p>
    <div><img src="../assets/docs_icon.png"/></div>
    <div>
        <ul>
            <li>
                <a href="{{site.baseurl}}/docs/de/usage/">Wie arbeitet man mit Orcas?</a>
            </li>
            <li>
                <a href="{{site.baseurl}}/docs/de/installation/">Installation</a> Was muss ich tun, um Orcas in meinem Projekt einsetzen zu können?
            </li>
            <li>
                <a href="{{site.baseurl}}/docs/de/examples/">Examples</a> Beispielprojekte
            </li>
            <li>
                <a href="{{site.baseurl}}/docs/de/ant-tasks/">ant Tasks</a> Wie erstelle ich einen Gesamtablauf mit ant?
            </li>
            <li>
                <a href="{{site.baseurl}}/docs/de/statics-syntax/">Tabellen Syntax</a> Wie sehen die Tabellenskripte aus?
            </li>
            <li>
                <a href="{{site.baseurl}}/docs/de/domain-extension/">Domain Extensions </a> 
                Wie kann ich projektspezifische Erweiterungen einfach integrieren?
            </li>
            <li>
                <a href="{{site.baseurl}}/docs/de/extensions/">Extensions</a>
                Wie kann ich spezielle projektspezifische Erweiterungen integrieren?
            </li>
            <li>
                <a href="{{site.baseurl}}/docs/de/how-it-works/">Funktionsweise Orcas </a>   
                Wie funktioniert Orcas?
            </li>
        </ul>
    </div>
</div>

<div class="clearfix" id="pros-cons">
    <h2>VORTEILE | NACHTEILE</h2>
    <div class="pros">
        <ul>
            <li>Soll-Zustand in Textskriptdateien vereinfacht Versionsverwaltung</li>
            <li>Skripte dienen als echte "Referenz" und können Referenzschema ersetzt</li>
            <li>Fehleranfällige DB-Releaseskripte werden nicht benötigt</li>
            <li>Beliebig viele Schemata für Entwicklung und Tests ohne Abgleichaufaufwand</li>
        </ul>
    </div>
    <div class="cons">
        <ul>
            <li>Bei von Orcas nicht unterstützten Datenbankfunktionen müssen Teilbereiche manuell verwaltet werden</li>
            <li>Projektarbeiter müssen wissen, wie man mit Orcas arbeitet</li>
        </ul>
    </div>
</div>
<p class="credit">Icon made by Freepik from www.flaticon.com <p>
