package de.opitzconsulting.orcas.diff;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

public class OrcasUnitTests {
    private static final Logger logger = LoggerFactory.getLogger(OrcasUnitTests.class);

    @TestFactory
    public Collection<DynamicTest> tests() {
        File testsFolder = new File("src/test/resources/tests");
        return Stream.of(testsFolder.listFiles())
                     .map(p -> {
                             List<DynamicTest> lDynamicTests = Stream
                                 .of(Database.values())
                                 .map(pDatabase -> {
                                     File lExpectedSpoolFile = new File(p, "assert_" + pDatabase + "_spool.sql");
                                     File lExpectedXmlLogFile = new File(p, "assert_" + pDatabase + "_log.xml");

                                     if (!lExpectedSpoolFile.exists() && !lExpectedXmlLogFile.exists()) {
                                         return null;
                                     }

                                     return DynamicTest.dynamicTest(p.getName() + "_" + pDatabase, () -> {
                                         try {
                                             ParametersCall lParametersCall = ParametersCall.createWithDefaults();

                                             File lSpoolFolder = new File("build/tests/orcas");
                                             File lActualSpoolFile = new File(lSpoolFolder, p.getName() + "/" + p.getName() + ".sql");
                                             File lActualXmlLogFile = new File(lSpoolFolder, p.getName() + "/xml.log");

                                             lParametersCall.setAdditionsOnly(p.getName().contains("additions_only"));
                                             lParametersCall.setMinimizeStatementCount(p.getName().contains("minimize_statement_count"));
                                             lParametersCall.setModelFile("");
                                             lParametersCall.setXmlLogFile(lActualXmlLogFile.toString());
                                             lParametersCall.setLogname(p.getName());
                                             lParametersCall.setSpoolfolder(lSpoolFolder.toString());
                                             lParametersCall.setLoglevel("debug");
                                             lParametersCall.setLogonly(true);
                                             lParametersCall.setDropmode(true);
                                             lParametersCall.setModelFiles(Collections.singletonList(new File(p, "act_zielzustand.sql")));
                                             File lAusgangszustandFile = new File(p, "arrange_ausgangszustand.sql");
                                             if (lAusgangszustandFile.exists()) {
                                                 lParametersCall.setSchemaFiles(Collections.singletonList(lAusgangszustandFile));
                                             } else {
                                                 lParametersCall.setSchemaFiles(Collections.emptyList());
                                             }

                                             pDatabase.apply(lParametersCall);

                                             new OrcasMain().mainRun(lParametersCall);

                                             //Files.copy(lActualSpoolFile.toPath(), lExpectedSpoolFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                             //Files.copy(lActualXmlLogFile.toPath(), lExpectedXmlLogFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                                             if (lExpectedSpoolFile.exists()) {
                                                 assertThat(lActualSpoolFile).hasSameContentAs(lExpectedSpoolFile);
                                             }
                                             if (lExpectedXmlLogFile.exists()) {
                                                 assertThat(lActualXmlLogFile).hasSameContentAs(lExpectedXmlLogFile);
                                             }
                                         } catch (Exception e) {
                                             logger.error(e, e::getMessage);
                                             Assertions.assertFalse(true, e.getMessage());
                                         }
                                     });
                                 })
                                 .filter(Objects::nonNull)
                                 .collect(Collectors.toList());

                             if (lDynamicTests.isEmpty()) {
                                 throw new RuntimeException("no tests: " + p);
                             }

                             return lDynamicTests;
                         }
                     )
                     .flatMap(Collection::stream)
                     .collect(Collectors.toList());
    }

    public enum Database {
        oracle(""),
        postgres("jdbc:postgresql"),
        mariadb("jdbc:mysql");

        private String dummyJdbcUrl;

        Database(String pDummyJdbcUrl) {
            dummyJdbcUrl = pDummyJdbcUrl;
        }

        void apply(ParametersCall pParametersCall) {
            pParametersCall.getJdbcConnectParameters().setJdbcUrl(dummyJdbcUrl);
        }
    }
}