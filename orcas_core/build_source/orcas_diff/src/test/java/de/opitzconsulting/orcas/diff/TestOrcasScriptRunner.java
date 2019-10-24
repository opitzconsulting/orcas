package de.opitzconsulting.orcas.diff;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.opitzconsulting.orcas.diff.OrcasScriptRunner.CommentHandler;

public class TestOrcasScriptRunner {
    @Test
    public void testIsInComment() {
        Assertions.assertEquals(false, isInComment("", false));
        Assertions.assertEquals(true, isInComment("", true));
        Assertions.assertEquals(true, isInComment(" /* odajfla", false));
        Assertions.assertEquals(false, isInComment(" */  ", true));
        Assertions.assertEquals(false, isInComment(" /*/  ", true));
        Assertions.assertEquals(true, isInComment(" /*/  ", false));
        Assertions.assertEquals(false, isInComment(" /* */  ", false));
        Assertions.assertEquals(false, isInComment(" /* */  ", true));
        Assertions.assertEquals(true, isInComment(" */  /*   ", true));
        Assertions.assertEquals(true, isInComment("*", true));
        Assertions.assertEquals(false, isInComment("/", false));
        Assertions.assertEquals(false, isInComment("--cxc/*", false));
        Assertions.assertEquals(false, isInComment("--*/", true));
        Assertions.assertEquals(false, isInComment("'/*'", false));
        Assertions.assertEquals(false, isInComment("'*/", true));
        Assertions.assertEquals(true, isInComment("'xy'/*", false));
        Assertions.assertEquals(false, isInComment("'xy''/*'", false));
        Assertions.assertEquals(false, isInComment("/*'", false, true));
    }

    private boolean isInComment(String pString, boolean pWasInComment) {
        return isInComment(pString, pWasInComment, false);
    }

    private boolean isInComment(String pString, boolean pWasInComment, boolean pWasInString) {
        CommentHandler lCommentHandler = new CommentHandler();

        if (pWasInComment) {
            lCommentHandler.handleLine("/*");
        } else {
            if (pWasInString) {
                lCommentHandler.handleLine("'");
            }
        }

        lCommentHandler.handleLine(pString.trim());

        return !lCommentHandler.isPlsqlTerminator("/");
    }
}
