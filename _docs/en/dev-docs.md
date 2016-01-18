---
layout: page
title: Developer documentation
permalink: /docs/dev-docs/
categories: 
- en
---

##Functionality

For the description of the functionality, see: [Functionality of Orcas]({{site.baseurl}}/docs/how-it-works/).

##Integration tests

For the description of the integration tests, see: [Integration tests]({{site.baseurl}}/docs/integration-tests/)

##Git

`todo: many things ...`
Changes to Orcas should be made in a separate developer branch. Each OC employee should already have the right to create a new developer branch and to push.
<br/>When a change is done, then a new feature branch should be created from the developer branch, for this is a pull request is then created in stash.
<br/><br/>Example:
<br/>Developer FSA wants to make a change. FSA creates a branch:
<br/>developer-fsa
<br/><br/>The Branch is always created by master branch (if it already exists, for example because FSA has already made changes, then the brought up to date master state by merge).
<br/><br/>Developer FSA committed and pushed after the developement is far completed, so that it can or should be taken into Orcas. For this FSA creates a new branch:
<br/><br/>Where OCSVERW-78 may be a reference to a JIRA point, or another halfway understandable name. The branch pullrequest-fsa-OCSVERW-78 is created from the branch developer-fsa. Then FSA sets a pull request for the branch pullrequest-fsa-OCSVERW-78 within stash (https://git.opitz-consulting.de/projects/OCFRW/repos/oc-schemaverwaltung). IMPORTANT: Further developement is not allowed on the branch pullrequest-fsa-OCSVERW-78, because every (pushed) change automatically lands in the pull request. FSA should keep working on the branch developer-fsa. It remains to FSA to decide whether working with the master branch state or the pullrequest-fsa-OCSVERW-78 state in the further development.
<br/>pullrequest-fsa-OCSVERW-78
<br/><br/>In the branch pullrequest-fsa-OCSVERW-78 if necessary corrections are made, which can then be merged in the developer-fsa branch if necessary.

##Method for SQL*Plus scripts

In order to implement the variant described in [Functionality of Orcas]({{site.baseurl}}/docs/how-it-works/) step 7a, the following procedure is provided:

1. Create a mini example
2. Implement a mini example
3. Create the complete example (without a domain concept)
4. Determine what is to be done in order to implement the complete example
  - What functionality is missing in the "core"
  - What is missing in the SQL*Plus scripts?
5. Implement missing points
6. Create a complete domain concept example
7. Determine what is to be done in order to implement the complete domain concept example
  - What functionality is missing in the "core"
  - What functionality is in the domain extension still needed?
8. Implement missing domain points
9. Perform test with the first real project

##Procedure Reverse Engineering

Reverse engineering should take place over the model data.

##Variance analysis

In step 10, 11 and 12 of [Functionality of Orcas]({{site.baseurl}}/docs/how-it-works/) is shown how the comparison with the database should run. Currently, the behavior is not (all) that. In a first implementation step the variance analysis should take place without the ADJUSTMENT (step 11). The implementation should be done in steps (eg in sequences).

##SQL*Plus standalone variant

A possibility is to be created to use Orcas without Ant or Java.
