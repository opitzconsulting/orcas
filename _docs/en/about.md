---
layout: index
title: About Orcas
permalink: /
navigation: false
titlepage: true 
categories: 
- en
---

<div id="titlepage-title"><h1>About Orcas</h1></div>
<div>
<div class="clearfix" id="short-description-container">
    <div>
        <img src="./assets/db_icon.png">
        <h4>DEPLOYMENT FRAMEWORK</h4>
    </div>
    <div>
        <img src="./assets/script_icon.png"/>
        <h4>VERSION CONTROL</h4>
    </div>
    <div>
        <img  src="./assets/osi_keyhole.png">
        <h4>OPEN SOURCE</h4>
    </div>
</div>

<div class="clearfix" id="description-panel">
    <h2>INTRODUCTION</h2>
    <div class="description-pt1">
        Orcas is a deployment framework for transforming an existing database schema into a target state. The state of the existing schema is irrelevant in most cases. If needed, unnecessary indexes, constraints, columns and tables will be deleted and necessary tables and columns will be added.
    </div>
    <input id="expand" type="checkbox" class="panel">
    <label for="expand" id="expand-title">more</label>
    <div class="description-pt2">
        Data types will be changed if possible. The target state will be provided in the form of SQL files, which are based on the CREATE / ALTER TABLE syntax. The use of Orcas has many advantages. One huge advantage is the possibility of versioning table creation scripts, which is a great help when working in a team, because changes can easily be recognized and undone if needed. An additional benefit is the ease of deployment on different databases without hassle so you have the same version of your source code on any number of databases.
        <br>
        <br>
        More information can be found in our <a href="http://www.opitz-consulting.com/fileadmin/user_upload/Collaterals/Artikel/whitepaper-orcas-EN.pdf" target="_blank">Whitepaper: Orcas -  Continuous Delivery for Databases</a> 
     </div>
</div>

<div class="clearfix" id="documentation-list">
    <h2>DOCUMENTATION</h2>
    <p>Hier sind die wichtigsten Bereiche der Dokumentation mit kurzer Beschreibung:</p>
    <div><img src="./assets/docs_icon.png"/></div>
    <div>
        <ul>
            <li>
                <a href="{{site.baseurl}}/docs/usage/">How to work with Orcas?</a>
            </li>
            <li>
                <a href="{{site.baseurl}}/docs/getting-started-gradle//">Gradle setup</a> What to do to get Orcas working with gradle
            </li>
            <li>
                <a href="{{site.baseurl}}/docs/getting-started-maven//">Maven setup</a> What to do to get Orcas working with maven
            </li>
            <li>
                <a href="{{site.baseurl}}/docs/statics-syntax/">Table syntax</a> What do table scripts look like?
            </li>
            <li>
                <a href="{{site.baseurl}}/docs/domain-extension/">Domain extensions </a> 
                How to integrate project specific extensions
            </li>
        </ul>
    </div>
</div>
<div class="clearfix" id="pros-cons">
    <h2>Pros | Cons</h2>
    <div class="pros">
        <ul>
            <li>The target state is managed in simple text-based script files. Because of this, you can use all benefits of a version control system (provide versions, change log, uniform versions, merge support etc.).</li>
            <li>The scripts are a reference in themselves, so you don’t have to search different schemas to get the latest version of a DB package or have to set a default scheme for references.</li>
            <li>You don’t need any complicated or error prone DB release scripts.</li>
            <li>You can create as many schemas for development or testing purposes as you want without any cumbersome comparison effort.</li>
       </ul>
    </div>
    <div class="cons">
        <ul>
            <li>When using unsupported database functions, these parts have to be managed manually.</li>
            <li>Project associates need to know how to work with Orcas.</li>
        </ul>
    </div>
</div>
</div>
