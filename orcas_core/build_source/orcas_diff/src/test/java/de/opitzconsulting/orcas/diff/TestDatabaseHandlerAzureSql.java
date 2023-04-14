package de.opitzconsulting.orcas.diff;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestDatabaseHandlerAzureSql {
    @Test
    public void testIsExpressionDifferentNotNullStatics_Basic() {
        assertEqual("id = 5", "id = (5)");
        assertEqual("id = 5", "(id = (5))");
        assertEqual("id=5", "id = 5");
        assertNotEqual("id = 5", "id = 6");
        assertNotEqual("ix = 5", "id = 5");
        assertNotEqual("id = 'A'", "id = 'a'");
        assertEqual("([id] = 5)", "id=5");
        assertNotEqual("id = 'A''B'", "id = 'AB'");
        assertNotEqual("id = 'A''B'", "id = 'A''b'");
    }

    @Test
    public void testIsExpressionDifferentNotNullStatics_OrLogic() {
        assertEqual("id = 0 or id = 1", "id = 1 or id = 0");
    }

    @Test
    public void testIsExpressionDifferentNotNullStatics_AndLogic() {
        assertEqual("id != 0 and id != 1", "id != 1 and id != 0");
    }

    @Test
    public void testIsExpressionDifferentNotNullStatics_Logic() {
        assertNotEqual("id = 0 and id = 1", "id = 1 or id = 0");
        assertNotEqual("(id = 0 or id = 1) and id2 = 5", "id = 0 or id = 1 and id2 = 5");
        assertEqual("id = 0 or (id = 1 and id2 = 5)", "id = 0 or id = 1 and id2 = 5");
        assertEqual("id = 0 or (id = 1 or id2 = 5)", "id = 0 or id = 1 or id2 = 5");
    }

    @Test
    public void testIsExpressionDifferentNotNullStatics_Between() {
        assertEqual("([id] >= (0) and [id] <= (6))", "id between 0 and 6");
        assertEqual("([id] >= (0) and [id] <= (6))", "ID BETWEEN 0 AND 6");
        assertNotEqual("([id] >= (0) and [id] <= (6))", "id between 0 and 7");
        assertEqual("([id] <= (6) and [id] >= (0))", "id between 0 and 6");
        assertNotEqual("([id] >= (0) and [id] <= (6))", "id between 6 and 0");
    }

    @Test
    public void testIsExpressionDifferentNotNullStatics_InList() {
        assertEqual("([id] = (0) or [id] = (6))", "id in (0,6)");
        assertEqual("([id] = '0' or [id] = ('6'))", "id in ('0','6')");
    }

    @Test
    public void testIsExpressionDifferentNotNullStatics_Complex() {
        assertEqual("([csps_dest_id] IS NULL AND [csps_isoc_id] IS NOT NULL OR [csps_dest_id] IS NOT NULL AND [csps_isoc_id] IS NULL)"
                , "(csps_dest_id is null and csps_isoc_id is not null) or (csps_dest_id is not null and csps_isoc_id is null)");
        assertEqual("([benu_extern_knz]=(0) AND [benu_passwort] IS NOT NULL OR [benu_extern_knz]=(1) AND [benu_passwort] IS NULL)"
                , "(benu_extern_knz = 0 and benu_passwort is not null) or (benu_extern_knz = 1 and benu_passwort is null)");
        assertEqual("([eisc_zusatzfunktion]='SFA' OR [eisc_zusatzfunktion]='TK' OR [eisc_zusatzfunktion] IS NULL)"
                , "eisc_zusatzfunktion in ('TK','SFA') or eisc_zusatzfunktion is null");
    }

    private static void assertEqual(String pExpression1, String pExpression2) {
        Assertions.assertEquals(DatabaseHandlerAzureSql.cleanupExpression(pExpression1), DatabaseHandlerAzureSql.cleanupExpression(pExpression2));
        Assertions.assertFalse(DatabaseHandlerAzureSql.isExpressionDifferentNotNullStatic(pExpression1, pExpression2));
    }

    private static void assertNotEqual(String pExpression1, String pExpression2) {
        Assertions.assertNotEquals(DatabaseHandlerAzureSql.cleanupExpression(pExpression1), DatabaseHandlerAzureSql.cleanupExpression(pExpression2));
        Assertions.assertTrue(DatabaseHandlerAzureSql.isExpressionDifferentNotNullStatic(pExpression1, pExpression2));
    }
}
