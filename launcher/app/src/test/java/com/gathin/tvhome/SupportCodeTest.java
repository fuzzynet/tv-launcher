package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Footer text. No window and no preferences file. */
public class SupportCodeTest {

    @Test
    public void configKey() {
        assertEquals("support_code", SupportCode.CONFIG_KEY);
    }

    @Test
    public void lettersDigitsAndHyphensAreKept() {
        assertEquals("AB-12CD", SupportCode.display("AB-12CD"));
        assertEquals("AB-12CD", SupportCode.display("  ab-12cd  "));
        assertEquals("CODE", SupportCode.display("code!"));
        assertEquals("HELLOWORLD", SupportCode.display("hello world!!"));
        assertEquals("", SupportCode.display(null));
        assertEquals("", SupportCode.display("   "));
        assertEquals("", SupportCode.display("!!!"));
        assertEquals("ABCDEFGHIJKLMNOP", SupportCode.display("abcdefghijklmnopqrstuvwxyz"));
        assertEquals(16, SupportCode.display("abcdefghijklmnopqrstuvwxyz").length());
    }

    @Test
    public void emptyCodeHidesAndPrefsWin() {
        assertEquals("", SupportCode.resolve(false, null, ""));
        assertEquals("", SupportCode.resolve(false, null, "   "));
        assertEquals("AB-12CD", SupportCode.resolve(false, null, "AB-12CD"));
        assertEquals("ZZ-9999", SupportCode.resolve(true, "ZZ-9999", "AB-12CD"));
        assertEquals("", SupportCode.resolve(true, "", "AB-12CD"));
        assertEquals("", SupportCode.resolve(true, null, "AB-12CD"));
        assertEquals("NOPE", SupportCode.resolve(true, "nope!", "AB-12CD"));
    }
}
