---
layout: page
title: Integration tests
permalink: /docs/integration-tests/
categories: 
- en
---

## Spool tests

### Functionality spooling

The integration tests use a spooling mechanism which essentially loads the IS schema data from the database and outputs them via XML export to a file (this way is also used for the reverse engineering).    
The spooling makes a few more requests on the data dictionary (this should be removed soon). The contents of the tables are also displayed (here only data types that could be spooled easily via sqlplus, eg no LOB data).

### Tests

The spool tests themselves are located in the folder orcas_integrationstest\testspool\tests. The folder contains subfolders each with one test case. The test case consists of two files (a.sql and b.sql). The test proceeds as follows:
![Visualization of spooling]({{site.baseurl}}/assets/spooling.GIF "spooling.GIF")

1. a.sql is executed in a (previously emptied) scheme.
2. The spooling is executed on this scheme and written into the file a.log.
3. b.sql is executed in the (previously emptied again) scheme.
4. The spooling is executed again in this Scheme and written into the file b.log.
5. It is checked that a.log and b.log are different. If not, the test case has failed.

With this procedure, it is very important that every single test case exactly test only one detail, because otherwise unwanted OR-operation would be performed ("The spooling detects a deviation in the data type OR in the precision").

## Integration tests

The actual integration tests are located in the folder orcas_integrationstest/tests. Like at the spool test there are again subfolders, and each of them is a separate test case. It is here, however, not necessary to create a separate test case for each detail (On the contrary: Each test case takes about 3 minutes, so the number of tests should not be too large). In each test case different test scenarios are performed. These are described below:

### Test scenario "normal"

![Visualization of test scenario "normal"]({{site.baseurl}}/assets/testszenario-normal.GIF "testszenario-normal.GIF")

1. In a (empty) scheme the script erzeuge_zielzustand.sql is imported.
2. With the spool script the scheme is written into a file.
3. On another (empty) scheme the script erzeuge_ausgangszustand.sql is imported.
Note: The script must be empty in a test case.
4. With Orcas the actual adjustment is performed.
   It is logged in the directory "Protokoll" (relevant only for the next test case).
5. The equalized scheme will also be written into a file.
6. It is checked whether the two schemes are identical.
7. Orcas is performed again. This time it is logged in the directory "Protokoll_svw_test".
8. The protocol of the second adjustment must be empty.
      This verifies that Orcas not unnecessarily dropt objects in the scheme and re-created (which would not be noticed without this test, but could lead to some significant performance issues).

### Test scenario "protocol"

![Visualization of test scenario "protocol"]({{site.baseurl}}/assets/testszenario-protokoll.GIF "testszenario-protokoll.GIF")

1. In a (empty) scheme the script erzeuge_ausgangszustand.sql is imported.
2. The protocol script, which has been previously generated in the previous test case is now running on the protocol scheme.
Note: That only takes place via SQL*Plus without Orcas.
3. The protocol scheme is written also into a file.
4. Also the protocol log file must be equal to the target script log file.

### Testszenario "extract"

![Visualization of test scenario "extract"]({{site.baseurl}}/assets/testszenario-extract.GIF "testszenarion-extract.GIF")

This test scenario is executed only if the property "test_extract" was not set to false.

1. During the extract test no table contents may be considered. Therefore, the spool script of data export is deactivated.
2. With orcas_extract the table scripts are generated to the destination script schema and stored in the directory extract_output.
3. In the (previously emptied) transfer scheme the adjustment starts. Since the scheme was previously empty, the case "re-create" is here implicitly also always tested.
4. The scheme will be spooled to a file again.
5. The spooled files must match.

### Testszenario "sqlplus-api"

![Visualization of test scenario sqlplus-api]({{site.baseurl}}/assets/testszenario-sqlplus-api.GIF "testszenario-sqlplus-api.GIF")

This test scenario is executed only if the directory tabellen_sqlplus exists.

1. On the (previously emptied) transfer schema the script erzeuge_ausgangszustand.sql is imported.
2. With Orcas the adjustment is performed again. But this time with the SQL*Plus API version.
3. The scheme will be spooled to a file again.
4. Again, the files must be equal.
