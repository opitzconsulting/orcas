package de.opitzconsulting.orcas.diff;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestOrcasScriptRunner {
    @Test
    public void testIsInComment() {
        Assertions.assertEquals(false, OrcasScriptRunner.isInComment("", false));
        Assertions.assertEquals(true, OrcasScriptRunner.isInComment("", true));
        Assertions.assertEquals(true, OrcasScriptRunner.isInComment(" /* odajfla", false));
        Assertions.assertEquals(false, OrcasScriptRunner.isInComment(" */  ", true));
        Assertions.assertEquals(false, OrcasScriptRunner.isInComment(" /*/  ", true));
        Assertions.assertEquals(true, OrcasScriptRunner.isInComment(" /*/  ", false));
        Assertions.assertEquals(false, OrcasScriptRunner.isInComment(" /* */  ", false));
        Assertions.assertEquals(false, OrcasScriptRunner.isInComment(" /* */  ", true));
        Assertions.assertEquals(true, OrcasScriptRunner.isInComment(" */  /*   ", true));
        Assertions.assertEquals(true, OrcasScriptRunner.isInComment("*", true));
        Assertions.assertEquals(false, OrcasScriptRunner.isInComment("/", false));
        Assertions.assertEquals(false, OrcasScriptRunner.isInComment("--cxc/*", false));
        Assertions.assertEquals(false, OrcasScriptRunner.isInComment("--*/", true));
    }
}
