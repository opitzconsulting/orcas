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
<div class="clearfix" id="short-description-container">
    <div>
        <img src="./assets/db_icon.png">
        <h4>DEPLOYMENT FRAMEWORK</h4>
        <p>Transformation of existing database scheme into target state</p>
    </div>
    <div>
        <img src="./assets/script_icon.png"/>
        <h4>VERSION CONTROL</h4>
        <p>Simplified maintenance and integration into existing projects via text files</p>
    </div>
    <div>
        <img  src="./assets/cc_icon.png">
        <h4>OPEN SOURCE</h4>
        <p>Oracle Adaptive Systems is free and compatible with Oracle RDBMS</p>
    </div>
</div>

<div class="clearfix" id="container">
<h2>WHERE USING ORCAS CAN BE HELPFUL</h2>
    <div><h1>Development</h1></div>
    <div>
        <div id="container-first-child">
            <ul>
                <li>Differences in size of columns in testing environment and development database</li>
                <li>Questions on SVN/Git version compatibility of the development database</li>
                <li>Index mistakes in the production database</li>
                <li>Less complex syntax</li>
                <li>Primary key errors</li>
                <li>Tracking changes</li>
            </ul>
        </div>
    </div>
    <div><h1>Go-live date</h1></div>
    <div>
        <div id="container-second-child">
            <ul>
                <li>Synchronized versions of development, test and productive databases</li>
                <li>Versioning, branching and merging of databases</li>
                <li>Implementing uniform database conventions</li>
                <li>Parallel access on the development database</li>
                <li>Operating agile datanases </li>
            </ul>
        </div>
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
                <a href="{{site.baseurl}}/docs/installation/">Installation</a> What to do to get Orcas working within my own project
            </li>
            <li>
                <a href="{{site.baseurl}}/docs/de/examples/">Examples</a> Example projects
            </li>
            <li>
                <a href="{{site.baseurl}}/docs/de/ant-tasks/">ant tasks</a> How to setup the necessary ant processes
            </li>
            <li>
                <a href="{{site.baseurl}}/docs/de/statics-syntax/">Table  syntax</a> What do table creation / alteration scripts look like?
            </li>
            <li>
                <a href="{{site.baseurl}}/docs/de/domain-extension/">Domain extensions </a> 
                How to integrate project specific extensions
            </li>
            <li>
                <a href="{{site.baseurl}}/docs/de/extensions/">Extensions</a>
                How to integrate special project specific extensions?
            </li>
            <li>
                <a href="{{site.baseurl}}/docs/de/how-it-works/">Functionality of Orcas</a>   
                How does Orcas work
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
